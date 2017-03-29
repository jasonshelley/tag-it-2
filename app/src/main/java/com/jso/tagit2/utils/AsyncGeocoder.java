package com.jso.tagit2.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.jso.tagit2.SharedPrefsHelper;
import com.jso.tagit2.database.CatchesTable;
import com.jso.tagit2.models.Catch;
import com.jso.tagit2.provider.TagIt2Provider;

import java.io.IOException;
import java.util.List;

/**
 * Created by jshelley on 28/03/2017.
 */

public class AsyncGeocoder extends AsyncTask<Catch, Void, Void> {

    Context context;

    public AsyncGeocoder(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Catch... params) {
        Catch c = params[0];

        Geocoder geocoder = new Geocoder(context);
        LatLng location = c.getPosition();
        try {
            List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            if (addresses.size() == 1) {
                c.locationDescription = addresses.get(0).getFeatureName();

                ContentResolver resolver = context.getContentResolver();
                ContentValues values = CatchesTable.getContentValues(c);
                resolver.update(Uri.withAppendedPath(TagIt2Provider.Contract.CATCHES_URI, String.valueOf(c._id)), values, null, null);
            }
        } catch (IOException ioe) {
            Log.d("onLocationChanged", "Failed to geocode");
        }

        return null;
    }
}
