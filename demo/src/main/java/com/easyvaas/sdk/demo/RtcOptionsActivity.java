package com.easyvaas.sdk.demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.easyvaas.sdk.demo.utils.SingleToast;
import com.easyvaas.sdk.player.PlayerConstants;

public class RtcOptionsActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText channelIDEt;
    private Button rtcBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtc_options);

        channelIDEt = (EditText) findViewById(R.id.channel_id_et);
        channelIDEt.setText(Constant.CHANNEL_ID);

        rtcBtn = (Button) findViewById(R.id.rtc_start_btn);
        rtcBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rtc_start_btn:
                String channelID="";

                if (!TextUtils.isEmpty(channelIDEt.getText().toString())) {
                    channelID = channelIDEt.getText().toString();
                } else {
                    SingleToast.show(getApplicationContext(), R.string.msg_channel_is_empty);
                    return;
                }


                Intent intent = new Intent(this, RTCActivity.class);
                intent.putExtra(RTCActivity.EXTRA_RTC_CONFIG_BEAN, channelID);
                startActivity(intent);
                break;
        }
    }
}
