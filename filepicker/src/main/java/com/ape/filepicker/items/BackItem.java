package com.ape.filepicker.items;


import com.ape.filepicker.ExploreItemViewHolder;
import com.ape.filepicker.R;

import java.io.File;


/**
 * Created by kiolt_000 on 01/10/2014.
 */
public class BackItem extends ExplorerItem {

    public BackItem() {
        super(new File(""), false, "..", R.drawable.picker_folder, true);
    }

    @Override
    public String getTitle() {
        return "..";
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void bindData(ExploreItemViewHolder holder) {
        holder.setTitle(getTitle());
        holder.disableSubtitle();
        holder.enableDivider();
    }
}
