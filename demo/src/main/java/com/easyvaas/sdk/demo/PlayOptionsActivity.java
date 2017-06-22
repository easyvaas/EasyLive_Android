package com.easyvaas.sdk.demo;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.easyvaas.sdk.demo.bean.PlayOption;
import com.easyvaas.sdk.demo.utils.SingleToast;
import com.easyvaas.sdk.player.PlayerConstants;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

public class PlayOptionsActivity extends ActionBarActivity implements View.OnClickListener {
    private static final String TAG = PlayOptionsActivity.class.getSimpleName();
    private static final String TEST_VIDEO_PATH = "http://wsflv.yizhibo.tv/record/liyahtest.flv";

    public static final int REQUEST_CODE = 111;

    private RadioButton liveBtn;
    private RadioButton vodBtn;

    private RadioGroup liveSupportGroup;
    private RadioGroup vodSupportGroup;

    private RadioButton liveRtmpBtn;
    private RadioButton liveHttpFlvBtn;
    private RadioButton liveHttpHlsBtn;

    private RadioButton vodHttpMp4Btn;
    private RadioButton vodHttpTsBtn;
    private RadioButton vodHttpFlvBtn;
    private RadioButton vodHttpHlsBtn;

    private EditText videoPathEt;
    private Button playStartBtn;

    private PlayOption playOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_options);

        liveBtn = (RadioButton) findViewById(R.id.radiobutton_live);
        liveBtn.setChecked(true);
        liveBtn.setOnClickListener(this);
        vodBtn = (RadioButton) findViewById(R.id.radiobutton_playback);
        vodBtn.setOnClickListener(this);

        liveSupportGroup = (RadioGroup) findViewById(R.id.live_play_support_group);
        vodSupportGroup = (RadioGroup) findViewById(R.id.vod_play_support_group);

        liveRtmpBtn = (RadioButton) findViewById(R.id.radiobutton_rtmp);
        liveHttpFlvBtn = (RadioButton) findViewById(R.id.radiobutton_http_flv);
        liveHttpHlsBtn = (RadioButton) findViewById(R.id.radiobutton_http_hls);

        vodHttpMp4Btn = (RadioButton) findViewById(R.id.radiobutton_http_mp4);
        vodHttpTsBtn = (RadioButton) findViewById(R.id.radiobutton_http_ts);
        vodHttpFlvBtn = (RadioButton) findViewById(R.id.radiobutton_http_flv_vod);
        vodHttpHlsBtn = (RadioButton) findViewById(R.id.radiobutton_http_hls_vod);

        videoPathEt = (EditText) findViewById(R.id.video_path_et);
        //videoPathEt.setText(TEST_VIDEO_PATH);

        playStartBtn = (Button) findViewById(R.id.play_start_btn);
        playStartBtn.setOnClickListener(this);

        playOption = new PlayOption();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_scan) {
            Intent intent = new Intent(getApplication(), CaptureActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    videoPathEt.setText(result);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    SingleToast.show(getApplicationContext(), R.string.msg_scan_qrcode_error);
                }
            }
        }
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_start_btn:
                boolean isWide = false;
                boolean isLive = false;
                boolean isFlv = false;
                int playSupport = 0;
                String videoPath="";

                if (liveBtn.isChecked()) {
                    isLive = true;
                } else if (vodBtn.isChecked()) {
                    isLive = false;
                }

                playOption.setIsLive(isLive);
                playOption.setIsWide(isWide);

                if (isLive) {
                    if (liveRtmpBtn.isChecked()) {
                        playSupport = PlayerConstants.LIVE_RTMP_PULLE;
                    } else if (liveHttpFlvBtn.isChecked()) {
                        playSupport = PlayerConstants.LIVE_HTTP_FLVE;
                        isFlv = true;
                    } else if (liveHttpHlsBtn.isChecked()) {
                        playSupport = PlayerConstants.LIVE_HTTP_HLSE;
                    }
                } else {
                    if (vodHttpMp4Btn.isChecked()) {
                        playSupport = PlayerConstants.VOD_HTTP_MP4E;
                    } else if (vodHttpTsBtn.isChecked()) {
                        playSupport = PlayerConstants.VOD_HTTP_TSE;
                    } else if (vodHttpFlvBtn.isChecked()) {
                        playSupport = PlayerConstants.VOD_HTTP_FLVE;
                        isFlv = true;
                    } else if (vodHttpHlsBtn.isChecked()) {
                        playSupport = PlayerConstants.VOD_HTTP_HLSE;
                    }
                }

                playOption.setPlaySupport(playSupport);
                playOption.setIsFlv(isFlv);

                if (!TextUtils.isEmpty(videoPathEt.getText().toString())) {
                    videoPath = videoPathEt.getText().toString();
                } else {
                    SingleToast.show(getApplicationContext(), R.string.msg_lidorfid_is_empty);
                    return;
                }

                playOption.setLid(videoPath);

                Intent intent = new Intent(this, PlayerActivity.class);
                intent.putExtra(PlayerActivity.EXTRA_PLAY_CONFIG_BEAN, playOption);
                startActivity(intent);
                break;
        }
    }
}
