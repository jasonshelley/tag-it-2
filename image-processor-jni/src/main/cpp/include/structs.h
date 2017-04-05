//
// Created by jshelley on 5/04/2017.
//

#ifndef TAGIT2_STRUCTS_H
#define TAGIT2_STRUCTS_H

struct BLOCK {
    int start;
    int end;
};

struct POINT {
    int x;
    int y;
};

struct CENTRE {
    struct POINT start;
    struct POINT end;
};

struct MINMAX {
    int min;
    int max;
};

struct MINMAX calcMinMax(int startx, int starty, jbyte* input, int dx, int dy);

#endif //TAGIT2_STRUCTS_H
