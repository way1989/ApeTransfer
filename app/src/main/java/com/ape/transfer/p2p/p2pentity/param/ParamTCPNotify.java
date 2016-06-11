package com.ape.transfer.p2p.p2pentity.param;


import com.ape.transfer.p2p.p2pentity.P2PNeighbor;

/**
 * Created by 郭攀峰 on 2015/9/22.
 */
public class ParamTCPNotify {
    public P2PNeighbor Neighbor;
    public Object Obj;

    public ParamTCPNotify(P2PNeighbor dest, Object obj) {
        Neighbor = dest;
        Obj = obj;
    }
}
