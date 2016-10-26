package com.ape.transfer.p2p.p2pinterface;


import com.ape.transfer.p2p.p2pentity.P2PFileInfo;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;

/**
 * Created by 郭攀峰 on 2015/9/20.
 * 我要接受实现的接收回掉
 */
public interface ReceiveFileCallback {
    public boolean onQueryReceiving(P2PNeighbor src, P2PFileInfo files[]);

    public void onPreReceiving(P2PNeighbor src, P2PFileInfo files[]);

    public void onReceiving(P2PFileInfo files);

    public void onPostReceiving();

    public void onAbortReceiving(int error, String alias);
}
