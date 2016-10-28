package com.ape.transfer.p2p.beans.param;


import com.ape.transfer.p2p.beans.SigMessage;

import java.net.InetAddress;


/**
 * Created by way on 2016/10/20.
 */
public class ParamIPMsg {
    public SigMessage peerMSG;
    public InetAddress peerIAddress;
    public int peerPort;

    public ParamIPMsg(String msg, InetAddress addr, int port) {
        peerMSG = new SigMessage(msg);
        peerIAddress = addr;
        peerPort = port;
    }
}
