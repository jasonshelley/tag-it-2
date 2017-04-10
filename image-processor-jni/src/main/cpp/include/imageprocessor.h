//
// Created by jshelley on 5/04/2017.
//

#ifndef TAGIT2_IMAGE_PROCESSOR_H
#define TAGIT2_IMAGE_PROCESSOR_H

#include <stdlib.h>
#include "structs.h"

extern "C" {

    #include <jni.h>

    MINMAX calcMinMax(int startx, int starty, uint8_t *input, int dx, int dy);
    JNIEXPORT jint JNICALL
    Java_com_jso_tagit2_imageprocessing_ImageProcessor_processImage(JNIEnv *env, jobject instance,
                                                                    jbyteArray buffer_, jint width,
                                                                    jint height, jint span, jobject outputSurface);
}
#endif //TAGIT2_IMAGE_PROCESSOR_H
