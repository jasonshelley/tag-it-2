package com.jso.tagit.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/** The object model for the data we are sending through endpoints */
@Entity
public class Fisherman {

    @Id private String id;
    @Index private String userid;
    @Index private long last_modified;
    private int marker_hue;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserid()
    {
        return userid;
    }

    public void setUserid(String userid)
    {
        this.userid = userid;
    }

    public long getLast_modified()
    {
        return last_modified;
    }

    public void setLast_modified(long last_modified) {
        this.last_modified = last_modified;
    }

    public int getMarker_hue()
    {
        return marker_hue;
    }

    public void setMarker_hue(int marker_hue)
    {
        this.marker_hue = marker_hue;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}