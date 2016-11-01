package com.ape.transfer.p2p.core.send;


import android.util.Log;

import com.ape.transfer.p2p.util.Constant;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by way on 2016/10/21.
 */
public class SendServerProxyProxy implements ISendServerProxy {
    private static final String TAG = "SendServerProxyProxy";
    private static final int CORE_POOL_SIZE = Constant.MAXIMUM_POOL_SIZE;
    private static final int KEEP_ALIVE_TIME = 1;
    private SendManager mSendManager;
    private ThreadPoolExecutor mThreadPoolExecutor;

    public SendServerProxyProxy(SendManager sendManager) {
        this.mSendManager = sendManager;
        mThreadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE,
                Constant.MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>());
        mThreadPoolExecutor.allowCoreThreadTimeOut(true);
    }

    @Override
    public void handleAccept(SelectionKey key) throws IOException {
        Log.d(TAG, "handle accept");

        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        //获取客户端连接的通道
        SocketChannel socketChannel = serverSocketChannel.accept();
        String peerIp = socketChannel.socket().getInetAddress().getHostAddress();
        Sender sender = mSendManager.getSender(peerIp);
        if (sender == null) {
            socketChannel.close();
            return;
        }
        //设置为非阻塞
        socketChannel.configureBlocking(false);

        SendTask sendTask = new SendTask(sender, socketChannel);
        if (sendTask.prepare() == SendTask.TRANS_START) {
            //在和客户端连接成功之后，为了可以给客户端写数据，需要给通道设置写的权限
            socketChannel.register(key.selector(), SelectionKey.OP_WRITE, sendTask); //将task attach到key中
        } else {
            socketChannel.close();
            return;
        }
        //sender.mSendTasks.add(sendTask);
        sendTask.notifySender(Constant.Command.SEND_TCP_ESTABLISHED);
    }

    @Override
    public void handleRead(SelectionKey key) throws IOException {

    }

    @Override
    public void handleWrite(SelectionKey key) throws IOException {
        Log.d(TAG, "handle write");

        SendTask sendTask = (SendTask) key.attachment();
        key.cancel();

        mThreadPoolExecutor.execute(sendTask);
    }
}
