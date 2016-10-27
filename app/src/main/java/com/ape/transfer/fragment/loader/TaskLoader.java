package com.ape.transfer.fragment.loader;

import android.content.Context;

import com.ape.transfer.p2p.beans.TransferFile;
import com.ape.transfer.provider.TaskHistory;

import java.util.ArrayList;

/**
 * Created by android on 16-7-5.
 */
public class TaskLoader extends BaseLoader<TransferFile> {
    private int mDirection;

    public TaskLoader(Context context, int direction) {
        super(context);
        mDirection = direction;
    }

    @Override
    public Result loadInBackground() {
        ArrayList<TransferFile> fileInfos = TaskHistory.getInstance().getAllFileInfos(mDirection == 0);

        Result result = new Result();
        result.lists = fileInfos;
        return result;
    }
}
