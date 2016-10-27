package com.ape.transfer.p2p.beans.param;


import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.p2p.beans.TransferFile;

/**
 * Created by way on 2016/10/20.
 */
public class ParamSendFiles {
    public Peer[] neighbors;
    public TransferFile[] files;

    public ParamSendFiles(Peer[] neighbors, TransferFile[] files) {
        this.neighbors = neighbors;
        this.files = files;
    }
}
