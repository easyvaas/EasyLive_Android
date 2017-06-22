package com.easyvaas.sdk.demo.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easyvaas.sdk.demo.R;

/**
 * Created by liya on 15/10/16.
 */
public class Utils {
    private static StringBuilder sFormatBuilder = new StringBuilder();
    private static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
    private static final Object[] sTimeArgs = new Object[5];

    public static Dialog getProcessDialog(Activity activity, CharSequence message,
                                          boolean dismissTouchOutside, boolean cancelable) {
        final LayoutInflater inflater = LayoutInflater.from(activity);
        View v = inflater.inflate(R.layout.progress_hud, null);
        Dialog dialog = getCustomDialog(activity, v, dismissTouchOutside, cancelable);
        if (dismissTouchOutside) {
            dialog.setCanceledOnTouchOutside(true);
        } else {
            dialog.setCanceledOnTouchOutside(false);
        }

        ImageView spinner = (ImageView) v.findViewById(R.id.spinnerImageView);
        ((AnimationDrawable) spinner.getBackground()).start();
        TextView messageTv = (TextView) v.findViewById(R.id.message);
        if (TextUtils.isEmpty(message)) {
            messageTv.setVisibility(View.GONE);
        } else {
            messageTv.setText(message);
            messageTv.setVisibility(View.VISIBLE);
        }

        return dialog;
    }

    public static Dialog getCustomDialog(final Activity activity, View view, boolean dismissTouchOutside,
                                         boolean cancelable) {
        Dialog dialog = new Dialog(activity, R.style.Dialog_FullScreen);
        dialog.setContentView(view);
        dialog.setCancelable(cancelable);
        dialog.setCanceledOnTouchOutside(dismissTouchOutside);
        if (!cancelable) {
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.dismiss();
                        activity.finish();
                    }
                    return false;
                }
            });
        }
        return dialog;
    }

    public static Dialog getOneButtonDialog(final Activity activity, String content,
                                            boolean dismissTouchOutside, boolean cancelable,
                                            DialogInterface.OnClickListener confirmOnClickListener) {
        Dialog dialog = new AlertDialog.Builder(activity)
                .setPositiveButton(R.string.confirm, confirmOnClickListener)
                .setCancelable(cancelable)
                .setMessage(content)
                .create();
        dialog.setCanceledOnTouchOutside(dismissTouchOutside);
        if (!cancelable) {
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.dismiss();
                        activity.finish();
                    }
                    return false;
                }
            });
        }
        return dialog;
    }

    public static Dialog getButtonsDialog(Activity activity, int resId,
                                          DialogInterface.OnClickListener confirmOnClickListener) {
        return getButtonsDialog(activity, activity.getString(resId), true, true, confirmOnClickListener,
                null);
    }

    public static Dialog getButtonsDialog(Activity activity, String content,
                                          DialogInterface.OnClickListener confirmOnClickListener) {
        return getButtonsDialog(activity, content, true, true, confirmOnClickListener, null);
    }

    public static Dialog getButtonsDialog(Activity activity, String content, boolean dismissTouchOutside,
                                          boolean cancelable, DialogInterface.OnClickListener confirmOnClickListener,
                                          DialogInterface.OnClickListener cancelOnClickListener) {
        Dialog dialog = new AlertDialog.Builder(activity)
                .setNegativeButton(R.string.cancel, cancelOnClickListener)
                .setPositiveButton(R.string.confirm, confirmOnClickListener)
                .setCancelable(cancelable)
                .setMessage(content)
                .create();
        dialog.setCanceledOnTouchOutside(dismissTouchOutside);
        return dialog;
    }

    public static String getDurationTime(Context context, long startLongTime, long endLongTime) {
        long sec = (endLongTime - startLongTime) / 1000;
        String durationformat = context.getString(
                sec < 3600 ? R.string.duration_format_short : R.string.duration_format_long);

        /* Provide multiple arguments so the format can be changed easily
         * by modifying the xml.
         */
        sFormatBuilder.setLength(0);

        final Object[] timeArgs = sTimeArgs;
        timeArgs[0] = sec / 3600;
        timeArgs[1] = sec / 60;
        timeArgs[2] = (sec / 60) % 60;
        timeArgs[3] = sec;
        timeArgs[4] = sec % 60;

        return sFormatter.format(durationformat, timeArgs).toString();
    }

    public static String formatToDate(long millSeconds) {
        Date date = new Date(millSeconds);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return formatter.format(date);
    }

    public static String encodeValue(String value) {
        if (TextUtils.isEmpty(value)) {
            return value;
        }
        String result = Uri.encode(value);
        return result;
    }

    public static void copyAssetsFiles(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);
            if (fileNames.length > 0) {
                File file = new File(newPath);
                file.mkdirs();
                for (String fileName : fileNames) {
                    copyAssetsFiles(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
