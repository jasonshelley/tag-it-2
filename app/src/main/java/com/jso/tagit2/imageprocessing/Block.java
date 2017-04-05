package com.jso.tagit2.imageprocessing;

/**
 * Created by jshelley on 4/04/2017.
 */

public class Block {
    public int start;
    public int end;

    public int width() {
        return end - start;
    }
}
