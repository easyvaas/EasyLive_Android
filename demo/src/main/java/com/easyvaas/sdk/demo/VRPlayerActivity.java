package com.easyvaas.sdk.demo;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import com.easyvaas.sdk.demo.utils.Utils;
import com.easyvaas.sdk.demo.bean.PlayOption;
import com.easyvaas.sdk.demo.utils.Logger;
import com.easyvaas.sdk.demo.utils.SingleToast;
import com.easyvaas.sdk.vrplayer.EVVrPlayer;
import com.easyvaas.sdk.vrplayer.EVVrPlayerConstrants;
import com.easyvaas.sdk.vrplayer.EVVrVideoView;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class VRPlayerActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private final static String TAG = VRPlayerActivity.class.getSimpleName();
    public static final String EXTRA_PLAY_CONFIG_BEAN = "extra_play_config_bean";

    private EVVrVideoView mVideoView;
    private EVVrPlayer mPlayer;

    private PlayOption mPlayOption;

    private PowerManager.WakeLock mWakeLock;

    private Dialog mLoadingDialog;

    private boolean mIsGyroEnable = true;
    private boolean mIsCanPause;

    private MediaController mMediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vrplayer);

        mPlayOption = (PlayOption) getIntent().getSerializableExtra(EXTRA_PLAY_CONFIG_BEAN);

        initUIComponents();
        initImageLoader();

        if (mPlayOption != null) {
            mPlayer = new EVVrPlayer(mVideoView, this);
            mPlayer.setPlayerListener(playerListener);
            mPlayer.setControlListener(controlListener);
            mPlayer.setGyroEnable(mIsGyroEnable);
            //mPlayer.setViewMode(EVVrPlayerConstrants.VIEWMODE_DEF);
            mPlayer.startPlay(mPlayOption.getVideoPath(), mPlayOption.isLive());

            if (!isFinishing()) {
                showLoadingDialog(R.string.loading_data, true, true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPlayer != null) {
            mPlayer.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayer != null) {
            mPlayer.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.release();
        }
        releaseWakeLock();
        dismissLoadingDialog();
    }

    //private boolean isNeedVR = false;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        } else {
        }
    }

    @Override
    public void onBackPressed() {
        /*if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return;
        }*/
        if (findViewById(R.id.player_bottom_action_bar).getVisibility() != View.VISIBLE) {
            toggleProgressBar(false);
        } else {
            super.onBackPressed();
        }
    }

    private void hideBottomView() {
        toggleProgressBar(false);
        findViewById(R.id.player_bottom_action_bar).setVisibility(View.GONE);
        findViewById(R.id.live_close_iv).setVisibility(View.GONE);
    }

    private void showBottomView() {
        findViewById(R.id.player_bottom_action_bar).setVisibility(View.VISIBLE);
        findViewById(R.id.live_close_iv).setVisibility(View.VISIBLE);
    }

    private void initUIComponents() {
        mVideoView = (EVVrVideoView) findViewById(R.id.vr_player_view);
        mVideoView.setOnClickListener(this);
        findViewById(R.id.live_close_iv).setOnClickListener(this);
        findViewById(R.id.player_bottom_360_btn).setOnClickListener(this);
        ((CheckBox) findViewById(R.id.player_bottom_vr_btn)).setOnCheckedChangeListener(this);
        findViewById(R.id.player_bottom_360_btn).setSelected(true);

        if (mPlayOption.isLive()) {
            findViewById(R.id.player_bottom_progress_btn).setVisibility(View.GONE);
        } else {
            findViewById(R.id.player_bottom_progress_btn).setOnClickListener(this);
        }

        mMediaController = new MediaController(this);
        mMediaController.setMediaPlayer(mediaControl);
        mMediaController.setAnchorView(mVideoView);
        mMediaController.setPositionChangeListener(new MediaController.PositionChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, long position, boolean fromuser) {

            }
        });
    }

    private void initImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.NONE).cacheInMemory()
                .cacheOnDisc().build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this).defaultDisplayImageOptions(defaultOptions)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                //.writeDebugLogs()
                .tasksProcessingOrder(QueueProcessingType.FIFO).build();
        ImageLoader.getInstance().init(config);
    }

    private void toggleProgressBar(boolean show) {
        if (mMediaController == null || mVideoView == null) {
            return;
        }
        View actionBar = findViewById(R.id.player_bottom_action_bar);
        if (show) {
            actionBar.setVisibility(View.INVISIBLE);
            mMediaController.show();
        } else {
            actionBar.setVisibility(View.VISIBLE);
            mMediaController.hide();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.live_close_iv:
                finish();
                break;
            case R.id.player_bottom_360_btn:
                if (mPlayer != null) {
                    mIsGyroEnable = !mIsGyroEnable;
                    mPlayer.setGyroEnable(mIsGyroEnable);
                }
                if (!findViewById(R.id.player_bottom_360_btn).isSelected()) {
                    findViewById(R.id.player_bottom_360_btn).setSelected(true);
                } else {
                    findViewById(R.id.player_bottom_360_btn).setSelected(false);
                }
                break;
            case R.id.player_bottom_progress_btn:
                toggleProgressBar(true);
                break;
            default:
                toggleProgressBar(false);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        mPlayer.setGyroEnable(true);
        if (b) {
            //
            mPlayer.setGyroEnable(mIsGyroEnable);
            mPlayer.setViewMode(EVVrPlayerConstrants.VIEWMODE_VR_HORIZONTAL);
            Utils.hideStatusBar(mVideoView, true);
            hideBottomView();
            SingleToast.show(VRPlayerActivity.this, R.string.exit_vr_pattern);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏
        } else {
            //
            mPlayer.setGyroEnable(mIsGyroEnable);
            mPlayer.setViewMode(EVVrPlayerConstrants.VIEWMODE_DEF);
            Utils.hideStatusBar(mVideoView, false);
            showBottomView();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void acquireWakeLock() {
        if (null == mWakeLock) {
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);
        }

        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if (null != mWakeLock) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    private void showLoadingDialog(int resId, boolean dismissTouchOutside, boolean cancelable) {
        showLoadingDialog(getString(resId), dismissTouchOutside, cancelable);
    }

    private void showLoadingDialog(String message, boolean dismissTouchOutside, boolean cancelable) {
        if (mLoadingDialog == null) {
            mLoadingDialog = Utils.getProcessDialog(this, message, dismissTouchOutside, cancelable);
        }
        mLoadingDialog.setCancelable(cancelable);
        mLoadingDialog.setCanceledOnTouchOutside(dismissTouchOutside);
        mLoadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    private EVVrPlayer.EVVrPlayerListener playerListener = new EVVrPlayer.EVVrPlayerListener() {
        @Override
        public void playOnLoading() {
            Logger.d(TAG, "EVVrPlayer OnLoading");
        }

        @Override
        public void playOnLoaded() {
            Logger.d(TAG, "EVVrPlayer OnLoaded");
            findViewById(R.id.player_bottom_360_btn).setEnabled(true);
            findViewById(R.id.player_bottom_vr_btn).setEnabled(true);
            findViewById(R.id.player_bottom_progress_btn).setEnabled(true);
            acquireWakeLock();
            dismissLoadingDialog();
        }

        @Override
        public void playOnEnter(EVVrPlayerConstrants.EVVrRamaData ramaData) {
            Logger.d(TAG, "EVVrPlayer OnEnter");
            findViewById(R.id.player_bottom_360_btn).setEnabled(false);
            findViewById(R.id.player_bottom_vr_btn).setEnabled(false);
            findViewById(R.id.player_bottom_progress_btn).setEnabled(false);
        }

        @Override
        public void playOnLeave(EVVrPlayerConstrants.EVVrRamaData ramaData) {
            Logger.d(TAG, "EVVrPlayer OnLeave");
        }

        @Override
        public void playOnError(int code) {
            Logger.e(TAG, "EVVrPlayer OnError: " + code);
            Utils.getOneButtonDialog(VRPlayerActivity.this, getString(R.string.msg_play_error)
                    + ", errcode: " + code, false, false, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    releaseWakeLock();
                    dismissLoadingDialog();
                    finish();
                }
            }).show();
        }
    };

    private EVVrPlayer.IEVVrPlayerControlListener controlListener = new EVVrPlayer.IEVVrPlayerControlListener() {
        @Override
        public void OnInit() {
            Logger.d(TAG, "EVVrPlayer OnInit");
        }

        @Override
        public void OnStatusChanged(int status) {
            switch (status) {
                case EVVrPlayerConstrants.VIDEO_STATUS_PAUSE:
                    mIsCanPause = true;
                    break;
                case EVVrPlayerConstrants.VIDEO_STATUS_STOP:
                    mIsCanPause = true;
                    break;
                case EVVrPlayerConstrants.VIDEO_STATUS_PLAYING:
                    mIsCanPause = false;
                    break;
                default:
                    break;
            }
        }

        @Override
        public void OnProgressChanged(int curTime, int bufTime, int maxTime) {

        }

        @Override
        public void OnSeekFinished() {

        }

        @Override
        public void OnPlayerError(int status, String msg) {
            Logger.e(TAG, "EVVrPlayer OnPlayerError: " + msg);
            String msgStr = TextUtils.isEmpty(msg) ? getString(R.string.msg_play_error) : msg;
            Utils.getOneButtonDialog(VRPlayerActivity.this, msgStr, false, false,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            releaseWakeLock();
                            dismissLoadingDialog();
                            finish();
                        }
                    }).show();
        }
    };

    private MediaController.MediaPlayerControl mediaControl = new MediaController.MediaPlayerControl() {
        @Override
        public void start() {
            if (mPlayer != null) {
                mPlayer.start();
            }
        }

        @Override
        public void pause() {
            if (mPlayer != null) {
                mPlayer.pause();
            }
        }

        @Override
        public int getDuration() {
            return mPlayer != null ? mPlayer.getDuration() : 0;
        }

        @Override
        public int getCurrentPosition() {
            return mPlayer != null ? mPlayer.getCurPosition() : 0;
        }

        @Override
        public void seekTo(long pos) {
            if (mPlayer != null) {
                mPlayer.seekTo((int) pos);
            }
        }

        @Override
        public boolean isPlaying() {
            return !mIsCanPause;
        }

        @Override
        public int getBufferPercentage() {
            return mPlayer != null ? mPlayer.getreadBufferingPercent() : 0;
        }

        @Override
        public boolean canPause() {
            return !mIsCanPause;
        }

        @Override
        public boolean canSeekBackward() {
            return true;
        }

        @Override
        public boolean canSeekForward() {
            return true;
        }
    };
}
