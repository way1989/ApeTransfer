package com.ape.transfer.model;

import com.ape.transfer.p2p.beans.Peer;

/**
 * Created by android on 16-10-27.
 */

public class PeerEvent {
    public static final int ADD = 0;
    public static final int REMOVED = 1;
    private Peer peer;
    private int type;

    public PeerEvent(Peer peer, int type) {
        this.peer = peer;
        this.type = type;
    }

    public Peer getPeer() {
        return peer;
    }

    public int getType() {
        return type;
    }
}
