package com.jso.tagit2.imageprocessing;

/**
 * Created by jshelley on 5/04/2017.
 */

public abstract class ImageProcessor {

    static {
        System.loadLibrary("image-processor-jni");
    }
    public static native void processImage(byte[] buffer, int width, int height, int span);
}
