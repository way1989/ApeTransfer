package com.ape.transfer.p2p.core.send;


import android.util.Log;

import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.p2p.beans.TransferFile;
import com.ape.transfer.p2p.core.WorkHandler;
import com.ape.transfer.p2p.util.Constant;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by way on 2016/10/20.
 */
public class SendTask implements Runnable {
    public final static int TRANS_START = 1;
    public final static int TRANS_OVER = 2;
    private static final String TAG = "SendTask";
    private Sender mSender;
    private SocketChannel mSocketChannel; //与客户端通信的通道
    private WorkHandler mWorkHandler;
    private Peer mNeighbor;
    private TransferFile mSendFileInfo;
    private RandomAccessFile mRandomAccessFile;
    private FileChannel mFileChannel;
    private MappedByteBuffer mMappedByteBuffer;
    //private Thread mCurrentThread;

    public SendTask(Sender sender, SocketChannel socketChannel) {
        this.mSender = sender;
        this.mSocketChannel = socketChannel;
        this.mWorkHandler = sender.p2PHandler;
        this.mNeighbor = sender.neighbor;
    }

    @Override
    public void run() {
        Log.d(TAG, "send task run function");
        //super.pause();
        //mCurrentThread = Thread.currentThread();

        int len = 0;
        long lastLen = 0L;
        final float update = mSendFileInfo.size / 100.0f;

        while (mSendFileInfo.position != mSendFileInfo.size) {
            if (Thread.interrupted()) {
                release();
                break;
            }
            try {
                len = mSocketChannel.write(mMappedByteBuffer);
            } catch (IOException e) {
                e.printStackTrace();
                notifySender(Constant.CommandNum.SEND_LINK_ERROR);
                release();
            }

            mSendFileInfo.position += len;

            if ((mSendFileInfo.position - lastLen) > update) {
                lastLen = mSendFileInfo.position;
                notifySender(Constant.CommandNum.SEND_PERCENTS);
            }
        }// end of while
        notifySender(Constant.CommandNum.SEND_PERCENTS);
        try {
            mRandomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRandomAccessFile = null;

        if (Thread.interrupted())
            release();

        //super.resume();
    }

    public int prepare() {
        Log.d(TAG, "send task prepare function sender file = " + mSender.mSendFileQueue.peek());

        mSendFileInfo = mSender.mSendFileQueue.peek();
        mSendFileInfo.position = 0;

        try {
            mRandomAccessFile = new RandomAccessFile(new File(mSendFileInfo.path), "r");
            mFileChannel = mRandomAccessFile.getChannel();
            mMappedByteBuffer = mFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, mSendFileInfo.size); //将文件映射到内存

            return TRANS_START;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return TRANS_OVER;
    }

    public void notifySender(int cmd) {
        //ParamTCPNotify notify = new ParamTCPNotify(mNeighbor, obj);
        if (mWorkHandler != null)
            mWorkHandler.send2Handler(cmd, Constant.Src.SEND_TCP_THREAD,
                    Constant.Recipient.FILE_SEND, mNeighbor);
    }

//    public void quit() {
//        if (mCurrentThread != null && mCurrentThread.isAlive())
//            mCurrentThread.interrupt();
//        else
//            release();
//    }

    private synchronized void release() {
        if (mSocketChannel != null) {
            try {
                mSocketChannel.socket().close();
                mSocketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mRandomAccessFile != null) {
            try {
                mRandomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mFileChannel != null) {
            try {
                mFileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
