package com.thoughtworks.wechatmoment.base;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

public class BaseApplication extends Application {

    private static Context mContext;
    private static Handler mHandler;
    private static long mMainThreadId;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mHandler = new Handler();
        mMainThreadId = android.os.Process.myTid();
    }

    public static Context getContext() {
        return mContext;
    }

    public static Handler getHandler() {
        return mHandler;
    }

    public static long getMainThreadId() {
        return mMainThreadId;
    }
}
