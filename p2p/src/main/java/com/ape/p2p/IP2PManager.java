package com.ape.p2p;

import com.ape.p2p.bean.P2PFileInfo;
import com.ape.p2p.bean.P2PNeighbor;

/**
 * Created by android on 16-10-20.
 */

public interface IP2PManager {
    public void start(P2PNeighbor me);

    public void stop();

    public void sendFile(P2PNeighbor[] dsts, P2PFileInfo[] files, SendFileCallback callback);

    public void receiveFile(ReceiveFileCallback callback);

    public void cancelReceive();

    public void cancelSend(P2PNeighbor neighbor);

}
