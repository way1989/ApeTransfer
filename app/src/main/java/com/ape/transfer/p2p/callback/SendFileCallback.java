package com.ape.transfer.p2p.callback;


import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.p2p.beans.TransferFile;

/**
 * Created by way on 2016/10/20.
 * 我要发送实现的发送回调
 */
public interface SendFileCallback {
    void onPreSending();

    void onSending(TransferFile file, Peer dest);

    void onPostSending(Peer dest);

    void onPostAllSending();

    void onAbortSending(int error, Peer dest);
}
