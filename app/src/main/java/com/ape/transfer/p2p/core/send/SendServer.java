package com.ape.transfer.p2p.core.send;


import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;


/**
 * Created by way on 2016/10/20.
 * 发送文件的服务
 */
public class SendServer extends Thread {
    private static final String TAG = "SendServer";

    private ISendServerProxy mServerProxy;
    private Selector mSelector;
    private ServerSocketChannel mServerSocketChannel;

    public SendServer(ISendServerProxy serverProxy, int port) {
        this.mServerProxy = serverProxy;
        try {//创建服务器端的SocketChannel
            //获取一个通道管理器
            mSelector = Selector.open();
            //获取一个ServerSocket通道
            mServerSocketChannel = ServerSocketChannel.open();
            //设置通道为非阻塞方式
            mServerSocketChannel.configureBlocking(false);
            mServerSocketChannel.socket().setReuseAddress(true);//必须放在bind前面 否则没有用
            //将该通道所对应的ServerSocket绑定到端口
            mServerSocketChannel.socket().bind(new InetSocketAddress(port));
            //将通道管理器与该通道绑定，并为该通道注册accept事件，当该事件到达时selector.select()会返回，没有一直阻塞
            mServerSocketChannel.register(mSelector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Log.i(TAG, "socket server started.");

            //采用轮询的方式监听selector上是否有需要处理的事件
            while (true) {
                if (isInterrupted()) return;

                //当注册的方法到达时，返回，否则一直会阻塞
                if (mSelector.select() == 0) continue;

                Set<SelectionKey> selectionKeys = mSelector.selectedKeys();
                Iterator<SelectionKey> it = selectionKeys.iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    if (key.isAcceptable()) {
                        mServerProxy.handleAccept(key);
                    }
                    if (key.isReadable()) {
                        mServerProxy.handleRead(key);
                    }
                    if (key.isWritable()) {
                        mServerProxy.handleWrite(key);
                    }
                    it.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void quit() {
        interrupt();
        release();
    }

    private void release() {
        Log.d(TAG, "send server release");
        if (mServerSocketChannel != null) {
            try {
                mServerSocketChannel.socket().close();
                mServerSocketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mSelector != null) {
            try {
                mSelector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
