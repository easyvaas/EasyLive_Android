package com.easyvaas.sdk.demo.utils;

import android.content.Context;
import android.widget.Toast;

public class SingleToast {

    public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
    public static final int LENGTH_LONG = Toast.LENGTH_LONG;

    private static Toast mToast;

    public static void show(Context context, int resId) {
        if (context == null) {
            return;
        }
        realShow(context, context.getString(resId), Toast.LENGTH_SHORT);
    }

    public static void show(Context context, String text) {
        realShow(context, text, Toast.LENGTH_SHORT);
    }

    public static void show(Context context, int resId, int duration) {
        if (context == null) {
            return;
        }
        realShow(context, context.getString(resId), duration);
    }

    public static void show(Context context, String text, int duration) {
        realShow(context, text, duration);
    }

    private static void realShow(Context context, String text, int duration) {
        if (context == null) {
            return;
        }
        if (mToast == null) {
            mToast = Toast.makeText(context.getApplicationContext(), text, duration);
        } else {
            // mToast.cancel();
            mToast.setDuration(duration);
            mToast.setText(text);
        }
        mToast.show();
    }
}
