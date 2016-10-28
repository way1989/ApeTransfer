package com.ape.transfer.p2p.beans.param;


import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.p2p.beans.TransferFile;

/**
 * Created by way on 2016/10/20.
 */
public class ParamSendFiles {
    public Peer[] neighbors;
    public TransferFile[] transferFiles;

    public ParamSendFiles(Peer[] neighbors, TransferFile[] transferFiles) {
        this.neighbors = neighbors;
        this.transferFiles = transferFiles;
    }
}
