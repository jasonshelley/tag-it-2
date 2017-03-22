package com.jso.tagit2.interfaces;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by JSHELLEY on 13/03/2017.
 */

public interface IStateManager {

    void go(int newState, JSONObject args) throws JSONException;
}
