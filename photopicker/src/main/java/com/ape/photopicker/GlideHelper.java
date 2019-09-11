package com.ape.photopicker;

import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class GlideHelper {

    public static void loadResource(String path, @NonNull ImageView image) {
        Glide.with(image.getContext())
                .load(path)
                .error(R.drawable.image_not_exist)
                .placeholder(R.drawable.ic_default_image)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .fitCenter()
                .into(image);
    }

}
