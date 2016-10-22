package com.ape.transfer.p2p.p2pcore.send;


import android.util.Log;

import com.ape.transfer.p2p.p2pinterface.ISendServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;


/**
 * Created by 郭攀峰 on 2015/9/21.
 */
public class SendServer extends Thread {
    private static final String TAG = "SendServer";

    private ISendServer handler;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public SendServer(ISendServer handler, int port) {
        this.handler = handler;
        try {//创建服务器端的SocketChannel
            //获取一个通道管理器
            selector = Selector.open();
            //获取一个ServerSocket通道
            serverSocketChannel = ServerSocketChannel.open();
            //设置通道为非阻塞方式
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().setReuseAddress(true);//必须放在bind前面 否则没有用
            //将该通道所对应的ServerSocket绑定到端口
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            //将通道管理器与该通道绑定，并为该通道注册accept事件，当该事件到达时selector.select()会返回，没有一直阻塞
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        }catch (IOException e) {
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
                if (selector.select() == 0) continue;

                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectionKeys.iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    if (key.isAcceptable()) {
                        handler.handleAccept(key);
                    }
                    if (key.isReadable()) {
                        handler.handleRead(key);
                    }
                    if (key.isWritable()) {
                        handler.handleWrite(key);
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
        if (serverSocketChannel != null) {
            try {
                serverSocketChannel.socket().close();
                serverSocketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
