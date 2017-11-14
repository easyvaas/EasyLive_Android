package com.easyvaas.sdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import com.easyvaas.sdk.core.bean.StreamInfoEntity;
import com.easyvaas.sdk.core.net.ApiHelper;
import com.easyvaas.sdk.core.net.MyRequestCallBack;
import com.easyvaas.sdk.demo.bean.LiveOption;
import com.easyvaas.sdk.demo.utils.Logger;
import com.easyvaas.sdk.demo.utils.SingleToast;
import com.easyvaas.sdk.live.wrapper.LiveConstants;

public class LiveOptionsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = LiveOptionsActivity.class.getSimpleName();

    private static final int MAX_VIDEO_BR_DEFAULT = 800;
    private static final int INIT_VIDEO_BR_DEFAULT = 500;
    private static final int AUDIO_BITRATE_DEFAULT = 32;
    private static final int STREAM_VALIDITY_SECOND = 120;

    private RadioButton resolution360button;
    private RadioButton resolution540button;
    private RadioButton resolution720button;

    private RadioButton orientationPortrait;
    private RadioButton orientationLandscape;

    private RadioButton codecAACLCbutton;
    private RadioButton codecAACHEbutton;

    //private EditText maxVideoBitrateEt;
    private EditText initVideoBitrateEt;

    private RadioButton bitrate16Kbutton;
    private RadioButton bitrate32Kbutton;
    private RadioButton bitrate48Kbutton;

    private CheckBox bgmMixCb;
    private CheckBox frontCameraCb;
    private CheckBox beautyOnCb;

    private EditText vidEt;

    private Button liveStartBtn;

    private LiveOption liveOption;

    private String mKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_options);

        orientationPortrait = (RadioButton)findViewById(R.id.radiobutton_portrait);
        orientationPortrait.setChecked(true);
        orientationPortrait.setOnClickListener(this);
        orientationLandscape = (RadioButton)findViewById(R.id.radiobutton_landscape);
        orientationLandscape.setOnClickListener(this);

        resolution360button = (RadioButton)findViewById(R.id.radiobutton_360p);
        resolution360button.setChecked(true);
        resolution540button = (RadioButton)findViewById(R.id.radiobutton_540p);
        resolution720button = (RadioButton)findViewById(R.id.radiobutton_720p);


        //maxVideoBitrateEt = (EditText)findViewById(R.id.max_video_bitrate_et);
        //maxVideoBitrateEt.setText(MAX_VIDEO_BR_DEFAULT+"");
        initVideoBitrateEt = (EditText)findViewById(R.id.init_video_bitrate_et);
        initVideoBitrateEt.setText(INIT_VIDEO_BR_DEFAULT + "");

        bitrate16Kbutton = (RadioButton)findViewById(R.id.audio_bitrate_16k);
        bitrate16Kbutton.setChecked(true);
        bitrate32Kbutton = (RadioButton)findViewById(R.id.audio_bitrate_32k);
        bitrate48Kbutton = (RadioButton)findViewById(R.id.audio_bitrate_48k);

        codecAACLCbutton = (RadioButton)findViewById(R.id.codec_aac_lc);
        codecAACLCbutton.setChecked(true);
        codecAACHEbutton = (RadioButton)findViewById(R.id.codec_aac_he);

        bgmMixCb = (CheckBox)findViewById(R.id.bgm_mixer_cb);
        frontCameraCb = (CheckBox)findViewById(R.id.use_front_camera_cb);
        frontCameraCb.setChecked(true);
        beautyOnCb = (CheckBox)findViewById(R.id.beauty_on_cb);
        //beautyOnCb.setChecked(true);

        vidEt = (EditText)findViewById(R.id.video_vid_et);
        final long start = System.currentTimeMillis();
        ApiHelper.getInstance(getApplicationContext()).getStream(new MyRequestCallBack<StreamInfoEntity>() {
            @Override public void onSuccess(String url, StreamInfoEntity result) {
                Logger.d(TAG, "genstream delay: " + (int) (System.currentTimeMillis() - start));
                vidEt.setText(result.getLid());
            }

            @Override public void onError(String url, int errorCode, String errorInfo) {
                SingleToast.show(getApplicationContext(), R.string.msg_get_lid_fail);
                super.onError(url, errorCode, errorInfo);
                finish();
            }

            @Override public void onFailure(String url, String msg) {
                SingleToast.show(getApplicationContext(), R.string.msg_get_lid_fail);
                finish();
            }
        });


        liveStartBtn = (Button)findViewById(R.id.live_start_btn);
        liveStartBtn.setOnClickListener(this);

        liveOption = new LiveOption();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_live_options, menu);
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

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.live_start_btn:
                int videoResolution = LiveConstants.VIDEO_RESOLUTION_360P;
                int maxVideoBitrate = 800;
                int initVideoBitrate = 500;
                int audioBitrate = LiveConstants.AUDIO_BITRATE_32K;

                if (resolution360button.isChecked()) {
                    videoResolution = LiveConstants.VIDEO_RESOLUTION_360P;
                } else if (resolution540button.isChecked()) {
                    videoResolution = LiveConstants.VIDEO_RESOLUTION_540P;
                } else if (resolution720button.isChecked()) {
                    videoResolution = LiveConstants.VIDEO_RESOLUTION_720P;
                }
                liveOption.setVideoResolution(videoResolution);

                /*if (!TextUtils.isEmpty(maxVideoBitrateEt.getText().toString())) {
                    maxVideoBitrate = Integer.parseInt(maxVideoBitrateEt.getText().toString());
                }
                liveOption.setMaxVideoBitrate(maxVideoBitrate);*/
                if (!TextUtils.isEmpty(initVideoBitrateEt.getText().toString())) {
                    initVideoBitrate = Integer.parseInt(initVideoBitrateEt.getText().toString());
                }
                liveOption.setInitVideoBitrate(initVideoBitrate);


                if (bitrate16Kbutton.isChecked()) {
                    audioBitrate = LiveConstants.AUDIO_BITRATE_16K;
                } else if (bitrate32Kbutton.isChecked()) {
                    audioBitrate = LiveConstants.AUDIO_BITRATE_32K;
                } else if (bitrate48Kbutton.isChecked()) {
                    audioBitrate = LiveConstants.AUDIO_BITRATE_48K;
                }

                liveOption.setAudioBitrate(audioBitrate);

                if (codecAACLCbutton.isChecked()) {
                    liveOption.setAudioCodec(LiveConstants.AUDIO_CODEC_AAC_LC);
                } else if (codecAACHEbutton.isChecked()) {
                    liveOption.setAudioCodec(LiveConstants.AUDIO_CODEC_AAC_HE);
                }

                liveOption.setBgmMix(bgmMixCb.isChecked());
                liveOption.setUseFrontCamera(frontCameraCb.isChecked());
                liveOption.setIsBeautyOn(beautyOnCb.isChecked());
                liveOption.setPortrait(orientationPortrait.isChecked());

                String lid = vidEt.getText().toString();
                if (TextUtils.isEmpty(lid)) {
                    SingleToast.show(getApplicationContext(), R.string.msg_vid_is_empty);
                    return;
                } else {
                    liveOption.setLid(lid);
                    liveOption.setKey(mKey);
                }

                Intent intent = new Intent(this, RecorderActivity.class);
                intent.putExtra(RecorderActivity.EXTRA_LIVE_CONFIG_BEAN, liveOption);
                startActivity(intent);
                finish();
                break;
            case R.id.radiobutton_portrait:
                resolution360button.setText(R.string.resolution_360p);
                resolution540button.setText(R.string.resolution_540p);
                resolution720button.setText(R.string.resolution_720p);
                break;
            case R.id.radiobutton_landscape:
                resolution360button.setText(R.string.resolution_360p_land);
                resolution540button.setText(R.string.resolution_540p_land);
                resolution720button.setText(R.string.resolution_720p_land);
                break;
        }
    }
}
