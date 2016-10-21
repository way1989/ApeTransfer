package com.ape.p2p.core.send;


import android.util.Log;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;


/**
 * Created by way on 2016/10/21.
 */
public class SendServer extends Thread {
    private static final String TAG = "SendServer";

    private ISendServer handler;
    private int port;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private boolean ready = false;

    public SendServer(ISendServer handler, int port) {
        this.handler = handler;
        this.port = port;
    }

    @Override
    public void run() {
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

            Log.i(TAG, "socket server started.");

            onReady();

            //采用轮询的方式监听selector上是否有需要处理的事件
            while (true) {
                int keys = selector.select();//当注册的方法到达时，返回，否则一直会阻塞
                if (isInterrupted())
                    return;
                if (keys > 0) {
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

    public void isReady() {
        synchronized (this) {
            while (!ready) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onReady() {
        synchronized (this) {
            ready = true;
            notifyAll();
        }
    }
}