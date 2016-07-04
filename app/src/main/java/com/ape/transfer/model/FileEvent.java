package com.ape.transfer.model;

import java.util.ArrayList;

/**
 * Created by android on 16-7-4.
 */
public class FileEvent {
    private ArrayList<FileItem> mMsg;

    public FileEvent(ArrayList<FileItem> msg) {
        mMsg = msg;
    }

    public ArrayList<FileItem> getMsg() {
        return mMsg;
    }
}
