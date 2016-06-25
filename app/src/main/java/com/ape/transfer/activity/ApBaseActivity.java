package com.ape.transfer.activity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.ape.transfer.service.WifiApService;
import com.ape.transfer.util.Log;

public abstract class ApBaseActivity extends RequestWriteSettingsBaseActivity {
    private static final String TAG = "ApBaseActivity";
    protected WifiApService.WifiApBinder mWifiApService;
    protected boolean isOpeningWifiAp;
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

    /**
     * after service connected, will can the function, the son activity can do
     * anything that need service connected
     */
    protected abstract void afterServiceConnected();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService();
    }

    private void bindService() {
        this.getApplicationContext().bindService(new Intent(this, WifiApService.class),
                mServiceCon, Service.BIND_AUTO_CREATE);
    }

    protected void unBindService() {
        if (mWifiApService != null) {
            mWifiApService.setOnWifiApStatusListener(null);
        }
        try {
            this.getApplicationContext().unbindService(mServiceCon);
        } catch (Exception e) {
        }
    }

    protected void startService() {
        this.startService(new Intent(this, WifiApService.class));
    }

    protected void stopService() {
        if (mWifiApService != null) {
            mWifiApService.reset();
        }
        this.stopService(new Intent(this, WifiApService.class));
    }

}
