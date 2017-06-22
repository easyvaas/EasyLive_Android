package com.easyvaas.sdk.demo;

import android.app.Application;

import com.easyvaas.sdk.core.EVSdk;
import com.uuzuche.lib_zxing.ZApplication;

/**
 * Created by liya on 16/7/8.
 */
public class EVApplication extends ZApplication {
    private static final String TAG = EVApplication.class.getSimpleName();

    private static EVApplication app;

    @Override public void onCreate() {
        super.onCreate();

        app = this;

        EVSdk.enableDebugLog();
    }

    public static EVApplication getApp() {
        return app;
    }
}
