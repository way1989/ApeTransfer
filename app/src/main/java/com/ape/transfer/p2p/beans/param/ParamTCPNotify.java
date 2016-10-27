package com.ape.transfer.p2p.beans.param;


import com.ape.transfer.p2p.beans.Peer;

/**
 * Created by way on 2016/10/20.
 */
public class ParamTCPNotify {
    public Peer Neighbor;
    public Object Obj;

    public ParamTCPNotify(Peer dest, Object obj) {
        Neighbor = dest;
        Obj = obj;
    }
}
