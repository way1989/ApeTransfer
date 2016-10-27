package com.ape.transfer.model;

import java.util.ArrayList;

/**
 * Created by android on 16-7-4.
 */
public class FileEvent {
    private ArrayList<FileItem> mFileItems;

    public FileEvent(ArrayList<FileItem> fileItems) {
        mFileItems = fileItems;
    }

    public ArrayList<FileItem> getFileItemList() {
        return mFileItems;
    }
}
