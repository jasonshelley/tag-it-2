package com.jso.tagit2.imageprocessing;

import android.view.Surface;

/**
 * Created by jshelley on 5/04/2017.
 */

public abstract class ImageProcessor {

    static {
        System.loadLibrary("image-processor-jni");
    }
    public static native int processImage(byte[] buffer, int width, int height, int span, Surface outputSurface);
}
