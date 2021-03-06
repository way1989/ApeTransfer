package com.ape.transfer.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.ape.transfer.App;

import java.lang.reflect.Method;

/**
 * @author liweiping
 * @description Wifi热点Ap管理者
 */
public class WifiApUtils {
    // Wifi AP广播action（本应该用反射获取，但为了减少不必要的代码，在这里定义，并与源码保持一致）
    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    /**
     * The lookup key for an int that indicates whether Wi-Fi AP is enabled,
     * disabled, enabling, disabling, or failed.  Retrieve it with
     * {@link android.content.Intent#getIntExtra(String, int)}.
     *
     * @see #WIFI_AP_STATE_DISABLED
     * @see #WIFI_AP_STATE_DISABLING
     * @see #WIFI_AP_STATE_ENABLED
     * @see #WIFI_AP_STATE_ENABLING
     * @see #WIFI_AP_STATE_FAILED
     */
    public static final String EXTRA_WIFI_AP_STATE = "wifi_state";
    /**
     * Wi-Fi AP is currently being disabled. The state will change to
     * {@link #WIFI_AP_STATE_DISABLED} if it finishes successfully.
     *
     * @see #WIFI_AP_STATE_CHANGED_ACTION
     */
    public static final int WIFI_AP_STATE_DISABLING = 10;
    /**
     * Wi-Fi AP is disabled.
     *
     * @see #WIFI_AP_STATE_CHANGED_ACTION
     */
    public static final int WIFI_AP_STATE_DISABLED = 11;
    /**
     * Wi-Fi AP is currently being enabled. The state will change to
     * {@link #WIFI_AP_STATE_ENABLED} if it finishes successfully.
     *
     * @see #WIFI_AP_STATE_CHANGED_ACTION
     */
    public static final int WIFI_AP_STATE_ENABLING = 12;
    /**
     * Wi-Fi AP is enabled.
     *
     * @see #WIFI_AP_STATE_CHANGED_ACTION
     */
    public static final int WIFI_AP_STATE_ENABLED = 13;
    /**
     * Wi-Fi AP is in a failed state. This state will occur when an error occurs during
     * enabling or disabling
     *
     * @see #WIFI_AP_STATE_CHANGED_ACTION
     */
    public static final int WIFI_AP_STATE_FAILED = 14;

    //Ap Change
    public static final String ACTION_TETHER_STATE_CHANGED =
            "android.net.conn.TETHER_STATE_CHANGED";

    /**
     * gives a String[] listing all the interfaces configured for
     * tethering and currently available for tethering.
     */
    public static final String EXTRA_AVAILABLE_TETHER = "availableArray";

    /**
     * gives a String[] listing all the interfaces currently tethered
     * (ie, has dhcp support and packets potentially forwarded/NATed)
     */
    public static final String EXTRA_ACTIVE_TETHER = "activeArray";

    /**
     * gives a String[] listing all the interfaces we tried to tether and
     * failed.
     * for any interfaces listed here.
     */
    public static final String EXTRA_ERRORED_TETHER = "erroredArray";
    /* 数据段begin */
    private final static String TAG = "WifiApUtils";
    /*单利对象*/
    private volatile static WifiApUtils sWifiApUtils;
    // WifiManager引用
    private WifiManager mWifiManager;

    private WifiApUtils() {
        mWifiManager = (WifiManager) App.getApp().getSystemService(Context.WIFI_SERVICE);
    }

    public static WifiApUtils getInstance() {
        if (sWifiApUtils == null) {
            synchronized (WifiApUtils.class) {
                if (sWifiApUtils == null) {
                    sWifiApUtils = new WifiApUtils();
                }
            }
        }
        return sWifiApUtils;
    }

    //尝试获取MAC地址
    private String getMacAddress() {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo == null || TextUtils.isEmpty(wifiInfo.getMacAddress())) {
            return null;
        }
        return wifiInfo.getMacAddress();
    }

    public String getWifiMacFromDevice() {
        String mac = getMacAddress();
        if (!TextUtils.isEmpty(mac)) {
            return mac;
        }

        //获取失败，尝试打开wifi获取
        mWifiManager.setWifiEnabled(true);
        for (int count = 0; count < 3; count++) {
            //如果第一次没有成功，第二次做500毫秒的延迟。
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mac = getMacAddress();
            if (!TextUtils.isEmpty(mac)) {
                Log.i(TAG, "getWifiMacFromDevice try count = " + count);
                return mac;
            }
        }
        return null;
    }

    public WifiConfiguration getWifiApConfiguration() {
        WifiConfiguration config = null;
        try {
            Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
            config = (WifiConfiguration) method.invoke(mWifiManager, (Object[]) null);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }

        return config;
    }

    public boolean setWifiApConfiguration(WifiConfiguration config) {
        boolean ret = false;
        try {
            Method method = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            ret = (Boolean) method.invoke(mWifiManager, config);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }

        return ret;
    }

    public boolean isWifiApEnabled() {
        boolean ret = false;
        try {
            Method method = mWifiManager.getClass().getMethod("isWifiApEnabled");
            ret = (Boolean) method.invoke(mWifiManager, (Object[]) null);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }

        return ret;
    }

    public boolean setWifiApEnabled(WifiConfiguration config, boolean enabled) {
        boolean ret = false;
        try {
            Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            ret = (Boolean) method.invoke(mWifiManager, config, enabled);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }

        return ret;
    }

    public WifiConfiguration generateWifiConfiguration(AuthenticationType type, String ssid, String MAC, String password) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = ssid;
        config.BSSID = MAC;
        Log.d(TAG, "MAC = " + config.BSSID);
        switch (type) {
            case TYPE_NONE:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;
            case TYPE_WPA:
                config.preSharedKey = password;
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
                config.preSharedKey = password;
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

    // 认证加密类型
    public enum AuthenticationType {
        TYPE_NONE, TYPE_WPA, TYPE_WPA2
    }
}
