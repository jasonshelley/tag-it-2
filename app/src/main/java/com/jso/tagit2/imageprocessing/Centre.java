package com.jso.tagit2.imageprocessing;

import android.graphics.Point;

/**
 * Created by jshelley on 4/04/2017.
 */

public class Centre {
    public Point start;
    public Point end;

    public Point mid() {
        return new Point(start.x + (end.x - start.x)/2, start.y + (end.y - start.y)/2);
    }
}
