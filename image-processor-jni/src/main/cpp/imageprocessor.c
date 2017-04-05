#ifdef _cplusplus

extern "C" {

#endif

#include <jni.h>
#include <stdlib.h>
#include "image-processor.h"
#include "structs.h"

jint _span, _width, _height;

JNIEXPORT void JNICALL
Java_com_jso_tagit2_imageprocessing_ImageProcessor_processImage(JNIEnv *env, jobject instance,
                                                                jbyteArray buffer_, jint width,
                                                                jint height, jint span) {
    _span = span;
    _width = width;
    _height = height;

    jbyte *buffer = (*env)->GetByteArrayElements(env, buffer_, 0);
    int length = (*env)->GetArrayLength(env, buffer_);
    jbyte *start = buffer;
    jbyte *end = start + width; // pointer to the jbyte AFTER the last jbyte
    jbyte *cur = start;
    jbyte *outdata = malloc(length);
    // let's segment our image into 16ths and do our calcs within each block
    double dx = 32;
    double dy = 32;
    double btopx = 0, btopy = 0; // block x and y
    for (btopy = 0; btopy < height; btopy += dy) {
        for (btopx = 0; btopx < width; btopx += dx) {
            // first get the min/max within the block
            struct MINMAX mm = calcMinMax((int) btopx, (int) btopy,
                                          start + (int) btopy * span + (int) btopx,
                                          (int) dx, (int) dy);
            int threshold = (int) (mm.min + (mm.max - mm.min) / 2.0);
            // use a factor to account for black being washed out

            int endx = (int) (btopx + dx * 3);
            if (endx > width)
                endx = width;
            int endy = (int) (btopy + dy);
            if (endy > height)
                endy = height;
            // now apply that threshold
            for (int by = (int) btopy; by < endy; by++) {
                for (int bx = (int) btopx; bx < endx; bx++) {
                    jbyte *y = start + by * span + bx;
                    int gray = (int)*y;
                    jbyte *rout = outdata + by * span + bx;
                    jbyte val = 0;
                    if (mm.min == mm.max)
                        val = (jbyte) mm.min;
                    else
                        val = (jbyte) (gray > threshold ? 127 : 0);
                    *rout = val;
                }
            }
        }
    }

    memcpy(buffer, outdata, length);
    free(outdata);
//    (*env)->ReleaseByteArrayElements(env, buffer_, buffer, 0);
}

struct MINMAX
calcMinMax(int startx, int starty, jbyte *input, int dx, int dy) {
    struct MINMAX mm;
    mm.min = -128;   // stupid java bytes
    mm.max = 127;

    int endx = startx + dx;
    if (endx > _width)
        endx = _width;

    int endy = starty + dy;
    if (endy > _height)
        endy = _height;

    jbyte * startrow = input;
    jbyte * cur = input;
    for (int y = starty; y < endy; y++) {
        for (int x = startx; x < endx; x++) {
            int gray = (int) *cur++;

            if (gray > mm.max) mm.max = gray;
            if (gray < mm.min) mm.min = gray;
        }
        startrow += _span;
        cur = startrow;
    }

    return mm;
}

#ifdef _cplusplus

}

#endif
