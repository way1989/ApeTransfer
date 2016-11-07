package com.ape.transfer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.ape.transfer.R;
import com.ape.transfer.model.PeerEvent;
import com.ape.transfer.service.TransferService;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.RxBus;
import com.trello.rxlifecycle.android.ActivityEvent;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by android on 16-11-3.
 */

public abstract class BaseWifiConnectActivity extends BaseActivity {
    protected static final int MSG_START_P2P = 0;
    protected static final int MSG_TIMEOUT = 1;
    protected static final long DURATION_TIMEOUT = 30000L;
    protected static final long DELAY_START_P2P = 500L;
    private static final String TAG = "BaseWifiConnectActivity";
    protected final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_START_P2P:
                    startP2P();
                    break;
                case MSG_TIMEOUT:
                    Toast.makeText(getApplicationContext(), R.string.text_connetion_tip, Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        }
    };
    private final BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                handleWifiState(state);
            } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                handleScanResult();
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                NetworkInfo.State state = networkInfo.getState();
                handleConnectState(state);
            }
        }
    };

    protected void handleWifiState(int state) {
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
                break;
        }
    }

    protected void handleConnectState(NetworkInfo.State state) {
        switch (state) {
            case CONNECTED:
                Log.d(TAG, "wifi connected");
                break;
            case CONNECTING:
                Log.d(TAG, "wifi connecting");
                break;
            case DISCONNECTED:
                Log.d(TAG, "wifi disconnected");
                break;
            case DISCONNECTING:
                Log.d(TAG, "wifi disconnecting");
                break;
            case SUSPENDED:
                Log.d(TAG, "wifi suspended");
                break;
            case UNKNOWN:
                Log.d(TAG, "wifi unknown");
                break;
            default:
                break;
        }
    }

    protected void handleScanResult() {
        Log.d(TAG, "handle scan Result...");
    }

    protected abstract boolean isNeedScanWifi();

    protected abstract void onPeerChanged(PeerEvent peerEvent);

    private void startP2P() {
        Log.i(TAG, "startP2P...");
        Intent intent = new Intent(this, TransferService.class);
        intent.setAction(TransferService.ACTION_START_P2P);
        startService(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceiver();
        RxBus.getInstance().toObservable(PeerEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<PeerEvent>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Action1<PeerEvent>() {
                    @Override
                    public void call(PeerEvent peerEvent) {
                        //do some thing
                        onPeerChanged(peerEvent);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
        mHandler.removeCallbacksAndMessages(null);
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        if (isNeedScanWifi()) {
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        }
        registerReceiver(mWifiStateReceiver, intentFilter);
    }


    private void unregisterReceiver() {
        try {
            unregisterReceiver(mWifiStateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
