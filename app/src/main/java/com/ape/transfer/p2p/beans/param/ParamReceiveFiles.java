package com.ape.transfer.p2p.beans.param;


import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.p2p.beans.TransferFile;

/**
 * Created by way on 2016/10/20.
 */
public class ParamReceiveFiles {
    public Peer Neighbor;
    public TransferFile[] Files;

    public ParamReceiveFiles(Peer dest, TransferFile[] files) {
        Neighbor = dest;
        Files = files;
    }
}
