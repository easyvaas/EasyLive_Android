package com.easyvaas.sdk.demo.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Preferences {
    private static final String PREF_NAME = "yzbsdk.local.dbfile";

    public static final String KEY_SESSION_ID = "sessionid";
    public static final String KEY_USER_NUMBER = "name";
    public static final String KEY_USER_NICKNAME = "nickname";
    public static final String KEY_IS_LOGOUT = "is_logout";
    public static final String KEY_VIDEO_LIST = "videolist";
    public static final String KEY_APP_KEY ="appkey";

    private static Preferences mPreferences;

    private SharedPreferences mSettings;

    private Preferences(Context context) {
        mSettings = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static Preferences getInstance(Context context) {
        if (mPreferences == null) {
            mPreferences = new Preferences(context.getApplicationContext());
        }
        return mPreferences;
    }

    public boolean isContains(String key) {
        return mSettings.contains(key);
    }

    public int getInt(String name, int def) {
        return mSettings.getInt(name, def);
    }

    public void putInt(String name, int value) {
        SharedPreferences.Editor edit = mSettings.edit();
        edit.putInt(name, value);
        edit.apply();
    }

    public String getString(String key) {
        return mSettings.getString(key, "");
    }

    public String getString(String key, String defaultValue) {
        return mSettings.getString(key, defaultValue);
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor edit = mSettings.edit();
        edit.putString(key, value);
        edit.apply();
    }

    public long getLong(String key, long defaultValue) {
        return mSettings.getLong(key, defaultValue);
    }

    public void putLong(String key, long value) {
        SharedPreferences.Editor edit = mSettings.edit();
        edit.putLong(key, value);
        edit.apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return mSettings.getBoolean(key, defaultValue);
    }

    public void putBoolean(String key, boolean bool) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(key, bool);
        editor.apply();
    }

    public void putList(String key, List<Map<String, String>> values) {
        SharedPreferences.Editor editor = mSettings.edit();

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < values.size(); ++i) {
            Map<String, String> itemMap = values.get(i);

            Iterator<Map.Entry<String, String>> iterator = itemMap.entrySet().iterator();

            JSONObject object = new JSONObject();

            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                try {
                    object.put(entry.getKey(), entry.getValue());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            jsonArray.put(object);
        }

        editor.putString(key, jsonArray.toString());
        editor.apply();
    }

    public List<Map<String, String>> getList(String key) {
        List<Map<String, String>> values = new ArrayList<>();

        String result = mSettings.getString(key, "");
        if (TextUtils.isEmpty(result)) {
            return values;
        }

        try {
            JSONArray array = new JSONArray(result);

            for (int i = 0; i < array.length(); ++i) {
                JSONObject itemObj = array.getJSONObject(i);
                Map<String, String> itemMap = new HashMap<>();

                JSONArray names = itemObj.names();
                if (null != names) {
                    for (int j = 0; j < names.length(); ++j) {
                        String name = names.getString(j);
                        String value = itemObj.getString(name);
                        itemMap.put(name, value);
                    }
                }
                values.add(itemMap);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return values;
    }

    public void remove(String key) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.remove(key);
        editor.apply();
    }

    public void clear() {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.clear();
        editor.apply();
    }
}

