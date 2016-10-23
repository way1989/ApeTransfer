package com.ape.p2p.core.send;

import android.util.Log;

import com.ape.p2p.bean.ParamIPMsg;
import com.ape.p2p.util.P2PConstant;

import java.net.InetAddress;
import java.util.HashMap;

/**
 * Created by way on 2016/10/21.
 */

public class SendManager {
    private static final String TAG = "SendManager";

    private Callback mCallback;
    private HashMap<String, Sender> mSenders;

    private SendServer sendServer;
    private SendServerHandler sendServerHandler;

    public SendManager(Callback callback) {
        mCallback = callback;
        mSenders = new HashMap<>();
        init();
    }

    public void onParseMessage(ParamIPMsg ipMsg) {
        switch (ipMsg.peerMSG.commandNum) {
            default:
                break;
        }
    }

    public void stop() {

    }

    private void init() {
        mSenders.clear();
    }

    public void startSend(String peerIP, Sender fileSender) {
        if (sendServer == null) {
            Log.d(TAG, "SendManager start send");

            sendServerHandler = new SendServerHandler(this);
            sendServer = new SendServer(sendServerHandler, P2PConstant.PORT);
            sendServer.start();
            sendServer.isReady();
        }
        mSenders.put(fileSender.neighbor.ip, fileSender);
    }

    public void removeSender(String peerIP) {
        mSenders.remove(peerIP);
        checkAllOver();
    }

    public void checkAllOver() {
        if (mSenders.isEmpty()) {
            quit();
        }
    }

    public void quit() {
        mSenders.clear();
        if (sendServer != null) {
            sendServer.quit();
            sendServer = null;
        }
    }

    protected Sender getSender(String peerIP) {
        return mSenders.get(peerIP);
    }

    public interface Callback {
        void sendMessage2Peer(InetAddress sendTo, int cmd, String add);

    }
}
