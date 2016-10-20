package com.ape.p2p;


import com.ape.p2p.bean.P2PFileInfo;
import com.ape.p2p.bean.P2PNeighbor;

/**
 * Created by 郭攀峰 on 2015/9/20.
 * 我要接受实现的接收回掉
 */
public interface ReceiveFileCallback {
    public boolean queryReceiving(P2PNeighbor src, P2PFileInfo files[]);

    public void onPreReceiving(P2PNeighbor src, P2PFileInfo files[]);

    public void onReceiving(P2PFileInfo files);

    public void onPostReceiving();

    public void onAbortReceiving(int error, String alias);
}
