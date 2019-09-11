package com.ape.transfer.util;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.ape.transfer.App;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

/**
 * @author liweiping
 * @description Wifi管理者，实质上是WifiManager的代理
 */
public class WifiUtils {
    private final static String TAG = "WifiUtils";
    private static final String DEFAULT_GATEWAY_IP = "192.168.43.1";
    // 单例
    private volatile static WifiUtils sWifiUtils;

    // WifiManager引用
    private WifiManager mWifiManager;

    private WifiUtils() {
        mWifiManager = (WifiManager) App.getApp().getSystemService(Context.WIFI_SERVICE);
    }

    public static WifiUtils getInstance() {
        if (sWifiUtils == null) {
            synchronized (WifiUtils.class) {
                if (sWifiUtils == null) {
                    sWifiUtils = new WifiUtils();
                }
            }
        }
        return sWifiUtils;
    }

    public String getLocalIP() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            Log.e("TransferService", "networkInterfaces = " + networkInterfaces);
            if (networkInterfaces == null) {
                return "";
            }

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && (address instanceof Inet4Address)) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            Log.e("TransferService", "", e);
        }

        return "";
    }

    public String getGatewayIP() {
        DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
        return convertIPv4IntToStr(dhcpInfo.serverAddress);
    }

    private String convertIPv4IntToStr(int ip) {
        if (ip <= 0) {
            return DEFAULT_GATEWAY_IP;
        }
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
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
        if (config == null) {
            return false;
        }

        int networkID = mWifiManager.addNetwork(config);
        return networkID != -1 && mWifiManager.enableNetwork(networkID, true);

    }

    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    public WifiInfo getConnectionInfo() {
        return mWifiManager.getConnectionInfo();
    }

    public String getSSID() {
        return getConnectionInfo().getSSID();
    }

    // 认证加密类型
    public enum AuthenticationType {
        TYPE_NONE, TYPE_WEP, TYPE_WPA, TYPE_WPA2
    }
}
