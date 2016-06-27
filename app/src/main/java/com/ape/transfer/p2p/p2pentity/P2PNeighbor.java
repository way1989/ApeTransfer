package com.ape.transfer.p2p.p2pentity;


import java.io.Serializable;
import java.net.InetAddress;

/**
 * Created by 郭攀峰 on 2015/9/19.
 * 局域网的用户
 */
public class P2PNeighbor implements Serializable{

    private static final long serialVersionUID = -5191365431255505545L;
    public String alias;
    public int icon;
    public String ip;
    public InetAddress inetAddress;

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        P2PNeighbor s = (P2PNeighbor) obj;

        if ((s.ip == null))
            return false;

        return (this.ip.equals(s.ip));
    }
}
