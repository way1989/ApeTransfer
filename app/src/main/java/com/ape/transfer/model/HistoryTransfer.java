package com.ape.transfer.model;

import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.p2p.beans.TransferFile;

/**
 * Created by android on 16-10-31.
 */

public class HistoryTransfer {
    public TransferFile transferFile;
    public Peer peer;
    private ProgressListener mListener;

    public HistoryTransfer(Peer peer, TransferFile transferFile) {
        this.peer = peer;
        this.transferFile = transferFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)//先检查是否其自反性,后比较o是否为空,这样效率高
            return true;

        if (o == null)
            return false;

        if (!(o instanceof HistoryTransfer))
            return false;

        HistoryTransfer transfer = (HistoryTransfer) o;
        return transferFile.equals(transfer.transferFile);
    }

    @Override
    public int hashCode() {
        return transferFile.hashCode();
    }

    public void updateProgress() {
        if (mListener != null)
            mListener.updateProgress(transferFile);
    }

    public void setProgressListener(ProgressListener listener) {
        mListener = listener;
    }

    public interface ProgressListener {
        void updateProgress(TransferFile file);
    }
}
