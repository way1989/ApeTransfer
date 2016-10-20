package com.ape.p2p;


import com.ape.p2p.bean.P2PNeighbor;

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
