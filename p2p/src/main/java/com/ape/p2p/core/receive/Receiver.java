package com.ape.p2p.core.receive;

import com.ape.p2p.bean.P2PFileInfo;
import com.ape.p2p.bean.P2PNeighbor;

/**
 * Created by way on 2016/10/21.
 */

public class Receiver {
    private static final String TAG = "Receiver";
    public ReceiveManager receiveManager;
    public P2PNeighbor neighbor;
    public P2PFileInfo[] files;
    //public P2PWorkHandler p2PHandler;
    protected ReceiveTask receiveTask = null;
    boolean flagPercent = false;

    public Receiver(ReceiveManager receiveManager, P2PNeighbor neighbor,
                    P2PFileInfo[] files) {
        this.receiveManager = receiveManager;
        this.neighbor = neighbor;
        this.files = files;
        //p2PHandler = receiveManager.p2PHandler;
    }
}
