package com.jso.tagit2.models;

import android.database.Cursor;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by jshelley on 23/03/2017.
 */

public class Catch {
    public long _id;
    public String catchId;

    public boolean isSynced;
    public long lastModified;

    public String fisher;
    public String bait;
    public String species;

    public double latitude;
    public double longitude;

    public double length;
    public double weight;

    public String locationDescription;

    public String imagePath;

    public long timestamp;

    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }
}
