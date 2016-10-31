package com.ape.transfer.p2p.callback;


import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.p2p.beans.TransferFile;

/**
 * Created by way on 2016/10/20.
 * 我要接受实现的接收回掉
 */
public interface ReceiveFileCallback {
    void onPreReceiving(Peer src, TransferFile files[]);

    void onReceiving(Peer src, TransferFile files);

    void onPostReceiving();

    void onAbortReceiving(int error, String alias);
}
