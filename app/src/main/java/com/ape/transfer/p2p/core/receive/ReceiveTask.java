package com.ape.transfer.p2p.core.receive;


import android.util.Log;

import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.p2p.beans.TransferFile;
import com.ape.transfer.p2p.beans.param.ParamTCPNotify;
import com.ape.transfer.p2p.core.P2PManager;
import com.ape.transfer.p2p.core.WorkHandler;
import com.ape.transfer.p2p.util.Constant;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by way on 2016/10/20.
 * 接收端的线程实现
 */
public class ReceiveTask extends Thread {
    private static final String TAG = "ReceiveTask";
    private Peer sendPeer;
    private Socket socket;
    private boolean finished = false;
    private BufferedOutputStream bufferedOutputStream;
    private BufferedInputStream bufferedInputStream;
    private byte[] READ_BUFFER = new byte[512];
    private WorkHandler p2PHandler;
    private Receiver receiver;

    public ReceiveTask(WorkHandler handler, Receiver receiver) {
        this.p2PHandler = handler;
        this.receiver = receiver;
        this.sendPeer = receiver.mNeighbor;
    }

    @Override
    public void run() {
        loop:
        for (int i = 0; i < receiver.mReceiveFileInfos.length; i++) {
            if (isInterrupted())
                break;
            try {
                socket = new Socket(sendPeer.ip, Constant.FILE_TRANSFER_PORT);
                notifyReceiver(Constant.Command.RECEIVE_TCP_ESTABLISHED, null);

                TransferFile fileInfo = receiver.mReceiveFileInfos[i];

                Log.d(TAG, "prepare to receive file:" + fileInfo.name + "; mReceiveFileInfos size = "
                        + receiver.mReceiveFileInfos.length);

                String path = P2PManager.getSavePath(fileInfo.type);
                if (fileInfo.type == Constant.TYPE.BACKUP) {
                    path += fileInfo.path.substring(fileInfo.path.indexOf(Constant.TYPE_NAME.BACKUP)
                            + Constant.TYPE_NAME.BACKUP.length(), fileInfo.path.lastIndexOf(File.separator));
                }
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
                long lastLen = 0L;
                final float update = fileInfo.size / 100.0f;
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
                    if (fileInfo.position - lastLen > update) {
                        lastLen = total;
                        notifyReceiver(Constant.Command.RECEIVE_PERCENT, new ParamTCPNotify(sendPeer, fileInfo));
                    }

                    if (total >= fileInfo.size) {
                        Log.d(TAG, "total > file info size");
                        break;
                    }
                } // end of while
                notifyReceiver(Constant.Command.RECEIVE_PERCENT, new ParamTCPNotify(sendPeer, fileInfo));

                Log.d(TAG, "receive file " + fileInfo.name + " success");

                socket.close();

                if (i == receiver.mReceiveFileInfos.length - 1) {
                    Log.d(TAG, "receive file over");
                    notifyReceiver(Constant.Command.RECEIVE_OVER, null);
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
        if (!finished && p2PHandler != null)
            p2PHandler.send2Handler(cmd, Constant.Src.RECEIVE_TCP_THREAD,
                    Constant.Recipient.FILE_RECEIVE, obj);
    }
}
