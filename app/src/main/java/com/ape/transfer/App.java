package com.ape.transfer;

import android.app.Application;
import android.content.Context;
import android.text.format.Formatter;

import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.ape.transfer.util.Log;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by way on 16/6/10.
 */
public class App extends Application {
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        if (BuildConfig.BUGLY_ENABLED) {
            CrashReport.initCrashReport(mContext, String.valueOf(BuildConfig.BUGLY_APPID), false);
            FeedbackAPI.initAnnoy(this, String.valueOf(BuildConfig.FEEDBACK_APPKEY));
        }
        if(BuildConfig.DEBUG)
            LeakCanary.install(this);
//        SmileProcessor smileProcessor = new SmileProcessor(this);
//        smileProcessor.loadEmoji();
        long maxMemory = Runtime.getRuntime().maxMemory();
        String result = Formatter.formatFileSize(mContext, maxMemory);
        Log.i("broncho", "maxMemory = " + result);
    }
}
