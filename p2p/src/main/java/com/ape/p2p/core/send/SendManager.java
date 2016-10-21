package com.ape.p2p.core.send;

import android.util.Log;

import com.ape.p2p.core.communicate.P2PWorkHandler;
import com.ape.p2p.util.P2PConstant;

import java.util.HashMap;

/**
 * Created by way on 2016/10/21.
 */

public class SendManager {
    private static final String TAG = "SendManager";

    private P2PWorkHandler p2PHandler;
    private HashMap<String, Sender> mSenders;

    private SendServer sendServer;
    private SendServerHandler sendServerHandler;

    public SendManager(P2PWorkHandler handler) {
        this.p2PHandler = handler;
        mSenders = new HashMap<>();
        init();
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
}
