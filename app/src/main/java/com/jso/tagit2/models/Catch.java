package com.jso.tagit2.models;

import android.database.Cursor;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by jshelley on 23/03/2017.
 */

public class Catch {

    public Catch() {
        timestamp = System.currentTimeMillis() / 1000L;
        isSynced = false;
        lastModified = timestamp;
    }

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

    public String thumbnailPath;
    public String imagePath;

    public long timestamp;

    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    public void setPosition(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }
}
