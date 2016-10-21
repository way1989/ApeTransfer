package com.ape.p2p.core.receive;


import android.util.Log;

import com.ape.p2p.bean.P2PFileInfo;
import com.ape.p2p.core.communicate.P2PWorkHandler;
import com.ape.p2p.util.P2PConstant;
import com.ape.p2p.util.Util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by way on 2016/10/21.
 * 接收端的线程实现
 */
public class ReceiveTask extends Thread {
    private static final String TAG = "ReceiveTask";
    private static final byte[] READ_BUFFER = new byte[512];
    private String sendIp;
    private Socket socket;
    private boolean finished = false;
    private BufferedOutputStream bufferedOutputStream;
    private BufferedInputStream bufferedInputStream;
    //private P2PWorkHandler p2PHandler;
    private Receiver receiver;

    public ReceiveTask(P2PWorkHandler handler, Receiver receiver) {
        //this.p2PHandler = handler;
        this.receiver = receiver;
        this.sendIp = receiver.neighbor.ip;
    }

    @Override
    public void run() {
        loop:
        for (int i = 0; i < receiver.files.length; i++) {
            if (isInterrupted())
                break;
            try {
                socket = new Socket(sendIp, P2PConstant.PORT);
                notifyReceiver(P2PConstant.CommandNum.RECEIVE_TCP_ESTABLISHED, null);

                P2PFileInfo fileInfo = receiver.files[i];

                Log.d(TAG, "prepare to receive file:" + fileInfo.name + "; files size = "
                        + receiver.files.length);

                String path = Util.getSavePath(fileInfo.type);
                File fileDir = new File(path);
                if (!fileDir.exists())
                    fileDir.mkdirs();

                File receiveFile = new File(fileDir, fileInfo.name);
                if (receiveFile.exists())
                    receiveFile.delete();

                bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(
                        receiveFile));

                long total = 0L;
                int len = 0;
                int lastPercent = 0, percent = 0;
                bufferedInputStream = new BufferedInputStream(socket.getInputStream());
                while ((len = bufferedInputStream.read(READ_BUFFER)) != -1) {
                    if (isInterrupted()) {
                        receiveFile.delete();
                        break loop;
                    }
                    bufferedOutputStream.write(READ_BUFFER, 0, len);

                    total += len;
                    percent = (int) (((float) total / fileInfo.size) * 100);
                    fileInfo.position = total;//add by liweiping
                    if (percent - lastPercent > 1 || percent == 100) {
                        lastPercent = percent;
                        fileInfo.setPercent(percent);
                        notifyReceiver(P2PConstant.CommandNum.RECEIVE_PERCENT, fileInfo);
                    }

                    if (total >= fileInfo.size) {
                        Log.d(TAG, "total > file info size");
                        break;
                    }
                } // end of while

                receiveFile = null;
                fileInfo.setPercent(100);
                notifyReceiver(P2PConstant.CommandNum.RECEIVE_PERCENT, fileInfo);

                Log.d(TAG, "receive file " + fileInfo.name + " success");

                socket.close();

                if (i == receiver.files.length - 1) {
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
//            if (p2PHandler != null)
//                p2PHandler.send2Handler(cmd, P2PConstant.Src.RECEIVE_TCP_THREAD,
//                        P2PConstant.Recipient.FILE_RECEIVE, obj);
        }
    }
}
