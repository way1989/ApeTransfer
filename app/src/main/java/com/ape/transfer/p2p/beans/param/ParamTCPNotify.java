package com.ape.transfer.p2p.beans.param;


import com.ape.transfer.p2p.beans.Peer;

/**
 * Created by way on 2016/10/20.
 */
public class ParamTCPNotify {
    public Peer peer;
    public Object object;

    public ParamTCPNotify(Peer dest, Object obj) {
        peer = dest;
        object = obj;
    }
}
