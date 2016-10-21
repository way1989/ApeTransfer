package com.ape.p2p;


import android.os.HandlerThread;

import com.ape.p2p.bean.P2PFileInfo;
import com.ape.p2p.bean.P2PNeighbor;
import com.ape.p2p.core.communicate.P2PWorkHandler;


/**
 * Created by android on 16-10-20.
 */

public class P2PManager implements IP2PManager, P2PWorkHandler.Callback {
    private static final String TAG = "P2PManager";
    private static P2PManager manager = new P2PManager();
    private P2PWorkHandler mWorkHandler;

    private P2PManager() {

    }

    public static P2PManager getInstance() {
        return manager;
    }

    @Override
    public void start(P2PNeighbor self) {
        HandlerThread handlerThread = new HandlerThread(TAG, Thread.MAX_PRIORITY);
        handlerThread.start();
        mWorkHandler = new P2PWorkHandler(handlerThread.getLooper(), self, this);
        mWorkHandler.onLine();
    }

    @Override
    public void stop() {
        if (mWorkHandler != null) {
            mWorkHandler.quit();
            mWorkHandler = null;
        }
    }

    @Override
    public void sendFile(P2PNeighbor[] dsts, P2PFileInfo[] files, SendFileCallback callback) {

    }

    @Override
    public void receiveFile(ReceiveFileCallback callback) {

    }

    @Override
    public void cancelReceive() {

    }

    @Override
    public void cancelSend(P2PNeighbor neighbor) {

    }

    @Override
    public void onNeighborFound(P2PNeighbor neighbor) {

    }

    @Override
    public void onNeighborRemoved(P2PNeighbor neighbor) {

    }
}
