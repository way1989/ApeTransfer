package com.ape.transfer.model;

import com.ape.transfer.p2p.beans.TransferFile;

/**
 * Created by android on 16-7-5.
 */
public class P2PFileInfoEvent {
    private TransferFile mMsg;

    public P2PFileInfoEvent(TransferFile msg) {
        mMsg = msg;
    }

    public TransferFile getMsg() {
        return mMsg;
    }
}
