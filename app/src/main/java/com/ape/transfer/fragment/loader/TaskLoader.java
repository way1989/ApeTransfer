package com.ape.transfer.fragment.loader;

import android.content.Context;

import com.ape.transfer.model.HistoryTransfer;
import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.p2p.beans.TransferFile;
import com.ape.transfer.provider.DeviceHistory;
import com.ape.transfer.provider.TaskHistory;

import java.util.ArrayList;

/**
 * Created by android on 16-7-5.
 */
public class TaskLoader extends BaseLoader<HistoryTransfer> {
    private int mDirection;

    public TaskLoader(Context context, int direction) {
        super(context);
        mDirection = direction;
    }

    @Override
    public Result loadInBackground() {
        ArrayList<HistoryTransfer> fileInfos = getData();

        Result<HistoryTransfer> result = new Result<>();
        result.lists = fileInfos;
        return result;
    }

    private ArrayList<HistoryTransfer> getData() {
        ArrayList<HistoryTransfer> results = new ArrayList<>();
        ArrayList<TransferFile> fileInfos = TaskHistory.getInstance()
                .getAllFileInfos(mDirection == TransferFile.Direction.DIRECTION_SEND);
        for (TransferFile file : fileInfos) {
            Peer peer = DeviceHistory.getInstance().getDevice(file.wifiMac);
            results.add(new HistoryTransfer(peer, file));
        }
        return results;
    }
}
