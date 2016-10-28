package com.ape.transfer.p2p.core;


import android.util.Log;

import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.p2p.beans.SigMessage;
import com.ape.transfer.p2p.beans.param.ParamIPMsg;
import com.ape.transfer.p2p.util.Constant;

import java.net.InetAddress;
import java.util.HashMap;


/**
 * Created by way on 2016/10/23.
 */
public class PeerManager {
    private static final String TAG = "PeerManager";
    private WorkHandler p2PHandler;
    private HashMap<String, Peer> mNeighbors;

    public PeerManager(WorkHandler handler) {
        p2PHandler = handler;
        mNeighbors = new HashMap<>();
    }

    public void dispatchMSG(ParamIPMsg ipmsg) {
        switch (ipmsg.peerMSG.commandNum) {
            case Constant.CommandNum.ON_LINE: //收到上线广播
                Log.d(TAG, "receive on_line and send on_line_ans message");
                addNeighbor(ipmsg.peerMSG, ipmsg.peerIAddress);
                //回复我上线
                p2PHandler.send2Neighbor(ipmsg.peerIAddress, Constant.CommandNum.ON_LINE_ANS, null);
                break;
            case Constant.CommandNum.ON_LINE_ANS: //收到对方上线的回复
                Log.d(TAG, "received on_line_ans message");
                addNeighbor(ipmsg.peerMSG, ipmsg.peerIAddress);
                break;
            case Constant.CommandNum.OFF_LINE:
                delNeighbor(ipmsg.peerIAddress.getHostAddress());
                break;

        }
    }

    public HashMap<String, Peer> getNeighbors() {
        return mNeighbors;
    }

    private void addNeighbor(SigMessage sigMessage, InetAddress address) {
        String ip = address.getHostAddress();
        Peer neighbor = mNeighbors.get(ip);
        if (neighbor != null) return;

        neighbor = new Peer();
        neighbor.alias = sigMessage.senderAlias;
        neighbor.avatar = sigMessage.senderAvatar;
        neighbor.ip = ip;
        neighbor.inetAddress = address;
        neighbor.wifiMac = sigMessage.wifiMac;
        neighbor.brand = sigMessage.brand;
        neighbor.mode = sigMessage.mode;
        neighbor.sdkInt = sigMessage.sdkInt;
        neighbor.versionCode = sigMessage.versionCode;
        neighbor.databaseVersion = sigMessage.databaseVersion;
        neighbor.lastTime = System.currentTimeMillis();
        mNeighbors.put(ip, neighbor);

        p2PHandler.send2UI(Constant.UI_MSG.ADD_NEIGHBOR, neighbor);
    }

    private void delNeighbor(String ip) {
        Peer neighbor = mNeighbors.get(ip);
        if (neighbor == null) return;
        mNeighbors.remove(ip);
        p2PHandler.send2UI(Constant.UI_MSG.REMOVE_NEIGHBOR, neighbor);

    }
}
