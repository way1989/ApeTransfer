package com.ape.transfer.activity;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import com.ape.transfer.R;
import com.ape.transfer.model.ApStatusEvent;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.WifiApUtils;

import java.util.ArrayList;

public abstract class ApBaseActivity extends BaseActivity {
    private static final String TAG = "ApBaseActivity";
    private static final int OPEN_WIFI_AP = 0;
    private static final int CLOSE_WIFI_AP = 1;
    private static final long CLOSE_WIFI_AP_DELAY = 1000L;
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
            } else if (WifiApUtils.ACTION_TETHER_STATE_CHANGED.equals(action)) {
                ArrayList<String> available = intent.getStringArrayListExtra(
                        WifiApUtils.EXTRA_AVAILABLE_TETHER);
                ArrayList<String> active = intent.getStringArrayListExtra(
                        WifiApUtils.EXTRA_ACTIVE_TETHER);
                ArrayList<String> errored = intent.getStringArrayListExtra(
                        WifiApUtils.EXTRA_ERRORED_TETHER);

                updateTetherState(available.toArray(), active.toArray(), errored.toArray());
            } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                enableWifiSwitch();
            }

        }
    };
    private boolean isOpeningWifiAp;
    private WifiManager mWifiManager;
    private WifiApUtils mWifiApUtils;
    private boolean isWifiDefaultEnabled;
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

    @Override
    public void onBackPressed() {
        if (isOpeningWifiAp) {
            Toast.makeText(getApplicationContext(), R.string.waiting_creating_ap, Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }

    protected abstract String getSSID();

    protected abstract boolean shouldCloseWifiAp();

    protected abstract void onWifiApStatusChanged(ApStatusEvent status);

    protected void startWifiAp() {
        isOpeningWifiAp = true;
        initData();
        registerReceiver();
        if (isWifiApEnabled()) {
            if (TextUtils.equals(getWifiApConfiguration().SSID, getSSID())) {
                Log.i(TAG, "post status... status == WifiApUtils.WIFI_AP_STATE_ENABLED ");
                postStatus(WifiApUtils.WIFI_AP_STATE_ENABLED);
            } else {
                setWifiApDisabled();
                mHandler.removeMessages(OPEN_WIFI_AP);
                mHandler.sendEmptyMessageDelayed(OPEN_WIFI_AP, 2000L);
            }
            return;
        }
        mHandler.removeMessages(OPEN_WIFI_AP);
        mHandler.sendEmptyMessage(OPEN_WIFI_AP);
    }

    protected void stopWifiAp() {
        mHandler.removeMessages(CLOSE_WIFI_AP);
        //delay close wifi ap to send offline messagecloseWifiAp();
        mHandler.sendEmptyMessageDelayed(CLOSE_WIFI_AP, CLOSE_WIFI_AP_DELAY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (shouldCloseWifiAp()) {
            stopWifiAp();
        }
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            //ignore this exception
        }
    }

    private void initData() {
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiApUtils = WifiApUtils.getInstance(getApplicationContext());
    }

    private void registerReceiver() {
        IntentFilter intentFilterForAp = new IntentFilter();
        intentFilterForAp.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilterForAp.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intentFilterForAp.addAction(WifiApUtils.WIFI_AP_STATE_CHANGED_ACTION);
        intentFilterForAp.addAction(WifiApUtils.ACTION_TETHER_STATE_CHANGED);
        registerReceiver(receiver, intentFilterForAp);
    }


    private void updateTetherState(Object[] available, Object[] tethered, Object[] errored) {
        Log.i(TAG, "updateTetherState available.size = " + available.length + ", tethered.size = "
                + tethered.length + ", errored.size = " + errored.length);
    }

    private void handleWifiApStateChanged(int state) {
        Log.i(TAG, "handleWifiApStateChanged... state = " + state);
        postStatus(state);//发出通知

        switch (state) {
            case WifiApUtils.WIFI_AP_STATE_DISABLING:
                Log.d(TAG, "wifi ap disabling");
                break;
            case WifiApUtils.WIFI_AP_STATE_DISABLED:
                Log.d(TAG, "wifi ap disabled");
                //stopForeground(true);
                break;
            case WifiApUtils.WIFI_AP_STATE_ENABLING:
                Log.d(TAG, "wifi ap enabling");
                break;
            case WifiApUtils.WIFI_AP_STATE_ENABLED:
                Log.d(TAG, "wifi ap enabled");
                isOpeningWifiAp = false;
                stayForeground();
                break;
            case WifiApUtils.WIFI_AP_STATE_FAILED:
                Log.d(TAG, "wifi ap failed");
                isOpeningWifiAp = false;
                //stopForeground(true);
                break;
            default:
                isOpeningWifiAp = false;
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
                .setSmallIcon(R.drawable.x_ic_wifi_tethering)
                .build();
        //notification.flags |= Notification.FLAG_HIDE_NOTIFICATION;//just for MTK platform
        //startForeground(1, notification);
    }

    private void setWifiApEnabled() {
        isWifiDefaultEnabled = mWifiManager.isWifiEnabled();

        String wifiMAC = mWifiApUtils.getWifiMacFromDevice();
        if (TextUtils.isEmpty(wifiMAC)) {
            Log.i(TAG, "post status... wifiMAC == null ");
            postStatus(WifiApUtils.WIFI_AP_STATE_FAILED);
            return;
        }
        mWifiManager.setWifiEnabled(false);
        if (TextUtils.isEmpty(getSSID())) {
            Log.i(TAG, "post status... mWifiApSSID == null ");
            postStatus(WifiApUtils.WIFI_AP_STATE_FAILED);
            return;
        }
        mWifiApUtils.setWifiApEnabled(mWifiApUtils.generateWifiConfiguration(
                WifiApUtils.AuthenticationType.TYPE_NONE, getSSID(), wifiMAC, null), true);

    }

    private void setWifiApDisabled() {
        if (mWifiApUtils != null && mWifiApUtils.isWifiApEnabled()) {
            mWifiApUtils.setWifiApEnabled(null, false);
            mWifiManager.setWifiEnabled(isWifiDefaultEnabled);
        }
    }

    protected boolean isWifiApEnabled() {
        if(mWifiApUtils == null)
            return false;
        return mWifiApUtils.isWifiApEnabled();
    }

    private void postStatus(final int status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onWifiApStatusChanged(new ApStatusEvent(getSSID(), status));
            }
        });
    }

    private WifiConfiguration getWifiApConfiguration() {
        return mWifiApUtils.getWifiApConfiguration();
    }

}
