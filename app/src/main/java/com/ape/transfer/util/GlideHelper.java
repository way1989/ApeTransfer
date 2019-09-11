package com.ape.transfer.util;

import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.ape.transfer.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class GlideHelper {

    public static void loadResource(String path, @NonNull ImageView image) {
        Glide.with(image.getContext())
                .load(path)
                .error(R.drawable.his_icon_image)
                .placeholder(R.drawable.his_icon_image)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .centerCrop()
                .into(image);
    }

    public static void loadCropResource(String path, @NonNull ImageView image) {
        Glide.with(image.getContext())
                .load(path)
                .error(R.drawable.his_icon_image)
                .placeholder(R.drawable.his_icon_image)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .fitCenter()
                .into(image);
    }

}
