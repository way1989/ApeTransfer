package com.ape.transfer.util;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.util.List;

/**
 * @author liweiping
 * @description Wifi客户端Ap管理者，实质上是WifiManager的代理
 */
public class WifiUtils {
    /* 数据段begin */
    private final static String TAG = "WifiUtils";
    // 单例
    private static WifiUtils mWifiApClientManager;
    // WifiManager引用
    private WifiManager mWifiManager;

    /* 函数段begin */
    private WifiUtils(WifiManager wifiManager) {
        mWifiManager = wifiManager;
    }

    /* 数据段end */

    public synchronized static WifiUtils getInstance(WifiManager wifiManager) {
        if (mWifiApClientManager == null) {
            mWifiApClientManager = new WifiUtils(wifiManager);
        }

        return mWifiApClientManager;
    }

    public boolean setWifiEnabled(boolean enabled) {
        return mWifiManager.setWifiEnabled(enabled);
    }

    public boolean startScan() {
        return mWifiManager.startScan();
    }

    public List<ScanResult> getScanResults() {
        return mWifiManager.getScanResults();
    }

    public AuthenticationType getWifiAuthenticationType(String capabilities) {
        if (capabilities.contains("WPA2")) {
            return AuthenticationType.TYPE_WPA2;
        }

        if (capabilities.contains("WPA")) {
            return AuthenticationType.TYPE_WPA;
        }

        if (capabilities.contains("WEP")) {
            return AuthenticationType.TYPE_WEP;
        }

        return AuthenticationType.TYPE_NONE;
    }

    public WifiConfiguration generateWifiConfiguration(AuthenticationType type, String ssid, String password) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = String.format("\"%s\"", ssid);
        switch (type) {
            case TYPE_NONE:
                config.wepKeys[0] = "\"\"";
                config.wepTxKeyIndex = 0;
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;
            case TYPE_WEP:
                config.wepKeys[0] = String.format("\"%s\"", password);
                config.wepTxKeyIndex = 0;
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;
            case TYPE_WPA:
                config.preSharedKey = String.format("\"%s\"", password);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.NONE);
                config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                break;
            case TYPE_WPA2:
                config.preSharedKey = String.format("\"%s\"", password);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.NONE);
                config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                break;
            default:
                break;
        }

        return config;
    }

    public boolean connect(WifiConfiguration config) {
        if (mWifiManager == null || config == null) {
            return false;
        }

        int networkID = mWifiManager.addNetwork(config);
        if (networkID == -1) {
            return false;
        }

        return mWifiManager.enableNetwork(networkID, true);
    }

    // 认证加密类型
    public enum AuthenticationType {
        TYPE_NONE, TYPE_WEP, TYPE_WPA, TYPE_WPA2
    }
    /* 函数段end */
}
