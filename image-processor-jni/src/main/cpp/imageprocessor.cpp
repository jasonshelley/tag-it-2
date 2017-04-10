
#include <android/native_window_jni.h>
#include <android/log.h>
#include <math.h>
#include <time.h>
#include "imageprocessor.h"
#include "structs.h"

static double now_ms(void);

jint _span, _width, _height;

JNIEXPORT jint JNICALL
Java_com_jso_tagit2_imageprocessing_ImageProcessor_processImage(JNIEnv *env, jobject instance,
                                                                jbyteArray buffer_, jint width,
                                                                jint height, jint span, jobject outputSurface) {
    _span = span;
    _width = width;
    _height = height;

    long starttime = now_ms();

    ANativeWindow * win = ANativeWindow_fromSurface(env, outputSurface);
    ANativeWindow_setBuffersGeometry(win, width, height, WINDOW_FORMAT_RGBA_8888);
    ANativeWindow_Buffer winbuf;
    if (ANativeWindow_lock(win, &winbuf, NULL) != 0) {
        ANativeWindow_release(win);
        return 0;
    }

    if (winbuf.format != WINDOW_FORMAT_RGBA_8888) {
        ANativeWindow_unlockAndPost(win);
        ANativeWindow_release(win);
    }

    long millis = now_ms() - starttime;
    __android_log_print(ANDROID_LOG_DEBUG, "FishMeasurement", "Got native window locked %ld", millis);

    double fishwidth = 0;

    int outwidth = winbuf.width;    // see format check above
    int outheight = winbuf.height;
    int outstride = winbuf.stride * 4;
    int outlength = outstride * winbuf.height;

    uint8_t *buffer = reinterpret_cast<uint8_t *>(env->GetByteArrayElements(buffer_, 0));
    int length = env->GetArrayLength(buffer_);
    uint8_t *start = buffer;
    uint8_t *end = start + span * height;
    uint8_t *outdata = (uint8_t *)winbuf.bits;

    int16_t * gradient = (int16_t *)malloc(length * sizeof(int16_t));
    int16_t * gradienth = (int16_t *)malloc(length * sizeof(int16_t));
    int16_t * gradientv = (int16_t *)malloc(length * sizeof(int16_t));

    millis = now_ms() - starttime;
    __android_log_print(ANDROID_LOG_DEBUG, "FishMeasurement", "allocated buffers %ld", millis);

    // let's segment our image into 16ths and do our calcs within each block
    double dx = width;
    double dy = height;
    double btopx = 0, btopy = 0; // block x and y
    MINMAX mm;
    float threshold;
    int transMatrix[] = {-1, -2, 0, 2, 1};
    int offset = 2;
    int tp = 0; // transMatrixPos

    // let's have a go at edge detection rather than binarization
    int x, y;
    int16_t *pout = gradient + offset;
    int res, m;

    int gthresh = 64;

    uint8_t *pin;

    // horizontal scan
    // detect gradients
    for (y = offset; y < height - offset; y++) {
        for (x = offset; x < width - offset; x++) {
            pin = start + y * span + x;
            pout = gradient + y * span + x;
            res = 0;
            for (m = -2; m <= 2; m++)
                res += *(pin + m) * transMatrix[m + 2];
            *pout = res;
        }
    }
    // leave only peaks
    int16_t * gin;
    for (y = offset; y < height - offset; y++) {
        for (x = offset; x < width - offset; x++) {
            gin = gradient + y * span + x;
            pout = gradienth + y * span + x;
            if (*gin >= 0) {
                if (*gin > *(gin - 1) && *gin > *(gin + 1) && *gin > gthresh)
                    *pout = 255;
                else
                    *pout = 0;
            } else {
                if (*gin < *(gin - 1) && *gin < *(gin + 1) && *gin < -gthresh)
                    *pout = -255;
                else
                    *pout = 0;
            }
        }
    }
    // vertical scan
    // detect gradients
    for (x = offset; x < width - offset; x++) {
        for (y = offset; y < height - offset; y++) {
            pin = start + y * span + x;
            pout = gradient + y * span + x;
            res = 0;
            for (m = -2; m <= 2; m++)
                res += *(pin + m * span) * transMatrix[m + 2];
            *pout = res;
        }
    }
    // leave only peaks
    for (x = offset; x < width - offset; x++) {
        for (y = offset; y < height - offset; y++) {
            gin = gradient + y * span + x;
            pout = gradientv + y * span + x;
            if (*gin >= 0) {
                if (*gin > *(gin - span) && *gin > *(gin + span) && *gin > gthresh)
                    *pout = 255;
                else
                    *pout = 0;
            } else {
                if (*gin < *(gin - span) && *gin < *(gin + span) && *gin < -gthresh)
                    *pout = -255;
                else
                    *pout = 0;
            }
        }
    }

    millis = now_ms() - starttime;
    __android_log_print(ANDROID_LOG_DEBUG, "FishMeasurement", "calculate gradients %ld", millis);

    uint8_t * o, * b, *bend;
    b = buffer;
    int16_t  *gh = gradienth;
    int16_t  *gv = gradientv;
    o = outdata;
    int16_t * gend = gh + height * span;
    while (gh < gend) {
        if (*gh == 0 || *gv == 0) {
            *o = o[1] = o[2] = o[3] = 0;
        } else if (*gh == 255 || *gv == 255) {
            *o = 255;
            o[1] = o[2] = 0;
            o[3] = 255;
        } else {
            o[1] = 255;
            o[0] = o[2] = 0;
            o[3] = 255;
        }

        b++;
        gh++;
        gv++;
        o += 4;
    }
    millis = now_ms() - starttime;
    __android_log_print(ANDROID_LOG_DEBUG, "FishMeasurement", "drawn gradients %ld", millis);

    // so now we have our binary image, let's search for the location patterns
    // the magic ration is 1,1,3,1,1 starting with dark
    POINT cx[1000];
    int cxpos = 0;

    BLOCK blocks[1000];
    int blockpos = 0;

    int count = 0;
    int countpos = 0;
    int16_t * startline = gradienth;
    int16_t * gcur;
    int16_t prev = 0;
    int templates[] = {1, 2, 4, 5};
    float fratios[5];
    float fuzzy1 = 0.3f;
    float fuzzy3 = 3 * fuzzy1;
    // horizontal search
    for (y = 0; y < height; y++)
    {
        blockpos = 0;
        blocks[blockpos].start = 0;
        blocks[blockpos].end = 0;
        gcur = startline;
        for (x = 0; x < width; x++)
        {
            if (*gcur != prev)
            {
                if (*gcur == 255) // we've gone from dark to light
                {
                    int blockcount = blockpos + 1;
                    // look back at the counts to see if we can find the pattern
                    if (blockcount >= 5) // minimum for the location pattern
                    {
                        for (int t = 0; t < 4; t++)
                        {
                            // start by using the first one as the template
                            int blockwidth = blockWidth(blocks[blockcount - templates[t]]);
                            if (blockwidth == 0)
                                continue;
                            for (int i = 0; i < 5; i++)
                            {
                                fratios[i] = blockWidth(blocks[blockcount - 5 + i]) / (float)blockwidth;
                            }
                            int midx = (blocks[blockcount - 3].start + blockWidth(blocks[blockcount - 3])/2);
                            if (fratios[0] > 1 - fuzzy1 && fratios[0] < 1 + fuzzy1 &&
                                fratios[1] > 1 - fuzzy1 && fratios[1] < 1 + fuzzy1 &&
                                fratios[2] > 3 - fuzzy3 && fratios[2] < 3 + fuzzy3 &&
                                fratios[3] > 1 - fuzzy1 && fratios[3] < 1 + fuzzy1 &&
                                fratios[4] > 1 - fuzzy1 && fratios[4] < 1 + fuzzy1)
                            {
                                // Yay! we've found one
                                int midpoint = y*outstride + midx*4;

                                cx[++cxpos].x = midx;
                                cx[cxpos].y = y;
                                *(outdata + midpoint) = 255;
                                *(outdata + midpoint + 1) = 0;
                                *(outdata + midpoint + 2) = 255;
                                *(outdata + midpoint + 2) = 0;
                                break;
                            }
                        }
                    }
                }
                if (blockWidth(blocks[blockpos]) > 3)
                    blockpos++;
                blocks[blockpos].start = x;
                blocks[blockpos].end = x;
            } else if (*gcur == 0) {
                blocks[blockpos].end++;
            }
            prev = *gcur;
            gcur++;
        }
        countpos = 0;
        startline += span;
    }
    // vertical search
    int16_t * startcol = gradientv;
    POINT* cy = new POINT[1000];
    int cypos = 0;
    prev = 0;
    for (x = 0; x < width; x++)
    {
        blockpos = 0;
        blocks[blockpos].start = 0;
        blocks[blockpos].end = 0;
        gcur = startcol;
        for (y = 0; y < height; y++)
        {
            if (*gcur != prev)
            {
                if (*gcur == 255) // we've gone from dark to light
                {
                    int blockcount = blockpos + 1;
                    // look back at the counts to see if we can find the pattern
                    if (blockcount >= 5) // minimum for the location pattern
                    {
                        // start by using the first one as the template
                        for (int t = 0; t < 4; t++)
                        {
                            int blockwidth = blockWidth(blocks[blockcount - templates[t]]);
                            if (blockwidth == 0)
                                continue;
                            for (int i = 0; i < 5; i++)
                            {
                                fratios[i] = blockWidth(blocks[blockcount - 5 + i]) / (float)blockwidth;
                            }
                            int midy = (blocks[blockcount - 3].start + blockWidth(blocks[blockcount - 3])/2);
                            if (fratios[0] > 1 - fuzzy1 && fratios[0] < 1 + fuzzy1 &&
                                fratios[1] > 1 - fuzzy1 && fratios[1] < 1 + fuzzy1 &&
                                fratios[2] > 3 - fuzzy3 && fratios[2] < 3 + fuzzy3 &&
                                fratios[3] > 1 - fuzzy1 && fratios[3] < 1 + fuzzy1 &&
                                fratios[4] > 1 - fuzzy1 && fratios[4] < 1 + fuzzy1)
                            {
                                int midpoint = midy*outstride + x*4;

                                // Yay! we've found one
                                cy[++cypos].x = x;
                                cy[cypos].y = midy;
                                *(outdata + midpoint) = 255;
                                *(outdata + midpoint + 1) = 0;
                                *(outdata + midpoint + 2) = 255;
                                *(outdata + midpoint + 2) = 0;
                                break;
                            }
                        }
                    }
                }
                if (blockWidth(blocks[blockpos]) > 3)
                    blockpos++;
                blocks[blockpos].start = y;
                blocks[blockpos].end = y;
            }
            blocks[blockpos].end++;
            prev = *gcur;
            gcur += span;
        }
        countpos = 0;
        startcol++;
    }

    millis = now_ms() - starttime;
    __android_log_print(ANDROID_LOG_DEBUG, "FishMeasurement", "found blocks %ld", millis);

    // now we (hopefully) have a bunch of centre points, let's figure out where the centres actually are
    // to find lines, we'll take all the points that are within 5 pixels of each other to be as generous as possible
    CENTRE acx[1000];
    int acxpos = 0;

    int maxwidth = 5;
    int maxheight = 5;

    int cxcount = cxpos + 1;
    int cycount = cypos + 1;
    // create two 'isdeleted' arrays
    bool isxd[cxcount];
    bool isyd[cxcount];
    memset(isxd, 0, sizeof(isxd));
    memset(isyd, 0, sizeof(isyd));

    CENTRE curCentre;
    bool curCentreValid = false;
    bool found;

    POINT locationCentres[20];
    int lcpos = 0;

    int arrayzero = 0;  // first array element that isn't deleted

    dx = dy = 2;

    if (cxpos > 0 && cypos > 0)
    {
        while (cxcount > 0)
        {
            for (arrayzero = 0; isxd[arrayzero]; arrayzero++);

            if (curCentreValid && curCentre.end.y - curCentre.start.y > 4) {
                acx[acxpos].end = curCentre.end;
                acx[acxpos].start = curCentre.start;
                acxpos++;

                int midx = centreMid(curCentre).x;
                int midy = centreMid(curCentre).y;

                // we now have a vertical line indicating a center
                // let's see if there's a y centre somewhere around the middle of our x
                for (y =0; y <= cypos; y++) {
                    if (cy[y].x > midx - dx && cy[y].x < midx + dx  &&
                        cy[y].y > midy - dy && cy[y].y < midy + dy) {
                        locationCentres[lcpos].x = midx;
                        locationCentres[lcpos].y = midy;
                        lcpos++;
                        break;
                    }
                }
            }

            curCentreValid = true;
            curCentre.start = cx[arrayzero];
            curCentre.end = cx[arrayzero];

            bool found = false;
            do
            {
                found = false;
                for (int i = arrayzero + 1; i < cxpos + 1; i++)
                {
                    if (isxd[i])
                        continue;

                    if (cx[i].x  > curCentre.start.x - maxwidth && cx[i].x < curCentre.end.x + maxwidth &&
                        cx[i].y > curCentre.start.y - maxwidth && cx[i].y < curCentre.end.y + maxwidth)
                    {
                        if (cx[i].y < curCentre.start.y)
                            curCentre.start = cx[i];
                        if (cx[i].y > curCentre.end.y)
                            curCentre.end = cx[i];
                        isxd[i] = true;
                        cxcount--;
                        found = true;
                    }
                }
            } while (found);
            isxd[arrayzero] = true;
            cxcount--;
        }
        if (curCentreValid && curCentre.end.y - curCentre.start.y > 4) {
            acx[acxpos].end = curCentre.end;
            acx[acxpos].start = curCentre.start;
            acxpos++;
            int midx = centreMid(curCentre).x;
            int midy = centreMid(curCentre).y;

            // we now have a vertical line indicating a center
            // let's see if there's a y centre somewhere around the middle of our x
            for (y =0; y <= cypos; y++) {
                if (cy[y].x > midx - dx && cy[y].x < midx + dx  &&
                    cy[y].y > midy - dy && cy[y].y < midy + dy) {
                    locationCentres[lcpos].x = midx;
                    locationCentres[lcpos].y = midy;
                    lcpos++;
                }
            }
        }
    }

    millis = now_ms() - starttime;
    __android_log_print(ANDROID_LOG_DEBUG, "FishMeasurement", "found centres %ld", millis);

    // first let's aggregate any close by centres
    bool iscd[lcpos + 1];
    memset(iscd, 0, sizeof(iscd));

    for (int lc = 0; lc < lcpos; lc++) {
        if (locationCentres[lc].x == 0 && locationCentres[lc].y == 0)
        {
            iscd[lc] = true;
            continue;
        }
        for (int lcd = lc + 1; lcd < lcpos; lcd++) {
            if (iscd[lcd])
                continue;
            if (locationCentres[lc].x - locationCentres[lcd].x < 3 && locationCentres[lc].x - locationCentres[lcd].x > -3 &&
                locationCentres[lc].y - locationCentres[lcd].y < 3 && locationCentres[lc].y - locationCentres[lcd].y > -3) {
                iscd[lcd] = true;
            }
        }
    }
    POINT finalCentres[20];
    int fcpos = 0;
    for (int lc = 0; lc < lcpos; lc++) {
        if (!iscd[lc])
            finalCentres[fcpos++] = locationCentres[lc];
    }

    if (fcpos == 3) {
        // alrighty then... if we've located 3 location centres, let's calculate the distortion

        int right = 0;
        int bottom = 0;
        int topleft = 0;
        for (int lc = 0; lc < 3; lc++) {
            if (finalCentres[lc].x > finalCentres[right].x) right = lc;
            if (finalCentres[lc].y > finalCentres[bottom].y) bottom = lc;
        }
        topleft = 3 - right - bottom;

        int lc = topleft;
        for (x = -10; x < 10; x++) {
            for (y = -10; y < 10; y++) {
                *(outdata + (finalCentres[lc].y + y) * outstride +
                  (finalCentres[lc].x + x) * 4) = 255;
                *(outdata + (finalCentres[lc].y + y) * outstride +
                  (finalCentres[lc].x + x) * 4 + 1) = 0;
                *(outdata + (finalCentres[lc].y + y) * outstride +
                  (finalCentres[lc].x + x) * 4 + 2) = 0;
                *(outdata + (finalCentres[lc].y + y) * outstride +
                  (finalCentres[lc].x + x) * 4 + 3) = 255;
            }
        }
        lc = right;
        for (x = -10; x < 10; x++) {
            for (y = -10; y < 10; y++) {
                *(outdata + (finalCentres[lc].y + y) * outstride +
                  (finalCentres[lc].x + x) * 4) = 0;
                *(outdata + (finalCentres[lc].y + y) * outstride +
                  (finalCentres[lc].x + x) * 4 + 1) = 255;
                *(outdata + (finalCentres[lc].y + y) * outstride +
                  (finalCentres[lc].x + x) * 4 + 2) = 0;
                *(outdata + (finalCentres[lc].y + y) * outstride +
                  (finalCentres[lc].x + x) * 4 + 3) = 255;
            }
        }
        lc = bottom;
        for (x = -10; x < 10; x++) {
            for (y = -10; y < 10; y++) {
                *(outdata + (finalCentres[lc].y + y) * outstride +
                  (finalCentres[lc].x + x) * 4) = 0;
                *(outdata + (finalCentres[lc].y + y) * outstride +
                  (finalCentres[lc].x + x) * 4 + 1) = 0;
                *(outdata + (finalCentres[lc].y + y) * outstride +
                  (finalCentres[lc].x + x) * 4 + 2) = 255;
                *(outdata + (finalCentres[lc].y + y) * outstride +
                  (finalCentres[lc].x + x) * 4 + 3) = 255;
            }
        }

        double alpha = tan((double)(finalCentres[right].y - finalCentres[topleft].y)/(finalCentres[right].x - finalCentres[topleft].x));
        double beta = tan((double)(finalCentres[bottom].x - finalCentres[topleft].x)/(finalCentres[bottom].y - finalCentres[topleft].y));

        for (x = finalCentres[topleft].x; x < finalCentres[right].x; x++) {
            y = finalCentres[topleft].y + (int)(atan(alpha)* (x - finalCentres[topleft].x));
            *(outdata + y * outstride + x * 4) = 0;
            *(outdata + y * outstride + x * 4 + 1) = 255;
            *(outdata + y * outstride + x * 4 + 2) = 255;
            *(outdata + y * outstride + x * 4 + 3) = 255;
        }
        for (y = finalCentres[topleft].y; y < finalCentres[bottom].y; y++) {
            x = finalCentres[topleft].x + (int)(atan(beta)* (y - finalCentres[topleft].y));
            *(outdata + y * outstride + x * 4) = 255;
            *(outdata + y * outstride + x * 4 + 1) = 0;
            *(outdata + y * outstride + x * 4 + 2) = 255;
            *(outdata + y * outstride + x * 4 + 3) = 255;
        }

        // looking good so far
        // now let's look for the fish
        
        // first attempt - scan vertically
//        threshold = (int) (mm.min + (mm.max - mm.min) * 3.0 / 4.0); // emphasize the dark for finding the fish
        int minheight = 5; // a column will be marked as crossed if there is a minimum of <minheight> dark pixels
        int crossingheight;
        int firstx = 0;
        int lastx = 0;
        int firsty = 0;
        int lasty = 0;
        for (x = finalCentres[topleft].x + 100; x < finalCentres[right].x - 100; x++) {
            crossingheight = 0;
            for (y = finalCentres[topleft].y; y < finalCentres[bottom].y; y++) {  // 50 for the location markers
                int dx = (x - finalCentres[topleft].x);
                int dy = (y - finalCentres[topleft].y);
                int transx = finalCentres[topleft].x + dx * cos(alpha) - dy * sin(alpha);
                int transy = finalCentres[topleft].y + dx * sin(alpha) + dy * cos(alpha);
                if (transx >= width) transx = width - 1;
                if (transy >= height) transy = height - 1;
                if (transx < 0) transx = 0;
                if (transy < 0) transy = 0;

                if (gradientv[transy * span + transx] == 0) {
                    crossingheight++;
                } else {
                    if (crossingheight > minheight) {
                        if (firstx == 0)
                            firstx = x;
                        lastx = x;    // continually update
                        if (y < firsty || firsty == 0)
                            firsty = y;
                        break;
                    }
                    crossingheight = 0;
                }
                if (gradienth[transy * span + transx] == -255) {
                    if (y < firsty || firsty == 0)
                        firsty = y;
                }
            }
            crossingheight = 0;
            for (y = finalCentres[bottom].y; y > finalCentres[topleft].y; y--) {
                int dx = (x - finalCentres[topleft].x);
                int dy = (y - finalCentres[topleft].y);
                int transx = finalCentres[topleft].x + dx * cos(alpha) - dy * sin(alpha);
                int transy = finalCentres[topleft].y + dx * sin(alpha) + dy * cos(alpha);
                if (transx >= width) transx = width - 1;
                if (transy >= height) transy = height - 1;
                if (transx < 0) transx = 0;
                if (transy < 0) transy = 0;

                if (gradienth[transy * span + transx] == 255) {
                    if (y > lasty)
                        lasty = y;
                    break;
                }
            }
        }

        firsty -= crossingheight;
        lasty += crossingheight;

        for (x = firstx; x <= lastx; x++) {
            for (y = firsty; y < lasty; y++) {
                int dx = (x - firstx);
                int dy = (y - firsty);
                int transx = firstx + dx * cos(alpha) - dy * sin(alpha);
                int transy = firsty + dx * sin(alpha) + dy * cos(alpha);
                if (transx >= width) transx = width - 1;
                if (transy >= height) transy = height - 1;
                if (transx < 0) transx = 0;
                if (transy < 0) transy = 0;

                outdata[transy * outstride + transx * 4] = 255;
                outdata[transy * outstride + transx * 4 + 3] = 255;

                outdata[transy * outstride + transx * 4 + 1] = *(buffer + y * span + x);
                outdata[transy * outstride + transx * 4 + 2] = *(buffer + y * span + x);
            }
        }

        int lengthpx = lastx - firstx;
        int framewidth = finalCentres[right].x - finalCentres[topleft].x;
        double fraction = (double)lengthpx / framewidth;

        double framewidthmm = 253;
        fishwidth = fraction * framewidthmm;

        __android_log_print(ANDROID_LOG_DEBUG, "FishMeasurement", "fish length: %.0fcm", fishwidth / 10.0);
    }
    millis = now_ms() - starttime;
    __android_log_print(ANDROID_LOG_DEBUG, "FishMeasurement", "done %ld", millis);


    free(gradient);
    free(gradienth);
    free(gradientv);

    ANativeWindow_unlockAndPost(win);
    ANativeWindow_release(win);

    return (int)(fishwidth / 10.0);
}

MINMAX
calcMinMax(int startx, int starty, uint8_t *input, int dx, int dy) {
    MINMAX mm;
    mm.min = 0;   // stupid java bytes
    mm.max = 255;

    int endx = startx + dx;
    if (endx > _width)
        endx = _width;

    int endy = starty + dy;
    if (endy > _height)
        endy = _height;

    uint8_t * startrow = input;
    uint8_t * cur = input;
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

int blockWidth(BLOCK b) {
    return b.end - b.start;
}

POINT centreMid(CENTRE c) {
    POINT mid;

    mid.x = c.start.x + (c.end.x - c.start.x) / 2;
    mid.y = c.start.y + (c.end.y - c.start.y) / 2;

    return mid;
}

static double now_ms(void) {

    struct timespec res;
    clock_gettime(CLOCK_REALTIME, &res);
    return 1000.0 * res.tv_sec + (double) res.tv_nsec / 1e6;

}