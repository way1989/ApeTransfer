package com.ape.transfer.fragment.loader;

import android.content.Context;

import com.ape.transfer.model.FileItem;
import com.ape.transfer.util.FileCategoryHelper;

import java.util.ArrayList;

/**
 * Created by android on 16-6-28.
 */
public class FileItemLoader extends BaseLoader<FileItem> {
    private FileCategoryHelper.FileCategory mFileCategory;

    public FileItemLoader(Context context, FileCategoryHelper.FileCategory fileCategory) {
        super(context);
        mFileCategory = fileCategory;
    }

    @Override
    public Result loadInBackground() {
        final Context context = getContext();
        ArrayList<FileItem> musicItems = FileItem.getMusicItems(context, mFileCategory);

        Result result = new Result();
        result.lists = musicItems;
        return result;
    }


}
