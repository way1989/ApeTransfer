package com.ape.p2p;


import com.ape.p2p.bean.P2PNeighbor;

/**
 * Created by way on 2016/10/22.
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
