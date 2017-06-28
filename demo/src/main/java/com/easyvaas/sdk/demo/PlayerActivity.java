package com.easyvaas.sdk.demo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.PowerManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import tv.danmaku.ijk.media.player.IMediaPlayer;

import com.easyvaas.sdk.demo.bean.PlayOption;
import com.easyvaas.sdk.demo.utils.Logger;
import com.easyvaas.sdk.demo.utils.SingleToast;
import com.easyvaas.sdk.demo.utils.Utils;
import com.easyvaas.sdk.live.base.interactive.OnInteractiveLiveListener;
import com.easyvaas.sdk.live.wrapper.EVLive;
import com.easyvaas.sdk.player.EVPlayer;
import com.easyvaas.sdk.player.PlayerConstants;
import com.easyvaas.sdk.player.base.EVPlayerBase;
import com.easyvaas.sdk.player.base.EVPlayerParameter;
import com.easyvaas.sdk.player.base.EVVideoView;

public class PlayerActivity extends Activity implements View.OnClickListener {
    private final static String TAG = PlayerActivity.class.getSimpleName();
    public static final String EXTRA_PLAY_CONFIG_BEAN = "extra_play_config_bean";

    private EVVideoView mVideoView;
    private EVPlayer mEVPlayer;
    private EVLive mEVLive;

    protected TextView mVideoTitleTv;
    protected View mTopInfoAreaView;

    private String mUrl;

    private PlayOption mPlayOption;

    private PowerManager.WakeLock mWakeLock;

    private Dialog mLoadingDialog;

    private MediaController mMediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mPlayOption = (PlayOption) getIntent().getSerializableExtra(EXTRA_PLAY_CONFIG_BEAN);

        initUIComponents();
        initListeners();

        if (mPlayOption != null) {
            mEVPlayer = new EVPlayer(this);

            if (mPlayOption.isLive()) {
                mEVLive = new EVLive(this);
            }

            EVPlayerParameter.Builder builder = new EVPlayerParameter.Builder();
            builder.setLive(mPlayOption.isLive());
            mEVPlayer.setParameter(builder.build());
            mEVPlayer.setVideoView(mVideoView);
            mEVPlayer.setEnableAutoReconnect(true);
            mEVPlayer.setOnPreparedListener(mOnPreparedListener);
            mEVPlayer.setOnCompletionListener(mOnCompletionListener);
            mEVPlayer.setOnInfoListener(mOnInfoListener);
            mEVPlayer.setOnErrorListener(mOnErrorListener);
            mEVPlayer.onCreate();

            startPlay();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (null != mEVPlayer) {
            mEVPlayer.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != mEVPlayer) {
            mEVPlayer.onResume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mEVPlayer) {
            mEVPlayer.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mEVPlayer) {
            mEVPlayer.onDestroy();
        }

        if (mEVLive != null) {
            mEVLive.onDestroy();
        }

        dismissLoadingDialog();
        releaseWakeLock();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startPlay() {
        mEVPlayer.watchStart(mPlayOption.getLid(), mPlayOption.isLive());

        if (!isFinishing()) {
            showLoadingDialog(R.string.loading_data, true, true);
        }
    }

    private void initUIComponents() {
        mVideoView = (EVVideoView) findViewById(R.id.player_surface_view);
        mTopInfoAreaView = this.findViewById(R.id.play_info_rl);
        mVideoTitleTv = (TextView) findViewById(R.id.player_title_tv);

        mMediaController = new MediaController(this);
        mMediaController.setMediaPlayer(mediaControl);
        mMediaController.setAnchorView(mVideoView);
    }

    private void initListeners() {
        findViewById(R.id.live_close_iv).setOnClickListener(this);

        CheckBox interactiveCb= (CheckBox) findViewById(R.id.interactive_live_cb);
        if (mPlayOption.isLive()) {
            interactiveCb.setOnCheckedChangeListener(mOnCheckedChangeListener);
            findViewById(R.id.player_bottom_progress_btn).setVisibility(View.GONE);
        } else {
            interactiveCb.setVisibility(View.GONE);
            findViewById(R.id.player_bottom_progress_btn).setOnClickListener(this);
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
                                mEVLive.initInteractiveLiveConfig(Constant.INTERACTIVE_LIVE_APP_ID,
                                        false);
                                mEVLive.setOnInteractiveLiveListener(mOnInteractiveLiveListener);
                                mEVLive.startInteractiveLive(Constant.CHANNEL_ID);
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
        mEVPlayer.onDestroy();
        mVideoView.setVisibility(View.INVISIBLE);
    }

    private void onInteractiveEnd() {
        mVideoView.setVisibility(View.VISIBLE);
        mEVPlayer.onCreate();

        startPlay();

        if (!isFinishing()) {
            showLoadingDialog(R.string.loading_data, true, true);
        }
    }

    private EVPlayerBase.OnPreparedListener mOnPreparedListener = new EVPlayerBase.OnPreparedListener() {
        @Override public boolean onPrepared() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    acquireWakeLock();
                    dismissLoadingDialog();
                }
            });
            return true;
        }
    };

    private EVPlayerBase.OnCompletionListener mOnCompletionListener = new EVPlayerBase.OnCompletionListener() {
        @Override public boolean onCompletion() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.getOneButtonDialog(PlayerActivity.this,
                            getString(R.string.msg_play_complete), false, false,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                    releaseWakeLock();
                                }
                            }).show();
                    if (null != mEVPlayer) {
                        mEVPlayer.watchStop();
                    }
                }
            });
            return true;
        }
    };

    private EVPlayerBase.OnInfoListener mOnInfoListener = new EVPlayerBase.OnInfoListener() {
        @Override public boolean onInfo(int what, final int extra) {
            switch (what) {
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isFinishing()) {
                                showLoadingDialog(R.string.loading_data, true, true);
                            }
                        }
                    });
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissLoadingDialog();
                        }
                    });
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    Logger.w(TAG, "open first video time(ms): " + extra);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mVideoTitleTv.setVisibility(View.VISIBLE);
                            mVideoTitleTv.setText("首屏打开时间: " + extra + " ms");
                        }
                    });
                    break;
            }
            return true;
        }
    };

    private EVPlayerBase.OnErrorListener mOnErrorListener = new EVPlayerBase.OnErrorListener() {
        @Override public boolean onError(int what, int extra) {
            switch (what) {
                case PlayerConstants.EV_PLAYER_ERROR_SDK_INIT:
                    showToastOnUiThread(R.string.msg_sdk_init_error);
                    break;
                case PlayerConstants.EV_PLAYER_ERROR_GET_RESOURCE:
                    showToastOnUiThread(R.string.msg_live_show_error);
                    break;
                case PlayerConstants.EV_PLAYER_ERROR_GET_URL:
                    showToastOnUiThread(R.string.msg_live_redirect_error);
                    break;
                case PlayerConstants.EV_PLAYER_ERROR_LIVE_EXCEPTION:
                    showToastOnUiThread(R.string.msg_play_live_error);
                    break;
                case PlayerConstants.EV_PLAYER_ERROR_NONE_STREAM:
                    showToastOnUiThread(R.string.msg_play_none_stream_error);
                    break;
                case PlayerConstants.EV_PLAYER_ERROR_NOT_ACCEPTABLE:
                    showToastOnUiThread(R.string.msg_play_not_support_error);
                    break;
                case PlayerConstants.EV_PLAYER_ERROR_PARAMETER:
                    showToastOnUiThread(R.string.msg_play_parameter_error);
                    break;
                case PlayerConstants.EV_PLAYER_ERROR_RECORD_EXCEPTION:
                    showToastOnUiThread(R.string.msg_play_record_error);
                    break;
                case PlayerConstants.EV_PLAYER_ERROR_FILE_EXCEPTION:
                    showToastOnUiThread(R.string.msg_play_file_error);
                    break;
                default:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.getOneButtonDialog(PlayerActivity.this,
                                    getString(R.string.msg_play_error), false, false,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            dismissLoadingDialog();
                                            releaseWakeLock();
                                            if (null != mEVPlayer) {
                                                mEVPlayer.onStop();
                                            }
                                            finish();
                                        }
                                    }).show();
                        }
                    });
                    break;
            }

            return true;
        }
    };

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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.live_close_iv:
                if (null != mEVPlayer) {
                    mEVPlayer.watchStop();
                }
                finish();
                break;
            case R.id.player_bottom_progress_btn:
                toggleProgressBar(true);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.player_bottom_action_bar).getVisibility() != View.VISIBLE) {
            toggleProgressBar(false);
        } else {
            super.onBackPressed();
        }
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

    private MediaController.MediaPlayerControl mediaControl = new MediaController.MediaPlayerControl() {
        @Override
        public void start() {
            if (mVideoView != null) {
                mVideoView.start();
            }
        }

        @Override
        public void pause() {
            if (mVideoView != null) {
                mVideoView.pause();
            }
        }

        @Override
        public int getDuration() {
            return mVideoView != null ? (int)mVideoView.getDuration() : 0;
        }

        @Override
        public int getCurrentPosition() {
            return mVideoView != null ? (int)mVideoView.getCurrentPosition() : 0;
        }

        @Override
        public void seekTo(long pos) {
            if (mVideoView != null) {
                mVideoView.seekTo(pos);
            }
        }

        @Override
        public boolean isPlaying() {
            return mVideoView != null && mVideoView.isPlaying();
        }

        @Override
        public int getBufferPercentage() {
            return mVideoView != null ? mVideoView.getBufferPercentage() : 0;
        }

        @Override
        public boolean canPause() {
            return mVideoView != null && mVideoView.canPause();
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
