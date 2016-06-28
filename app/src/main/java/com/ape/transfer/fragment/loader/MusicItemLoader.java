package com.ape.transfer.fragment.loader;

import android.content.Context;

import com.ape.transfer.model.MusicItem;

import java.util.ArrayList;

/**
 * Created by android on 16-6-28.
 */
public class MusicItemLoader extends
        BaseLoader<MusicItem> {


    public MusicItemLoader(Context context) {
        super(context);
    }

    @Override
    public Result loadInBackground() {
        final Context context = getContext();
        ArrayList<MusicItem> musicItems = MusicItem.getMusicItems(
                context);
        if (musicItems == null) {
            return new Result();
        }
        Result result = new Result();
        result.lists = musicItems;
        return result;
    }


}
