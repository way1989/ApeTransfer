package com.ape.p2p.bean;


import java.net.InetAddress;


/**
 * Created by 郭攀峰 on 2015/9/20.
 */
public class ParamIPMsg {
    public SigMessage peerMSG;
    public InetAddress peerIAddr;

    public ParamIPMsg(String msg, InetAddress addr) {
        peerMSG = new SigMessage(msg);
        peerIAddr = addr;
    }
}
