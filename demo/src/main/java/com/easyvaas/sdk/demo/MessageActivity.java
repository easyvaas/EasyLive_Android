package com.easyvaas.sdk.demo;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.easyvaas.sdk.core.bean.HistoryMsgEntity;
import com.easyvaas.sdk.core.bean.HistoryMsgListEntity;
import com.easyvaas.sdk.core.net.Constants;
import com.easyvaas.sdk.core.net.MyRequestCallBack;
import com.easyvaas.sdk.core.util.Preferences;
import com.easyvaas.sdk.demo.utils.SingleToast;
import com.easyvaas.sdk.message.base.bean.MsgInfoEntity;
import com.easyvaas.sdk.message.wrapper.MessageCallback;
import com.easyvaas.sdk.message.wrapper.EVMessage;
import com.easyvaas.sdk.message.wrapper.MessageConstants;

public class MessageActivity extends ActionBarActivity {
    private Button mSendBtn;
    private Button mConnectBtn;
    private Button mJoinBtn;
    private Button mLeaveBtn;
    private Button mLikeBtn;
    private Button mGetHistoryBtn;

    private EditText mUserIDEt;
    private EditText mTopicEt;
    private LinearLayout mLayout;
    private ScrollView mScroll;
    private EditText mSendCommetEt;

    private String mTopic;

    private EVMessage mEVMessage;

    private String mUserIDMine;

    private int mHistoryStart = 0;
    private int mHistoryCount = -1;

    private final MyHandler mHandler = new MyHandler(this);

    private static final int[] bg = { Color.WHITE, Color.GRAY };

    private static int bgIndex = 0;

    private final static String TOPIC_TEST = "system";

    private final static String TEST_MESSAGE = "hello world";
    private final static boolean TRI_FILTER = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        mSendBtn = (Button)findViewById(R.id.send_msg_btn);
        mSendBtn.setOnClickListener(mOnClickListener);
        mConnectBtn = (Button)findViewById(R.id.msg_connect_btn);
        mConnectBtn.setOnClickListener(mOnClickListener);
        mJoinBtn = (Button)findViewById(R.id.join_topic_btn);
        mJoinBtn.setOnClickListener(mOnClickListener);
        mLeaveBtn = (Button)findViewById(R.id.leave_topic_btn);
        mLeaveBtn.setOnClickListener(mOnClickListener);
        mLikeBtn = (Button)findViewById(R.id.like_btn);
        mLikeBtn.setOnClickListener(mOnClickListener);
        mGetHistoryBtn = (Button)findViewById(R.id.get_history_btn);
        mGetHistoryBtn.setOnClickListener(mOnClickListener);

        mUserIDEt = (EditText)findViewById(R.id.user_data_et);
        mTopicEt = (EditText)findViewById(R.id.topic_et);
        mScroll = (ScrollView)findViewById(R.id.msg_box_sv);
        mSendCommetEt = (EditText)findViewById(R.id.msg_et);

        mLayout = new LinearLayout(this);
        mLayout.setOrientation(LinearLayout.VERTICAL);
        mLayout.setBackgroundColor(0xff00ffff);
        mScroll.addView(mLayout);

        mUserIDMine = Preferences.getInstance(this).getString(Preferences.KEY_SDK_USER_ID, "");
        mUserIDEt.setText(mUserIDMine);

        mEVMessage = new EVMessage(this);
        mEVMessage.setMessageCallback(mMessageCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_message, menu);
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
        if (mEVMessage != null) {
            mEVMessage.close();
        }
        super.onDestroy();
    }

    static class MyHandler extends Handler {
        private SoftReference<MessageActivity> softReference;

        public MyHandler(MessageActivity activity) {
            softReference = new SoftReference<MessageActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    private void setMsg(final LinearLayout layout, final Context context, final int bgColur, final String msg,
            final String user) {
        TextView tv = new TextView(context);

        String userName;
        if (user.equals(mUserIDMine)) {
            userName = "我";
        } else {
            userName = user;
        }
        tv.setText(userName + "  说: ["
                + DateFormat.format("kk:mm:ss", Calendar.getInstance()) + "]\n"
                + msg);
        tv.setBackgroundColor(bgColur);
        layout.addView(tv);

        mHandler.post(mScrollToBottom);
    }

    private void setSystemMsg(final LinearLayout layout, final Context context, final int bgColur,
            final String msg) {
        TextView tv = new TextView(context);

        tv.setText("系统消息: ["
                + DateFormat.format("kk:mm:ss", Calendar.getInstance()) + "]\n"
                + msg);
        tv.setBackgroundColor(bgColur);
        layout.addView(tv);

        mHandler.post(mScrollToBottom);
    }

    private int getCurrColor() {
        return bg[(++bgIndex) % bg.length];
    }

    private MessageCallback mMessageCallback = new MessageCallback() {
        @Override public void onConnected() {
            setSystemMsg(mLayout, MessageActivity.this, getCurrColor(), "连接聊天服务器成功");
        }

        @Override public void onNewMessage(String message, String userdata, String userid,
                                           String topic, String type) {
            String msg = message;
            if (TextUtils.isEmpty(message)) {
                msg = userdata.toString();
            }
            setMsg(mLayout, MessageActivity.this, getCurrColor(), msg, userid);
        }

        @Override
        public void onHistoryMessage(List<MsgInfoEntity> msgs, int next, int count) {
            String msg = "历史消息, 共< " + count + " >条, next index: < " + next + " >\r\n";
            mHistoryStart = next;
            mHistoryCount = count;
            int size = msgs.size();
            for (int i = 0; i < size; i++) {
                MsgInfoEntity historyEntity = msgs.get(i);
                msg += historyEntity.getUserid() + " 说: " + historyEntity.getMessage() +
                        ", 消息类型: " + historyEntity.getType() + "\r\n";
            }
            setSystemMsg(mLayout, MessageActivity.this, getCurrColor(), msg);
        }

        @Override public void onUserJoin(List<String> users) {
            int size = users.size();
            for (int i = 0; i < size; i++) {
                setSystemMsg(mLayout, MessageActivity.this, getCurrColor(), "[" + users.get(i) + "] 来了");
            }
        }

        @Override public void onUserLeave(List<String> users) {
            int size = users.size();
            for (int i = 0; i < size; i++) {
                setSystemMsg(mLayout, MessageActivity.this, getCurrColor(), "[" + users.get(i) + "] 离开了");
            }
        }

        @Override public void onLikeCount(int likeCount) {
            setSystemMsg(mLayout, MessageActivity.this, getCurrColor(), "点赞数: " + likeCount);
        }

        @Override public void onWatchingCount(int watchingCount) {
            setSystemMsg(mLayout, MessageActivity.this, getCurrColor(), "正在观看人数: " + watchingCount);
        }

        @Override public void onWatchedCount(int watchedCount) {
            setSystemMsg(mLayout, MessageActivity.this, getCurrColor(), "已观看人数: " + watchedCount);
        }

        @Override public void onError(int errCode) {
            setSystemMsg(mLayout, MessageActivity.this, getCurrColor(), "聊天交互发生错误,错误码: " + errCode);
        }

        @Override public void onReconnecting() {
            setSystemMsg(mLayout, MessageActivity.this, getCurrColor(), "重新连接消息服务器");
        }

        @Override public void onReconnected() {
            setSystemMsg(mLayout, MessageActivity.this, getCurrColor(), "重连成功");
        }

        @Override public void onReconnectFailed() {
            setSystemMsg(mLayout, MessageActivity.this, getCurrColor(), "重连失败");
        }

        @Override public void onClose() {
            setSystemMsg(mLayout, MessageActivity.this, getCurrColor(), "连接关闭");
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override public void onClick(View v) {
            List<String> topics = new ArrayList<>();
            switch (v.getId()) {
                case R.id.send_msg_btn:
                    //messageHelper.sendAsString("hello world!");
                    final String comment = mSendCommetEt.getText().toString().trim();
                    if (TextUtils.isEmpty(comment)) {
                        SingleToast.show(getApplicationContext(), R.string.msg_comment_not_null);
                        return;
                    }

                    JSONObject userData = new JSONObject();
                    try {
                        userData.put("nickname", "easyvaas");
                        userData.put("id", "13220807");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    mEVMessage.send(mTopic, comment, userData.toString(), Constants.MESSAGE_TYPE_MSG,
                            mSendCallback);
                    break;
                case R.id.msg_connect_btn:
                    mTopic = mTopicEt.getText().toString().trim();
                    mEVMessage.connect(mTopic);
                    break;
                case R.id.leave_topic_btn:
                    mEVMessage.leaveTopic(mTopic);
                    break;
                case R.id.like_btn:
                    mEVMessage.addLikeCount(mTopic, 1);
                    break;
                case R.id.get_history_btn:
                    if (mHistoryStart == 0) {
                        if (mHistoryCount == 0) {
                            setSystemMsg(mLayout, MessageActivity.this, getCurrColor(), "---没有更多了---");
                        } else {
                            //获取最近10条
                            mEVMessage.getLastHistoryMsgs(mTopic, 20, "msg");
                        }
                    } else {
                        //当开始index不为零时,获取从开始index的10条
                        mEVMessage.getHistoryMsgs(mTopic, mHistoryStart, 20, "msg");
                    }
                    break;
            }
        }
    };

    private Runnable mScrollToBottom = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            //Logger.d("", "ScrollY: " + mScroll.getScrollY());
            int off = mLayout.getMeasuredHeight() - mScroll.getHeight();
            if (off > 0) {
                mScroll.scrollTo(0, off);
            }
        }
    };

    private MyRequestCallBack<String> mSendCallback = new MyRequestCallBack<String>() {
        @Override
        public void onSuccess(String url, String result) {
            //setMsg(mLayout, MessageActivity.this, getCurrColor(), comment, mUserIDMine);
        }

        @Override
        public void onError(String url, int errorCode, String errorInfo) {
            super.onError(url, errorCode, errorInfo);
            setSystemMsg(mLayout, MessageActivity.this, getCurrColor(),
                    "消息发送失败, 原因: " + errorInfo);
        }

        @Override
        public void onFailure(String url, String msg) {
            setSystemMsg(mLayout, MessageActivity.this, getCurrColor(),
                    "消息发送失败, 原因: " + msg);
        }
    };
}
