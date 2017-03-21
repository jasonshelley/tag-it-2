package com.jso.tagit2.models;

/**
 * Created by jshelley on 21/03/2017.
 */

public class User {
    public String id;
    public String name;
    public String provider;

    public boolean isLoggedIn() { return id != null && !id.isEmpty(); }
}
