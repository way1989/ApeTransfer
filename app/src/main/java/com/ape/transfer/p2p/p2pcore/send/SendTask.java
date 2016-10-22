package com.ape.transfer.p2p.p2pcore.send;


import android.util.Log;

import com.ape.transfer.p2p.p2pconstant.P2PConstant;
import com.ape.transfer.p2p.p2pcore.P2PWorkHandler;
import com.ape.transfer.p2p.p2pentity.P2PFileInfo;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by 郭攀峰 on 2015/9/22.
 */
public class SendTask extends OneByOneRunnable {
    public final static int TRANS_START = 1;
    public final static int TRANS_OVER = 2;
    private static final String TAG = "SendTask";
    private Sender sender;
    private SocketChannel socketChannel; //与客户端通信的通道
    private P2PWorkHandler p2PHandler;
    private P2PNeighbor neighbor;
    //private SocketTransInfo socketTransInfo;
    private P2PFileInfo mP2PFileInfo;
    private RandomAccessFile randomAccessFile = null;
    private FileChannel fileChannel;
    private MappedByteBuffer mappedByteBuffer = null;
    private Thread thread;

    public SendTask(Sender sender, SocketChannel socketChannel) {
        this.sender = sender;
        this.socketChannel = socketChannel;
        this.p2PHandler = sender.p2PHandler;
        this.neighbor = sender.neighbor;
    }

    @Override
    public void run() {
        Log.d(TAG, "send task run function");
        super.run();
        //super.pause();
        thread = Thread.currentThread();

        int len = 0;
        long lastLen = 0;
        final float update = mP2PFileInfo.size / 100.0f;

        while (mP2PFileInfo.position != mP2PFileInfo.size) {
            if (Thread.interrupted()) {
                release();
                break;
            }
            try {
                len = socketChannel.write(mappedByteBuffer);
            } catch (IOException e) {
                e.printStackTrace();
                notifySender(P2PConstant.CommandNum.SEND_LINK_ERROR);
                release();
            }

            mP2PFileInfo.position += len;

            if ((mP2PFileInfo.position - lastLen) > update) {
                lastLen = mP2PFileInfo.position;
                notifySender(P2PConstant.CommandNum.SEND_PERCENTS);
            }
        }// end of while
        notifySender(P2PConstant.CommandNum.SEND_PERCENTS);
        try {
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        randomAccessFile = null;

        if (Thread.interrupted())
            release();

        //super.resume();
    }

    public int prepare() {
        Log.d(TAG, "send task prepare function sender.index = " + sender.index);

        mP2PFileInfo = sender.files[sender.index];
        mP2PFileInfo.position = 0;

        try {
            randomAccessFile = new RandomAccessFile(new File(mP2PFileInfo.path), "r");
            fileChannel = randomAccessFile.getChannel();
            mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, mP2PFileInfo.size); //将文件映射到内存

            return TRANS_START;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return TRANS_OVER;
    }

    public void notifySender(int cmd) {
        //ParamTCPNotify notify = new ParamTCPNotify(neighbor, obj);
        if (p2PHandler != null)
            p2PHandler.send2Handler(cmd, P2PConstant.Src.SEND_TCP_THREAD,
                    P2PConstant.Recipient.FILE_SEND, neighbor);
    }

    public void quit() {
        if (thread != null && thread.isAlive())
            thread.interrupt();
        else
            release();
    }

    private synchronized void release() {
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

    }

}
