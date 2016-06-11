package com.ape.transfer.p2p.p2pentity.param;


import com.ape.transfer.p2p.p2pentity.P2PFileInfo;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;

/**
 * Created by 郭攀峰 on 2015/9/20.
 */
public class ParamSendFiles {
    public P2PNeighbor[] neighbors;
    public P2PFileInfo[] files;

    public ParamSendFiles(P2PNeighbor[] neighbors, P2PFileInfo[] files) {
        this.neighbors = neighbors;
        this.files = files;
    }
}
