package com.jso.tagit2.utils;


import com.google.android.gms.maps.model.LatLng;
import android.location.Location;
/**
 * Created by jshelley on 26/03/2017.
 */

public class LocationHelper {
    public static double distance(Location l1, Location l2) {
        double r = 6378100; // earth radius in metres

        double lat1 = Math.toRadians(l1.getLatitude());
        double lng1 = Math.toRadians(l1.getLongitude());
        double lat2 = Math.toRadians(l2.getLatitude());
        double lng2 = Math.toRadians(l2.getLongitude());

        double dLat = Math.toRadians(l2.getLatitude()-l1.getLatitude());
        double dLon = Math.toRadians(l2.getLongitude()-l1.getLongitude());
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = r * c; // Distance in m

        return d;
    }

}
