package com.ape.transfer.model;

import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.p2p.beans.TransferFile;

/**
 * Created by android on 16-7-5.
 */
public class TransferEvent {
    private Peer mPeer;
    private TransferFile mTransferFile;

    public TransferEvent(Peer peer, TransferFile transferFile) {
        mPeer = peer;
        mTransferFile = transferFile;
    }

    public TransferFile getTransferFile() {
        return mTransferFile;
    }

    public Peer getPeer() {
        return mPeer;
    }
}
