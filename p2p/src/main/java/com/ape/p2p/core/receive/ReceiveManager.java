package com.ape.p2p.core.receive;

import com.ape.p2p.bean.ParamIPMsg;

import java.net.InetAddress;

/**
 * Created by way on 2016/10/21.
 */

public class ReceiveManager {
    private static final String TAG = "ReceiveManager";
    private Callback mCallback;

    public ReceiveManager(Callback callback) {
        mCallback = callback;
    }

    public void onParseMessage(ParamIPMsg ipMsg) {
        switch (ipMsg.peerMSG.commandNum) {
            default:
                break;
        }
    }

    public void stop() {

    }

    public interface Callback {
        void sendMessage2Peer(InetAddress sendTo, int cmd, String add);

    }
}
