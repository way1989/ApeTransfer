package com.ape.p2p;

import com.ape.p2p.bean.P2PFileInfo;
import com.ape.p2p.bean.P2PNeighbor;

import java.util.Map;

/**
 * Created by android on 16-10-20.
 */

public interface IP2PManager {
    public void start(P2PNeighbor me, NeighborCallback neighborCallback);

    public void stop();

    public void sendFile(P2PNeighbor[] dsts, P2PFileInfo[] files, SendFileCallback callback);

    public void ackReceive();

    public void receiveFile(ReceiveFileCallback callback);

    public void cancelReceive();

    public void cancelSend(P2PNeighbor neighbor);

    public Map<String, P2PNeighbor> getNeighbors();

}
