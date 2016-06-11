package com.ape.transfer.p2p.p2pinterface;


import com.ape.transfer.p2p.p2pentity.P2PFileInfo;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;

/**
 * Created by 郭攀峰 on 2015/9/20.
 * 我要接受实现的接收回掉
 */
public interface ReceiveFile_Callback {
    public boolean QueryReceiving(P2PNeighbor src, P2PFileInfo files[]);

    public void BeforeReceiving(P2PNeighbor src, P2PFileInfo files[]);

    public void OnReceiving(P2PFileInfo files);

    public void AfterReceiving();

    public void AbortReceiving(int error, String alias);
}
