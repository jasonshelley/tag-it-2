package com.jso.tagit2.utils;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by jshelley on 22/03/2017.
 */

public class ImageAsyncLoaderTag {
    public String path;
    public ImageView imageView;
    public Bitmap bmp;

    public ImageAsyncLoaderTag(String path, ImageView imageView) {
        this.path = path;
        this.imageView = imageView;
    }
}
