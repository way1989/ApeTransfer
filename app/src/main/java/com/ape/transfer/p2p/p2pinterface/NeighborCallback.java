package com.ape.transfer.p2p.p2pinterface;


import com.ape.transfer.p2p.p2pentity.P2PNeighbor;

/**
 * Created by 郭攀峰 on 2015/9/19.
 * 局域网好友上线和掉线
 */
public interface NeighborCallback {
    /**
     * 局域网发现好友
     *
     * @param neighbor
     */
    public void NeighborFound(P2PNeighbor neighbor);

    /**
     * 局域网好友离开
     *
     * @param neighbor
     */
    public void NeighborRemoved(P2PNeighbor neighbor);
}
