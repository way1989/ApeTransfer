package com.ape.transfer.p2p.p2pcore.receive;


import android.util.Log;

import com.ape.transfer.p2p.p2pconstant.P2PConstant;
import com.ape.transfer.p2p.p2pcore.P2PManager;
import com.ape.transfer.p2p.p2pcore.P2PWorkHandler;
import com.ape.transfer.p2p.p2pentity.P2PFileInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by 郭攀峰 on 2015/9/21.
 * 接收端的线程实现
 */
public class ReceiveTask extends Thread {
    private static final String TAG = "ReceiveTask";
    private String sendIp;
    private Socket socket;
    private boolean finished = false;
    private BufferedOutputStream bufferedOutputStream;
    private BufferedInputStream bufferedInputStream;
    private byte[] READ_BUFFER = new byte[512];
    private P2PWorkHandler p2PHandler;
    private Receiver receiver;

    public ReceiveTask(P2PWorkHandler handler, Receiver receiver) {
        this.p2PHandler = handler;
        this.receiver = receiver;
        this.sendIp = receiver.mNeighbor.ip;
    }

    @Override
    public void run() {
        loop:
        for (int i = 0; i < receiver.mReceiveFileInfos.length; i++) {
            if (isInterrupted())
                break;
            try {
                socket = new Socket(sendIp, P2PConstant.PORT);
                notifyReceiver(P2PConstant.CommandNum.RECEIVE_TCP_ESTABLISHED, null);

                P2PFileInfo fileInfo = receiver.mReceiveFileInfos[i];

                Log.d(TAG, "prepare to receive file:" + fileInfo.name + "; mReceiveFileInfos size = "
                        + receiver.mReceiveFileInfos.length);

                String path = P2PManager.getSavePath(fileInfo.type);
                File fileDir = new File(path);
                if (!fileDir.exists())
                    fileDir.mkdirs();

                File receiveFile = new File(fileDir, fileInfo.name);
                if (receiveFile.exists())
                    receiveFile.delete();

                bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(
                        receiveFile));

                long total = 0L;
                int len;
                long lasteLen = 0;
                final float update = fileInfo.size/ 100.0f;
                //int lastPercent = 0, percent;
                bufferedInputStream = new BufferedInputStream(socket.getInputStream());
                while ((len = bufferedInputStream.read(READ_BUFFER)) != -1) {
                    if (isInterrupted()) {
                        receiveFile.delete();
                        break loop;
                    }
                    bufferedOutputStream.write(READ_BUFFER, 0, len);

                    total += len;
                    //percent = (int) (((float) total / fileInfo.size) * 100);
                    fileInfo.position = total;//add by liweiping
                    if(fileInfo.position - lasteLen > update){
                        lasteLen = total;
                        notifyReceiver(P2PConstant.CommandNum.RECEIVE_PERCENT, fileInfo);
                    }
//                    if (percent - lastPercent > 1 || percent == 100) {
//                        lastPercent = percent;
//                        fileInfo.setPercent(percent);
//                        notifyReceiver(P2PConstant.CommandNum.RECEIVE_PERCENT, fileInfo);
//                    }

                    if (total >= fileInfo.size) {
                        Log.d(TAG, "total > file info size");
                        break;
                    }
                } // end of while
                //fileInfo.setPercent(100);
                notifyReceiver(P2PConstant.CommandNum.RECEIVE_PERCENT, fileInfo);

                Log.d(TAG, "receive file " + fileInfo.name + " success");

                socket.close();

                if (i == receiver.mReceiveFileInfos.length - 1) {
                    Log.d(TAG, "receive file over");
                    notifyReceiver(P2PConstant.CommandNum.RECEIVE_OVER, null);
                    finished = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                finished = true;
            } finally {
                release();
            }
        } // end of loop

        release();
    }

    private void release() {
        if (bufferedOutputStream != null) {
            try {
                bufferedOutputStream.close();
                bufferedOutputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (bufferedInputStream != null) {
            try {
                bufferedInputStream.close();
                bufferedInputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (socket != null) {
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyReceiver(int cmd, Object obj) {
        if (!finished) {
            if (p2PHandler != null)
                p2PHandler.send2Handler(cmd, P2PConstant.Src.RECEIVE_TCP_THREAD,
                        P2PConstant.Recipient.FILE_RECEIVE, obj);
        }
    }
}
