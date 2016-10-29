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
        if (this == o)//先检查是否其自反性,后比较o是否为空,这样效率高
            return true;

        if (o == null)
            return false;

        if (!(o instanceof Peer))
            return false;

        final Peer peer = (Peer) o;

        return TextUtils.equals(this.ip, peer.ip);
    }

    @Override
    public int hashCode() {//hashCode主要是用来提高hash系统的查询效率。当hashCode中不进行任何操作时，可以直接让其返回 一常数，或者不进行重写。
        return this.ip.hashCode();
    }

    @Override
    public String toString() {
        return "Peer{" +
                "alias='" + alias + '\'' +
                ", avatar=" + avatar +
                ", ip='" + ip + '\'' +
                ", inetAddress=" + inetAddress +
                ", wifiMac='" + wifiMac + '\'' +
                ", mode='" + mode + '\'' +
                ", brand='" + brand + '\'' +
                ", sdkInt=" + sdkInt +
                ", versionCode=" + versionCode +
                ", databaseVersion=" + databaseVersion +
                ", lastTime=" + lastTime +
                '}';
    }
}
