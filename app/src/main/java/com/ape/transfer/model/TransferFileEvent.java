package com.ape.transfer.model;

import com.ape.transfer.p2p.beans.TransferFile;

/**
 * Created by android on 16-7-5.
 */
public class TransferFileEvent {
    private TransferFile mTransferFile;

    public TransferFileEvent(TransferFile transferFile) {
        mTransferFile = transferFile;
    }

    public TransferFile getTransferFile() {
        return mTransferFile;
    }
}
