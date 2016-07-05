package com.ape.transfer.fragment.loader;

import android.content.Context;

import com.ape.transfer.p2p.p2pentity.P2PFileInfo;
import com.ape.transfer.provider.TaskHistory;

import java.util.ArrayList;

/**
 * Created by android on 16-7-5.
 */
public class TaskLoader extends BaseLoader<P2PFileInfo> {
    private int mDirection;

    public TaskLoader(Context context, int direction) {
        super(context);
        mDirection = direction;
    }

    @Override
    public Result loadInBackground() {
        ArrayList<P2PFileInfo> fileInfos = TaskHistory.getInstance().getAllFileInfos(mDirection == 0);

        Result result = new Result();
        result.lists = fileInfos;
        return result;
    }
}
