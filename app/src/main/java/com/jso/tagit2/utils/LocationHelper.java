package com.jso.tagit2.utils;


import com.google.android.gms.maps.model.LatLng;
import android.location.Location;
/**
 * Created by jshelley on 26/03/2017.
 */

public class LocationHelper {
    public static double distance(double dlat1, double dlng1, double dlat2, double dlng2) {
        double lat1, lat2, lng1, lng2;
        lat1 = Math.toRadians(dlat1);
        lat2 = Math.toRadians(dlat2);
        lng2 = Math.toRadians(dlng1);
        lng1 = Math.toRadians(dlng2);

        double r = 6378100; // earth radius in metres

        double h = Math.sinh(lat2 - lat1) + Math.cos(lat1*Math.cos(lat2)*Math.sinh(lng2 - lng1));

        return 2 * r * Math.asin(Math.sqrt(h));
    }

}
