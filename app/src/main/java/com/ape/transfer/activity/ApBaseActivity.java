package com.ape.transfer.activity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;

import com.ape.transfer.R;
import com.ape.transfer.service.WifiApService;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.WifiApUtils;

public abstract class ApBaseActivity extends BaseActivity implements WifiApService.OnWifiApStatusListener {
    private static final String TAG = "ApBaseActivity";
    protected WifiApService.WifiApBinder mWifiApService;
    private boolean isOpeningWifiAp;
    private ServiceConnection mServiceCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mWifiApService = (WifiApService.WifiApBinder) service;
            afterServiceConnected();
            Log.i(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mWifiApService = null;
            Log.i(TAG, "onServiceDisconnected");
        }
    };

    protected void afterServiceConnected() {
        if (mWifiApService != null) {
            mWifiApService.setOnWifiApStatusListener(this);
            openWifiAp();
        } else {
            Log.i(TAG, "afterServiceConnected service == null");
            finish();
        }
    }

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

    @Override
    public void onWifiApStatusChanged(int status) {
        isOpeningWifiAp = false;
    }

    private void openWifiAp() {
        if (mWifiApService == null)
            return;

        if (mWifiApService.isWifiApEnabled() &&
                TextUtils.equals(mWifiApService.getWifiApConfiguration().SSID, getSSID())) {
            onWifiApStatusChanged(WifiApUtils.WIFI_AP_STATE_ENABLED);
            return;
        }
        mWifiApService.setWifiApSSID(getSSID());
        mWifiApService.openWifiAp();
        isOpeningWifiAp = true;
    }

    private void closeWifiAp() {
        if (mWifiApService != null) {
            mWifiApService.setOnWifiApStatusListener(null);
            mWifiApService.reset();
            mWifiApService.closeWifiAp();
        }
        unBindService();
        stopService();
    }


    protected void startWifiAp() {
        isOpeningWifiAp = true;
        startService();
        bindService();
    }

    protected void stopWifiAp() {
        closeWifiAp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWifiApService == null)
            return;
        if (!shouldCloseWifiAp()) {
            return;
        }
        closeWifiAp();
    }

    private void bindService() {
        if (mWifiApService == null)
            this.getApplicationContext().bindService(new Intent(this, WifiApService.class),
                    mServiceCon, Service.BIND_AUTO_CREATE);
    }

    private void unBindService() {
        //noinspection EmptyCatchBlock,EmptyCatchBlock
        try {
            this.getApplicationContext().unbindService(mServiceCon);
        } catch (Exception e) {
        }
    }

    private void startService() {
        this.startService(new Intent(this, WifiApService.class));
    }

    private void stopService() {

        this.stopService(new Intent(this, WifiApService.class));
    }

}
