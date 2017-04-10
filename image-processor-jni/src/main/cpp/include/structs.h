//
// Created by jshelley on 5/04/2017.
//

#ifndef TAGIT2_STRUCTS_H
#define TAGIT2_STRUCTS_H

typedef struct S_BLOCK {
    int start;
    int end;
} BLOCK;

typedef struct S_POINT {
    int x;
    int y;
} POINT;

typedef struct S_CENTRE {
    POINT start;
    POINT end;
} CENTRE;

typedef struct S_MINMAX {
    int min;
    int max;
} MINMAX;

int blockWidth(BLOCK b);
POINT centreMid(CENTRE c);

#endif //TAGIT2_STRUCTS_H
