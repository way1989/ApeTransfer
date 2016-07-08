package com.ape.transfer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ape.transfer.R;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;
import com.ape.transfer.service.TransferService;
import com.ape.transfer.service.TransferServiceUtil;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.PreferenceUtil;
import com.ape.transfer.util.WifiUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ApScanActivity extends BaseActivity implements View.OnClickListener,
        TransferService.Callback, TransferServiceUtil.Callback {
    private static final String TAG = "ApScanActivity";
    private static final int MSG_START_P2P = 0;
    private static final int MSG_START_SCAN_WIFI = 1;
    private static final int MSG_HANDLE_SCAN_RESULT = 2;
    private static final int MSG_CONNECT_TIMEOUT = 3;
    private static final int MSG_SCAN_WIFI_TIMEOUT = 4;
    private static final long CONNECT_TIMEOUT = 30000;
    @BindView(R.id.iv_scan)
    ImageView ivScan;
    @BindView(R.id.iv_head)
    ImageView ivHead;
    @BindView(R.id.mine_tv_name)
    TextView mineTvName;
    @BindView(R.id.rl_phones)
    RelativeLayout rlPhones;
    private WifiManager mWifiManager;
    private WifiUtils mWifiUtils;
    private Animation rotateAnim;
    private boolean isHandleScanResult;
    private boolean isHandleWifiConnected;

    private boolean isStartScan;
    private TransferService.P2PBinder mTransferService;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_START_P2P:
                    initP2P();
                    break;
                case MSG_START_SCAN_WIFI:
                    startScanWifi();
                    break;
                case MSG_HANDLE_SCAN_RESULT:
                    handleScanResult();
                    break;
                case MSG_CONNECT_TIMEOUT:
                    Toast.makeText(getApplicationContext(), R.string.text_connetion_tip, Toast.LENGTH_SHORT).show();
                    removeAllFromRadom(true);
                    break;
                case MSG_SCAN_WIFI_TIMEOUT:
                    Toast.makeText(getApplicationContext(), R.string.text_connetion_tip, Toast.LENGTH_SHORT).show();
                    startScanWifi();
                    break;
            }
        }
    };
    BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                handleWifiState(state);
            } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                Log.d(TAG, "wifi scanned");
                mHandler.removeMessages(MSG_HANDLE_SCAN_RESULT);
                mHandler.sendEmptyMessageDelayed(MSG_HANDLE_SCAN_RESULT, 250L);
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                NetworkInfo.State state = networkInfo.getState();
                handleConnectState(state);
            }
        }
    };

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private void handleConnectState(NetworkInfo.State state) {
        switch (state) {
            case CONNECTED:
                Log.d(TAG, "wifi connected");
                if (isHandleWifiConnected)
                    return;
                isHandleWifiConnected = true;
                if (!isHandleScanResult)
                    return;
                if (mWifiManager.getConnectionInfo().getSSID().contains("ApeTransfer@")) {
                    mHandler.removeMessages(MSG_START_P2P);
                    mHandler.sendEmptyMessageDelayed(MSG_START_P2P, 250L);//不知道为什么连接上后又会断开,然后又连上,所以这里延迟久一点
                }
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

    private void handleWifiState(int state) {
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
                if (!isStartScan) {
                    mHandler.removeMessages(MSG_START_SCAN_WIFI);
                    mHandler.sendEmptyMessageDelayed(MSG_START_SCAN_WIFI, 250L);
                }
                break;
            case WifiManager.WIFI_STATE_UNKNOWN:
                Log.d(TAG, "wifi unknown");
                break;
            default:
                break;
        }
    }

    private void handleScanResult() {
        Log.i(TAG, "handleScanResult... isStartScan = " + isStartScan + ", isHandleScanResult = " + isHandleScanResult);

        if (!isStartScan)
            return;
        if (isHandleScanResult)
            return;
        parseScanResults();

    }

    private void parseScanResults() {
        List<ScanResult> filterResults = new ArrayList<>();
        List<ScanResult> scanResults = mWifiUtils.getScanResults();
        Log.i(TAG, "parseScanResults size = " + scanResults.size());
        for (ScanResult scanResult : scanResults) {
            String ssid = scanResult.SSID;
            if (ssid.startsWith("ApeTransfer")) {
                Log.i(TAG, "parseScanResults ssid = " + ssid);
                String[] alias = ssid.split("@");
                if (alias.length > 1) {
                    filterResults.add(scanResult);
                }

            }
        }
        addToRandom(filterResults);

        //have no ssid equal ApeTransfer, rescan system will scan every 15 second
        //isHandleScanResult = false;
        //startScanWifi();
    }

    private void addToRandom(List<ScanResult> filterResults) {
        rlPhones.removeAllViews();
        if (filterResults.isEmpty())
            return;
        LayoutInflater inflater = getLayoutInflater();
        final int size = Math.min(filterResults.size(), 4);
        for (int i = 0; i < size; i++) {
            View item = inflater.inflate(R.layout.item_ap_scan_activity, rlPhones, false);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            switch (i) {
                case 0:
                    lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    //lp.bottomMargin = Screen.dp(8);
                    break;
                case 1:
                    lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    lp.addRule(RelativeLayout.CENTER_VERTICAL);
                    //lp.rightMargin = Screen.dp(8);
                    break;
                case 2:
                    lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    //lp.topMargin = Screen.dp(8);
                    break;
                case 3:
                    lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    lp.addRule(RelativeLayout.CENTER_VERTICAL);
                    //lp.leftMargin = Screen.dp(8);
                    break;
            }
            item.setLayoutParams(lp);
            item.setTag(filterResults.get(i));
            item.setOnClickListener(this);
            bindDatas(filterResults.get(i), item);
            rlPhones.addView(item);
        }
    }

    private void removeAllFromRadom(boolean reScanWifi) {
        mHandler.removeMessages(MSG_CONNECT_TIMEOUT);
        rlPhones.removeAllViews();
        ivScan.startAnimation(rotateAnim);
        if (reScanWifi)
            startScanWifi();
    }

    private void bindDatas(ScanResult scanResult, View item) {
        TextView name = (TextView) item.findViewById(R.id.tv_name);
        name.setText(scanResult.SSID.split("@")[1]);
    }

    private void startScanWifi() {
        Log.i(TAG, "startScanWifi... isWifiOpen = " + mWifiUtils.isWifiOpen());

        if (!mWifiUtils.isWifiOpen()) {
            mWifiUtils.setWifiEnabled(true);
            return;
        }

        mWifiUtils.startScan();
        isStartScan = true;
        isHandleScanResult = false;
        isHandleWifiConnected = false;

        mHandler.removeMessages(MSG_SCAN_WIFI_TIMEOUT);
        mHandler.sendEmptyMessageDelayed(MSG_SCAN_WIFI_TIMEOUT, 1500L);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ap_scan);
        ButterKnife.bind(this);
        initData();

        registerReceiver();

        startScanWifi();
    }

    private void initData() {
        rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        rotateAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        ivScan.startAnimation(rotateAnim);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiUtils = WifiUtils.getInstance(mWifiManager);

        ivHead.setImageResource(UserInfoActivity.HEAD[PreferenceUtil.getInstance().getHead()]);
        mineTvName.setText(PreferenceUtil.getInstance().getAlias());
    }

    private void initP2P() {
        Log.i(TAG, "init P2P mTransferService = " + mTransferService);
        if (mTransferService == null) {
            TransferServiceUtil.getInstance().setCallback(this);
            TransferServiceUtil.getInstance().bindTransferService();
        } else {
            startP2P();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTransferService != null && mTransferService.isEmpty()) {
            TransferServiceUtil.getInstance().unbindTransferService();
            TransferServiceUtil.getInstance().stopTransferService();
        }
        unregisterReceiver();

        removeAllFromRadom(false);
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mWifiStateReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        unregisterReceiver(mWifiStateReceiver);
    }

    private void startP2P() {
        Log.i(TAG, "startP2P...");
        mTransferService.startP2P();
    }

    @Override
    public void onNeighborChanged(List<P2PNeighbor> neighbors) {
        Log.i(TAG, "onNeighborChanged neighbors = " + neighbors);
        mHandler.removeMessages(MSG_CONNECT_TIMEOUT);
        if (!neighbors.isEmpty()) {
            Intent intent = new Intent(this, MainTransferActivity.class);
            intent.putExtra("neighbor", neighbors.get(0));
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "rl_phones  onClick....");
        v.setEnabled(false);
        ScanResult scanResult = (ScanResult) v.getTag();
        if (scanResult == null)
            return;
        final String capabilities = scanResult.capabilities;
        final String ssid = scanResult.SSID;
        boolean isWifiConnected = isWifiConnected(this);
        Log.i(TAG, "isWifiConnected = " + isWifiConnected + ", ssid = " + mWifiManager.getConnectionInfo().getSSID()
                + ", ScanResult ssid = " + "\"" + ssid + "\"");
        if (isWifiConnected && TextUtils.equals(mWifiManager.getConnectionInfo().getSSID(), "\"" + ssid + "\"")) {
            mHandler.removeMessages(MSG_START_P2P);
            mHandler.sendEmptyMessageDelayed(MSG_START_P2P, 250L);//不知道为什么连接上后又会断开,然后又连上,所以这里延迟久一点
        } else {
            WifiUtils.AuthenticationType type = mWifiUtils.getWifiAuthenticationType(capabilities);
            switch (type) {
                case TYPE_NONE:
                    mWifiUtils.connect(mWifiUtils.generateWifiConfiguration(type, ssid, null));
                    break;
                case TYPE_WEP:
                case TYPE_WPA:
                case TYPE_WPA2:
                    //mWifiUtils.onNeighborConnected(mWifiUtils.generateWifiConfiguration(type, ssid, "12345678"));
                default:
                    break;
            }
        }
        mHandler.removeMessages(MSG_CONNECT_TIMEOUT);
        mHandler.sendEmptyMessageDelayed(MSG_CONNECT_TIMEOUT, CONNECT_TIMEOUT);//连接超时处理

        isHandleScanResult = true;
        isHandleWifiConnected = false;

        ivScan.clearAnimation();
        ivScan.setVisibility(View.INVISIBLE);

        View title = v.findViewById(R.id.tv_connect);
        View progress = v.findViewById(R.id.iv_route);
        ImageView ivHead = (ImageView) v.findViewById(R.id.iv_head);
        title.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        ivHead.setImageResource(R.drawable.unconnect_focus);
        progress.startAnimation(rotateAnim);

    }

    @Override
    public void onServiceConnected(TransferService.P2PBinder service) {
        Log.i(TAG, "onServiceConnected mTransferService = " + service);
        mTransferService = service;
        if (mTransferService != null) {
            mTransferService.setCallback(ApScanActivity.this);
            startP2P();
        }
    }

    @Override
    public void onServiceDisconnected() {
        Log.i(TAG, "onServiceDisconnected mTransferService = " + mTransferService);
        mTransferService = null;
    }
}
