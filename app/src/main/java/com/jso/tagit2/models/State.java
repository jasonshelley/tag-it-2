package com.jso.tagit2.models;

import org.json.JSONObject;

/**
 * Created by jshelley on 21/03/2017.
 */

public class State {
    public final static int LOGIN = 0x00;
    public final static int CATCH_LIST = 0x01;
    public final static int MAP = 0x02;

    public State() {
        state = LOGIN;
    }

    public State(int state, JSONObject args) {
        this.state = state;
        this.args = args;
    }

    public int state;
    public JSONObject args;
}