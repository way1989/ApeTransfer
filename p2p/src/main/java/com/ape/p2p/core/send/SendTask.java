package com.ape.p2p.core.send;


import android.util.Log;


import com.ape.p2p.bean.P2PFileInfo;
import com.ape.p2p.bean.P2PNeighbor;
import com.ape.p2p.bean.ParamTCPNotify;
import com.ape.p2p.bean.SocketTransInfo;
import com.ape.p2p.util.P2PConstant;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by way on 2016/10/21.
 */
public class SendTask extends OneByOneRunnable {
    final static int TRANS_START = 1;
    private final static int TRANS_OVER = 2;
    private static final String TAG = "SendTask";
    private Sender sender;
    private SocketChannel socketChannel; //与客户端通信的通道
    //P2PWorkHandler p2PHandler;
    private P2PNeighbor neighbor;
    private SocketTransInfo socketTransInfo;
    private long lastTransferred;
    private int step;
    private RandomAccessFile randomAccessFile = null;
    private FileChannel fileChannel;
    private MappedByteBuffer mappedByteBuffer = null;
    private Thread thread;
    private boolean finished = false;

    public SendTask(Sender sender, SocketChannel socketChannel) {
        this.sender = sender;
        this.socketChannel = socketChannel;
        //this.p2PHandler = sender.p2PHandler;
        this.neighbor = sender.neighbor;
    }

    @Override
    public void run() {
        Log.d(TAG, "send task run function");
        super.pause();
        thread = Thread.currentThread();

        int len = 0;
        boolean idle = false;

        while (!idle) {
            if (Thread.interrupted()) {
                release();
                break;
            }
            try {
                len = socketChannel.write(mappedByteBuffer);
            } catch (IOException e) {
                e.printStackTrace();
                notifySender(P2PConstant.CommandNum.SEND_LINK_ERROR, null);
                release();
            }

            socketTransInfo.Transferred += len;
            socketTransInfo.Length -= len;
            socketTransInfo.Offset += len;

            if (socketTransInfo.Length == 0) {
                idle = true;
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                randomAccessFile = null;
            }

            if ((socketTransInfo.Transferred - lastTransferred) > step || idle) {
                lastTransferred = socketTransInfo.Transferred;
                if (!sender.flagPercents || idle) {
                    sender.flagPercents = true;
                    notifySender(P2PConstant.CommandNum.SEND_PERCENTS, socketTransInfo);
                }
            }
        }// end of while

        if (Thread.interrupted())
            release();

        super.resume();
    }

    public int prepare() {
        Log.d(TAG, "send task prepare function");

        socketTransInfo = new SocketTransInfo(sender.index);
        P2PFileInfo p2PFileInfo = sender.files[sender.index];
        p2PFileInfo.LengthNeeded = p2PFileInfo.size;
        socketTransInfo.Length = p2PFileInfo.size;
        socketTransInfo.Offset = 0;
        lastTransferred = 0;

        try {
            randomAccessFile = new RandomAccessFile(new File(p2PFileInfo.path), "r");
            fileChannel = randomAccessFile.getChannel();
            mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY,
                    socketTransInfo.Offset, socketTransInfo.Length); //将文件映射到内存
            step = (int) ((float) p2PFileInfo.size / 100 + 0.5);

            return TRANS_START;
        } catch (IOException e) {
            e.printStackTrace();
            return TRANS_OVER;
        }
    }

    public void notifySender(int cmd, Object obj) {
        ParamTCPNotify notify = new ParamTCPNotify(neighbor, obj);
        if (!finished) {//发送消息到UI，刷新进度
//            if (p2PHandler != null)
//                p2PHandler.send2Handler(cmd, P2PConstant.Src.SEND_TCP_THREAD,
//                        P2PConstant.Recipient.FILE_SEND, notify);
        }
    }

    public void quit() {
        if (thread != null && thread.isAlive())
            thread.interrupt();
        else
            release();
    }

    private synchronized void release() {
        if (!finished) {
            if (socketChannel != null) {
                try {
                    socketChannel.socket().close();
                    socketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileChannel != null) {
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            finished = true;
        }
    }

}
