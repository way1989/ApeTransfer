package com.ape.transfer;

import android.app.Application;
import android.text.format.Formatter;

import com.ape.transfer.util.Log;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by way on 16/6/10.
 */
public class App extends Application {
    private static final String TAG = "App";
    private static App mContext;

    public static App getApp() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        CrashReport.initCrashReport(this, BuildConfig.BUGLY_APPID, BuildConfig.DEBUG);

        LeakCanary.install(this);
//        SmileProcessor smileProcessor = new SmileProcessor(this);
//        smileProcessor.loadEmoji();
        long maxMemory = Runtime.getRuntime().maxMemory();
        String result = Formatter.formatFileSize(mContext, maxMemory);
        Log.i(TAG, "maxMemory = " + result);
    }
}
