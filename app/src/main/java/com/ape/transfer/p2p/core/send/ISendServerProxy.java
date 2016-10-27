package com.ape.transfer.p2p.core.send;


import java.io.IOException;
import java.nio.channels.SelectionKey;


/**
 * Created by way on 2016/10/20.
 * 处理NIO client与server交互
 */
public interface ISendServerProxy {
    /**
     * 处理客户端请求连接
     *
     * @param key
     * @throws IOException
     */
    void handleAccept(SelectionKey key) throws IOException;

    void handleRead(SelectionKey key) throws IOException;

    /**
     * 处理发送端写文件
     *
     * @param key
     * @throws IOException
     */
    void handleWrite(SelectionKey key) throws IOException;
}
