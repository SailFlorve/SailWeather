package com.sailflorve.sailweather;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePalApplication;

/**
 * 此Class方便全局获取Context
 */

public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        context = getApplicationContext();
        LitePalApplication.initialize(context);
    }

    public static Context getContext() {
        return context;
    }
}
