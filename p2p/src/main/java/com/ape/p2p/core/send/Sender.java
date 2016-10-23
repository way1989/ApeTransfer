package com.ape.p2p.core.send;

import com.ape.p2p.bean.P2PFileInfo;
import com.ape.p2p.bean.P2PNeighbor;

import java.util.ArrayList;

/**
 * Created by way on 2016/10/21.
 */

public class Sender {
    private static final String TAG = "Sender";

    private P2PWorkHandler p2PHandler;
    P2PFileInfo[] files;
    private SendManager sendManager;
    P2PNeighbor neighbor;
    ArrayList<SendTask> mSendTasks = new ArrayList<>();
    int index = 0;
    boolean flagPercents = false;

    public Sender(P2PWorkHandler handler, SendManager man, P2PNeighbor n, P2PFileInfo[] files) {
        this.p2PHandler = handler;
        this.sendManager = man;
        this.neighbor = n;
        this.files = files;
    }
}
