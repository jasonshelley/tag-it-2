package com.jso.tagit2.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.LocationSource;
import com.jso.tagit2.utils.LocationHelper;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Created by jshelley on 26/03/2017.
 */

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    long lastLocationTimestamp;
    Location currentLocation;

    GoogleApiClient apiClient;

    private long INTERVAL_ACQUIRING = 0;
    private long INTERVAL_TRACKING = 5 * 60 * 60 * 1000;    // 5 minutes

    private int ACCURACY_MIN = 10;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        currentLocation = new Location("tagit2");
        currentLocation.setAccuracy(Float.MAX_VALUE);

        if (apiClient == null) {    // if this is the first attempt to start the service
            apiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        }
        if (!apiClient.isConnected() && !apiClient.isConnecting()) {
            apiClient.connect();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        apiClient.disconnect();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest lr = new LocationRequest();
        lr.setInterval(INTERVAL_ACQUIRING);
        lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        lr.setSmallestDisplacement(0);  // we want updates regardless of whether the we've moved or not so that we can get better accuracy

        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, lr, this);
        }
        catch (SecurityException se) {

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        double currentAcc = currentLocation.getAccuracy();
        double newAcc = location.getAccuracy();

        if (newAcc <= currentAcc) {
            currentLocation = location;
        }

        if (currentLocation.getAccuracy() <= ACCURACY_MIN) {
            // we have a location worth reporting

        }
    }
}
