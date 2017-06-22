package com.easyvaas.sdk.demo;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.easyvaas.sdk.demo.bean.LiveOption;
import com.easyvaas.sdk.demo.utils.Logger;
import com.easyvaas.sdk.demo.utils.SingleToast;
import com.easyvaas.sdk.demo.utils.Utils;
import com.easyvaas.sdk.live.base.EVStreamerParameter;
import com.easyvaas.sdk.live.base.OnErrorListener;
import com.easyvaas.sdk.live.base.OnInfoListener;
import com.easyvaas.sdk.live.base.interactive.OnInteractiveLiveListener;
import com.easyvaas.sdk.live.wrapper.EVLive;
import com.easyvaas.sdk.live.wrapper.LiveConstants;
import com.ksyun.media.player.IMediaPlayer;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.io.File;
import java.lang.ref.SoftReference;


public class RecorderActivity extends Activity{
    private static final String TAG = RecorderActivity.class.getSimpleName();
    public static final String EXTRA_LIVE_CONFIG_BEAN = "extra_live_config_bean";

    private final static int PERMISSION_REQUEST_CAMERA_AUDIOREC = 1;

    private static final String TEST_MP3 = "/sdcard/Music/test.mp3";
    private static final String TEST_WATERMARK_PATH = "assets://watermark.png";
    private static final String BGM_FILE_NAME = "bgm.mp3";
    private static final float BGM_VOLUME = 1.0f;

    private static final int REQUEST_CODE_REGISTER = 1;

    private static final int MSG_REFRESH_START_TIME = 101;
    private MyHandler mHandler;

    protected TextView mVideoTopicTv;
    protected TextView mVideoTitleTv;
    protected TextView mDurationTv;
    protected View mTopInfoAreaView;
    private ImageView mQRCodeImageView;

    private View mOptionsView;

    private View mCameraViewFrame;
    private View mTextureViewFrame;
    private FrameLayout mFlRemoteViewContainer;

    private Dialog mNetworkInvalidDialog;
    private Dialog mConfirmStopDialog;
    private Dialog mVideoStoppedDialog;

    private GLSurfaceView mCameraPreviewView;
    private EVLive mEVLive;

    private long mStartTime;

    private String mVid;
    private String mLid;
    private String mUri;
    private String mBgmFilePath;

    private String mLiveTitle;

    private PowerManager.WakeLock mWakeLock;

    private LiveOption mliveOption;

    private boolean mQRCodeShow = false;
    private boolean mIsLandscape = false;
    private boolean mAutoStreaming = true;

    public Bitmap mBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mliveOption = (LiveOption) getIntent().getSerializableExtra(EXTRA_LIVE_CONFIG_BEAN);

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_recorder);
        mHandler = new MyHandler(this);

        mBgmFilePath = getFilesDir() + File.separator + BGM_FILE_NAME;
        if (!new File(mBgmFilePath).exists()) {
            Utils.copyAssetsFiles(this, BGM_FILE_NAME, mBgmFilePath);
        }

        initUIComponents();
        initListeners();

        mEVLive = new EVLive(this);
        EVStreamerParameter.Builder builder = new EVStreamerParameter.Builder();
        if (mliveOption != null) {
            builder.setVideoResolution(mliveOption.getVideoResolution())
                    .setInitVideoBitrate(mliveOption.getInitVideoBitrate())
                    .setAudioBitrate(mliveOption.getAudioBitrate())
                    .setAudioCodec(mliveOption.getAudioCodec())
                    .setUseFrontCamera(mliveOption.isUseFrontCamera())
                    .setIsBeautyOn(mliveOption.isBeautyOn())
                    .setPortrait(mliveOption.isPortrait())
                    .setDisplayRotation(mliveOption.isPortrait() ? 90 : 0)
                    .setAgoraId(Constant.INTERACTIVE_LIVE_APP_ID);
            mIsLandscape = !mliveOption.isPortrait();
        }

        setRequestedOrientation(mIsLandscape
                ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mEVLive.setParameter(builder.build());

        mEVLive.setDisplayOrientation(mIsLandscape ? 90 : 0);

        //如果需要连麦,必须先初始化连麦参数
        if (!TextUtils.isEmpty(Constant.INTERACTIVE_LIVE_APP_ID)) {
            mEVLive.initInteractiveLiveConfig(Constant.INTERACTIVE_LIVE_APP_ID, true);
        }

        //设置错误、信息回调
        mEVLive.setOnErrorListener(mErrorListener);
        mEVLive.setOnInfoListener(mInfoListener);

        startCameraPreviewWithPermCheck();

        if (mIsLandscape) {
            mEVLive.addWaterMarkLogo(TEST_WATERMARK_PATH, 0.05F, 0.09F, 0F, 0.1F, 0.8F);
        } else {
            mEVLive.addWaterMarkLogo(TEST_WATERMARK_PATH, 0.08F, 0.06F, 0.2F, 0F, 0.8F);
        }

        Thread.currentThread().setName("main thread");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(MSG_REFRESH_START_TIME);

        if (null != mEVLive) {
            mEVLive.endInteractiveLive();
            mEVLive.onDestroy();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    private void initUIComponents() {
        mTopInfoAreaView = this.findViewById(R.id.live_info_rl);
        mVideoTopicTv = (TextView) findViewById(R.id.player_category_tv);
        mVideoTitleTv = (TextView) findViewById(R.id.player_title_tv);
        mDurationTv = (TextView) findViewById(R.id.player_duration_tv);

        mQRCodeImageView = (ImageView) findViewById(R.id.qrcode_image);

        mOptionsView = this.findViewById(R.id.live_options_right_ll);
    }

    private void initListeners() {
        mOptionsView.findViewById(R.id.live_stop_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFinishing()) {
                    showConfirmStopDialog();
                }
            }
        });

        CheckBox cameraSwitchCb = (CheckBox)mOptionsView.findViewById(R.id.live_switch_camera_cb);
        cameraSwitchCb.setChecked(!mliveOption.isUseFrontCamera());
        cameraSwitchCb.setOnCheckedChangeListener(mOnCheckedChangeListener);

        CheckBox muteCb = (CheckBox)mOptionsView.findViewById(R.id.live_mute_cb);
        muteCb.setOnCheckedChangeListener(mOnCheckedChangeListener);

        CheckBox flashLightCb = (CheckBox)mOptionsView.findViewById(R.id.live_flash_cb);
        flashLightCb.setEnabled(!mliveOption.isUseFrontCamera());
        flashLightCb.setOnCheckedChangeListener(mOnCheckedChangeListener);

        CheckBox beautyCb = (CheckBox)mOptionsView.findViewById(R.id.live_beauty_cb);
        beautyCb.setOnCheckedChangeListener(mOnCheckedChangeListener);
        beautyCb.setChecked(mliveOption.isBeautyOn());
        if (mliveOption.isBeautyOn()) {
            beautyCb.setVisibility(View.VISIBLE);
        } else {
            beautyCb.setVisibility(View.GONE);
        }
        CheckBox interactiveCb= (CheckBox) mOptionsView.findViewById(R.id.interactive_live_cb);
        interactiveCb.setOnCheckedChangeListener(mOnCheckedChangeListener);

        mVideoTitleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mQRCodeShow) {
                    if (TextUtils.isEmpty(mLid)) {
                        showErrorToast(R.string.vid_was_empty);
                        return;
                    }
                    if (mBitmap == null) {
                        mBitmap = CodeUtils.createImage(mLid, 400, 400, null);
                    }
                    mQRCodeImageView.setImageBitmap(mBitmap);
                    mQRCodeImageView.setVisibility(View.VISIBLE);
                    mQRCodeShow = true;
                } else {
                    mQRCodeShow = false;
                    mQRCodeImageView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initBgmPlayer() {
        mEVLive.setBgmOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                Logger.d(TAG, "end of bgm");
            }
        });
        mEVLive.setBgmOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
                Logger.e(TAG, "bgm play error: " + what + ", extra: " + extra);
                return false;
            }
        });
        mEVLive.setBgmVolume(0.5F);
        mEVLive.setBgmMute(false);
        mEVLive.setEnableAudioMix(true);
    }

    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener =
            new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    switch (buttonView.getId()) {
                        case R.id.live_switch_camera_cb:
                            if (mEVLive != null) {
                                mEVLive.switchCamera();
                            }

                            CheckBox checkBox = (CheckBox) mOptionsView.findViewById(R.id.live_flash_cb);
                            checkBox.setEnabled(isChecked);
                            if (isChecked) {
                                checkBox.setChecked(false);
                            }
                            break;
                        case R.id.live_flash_cb:
                            if (mEVLive != null) {
                                mEVLive.switchFlashlight();
                            }
                            break;
                        case R.id.live_beauty_cb:
                            if (mEVLive != null) {
                                mEVLive.switchBeauty(isChecked);
                            }
                            break;
                        case R.id.live_mute_cb:
                            if (mEVLive != null) {
                                mEVLive.switchAudioMute();
                            }
                            break;
                        case R.id.interactive_live_cb:
                            if (TextUtils.isEmpty(Constant.INTERACTIVE_LIVE_APP_ID)) {
                                SingleToast.show(getApplicationContext(), R.string.interactive_id_empty);
                            } else {
                                if (isChecked) {
                                    Toast.makeText(RecorderActivity.this,
                                            R.string.start_interactive_tips, Toast.LENGTH_SHORT).show();
                                    mEVLive.setOnInteractiveLiveListener(mOnInteractiveLiveListener);
                                    if (mIsLandscape) {
                                        mEVLive.setRTCSubScreenRect(0.65F, 0.1F, 0.3F, 0.35F);
                                    } else {
                                        mEVLive.setRTCSubScreenRect(0.65F, 0.1F, 0.35F, 0.3F);
                                    }
                                    mEVLive.startInteractiveLive(Constant.CHANNEL_ID);
                                } else {
                                    Toast.makeText(RecorderActivity.this, R.string.end_interactive, Toast.LENGTH_SHORT)
                                            .show();
                                    mEVLive.endInteractiveLive();
                                }
                            }
                            break;
                    }
                }
            };

    @Override
    protected void onResume() {
        super.onResume();
        if (null != mEVLive) {
            mEVLive.onResume();

            startCameraPreviewWithPermCheck();
            Logger.w(TAG, "easyvaas-sdk-, onResume");
            mHandler.sendEmptyMessage(MSG_REFRESH_START_TIME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mEVLive) {
            mEVLive.onPause();
            mHandler.removeMessages(MSG_REFRESH_START_TIME);
        }
    }

    @Override
    public void onBackPressed() {
        showConfirmStopDialog();
    }

    private void showNetworkInvalidDialog() {
        if (isFinishing()) {
            return;
        }
        if (mNetworkInvalidDialog == null) {
            mNetworkInvalidDialog = Utils.getOneButtonDialog(this,
                    getResources().getString(R.string.no_network_dialog), false, false,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
        }
        mNetworkInvalidDialog.show();
    }

    private void showVideoStoppedDialog() {
        if (isFinishing()) {
            return;
        }
        if (mVideoStoppedDialog == null) {
            mVideoStoppedDialog = Utils.getOneButtonDialog(this,
                    getResources().getString(R.string.video_stopped_by_server_dialog), false, false,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
        }
        mVideoStoppedDialog.show();
    }

    private void showToastOnUiThread(final int resId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                SingleToast.show(getApplicationContext(), resId);
                finish();
            }
        });
    }

    private void showErrorToast(int resId) {
        if (isFinishing()) {
            return;
        }

        SingleToast.show(getApplicationContext(), resId);
        finish();
    }

    private OnErrorListener mErrorListener = new OnErrorListener() {
        @Override
        public boolean onError(int what) {
            switch (what) {
                case LiveConstants.EV_LIVE_ERROR_VERSION_LOW:
                    showErrorToast(R.string.title_version_not_supported);
                    break;
                case LiveConstants.EV_LIVE_ERROR_OPEN_CAMERA:
                    showErrorToast(R.string.title_call_camera_error);
                    break;
                case LiveConstants.EV_LIVE_ERROR_CREATE_AUDIORECORD:
                    showErrorToast(R.string.title_audio_record_error);
                    break;
                case LiveConstants.EV_LIVE_ERROR_STARTING:
                    break;
                case LiveConstants.EV_LIVE_ERROR_RECONNECT:
                    mHandler.removeMessages(MSG_REFRESH_START_TIME);
                    showNetworkInvalidDialog();

                    break;
                case LiveConstants.EV_LIVE_ERROR_VIDEO_ALREADY_STOPPED:
                    if (isFinishing()) {
                        return true;
                    }
                    Dialog dialog = Utils.getOneButtonDialog(RecorderActivity.this,
                            getString(R.string.dialog_title_live_stop), false, false,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            });
                    dialog.show();

                    break;
                case LiveConstants.EV_LIVE_ERROR_SDK_INIT:
                    showErrorToast(R.string.msg_sdk_init_error);
                    break;
                case LiveConstants.EV_LIVE_PUSH_LOCATE_ERROR:
                    showErrorToast(R.string.msg_push_locate_error);
                    break;
                case LiveConstants.EV_LIVE_PUSH_REDIRECT_ERROR:
                    showErrorToast(R.string.msg_push_redirect_error);
                    break;
            }
            return true;
        }
    };

    private OnInfoListener mInfoListener = new OnInfoListener() {
        @Override
        public boolean onInfo(int what) {
            switch (what) {
                case LiveConstants.EV_LIVE_INFO_CAMERA_INIT_DONE:
                    if (mAutoStreaming) {
                        mLid = mliveOption.getLid();
                        mEVLive.startStreaming(mLid, mliveOption.getKey());
                    }
                    break;
                case LiveConstants.EV_LIVE_INFO_RECONNECTING:
                    if (isFinishing()) {
                        return true;
                    }
                    SingleToast.show(getApplicationContext(), R.string.network_invalid_try_reconnect);
                    mHandler.removeMessages(MSG_REFRESH_START_TIME);

                    break;
                case LiveConstants.EV_LIVE_INFO_RECONNECTED:
                    if (isFinishing()) {
                        return true;
                    }
                    SingleToast.show(getApplicationContext(), R.string.network_ok_message);
                    mHandler.sendEmptyMessage(MSG_REFRESH_START_TIME);

                    break;
                case LiveConstants.EV_LIVE_INFO_START_SUCCESS:
                    break;
                case LiveConstants.EV_LIVE_INFO_STREAMING:
                    break;
                case LiveConstants.EV_LIVE_INFO_STREAM_SUCCESS:
                    acquireWakeLock();
                    mStartTime = System.currentTimeMillis();

                    if (isFinishing()) {
                        return true;
                    }
                    mDurationTv.setVisibility(View.VISIBLE);
                    mOptionsView.setVisibility(View.VISIBLE);
                    mVideoTitleTv.setVisibility(View.VISIBLE);
                    mVideoTitleTv.setText("lid: " + mLid + ", 点击弹出二维码");
                    mHandler.sendEmptyMessage(MSG_REFRESH_START_TIME);

                    if (mliveOption.isBgmMix()) {
                        initBgmPlayer();
                        mEVLive.startBgmPlayer(mBgmFilePath, true);
                    }
                    break;
            }
            return true;
        }
    };

    private OnInteractiveLiveListener mOnInteractiveLiveListener = new OnInteractiveLiveListener() {
        @Override
        public void onJoinChannelResult(boolean isSuccess) {

        }

        @Override
        public void onLeaveChannelSuccess() {
        }

        @Override
        public void onFirstRemoteVideoDecoded() {
            onInteractiveStart();
        }

        @Override
        public void onFirstLocalVideoFrame() {

        }

        @Override
        public void onUserOffline(int userId, int reason) {
            onInteractiveEnd();
        }

        @Override
        public void onError(int code, String message) {
        }
    };

    private void onInteractiveStart() {

    }

    private void onInteractiveEnd() {
        Toast.makeText(RecorderActivity.this, R.string.end_interactive, Toast.LENGTH_SHORT)
                .show();
        if (mEVLive != null) {
            mEVLive.endInteractiveLive();
        }

        ((CheckBox) mOptionsView.findViewById(R.id.interactive_live_cb)).setChecked(false);
    }

    static class MyHandler extends Handler {
        private SoftReference<RecorderActivity> softReference;

        public MyHandler(RecorderActivity activity) {
            softReference = new SoftReference<RecorderActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            RecorderActivity activity = softReference.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case MSG_REFRESH_START_TIME:
                    activity.mDurationTv.setText(Utils.getDurationTime(activity, activity.mStartTime,
                            System.currentTimeMillis()));
                    sendEmptyMessageDelayed(MSG_REFRESH_START_TIME, 1000);
                    break;
            }
        }
    }

    private void showConfirmStopDialog() {
        if (mConfirmStopDialog != null) {
            mConfirmStopDialog.show();
            return;
        }
        mConfirmStopDialog = new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.title_confirm_stop_live)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        liveStop();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                            dialog.dismiss();
                            return true;
                        }
                        return false;
                    }
                })
                .setCancelable(false)
                .create();
        mConfirmStopDialog.show();
    }

    private void liveStop() {
        releaseWakeLock();

        mHandler.removeMessages(MSG_REFRESH_START_TIME);
        if (mEVLive != null) {
            if (mliveOption.isBgmMix()) {
                mEVLive.stopBgmPlayer();
            }
            mEVLive.stopLive();
        }
        finish();
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

    private void startCameraPreviewWithPermCheck() {
        int cameraPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int audioPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (cameraPerm != PackageManager.PERMISSION_GRANTED ||
                audioPerm != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Log.e(TAG, "No CAMERA or AudioRecord permission, please check");
                SingleToast.show(this, R.string.title_camera_audio_permission);
            } else {
                String[] permissions = {Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions,
                        PERMISSION_REQUEST_CAMERA_AUDIOREC);
            }
        } else {
            startCameraPreview();
        }
    }


    private void startCameraPreview() {
        mCameraPreviewView = (GLSurfaceView) this.findViewById(R.id.camera_preview);

        mEVLive.setCameraPreview(mCameraPreviewView);
        mEVLive.startCameraPreview();

        //调用SDK对应的onCreate函数
        mEVLive.onCreate();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA_AUDIOREC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCameraPreview();
                } else {
                    Log.e(TAG, "No CAMERA or AudioRecord permission");
                    SingleToast.show(this, R.string.title_camera_audio_permission);
                }
                break;
            }
        }
    }
}
