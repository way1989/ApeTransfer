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
    private WorkHandler mWorkHandler;
    private HashMap<String, Peer> mPeerHashMap;

    public PeerManager(WorkHandler handler) {
        mWorkHandler = handler;
        mPeerHashMap = new HashMap<>();
    }

    public void dispatchMSG(ParamIPMsg ipmsg) {
        switch (ipmsg.peerMSG.commandNum) {
            case Constant.Command.ON_LINE: //收到上线广播
                Log.d(TAG, "receive on_line and send on_line_ans message");
                addPeer(ipmsg.peerMSG, ipmsg.peerIAddress);
                //回复我上线
                mWorkHandler.send2Neighbor(ipmsg.peerIAddress, Constant.Command.ON_LINE_ANS, null);
                break;
            case Constant.Command.ON_LINE_ANS: //收到对方上线的回复
                Log.d(TAG, "received on_line_ans message");
                addPeer(ipmsg.peerMSG, ipmsg.peerIAddress);
                break;
            case Constant.Command.OFF_LINE:
                removePeer(ipmsg.peerIAddress.getHostAddress());
                break;

        }
    }

    public HashMap<String, Peer> getPeerHashMap() {
        return mPeerHashMap;
    }

    private void addPeer(SigMessage sigMessage, InetAddress address) {
        String ip = address.getHostAddress();
        Log.d(TAG, "addPeer ip = " + ip + ", mPeerHashMap.size = "
                + mPeerHashMap.size() + ", mPeerHashMap containsKey = " + mPeerHashMap.containsKey(ip));
        Peer peer = mPeerHashMap.get(ip);
        if (peer != null) return;

        peer = new Peer();
        peer.alias = sigMessage.senderAlias;
        peer.avatar = sigMessage.senderAvatar;
        peer.ip = ip;
        peer.inetAddress = address;
        peer.wifiMac = sigMessage.wifiMac;
        peer.brand = sigMessage.brand;
        peer.mode = sigMessage.mode;
        peer.sdkInt = sigMessage.sdkInt;
        peer.versionCode = sigMessage.versionCode;
        peer.databaseVersion = sigMessage.databaseVersion;
        peer.lastTime = System.currentTimeMillis();
        mPeerHashMap.put(ip, peer);

        mWorkHandler.send2UI(Constant.UI.ADD_NEIGHBOR, peer);
    }

    private void removePeer(String ip) {
        Log.d(TAG, "addPeer ip = " + ip + ", mPeerHashMap containsKey = " + mPeerHashMap.containsKey(ip));
        Peer peer = mPeerHashMap.get(ip);
        if (peer == null) return;
        mPeerHashMap.remove(ip);
        mWorkHandler.send2UI(Constant.UI.REMOVE_NEIGHBOR, peer);

    }
}
