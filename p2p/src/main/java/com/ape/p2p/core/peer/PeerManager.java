package com.ape.p2p.core.peer;

import android.util.Log;

import com.ape.p2p.bean.P2PNeighbor;
import com.ape.p2p.bean.ParamIPMsg;
import com.ape.p2p.bean.SigMessage;
import com.ape.p2p.util.P2PConstant;

import java.net.InetAddress;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

/**
 * Created by way on 2016/10/22.
 */

public class PeerManager {
    private Callback mCallback;
    private HashMap<String, P2PNeighbor> mNeighbors;

    public PeerManager(Callback callback) {
        mCallback = callback;
        mNeighbors = new HashMap<>();
    }

    public void onParseMessage(ParamIPMsg ipMsg) {
        switch (ipMsg.peerMSG.commandNum) {
            case P2PConstant.CommandNum.ON_LINE: //收到上线广播
                Log.d(TAG, "receive on_line and send on_line_ans message");
                addNeighbor(ipMsg.peerMSG, ipMsg.peerIAddr);
                //回复上线
                mCallback.sendMessage2Peer(ipMsg.peerIAddr, P2PConstant.CommandNum.ON_LINE_ANS, null);
                break;
            case P2PConstant.CommandNum.ON_LINE_ANS: //收到对方上线的回复
                Log.d(TAG, "received on_line_ans message");
                addNeighbor(ipMsg.peerMSG, ipMsg.peerIAddr);
                break;
            case P2PConstant.CommandNum.OFF_LINE://收到离线的消息
                delNeighbor(ipMsg.peerIAddr.getHostAddress());
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

        mCallback.onNeighborFound(neighbor);
    }

    private void delNeighbor(String ip) {
        P2PNeighbor neighbor = mNeighbors.get(ip);
        if (neighbor == null) return;
        mNeighbors.remove(ip);
        mCallback.onNeighborRemoved(neighbor);
    }

    public void stop() {
        mNeighbors.clear();
    }

    public interface Callback {
        void sendMessage2Peer(InetAddress sendTo, int cmd, String add);

        void onNeighborFound(P2PNeighbor neighbor);

        void onNeighborRemoved(P2PNeighbor neighbor);
    }
}
