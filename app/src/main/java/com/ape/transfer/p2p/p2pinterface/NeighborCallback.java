package com.ape.transfer.p2p.p2pinterface;


import com.ape.transfer.p2p.p2pentity.P2PNeighbor;

/**
 * Created by way on 2016/10/26.
 * 局域网好友上线和掉线
 */
public interface NeighborCallback {
    /**
     * 局域网发现好友
     *
     * @param neighbor
     */
    public void onNeighborFound(P2PNeighbor neighbor);

    /**
     * 局域网好友离开
     *
     * @param neighbor
     */
    public void onNeighborRemoved(P2PNeighbor neighbor);
}
