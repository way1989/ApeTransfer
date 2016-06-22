package com.ape.transfer;

import android.app.Application;
import android.content.Context;
import android.text.format.Formatter;

import com.ape.emoji.SmileProcessor;
import com.ape.transfer.util.Log;
import com.tencent.bugly.crashreport.CrashReport;

import pl.tajchert.nammu.Nammu;

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
        if (BuildConfig.BUGLY_ENABLED)
            CrashReport.initCrashReport(mContext, getString(R.string.bugly_appid), false);

        Nammu.init(this);
        SmileProcessor smileProcessor = new SmileProcessor(this);
        smileProcessor.loadEmoji();
        long maxMemory = Runtime.getRuntime().maxMemory();
        String result = Formatter.formatFileSize(mContext, maxMemory);
        Log.i("broncho", "maxMemory = " + result);
    }
}
