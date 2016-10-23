package com.ape.transfer.p2p.p2pcore;


import android.util.Log;

import com.ape.transfer.p2p.p2pconstant.P2PConstant;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;
import com.ape.transfer.p2p.p2pentity.SigMessage;
import com.ape.transfer.p2p.p2pentity.param.ParamIPMsg;

import java.net.InetAddress;
import java.util.HashMap;


/**
 * Created by way on 2016/10/23.
 */
public class P2PPeerManager {
    private static final String TAG = "P2PPeerManager";
    private P2PWorkHandler p2PHandler;
    private HashMap<String, P2PNeighbor> mNeighbors;

    public P2PPeerManager(P2PWorkHandler handler) {
        p2PHandler = handler;
        mNeighbors = new HashMap<>();
    }

    public void dispatchMSG(ParamIPMsg ipmsg) {
        switch (ipmsg.peerMSG.commandNum) {
            case P2PConstant.CommandNum.ON_LINE: //收到上线广播
                Log.d(TAG, "receive on_line and send on_line_ans message");
                addNeighbor(ipmsg.peerMSG, ipmsg.peerIAddr);
                //回复我上线
                p2PHandler.send2Neighbor(ipmsg.peerIAddr, P2PConstant.CommandNum.ON_LINE_ANS, null);
                break;
            case P2PConstant.CommandNum.ON_LINE_ANS: //收到对方上线的回复
                Log.d(TAG, "received on_line_ans message");
                addNeighbor(ipmsg.peerMSG, ipmsg.peerIAddr);
                break;
            case P2PConstant.CommandNum.OFF_LINE:
                delNeighbor(ipmsg.peerIAddr.getHostAddress());
                break;

        }
    }

    public HashMap<String, P2PNeighbor> getNeighbors() {
        return mNeighbors;
    }

    private void addNeighbor(SigMessage sigMessage, InetAddress address) {
        String ip = address.getHostAddress();
        P2PNeighbor neighbor = mNeighbors.get(ip);
        if (neighbor != null) return;

        neighbor = new P2PNeighbor();
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
        mNeighbors.put(ip, neighbor);

        p2PHandler.send2UI(P2PConstant.UI_MSG.ADD_NEIGHBOR, neighbor);
    }

    private void delNeighbor(String ip) {
        P2PNeighbor neighbor = mNeighbors.get(ip);
        if (neighbor == null) return;
        mNeighbors.remove(ip);
        p2PHandler.send2UI(P2PConstant.UI_MSG.REMOVE_NEIGHBOR, neighbor);

    }
}
