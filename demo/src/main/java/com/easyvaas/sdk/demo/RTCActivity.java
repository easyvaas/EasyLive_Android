package com.easyvaas.sdk.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easyvaas.sdk.demo.utils.Logger;
import com.easyvaas.sdk.demo.utils.SingleToast;
import com.easyvaas.sdk.live.base.EVStreamerParameter;
import com.easyvaas.sdk.live.base.interactive.OnInteractiveLiveListener;
import com.easyvaas.sdk.live.wrapper.EVLive;
import com.easyvaas.sdk.player.base.EVVideoView;

public class RTCActivity extends Activity implements View.OnClickListener {
    private final static String TAG = RTCActivity.class.getSimpleName();
    public static final String EXTRA_RTC_CONFIG_BEAN = "extra_rtc_config_bean";

    private final static int PERMISSION_REQUEST_CAMERA_AUDIOREC = 1;

    private EVLive mEVLive;

    private String mChannelID;
    private GLSurfaceView mCameraPreviewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtc);

        mChannelID = getIntent().getStringExtra(EXTRA_RTC_CONFIG_BEAN);

        initUIComponents();
        initListeners();

        mEVLive = new EVLive(this);
        EVStreamerParameter.Builder builder = new EVStreamerParameter.Builder();
        builder.setUseFrontCamera(true)
                .setIsBeautyOn(false)
                .setAgoraId(Constant.INTERACTIVE_LIVE_APP_ID);
        mEVLive.setParameter(builder.build());
        if (!TextUtils.isEmpty(Constant.INTERACTIVE_LIVE_APP_ID)) {
            mEVLive.initInteractiveLiveConfig(Constant.INTERACTIVE_LIVE_APP_ID, false);
        }

        startCameraPreviewWithPermCheck();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != mEVLive) {
            mEVLive.onResume();
            Logger.w(TAG, "easyvaas-sdk-, onResume");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mEVLive) {
            mEVLive.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != mEVLive) {
            mEVLive.endInteractiveLive();
            mEVLive.onDestroy();
        }
    }

    private void initUIComponents() {
        mCameraPreviewView = (GLSurfaceView) this.findViewById(R.id.camera_preview);
    }

    private void initListeners() {
        findViewById(R.id.rtc_close_iv).setOnClickListener(this);

        CheckBox interactiveCb= (CheckBox) findViewById(R.id.interactive_live_cb);
        interactiveCb.setOnCheckedChangeListener(mOnCheckedChangeListener);
    }

    private void startCameraPreview() {
        mEVLive.setCameraPreview(mCameraPreviewView);
        mEVLive.startCameraPreview();
    }

    private void stopCameraPreview() {
        mEVLive.stopCameraPreview();
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

    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.interactive_live_cb:
                    if (TextUtils.isEmpty(Constant.INTERACTIVE_LIVE_APP_ID)) {
                        SingleToast.show(getApplicationContext(), R.string.interactive_id_empty);
                    } else {
                        if (isChecked) {
                            if (mEVLive != null) {
                                mEVLive.setOnInteractiveLiveListener(mOnInteractiveLiveListener);
                                mEVLive.setRTCSubScreenRect(0.65F, 0.1F, 0.35F, 0.3F);
                                //mEVLive.setEnableMainScreenRemote(false);
                                mEVLive.startInteractiveLive(mChannelID);
                            }
                        } else {
                            mEVLive.endInteractiveLive();
                        }
                    }

                    break;
            }
        }
    };

    private OnInteractiveLiveListener mOnInteractiveLiveListener = new OnInteractiveLiveListener() {
        @Override
        public void onJoinChannelResult(boolean isSuccess) {

        }

        @Override
        public void onLeaveChannelSuccess() {
            onInteractiveEnd();
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
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rtc_close_iv:
                finish();
                break;
        }
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
