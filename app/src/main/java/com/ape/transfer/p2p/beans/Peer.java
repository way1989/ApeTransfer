package com.ape.transfer.p2p.beans;


import java.io.Serializable;
import java.net.InetAddress;

/**
 * Created by way on 2016/10/19.
 * 局域网的用户
 */
public class Peer implements Serializable {

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
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        Peer s = (Peer) obj;

        if ((s.ip == null))
            return false;

        return (this.ip.equals(s.ip));
    }
}
