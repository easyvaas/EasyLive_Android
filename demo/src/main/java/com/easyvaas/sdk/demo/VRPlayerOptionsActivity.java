package com.easyvaas.sdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.easyvaas.sdk.demo.bean.PlayOption;
import com.easyvaas.sdk.demo.utils.SingleToast;

public class VRPlayerOptionsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = VRPlayerOptionsActivity.class.getSimpleName();

    private RadioButton liveBtn;
    private RadioButton vodBtn;

    private EditText videoPathEt;
    private Button playStartBtn;

    private PlayOption playOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vrplayer_options);

        liveBtn = (RadioButton) findViewById(R.id.radiobutton_live);
        //liveBtn.setChecked(true);
        liveBtn.setOnClickListener(this);
        vodBtn = (RadioButton) findViewById(R.id.radiobutton_playback);
        vodBtn.setOnClickListener(this);

        videoPathEt = (EditText) findViewById(R.id.video_path_et);

        playStartBtn = (Button) findViewById(R.id.play_start_btn);
        playStartBtn.setOnClickListener(this);

        playOption = new PlayOption();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_start_btn:
                boolean isWide = true;
                boolean isLive = false;
                String videoPath = "";

                if (liveBtn.isChecked()) {
                    isLive = true;
                } else if (vodBtn.isChecked()) {
                    isLive = false;
                }

                playOption.setIsLive(isLive);
                playOption.setIsWide(isWide);

                if (!TextUtils.isEmpty(videoPathEt.getText().toString())) {
                    videoPath = videoPathEt.getText().toString();
                } else {
                    /*SingleToast.show(getApplicationContext(), R.string.msg_lidorfid_is_empty);return;*/
                    videoPath = "http://media.qicdn.detu.com/@/70955075-5571-986D-9DC4-450F13866573/2016-05-19/573d15dfa19f3-2048x1024.m3u8";
                }

                playOption.setVideoPath(videoPath);

                Intent intent = new Intent(this, VRPlayerActivity.class);
                intent.putExtra(VRPlayerActivity.EXTRA_PLAY_CONFIG_BEAN, playOption);
                startActivity(intent);
                break;
        }
    }
}
