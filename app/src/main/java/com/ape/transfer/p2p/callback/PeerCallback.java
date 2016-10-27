package com.ape.transfer.p2p.callback;


import com.ape.transfer.p2p.beans.Peer;

/**
 * Created by way on 2016/10/26.
 * 局域网好友上线和掉线
 */
public interface PeerCallback {
    /**
     * 局域网发现好友
     *
     * @param peer
     */
    void onPeerFound(Peer peer);

    /**
     * 局域网好友离开
     *
     * @param peer
     */
    void onPeerRemoved(Peer peer);
}
