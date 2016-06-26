package com.ape.transfer.util.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;

import com.ape.transfer.R;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by root on 11/5/15.
 */
public class VoiceCaptureActor {

    public static final AtomicInteger LAST_ID = new AtomicInteger(0);

    private static final int STATE_STOPPED = 0;
    private static final int STATE_STARTED = 1;

    private int state = STATE_STOPPED;

    private AmrAudioRecorder mAmrAudioRecorder;
    private long playStartTime;
    Context context;
    VoiceCaptureCallback callback;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            Bundle data = msg.getData();
//            long dur = (long) data.get("duration");
//            double volume = (double) data.get("volume");
//            float db = (float) volume * 1.618f;
            //录音时长在60.05s到60.5s的范围内时停止录音
            long dur = SystemClock.uptimeMillis() - playStartTime;
            if(dur > 60100 && dur < 60500 )
                callback.onRecordStop(dur);
            else
                callback.onRecordProgress(SystemClock.uptimeMillis() - playStartTime);
        }
    };
    private AmrAudioRecorder.VoiceRecordingCallBack mVoiceRecordingCallBack = new AmrAudioRecorder.VoiceRecordingCallBack() {
        @Override
        public void onRecord(long duration, double volume) {
            Message m = Message.obtain();
            Bundle data = new Bundle();
            data.putDouble("volume", volume);
            data.putLong("duration", duration);
            m.setData(data);
            mHandler.sendMessage(m);
        }
    };
    public VoiceCaptureActor(Context context, VoiceCaptureCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void onStartMessage(String fileName) {
        if (state == STATE_STARTED) {
            return;
        }
        mAmrAudioRecorder = new AmrAudioRecorder(MediaRecorder.AudioSource.MIC,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, fileName);
        mAmrAudioRecorder.setVoiceRecordingCallBack(mVoiceRecordingCallBack);
        mAmrAudioRecorder.prepare();
        mAmrAudioRecorder.start();
        if (AmrAudioRecorder.State.ERROR == mAmrAudioRecorder.getState()) {
            //showToast(R.string.record_failed);
            onCrashMessage();
            return;
        }

        state = STATE_STARTED;
        playStartTime = SystemClock.uptimeMillis();
        vibrate(context);
    }


    public void onStopMessage(boolean cancel) {
        if (state != STATE_STARTED) {
            return;
        }
        if (mAmrAudioRecorder == null) {
            return;
        }
        mAmrAudioRecorder.stop();
        mAmrAudioRecorder = null;
        if (!cancel) {
            callback.onRecordStop(SystemClock.uptimeMillis() - playStartTime);
        }
        state = STATE_STOPPED;
    }

    public void onCrashMessage() {
        if (mAmrAudioRecorder == null) {
            return;
        }
        mAmrAudioRecorder.stop();
        mAmrAudioRecorder = null;
        callback.onRecordCrash();

        state = STATE_STOPPED;
    }

    private void vibrate(Context context) {
        try {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(20);
        } catch (Exception e) {
        }
    }



    public interface VoiceCaptureCallback {
        void onRecordProgress(long time);

        void onRecordCrash();

        void onRecordStop(long time);
    }
}
