package com.easyvaas.sdk.demo;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.easyvaas.sdk.core.EVSdk;
import com.easyvaas.sdk.demo.utils.SingleToast;

public class MainActivity extends ActionBarActivity {
    private final static String TAG = MainActivity.class.getSimpleName();

    private final static String APP_ID = "yizhibo";
    private final static String ACCESS_KEY = "yizhibo";
    private final static String SECRET_KEY = "helloworld";
    private final static String USER_ID = "13220807";

    private EditText appIdEt;
    private EditText accessKeyEt;
    private EditText secretKeyEt;
    private EditText userIdEt;

    private Button initSdkBtn;
    private Button livePushBtn;
    private Button messageBtn;
    private Button liveShowBtn;
    private Button rtcBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appIdEt = (EditText)findViewById(R.id.app_id_et);
        appIdEt.setText(APP_ID);
        accessKeyEt = (EditText)findViewById(R.id.access_key_et);
        accessKeyEt.setText(ACCESS_KEY);
        secretKeyEt = (EditText)findViewById(R.id.secret_key_et);
        secretKeyEt.setText(SECRET_KEY);
        userIdEt = (EditText)findViewById(R.id.user_id_et);
        userIdEt.setText(USER_ID);

        initSdkBtn = (Button)findViewById(R.id.init_sdk_btn);
        initSdkBtn.setOnClickListener(mOnClickListener);
        livePushBtn = (Button)findViewById(R.id.live_push_btn);
        livePushBtn.setOnClickListener(mOnClickListener);
        messageBtn = (Button)findViewById(R.id.message_btn);
        messageBtn.setOnClickListener(mOnClickListener);
        liveShowBtn = (Button)findViewById(R.id.live_show_btn);
        liveShowBtn.setOnClickListener(mOnClickListener);
        rtcBtn = (Button)findViewById(R.id.rtc_btn);
        rtcBtn.setOnClickListener(mOnClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override protected void onDestroy() {
        super.onDestroy();
        EVSdk.destroy();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override public void onClick(View v) {
            switch (v.getId()) {
                case R.id.live_push_btn:
                    startActivity(new Intent(getApplicationContext(), LiveOptionsActivity.class));
                    break;
                case R.id.message_btn:
                    startActivity(new Intent(getApplicationContext(), MessageActivity.class));
                    break;
                case R.id.live_show_btn:
                    startActivity(new Intent(getApplicationContext(), PlayOptionsActivity.class));
                    break;
                case R.id.init_sdk_btn:
                    initSdk();
                    break;
                case R.id.rtc_btn:
                    if (TextUtils.isEmpty(Constant.INTERACTIVE_LIVE_APP_ID)) {
                        SingleToast.show(getApplicationContext(), R.string.interactive_id_empty);
                    } else {
                        startActivity(new Intent(getApplicationContext(), RtcOptionsActivity.class));
                    }
                    break;
            }
        }
    };

    private void initSdk() {
        initSdkBtn.setText(R.string.msg_sdk_initing);
        initSdkBtn.setEnabled(false);
        livePushBtn.setEnabled(false);
        messageBtn.setEnabled(false);
        liveShowBtn.setEnabled(false);
        rtcBtn.setEnabled(false);

        String appID = appIdEt.getText().toString().trim();
        if (TextUtils.isEmpty(appID)) {
            SingleToast.show(getApplicationContext(), R.string.msg_app_id_not_null);
            return;
        }

        String accessKey = accessKeyEt.getText().toString().trim();
        if (TextUtils.isEmpty(accessKey)) {
            SingleToast.show(getApplicationContext(), R.string.msg_access_key_not_null);
            return;
        }

        String secretKey = secretKeyEt.getText().toString().trim();
        if (TextUtils.isEmpty(secretKey)) {
            SingleToast.show(getApplicationContext(), R.string.msg_secret_key_not_null);
            return;
        }

        String userID = userIdEt.getText().toString().trim();
        if (TextUtils.isEmpty(userID)) {
            SingleToast.show(getApplicationContext(), R.string.msg_user_id_not_null);
            return;
        }

        EVSdk.setInitListener(new EVSdk.OnSDKInitListener() {
            @Override public void onSuccess() {
                SingleToast.show(getApplicationContext(), R.string.msg_init_sdk_success);
                initSdkBtn.setText(R.string.msg_sdk_init_success);
                livePushBtn.setEnabled(true);
                messageBtn.setEnabled(true);
                liveShowBtn.setEnabled(true);
                rtcBtn.setEnabled(true);
            }

            @Override public void onFailed(int code, String msg) {
                SingleToast.show(getApplicationContext(), getString(R.string.msg_init_sdk_fail, msg));
                initSdkBtn.setText(R.string.msg_sdk_reinit);
                initSdkBtn.setEnabled(true);
            }
        });

        EVSdk.init(getApplicationContext(), appID, accessKey, secretKey, userID);
    }
}