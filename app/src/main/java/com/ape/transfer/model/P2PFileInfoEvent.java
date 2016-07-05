package com.ape.transfer.model;

import com.ape.transfer.p2p.p2pentity.P2PFileInfo;

/**
 * Created by android on 16-7-5.
 */
public class P2PFileInfoEvent {
    private P2PFileInfo mMsg;

    public P2PFileInfoEvent(P2PFileInfo msg) {
        mMsg = msg;
    }

    public P2PFileInfo getMsg() {
        return mMsg;
    }
}
