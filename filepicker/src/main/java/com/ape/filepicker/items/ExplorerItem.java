package com.ape.filepicker.items;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ape.filepicker.ExploreItemViewHolder;
import com.ape.filepicker.R;
import com.ape.filepicker.util.FileTypes;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;


/**
 * Created by kiolt_000 on 14/09/2014.
 */
public class ExplorerItem {
    protected final File file;
    private String fileType = null;
    private boolean selected = false;
    private boolean enabled = true;
    private int imageId = 0;

    public ExplorerItem(File file) {
        this.file = file;
    }

    public ExplorerItem(File file, boolean selected) {
        this(file);
        this.selected = selected;
    }

    public ExplorerItem(File file, boolean selected, String fileType) {
        this(file, selected);
        this.fileType = fileType;
    }

    public ExplorerItem(String path) {
        this(new File(path), false);
    }

    public ExplorerItem(File file, boolean selected, String fileType, int imageId, boolean enabled) {
        this.file = file;
        this.selected = selected;
        this.fileType = fileType;
        this.imageId = imageId;
        this.enabled = enabled;
    }

    public static void loadIcon(String path, @NonNull ImageView image) {
        Glide.with(image.getContext())
                .load(path)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(image);
    }

    public String getTitle() {
        return file.getName();
    }

    public String getSubtitle(Context context) {
        return null;
    }

    public int getImage() {
        return imageId;
    }

    public String getPath() {
        return file.getPath();
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public File getFile() {
        return file;
    }

    public String getFileType() {
        return fileType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Deprecated
    public void bindImage(View itemView) {
        ImageView holder = (ImageView) itemView.findViewById(R.id.image);
        holder.setScaleType(ImageView.ScaleType.CENTER);
        if (imageId != 0) {
            itemView.findViewById(R.id.type).setVisibility(View.INVISIBLE);
            holder.setVisibility(View.VISIBLE);
            holder.setImageResource(imageId);
        } else {
            holder.setImageResource(R.drawable.picker_file);
            TextView formatHolder = (TextView) itemView.findViewById(R.id.type);
            if (formatHolder != null) {
                formatHolder.setVisibility(View.VISIBLE);
                formatHolder.setText(fileType);
            }
        }
    }

    @Deprecated
    public void bindData(View itemView) {

    }

    public void bindImage(ExploreItemViewHolder holder) {
        if (!TextUtils.isEmpty(fileType)
                && (FileTypes.getType(fileType) == FileTypes.TYPE_PICTURE
                || FileTypes.getType(fileType) == FileTypes.TYPE_VIDEO)) {
            loadIcon(file.getAbsolutePath(), holder.getIconView());
            holder.setType("");
            return;
        }
        if (imageId != 0) {
            holder.setIcon(imageId);
            holder.setType("");
        } else {
            holder.setIcon(R.drawable.picker_file);
            holder.setType(fileType);
        }
    }

    public Long getLastModified() {
        return file.lastModified();
    }

    public void bindData(ExploreItemViewHolder holder) {
        holder.setTitle(getTitle());
        holder.setSubtitle(getSubtitle(holder.getContext()));
        holder.setSelected(selected);
        holder.enableDivider();
    }
}