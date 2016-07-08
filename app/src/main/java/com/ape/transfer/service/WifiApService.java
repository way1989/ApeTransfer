package com.ape.transfer.service;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;

import com.ape.transfer.R;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.WifiApUtils;

public class WifiApService extends Service {
    private static final String TAG = "WifiApService";
    private static final int OPEN_WIFI_AP = 0;
    private static final int CLOSE_WIFI_AP = 1;

    private IBinder mBinder = new WifiApBinder();
    private WifiManager mWifiManager;
    private WifiApUtils mWifiApUtils;
    // 服务端MAC（本机）
    private String mWifiApSSID;
    private boolean isWifiDefaultEnabled;
    private OnWifiApStatusListener mStatusListener;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiApUtils.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(
                        WifiApUtils.EXTRA_WIFI_AP_STATE, WifiApUtils.WIFI_AP_STATE_FAILED);
                handleWifiApStateChanged(state);
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                handleWifiStateChanged(state);
            } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                enableWifiSwitch();
            }

        }
    };
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case OPEN_WIFI_AP:
                    setWifiApEnabled();
                    break;
                case CLOSE_WIFI_AP:
                    setWifiApDisabled();
                    break;
            }
        }
    };

    public WifiApService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
        registerReceiver();
    }

    private void initData() {
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiApUtils = WifiApUtils.getInstance(mWifiManager);
    }

    private void registerReceiver() {
        IntentFilter intentFilterForAp = new IntentFilter();
        intentFilterForAp.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilterForAp.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intentFilterForAp.addAction(WifiApUtils.WIFI_AP_STATE_CHANGED_ACTION);
        registerReceiver(receiver, intentFilterForAp);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        mHandler.removeMessages(CLOSE_WIFI_AP);
        mHandler.sendEmptyMessageDelayed(CLOSE_WIFI_AP, 1500L);
        unregisterReceiver(receiver);
    }

    private void handleWifiApStateChanged(int state) {
        if (mStatusListener != null) {
            mStatusListener.onWifiApStatusChanged(state);
        } else {
            stopSelf();
        }
        switch (state) {
            case WifiApUtils.WIFI_AP_STATE_DISABLING:
                Log.d(TAG, "wifi ap disabling");
                break;
            case WifiApUtils.WIFI_AP_STATE_DISABLED:
                Log.d(TAG, "wifi ap disabled");
                stopForeground(true);
                break;
            case WifiApUtils.WIFI_AP_STATE_ENABLING:
                Log.d(TAG, "wifi ap enabling");
                break;
            case WifiApUtils.WIFI_AP_STATE_ENABLED:
                Log.d(TAG, "wifi ap enabled");
                stayForeground();
                break;
            case WifiApUtils.WIFI_AP_STATE_FAILED:
                Log.d(TAG, "wifi ap failed");
                stopForeground(true);
                break;
            default:
                Log.e(TAG, "wifi ap state = " + state);
                break;
        }
    }

    private void handleWifiStateChanged(int state) {
        switch (state) {
            case WifiManager.WIFI_STATE_DISABLING:
                Log.d(TAG, "wifi disabling");
                break;

            case WifiManager.WIFI_STATE_DISABLED:
                Log.d(TAG, "wifi disabled");
                break;

            case WifiManager.WIFI_STATE_ENABLING:
                Log.d(TAG, "wifi enabling");
                break;

            case WifiManager.WIFI_STATE_ENABLED:
                Log.d(TAG, "wifi enabled");

                break;

            case WifiManager.WIFI_STATE_UNKNOWN:
                Log.d(TAG, "wifi unknown");
                break;

            default:
                Log.e(TAG, "wifi state = " + state);
                break;
        }
    }

    private void enableWifiSwitch() {
        boolean isAirplaneMode = Settings.Global.getInt(getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        if (!isAirplaneMode) {

        } else {
        }

    }

    private void stayForeground() {
        Notification notification = new Notification.Builder(this)
                .setContentTitle(getString(R.string.notify_title))
                .setContentText(getString(R.string.notify_close_wlan))
                .setSmallIcon(R.drawable.notify_icon_white)
                .build();
        //notification.flags |= Notification.FLAG_HIDE_NOTIFICATION;//just for MTK platform
        startForeground(1, notification);
    }

    private void setWifiApEnabled() {
        isWifiDefaultEnabled = mWifiManager.isWifiEnabled();

        String wifiMAC = mWifiApUtils.getWifiMacFromDevice();
        if (TextUtils.isEmpty(wifiMAC)) {
            if (mStatusListener != null)
                mStatusListener.onWifiApStatusChanged(WifiApUtils.WIFI_AP_STATE_FAILED);
            return;
        }
        mWifiManager.setWifiEnabled(false);
        if (TextUtils.isEmpty(mWifiApSSID)) {
            if (mStatusListener != null)
                mStatusListener.onWifiApStatusChanged(WifiApUtils.WIFI_AP_STATE_FAILED);
            return;
        }
        mWifiApUtils.setWifiApEnabled(mWifiApUtils.generateWifiConfiguration(
                WifiApUtils.AuthenticationType.TYPE_NONE, mWifiApSSID, wifiMAC, null), true);

    }

    private void setWifiApDisabled() {
        if (mWifiApUtils != null && mWifiApUtils.isWifiApEnabled()) {
            mWifiApUtils.setWifiApEnabled(null, false);
            mWifiManager.setWifiEnabled(isWifiDefaultEnabled);
        }
    }

    public interface OnWifiApStatusListener {
        void onWifiApStatusChanged(int status);
    }

    public class WifiApBinder extends Binder {

        public void setOnWifiApStatusListener(OnWifiApStatusListener listener) {
            mStatusListener = listener;
        }

        public boolean isWifiApEnabled() {
            return mWifiApUtils.isWifiApEnabled();
        }

        public void setWifiApSSID(String ssid) {
            mWifiApSSID = ssid;
        }

        public void openWifiAp() {
            mHandler.removeMessages(OPEN_WIFI_AP);
            mHandler.sendEmptyMessage(OPEN_WIFI_AP);
        }

        public void closeWifiAp() {
            mHandler.removeMessages(CLOSE_WIFI_AP);
            mHandler.sendEmptyMessageDelayed(CLOSE_WIFI_AP, 1500L);//delay close wifi ap to send offline message
        }

        public WifiConfiguration getWifiApConfiguration() {
            return mWifiApUtils.getWifiApConfiguration();
        }

        /**
         * reset.
         */
        public void reset() {

        }

    }
}
