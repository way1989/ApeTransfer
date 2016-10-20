package com.ape.p2p;


import com.ape.p2p.bean.P2PFileInfo;
import com.ape.p2p.bean.P2PNeighbor;

/**
 * Created by 郭攀峰 on 2015/9/20.
 * 我要发送实现的发送回调
 */
public interface SendFileCallback {
    public void onPreSending();

    public void onSending(P2PFileInfo file, P2PNeighbor dest);

    public void onPostSending(P2PNeighbor dest);

    public void onPostAllSending();

    public void onAbortSending(int error, P2PNeighbor dest);
}
