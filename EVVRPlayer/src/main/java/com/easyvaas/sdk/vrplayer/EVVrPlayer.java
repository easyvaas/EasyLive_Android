package com.easyvaas.sdk.vrplayer;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.player.data.panoramas.PanoramaData;
import com.player.panoplayer.IPanoPlayerListener;
import com.player.panoplayer.IPanoPlayerVideoPluginListener;
import com.player.panoplayer.OptionType;
import com.player.panoplayer.OptionValue;
import com.player.panoplayer.PanoPlayer;
import com.player.panoplayer.PanoPlayerUrl;
import com.player.panoplayer.Plugin;
import com.player.panoplayer.ViewMode;
import com.player.panoplayer.plugin.VideoPlugin;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.StatisticsData;

/**
 * Author weizibo
 * Date 17/10/10
 * Version 1.0
 */

public class EVVrPlayer {
    private static final String TAG = EVVrPlayer.class.getSimpleName();

    private Context mContext;
    private EVVrVideoView mVideoView;
    private PanoPlayerUrl mPlayerUrl;
    private PanoPlayer mRenderer;
    private VideoPlugin mVideoPlugin;

    private Handler mHandler;

    private static final int RECONNECT_INTERVAL = 2000;
    private static final int RECONNECT_MAX_TIME = 10;
    private int mReconnectTime = 0;
    private long mCurrentPosition = 0L;
    private boolean mIsNeedReconnect = false;
    private boolean mAutoReconnect = false;

    private String mUrl = "";

    private boolean mStatisticsOn = true;

    private long mBufferingStartTime;
    private long mStartPlayTime;

    private final String PanoPlayerTemplate =
            "<DetuVr> "
                    + "<settings init=\"pano1\" initmode=\"default\" enablevr=\"true\" title=\"\"/>"
                    + "<scenes> "
                    + "<scene name=\"pano1\" title=\"\" thumburl=\"\" >"
                    + "<preview url=\"%s\" type=\"CUBESTRIP\" />"
                    + "<image type = \"%s\" url =\"%s\" device = \"0\" />"
                    + "<view fovmin='110' fovmax='170' gyroEnable=\"false\" viewmode='%s'/>"
                    + "</scene>"
                    + "</scenes>"
                    + "</DetuVr>";


    public EVVrPlayer(EVVrVideoView videoview, Context context) {
        mContext = context;
        mVideoView = videoview;
        mRenderer = mVideoView.getRender();
        mRenderer.setVideoPluginListener(mPluginListener);
        mPlayerUrl = new PanoPlayerUrl();
        mHandler = new Handler(context.getMainLooper());
    }

    public void startPlay(String url, boolean living) {
        /*if (TextUtils.isEmpty(url)) {
            mPlayerUrl.setXmlUrl("http://www.detu.com/ajax/pano/xml/159891");
        } else {*/
        goPlay(url, "default");
    }

    private void goPlay(String url, String modeStr) {
        String xmlstring = String.format(PanoPlayerTemplate, "", "video", mUrl = url, modeStr);
        mPlayerUrl.setXmlContent(xmlstring);
        OptionValue value = new OptionValue(OptionType.OPT_CATEGORY_CODEC, "hw_decoder", "true");
        List<OptionValue> values = new ArrayList<>(1);
        values.add(value);
        mRenderer.play(mPlayerUrl, values);
    }

    public void setViewMode(int mode) {
        ViewMode enumMode = ViewMode.VIEWMODE_DEF;
        String modeStr = "";
        switch (mode) {
            case EVVrPlayerConstrants.VIEWMODE_DEF:
                enumMode = ViewMode.VIEWMODE_DEF;
                modeStr = "default";
                break;
            case EVVrPlayerConstrants.VIEWMODE_FISHEYE:
                enumMode = ViewMode.VIEWMODE_FISHEYE;
                break;
            case EVVrPlayerConstrants.VIEWMODE_VR_HORIZONTAL:
                enumMode = ViewMode.VIEWMODE_VR_HORIZONTAL;
                modeStr = "vr";
                break;
            case EVVrPlayerConstrants.VIEWMODE_VR_VERTICAL:
                enumMode = ViewMode.VIEWMODE_VR_VERTICAL;
                modeStr = "vr";
                break;
            /*case EVVrPlayerConstrants.VIEWMODE_PLANE:
                enumMode = ViewMode.VIEWMODE_PLANE;
                break;
            case EVVrPlayerConstrants.VIEWMODE_LITTLEPLANET:
                enumMode = ViewMode.VIEWMODE_LITTLEPLANET;
                break;*/
            case EVVrPlayerConstrants.VIEWMODEL_SPHERE:
                enumMode = ViewMode.VIEWMODEL_SPHERE;
                modeStr = "sphere";
                break;
            case EVVrPlayerConstrants.VIEWMODEL_LINEFLAT:
                enumMode = ViewMode.VIEWMODEL_LINEFLAT;
                modeStr = "lineflat";
                break;
            /*case EVVrPlayerConstrants.VIEWMODEL_WIDE_ANGLE:
                enumMode = ViewMode.VIEWMODEL_WIDE_ANGLE;
                break;
            case EVVrPlayerConstrants.VIEWMODE_FRONTBACK:
                enumMode = ViewMode.VIEWMODE_FRONTBACK;
                break;*/
        }
        String xmlstring = String.format(PanoPlayerTemplate, "", "video", mUrl, modeStr);
        mPlayerUrl.setXmlContent(xmlstring);
        mRenderer.setViewMode(enumMode);
        //goPlay(mUrl,modeStr);
    }

    public void setGyroEnable(boolean enable) {
        mRenderer.setGyroEnable(enable);
    }

    public void start() {
        mVideoPlugin.start();
    }

    public void pause() {
        mVideoPlugin.pause();
    }

    public int getDuration() {
        return mVideoPlugin.getDuration();
    }

    public int getCurPosition() {
        return mVideoPlugin.getCurPosition();
    }

    public void seekTo(int time) {
        mVideoPlugin.seekTo(time);
    }

    public int getreadBufferingPercent() {
        return mVideoPlugin.getreadBufferingPercent();
    }

    public void onResume(){
        mVideoView.onResume();
    }

    public void onPause(){
        mVideoView.onPause();
    }

    public void release() {
        if (mRenderer != null) {
            mRenderer.release();
        }
    }

    public interface EVVrPlayerListener {
        void playOnLoading();

        void playOnLoaded();

        void playOnEnter(EVVrPlayerConstrants.EVVrRamaData ramaData);

        void playOnLeave(EVVrPlayerConstrants.EVVrRamaData ramaData);

        void playOnError(int playerErrorCode);
    }

    private EVVrPlayerListener mPlayerListener;

    public void setPlayerListener(EVVrPlayerListener l) {
        this.mPlayerListener = l;
        this.mRenderer.setListener(mIPanoPlayerListener);
    }

    private IPanoPlayerListener mIPanoPlayerListener = new IPanoPlayerListener() {
        @Override
        public void PanoPlayOnLoading() {
            mPlayerListener.playOnLoading();
        }

        @Override
        public void PanoPlayOnLoaded() {
            mPlayerListener.playOnLoaded();
        }

        @Override
        public void PanoPlayOnEnter(PanoramaData panoramaData) {
            mPlayerListener.playOnEnter(new EVVrPlayerConstrants.EVVrRamaData(panoramaData));
        }

        @Override
        public void PanoPlayOnLeave(PanoramaData panoramaData) {
            mPlayerListener.playOnLeave(new EVVrPlayerConstrants.EVVrRamaData(panoramaData));
        }

        @Override
        public void PanoPlayOnError(PanoPlayer.PanoPlayerErrorCode panoPlayerErrorCode) {
            switch (panoPlayerErrorCode) {
                case PANO_PLAY_SUCCESS:
                    mPlayerListener.playOnError(EVVrPlayerConstrants.ERRORCODE_PLAY_SUCCESS);
                    break;
                case PANO_IMAGE_LOAD_ERROR:
                    mPlayerListener.playOnError(EVVrPlayerConstrants.ERRORCODE_IMAGE_LOAD_ERROR);
                    break;
                case PANO_LACK_CALIBRATION:
                    mPlayerListener.playOnError(EVVrPlayerConstrants.ERRORCODE_LACK_CALIBRATION);
                    break;
                case PANO_PLAY_URL_IS_EMPTY:
                    mPlayerListener.playOnError(EVVrPlayerConstrants.ERRORCODE_PLAY_URL_IS_EMPTY);
                    break;
                case PANO_PANORAMALIST_IS_EMPTY:
                    mPlayerListener.playOnError(EVVrPlayerConstrants.ERRORCODE_RAMALIST_IS_EMPTY);
                    break;
                case PANO_SETTING_DATA_IS_EMPTY:
                    mPlayerListener.playOnError(EVVrPlayerConstrants.ERRORCODE_SETTING_DATA_IS_EMPTY);
                    break;
                case PANO_PLAY_MANAGER_DATA_IS_EMPTY:
                    mPlayerListener.playOnError(EVVrPlayerConstrants.ERRORCODE_PLAY_MANAGER_DATA_IS_EMPTY);
                    break;

            }
        }
    };

    public interface IEVVrPlayerControlListener {
        void OnInit();

        void OnStatusChanged(int status);

        void OnProgressChanged(int curTime, int bufTime, int maxTime);

        void OnSeekFinished();

        void OnPlayerError(int status, String msg);
    }

    private IEVVrPlayerControlListener mIEVVrPlayerControlListener;

    public void setControlListener(IEVVrPlayerControlListener l) {
        this.mIEVVrPlayerControlListener = l;
        this.mRenderer.setVideoPluginListener(mPluginListener);
    }

    private IPanoPlayerVideoPluginListener mPluginListener = new IPanoPlayerVideoPluginListener() {
        @Override
        public void PluginVideoOnInit() {
            Plugin plugin = mRenderer.getCurPlugin();
            if (plugin instanceof VideoPlugin) {
                mVideoPlugin = (VideoPlugin) plugin;
                //设置播放器日志级别
                //videoplugin.setLogLevel(IjkMediaPlayer.IJK_LOG_VERBOSE);
            }
            mIEVVrPlayerControlListener.OnInit();
        }

        @Override
        public void PluginVideoOnStatusChanged(PanoPlayer.PanoVideoPluginStatus panoVideoPluginStatus) {
            switch (panoVideoPluginStatus) {
                case VIDEO_STATUS_STOP:
                    mIEVVrPlayerControlListener.OnStatusChanged(EVVrPlayerConstrants.VIDEO_STATUS_BUFFER_EMPTY);
                    break;
                case VIDEO_STATUS_PAUSE:
                    mIEVVrPlayerControlListener.OnStatusChanged(EVVrPlayerConstrants.VIDEO_STATUS_PAUSE);
                    break;
                case VIDEO_STATUS_FINISH:
                    mIEVVrPlayerControlListener.OnStatusChanged(EVVrPlayerConstrants.VIDEO_STATUS_FINISH);
                    break;
                case VIDEO_STATUS_PLAYING:
                    mIEVVrPlayerControlListener.OnStatusChanged(EVVrPlayerConstrants.VIDEO_STATUS_PLAYING);
                    break;
                case VIDEO_STATUS_PREPARED:
                    mIEVVrPlayerControlListener.OnStatusChanged(EVVrPlayerConstrants.VIDEO_STATUS_PREPARED);
                    break;
                case VIDEO_STATUS_UNPREPARED:
                    mIEVVrPlayerControlListener.OnStatusChanged(EVVrPlayerConstrants.VIDEO_STATUS_UNPREPARED);
                    break;
                case VIDEO_STATUS_BUFFER_EMPTY:
                    mIEVVrPlayerControlListener.OnStatusChanged(EVVrPlayerConstrants.VIDEO_STATUS_BUFFER_EMPTY);
                    break;
                case VIDEO_STATUS_HW_TO_AVCODEC:
                    mIEVVrPlayerControlListener.OnStatusChanged(EVVrPlayerConstrants.VIDEO_STATUS_HW_TO_AVCODEC);
                    break;
            }
        }

        @Override
        public void PluginVideoOnProgressChanged(int curTime, int bufTime, int maxTime) {
            mIEVVrPlayerControlListener.OnProgressChanged(curTime, bufTime, maxTime);
        }

        @Override
        public void PluginVideoOnSeekFinished() {
            mIEVVrPlayerControlListener.OnSeekFinished();
        }

        @Override
        public void PluginVideOnPlayerError(PanoPlayer.PanoPlayerErrorStatus panoPlayerErrorStatus, String s) {
            switch (panoPlayerErrorStatus) {
                case ERRORSTATUS_FORMAT:
                    mIEVVrPlayerControlListener.OnPlayerError(EVVrPlayerConstrants.ERRORSTATUS_FORMAT, s);
                    break;
                case ERRORSTATUS_NERWORK:
                    mIEVVrPlayerControlListener.OnPlayerError(EVVrPlayerConstrants.ERRORSTATUS_NERWORK, s);
                    break;
            }
        }

        @Override
        public void PluginVideoOnStatisticsChanged(StatisticsData statisticsData) {

        }
    };


}
