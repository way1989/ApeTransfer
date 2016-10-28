package com.ape.transfer.p2p.beans;


import android.text.TextUtils;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Created by way on 2016/10/19.
 * 局域网的用户
 */
public class Peer implements Serializable {
    public static final String TAG = "Peer";
    private static final long serialVersionUID = -5191365431255505545L;
    public String alias;
    public int avatar;
    public String ip;
    public InetAddress inetAddress;

    public String wifiMac;
    public String mode;
    public String brand;
    public int sdkInt;
    public int versionCode;
    public int databaseVersion;
    public long lastTime;

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;

        Peer peer = (Peer) o;

        return TextUtils.equals(peer.wifiMac, this.wifiMac);

    }
}
