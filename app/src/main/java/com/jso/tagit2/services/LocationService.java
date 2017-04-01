package com.jso.tagit2.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.jso.tagit2.MainActivity;
import com.jso.tagit2.R;
import com.jso.tagit2.utils.LocationHelper;


/**
 * Created by jshelley on 26/03/2017.
 */

public class LocationService extends Service implements LocationListener, GpsStatus.Listener
{

    LocationServiceBinder binder = new LocationServiceBinder();

    long lastLocationTimestamp;
    Location currentLocation;
    GpsStatus currentStatus;
    GnssStatus currentGnssStatus;

    Notification notification;

    private int MODE_IDLE = 0x00;
    private int MODE_ACQUIRING = 0x01;
    private int MODE_TRACKING = 0x02;
    private int curMode = MODE_IDLE;

    private long INTERVAL_ACQUIRING = 1000;
    private long INTERVAL_TRACKING = 30 * 1000;

    private long EXPIRY = 1 * 60 * 1000;

    public static int ACCURACY_MIN = 20;

    public final static int REQUEST_STOP = 0x01;

    public final static String ACTION_STOP = "stop";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (currentLocation == null) {
            currentLocation = new Location("tagit2");
            currentLocation.setAccuracy(Float.MAX_VALUE);
            currentLocation.setTime(0);
        }

        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action == ACTION_STOP) {
                    stopForeground(true);
                    stopSelf();

                    return START_NOT_STICKY;
                }
            }
        }
//        if (curMode == MODE_IDLE || System.currentTimeMillis() - currentLocation.getTime() > EXPIRY)
            requestLocationUpdates(MODE_ACQUIRING);


        Intent stopIntent = new Intent(this, LocationService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(this, REQUEST_STOP, stopIntent, 0);

        Intent tagit2Intent = new Intent(this, MainActivity.class);
        PendingIntent pendingTagIt2Intent = PendingIntent.getActivity(this, 0, tagit2Intent, 0);

        notification = new NotificationCompat.Builder(this)
                .setContentTitle("TagIt II Location Service")
                .setTicker("TagIt II")
                .setContentText("")
                .setContentIntent(pendingTagIt2Intent)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", pendingIntent)
                .build();

        startForeground(1, notification);

        return START_STICKY;
    }

    private void requestLocationUpdates(int mode) {
        stopLocationUpdates();
        Log.v("LocationService", "Starting location updates");
        curMode = mode;
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        long interval = mode == MODE_ACQUIRING ? INTERVAL_ACQUIRING : INTERVAL_TRACKING;

        // permissions check performed in activity before starting service
        //noinspection MissingPermission
        lm.addGpsStatusListener(this);  // deprecated but not replaced in api 23
        // permissions check done in activity before starting service
        //noinspection MissingPermission
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval, 0, this);
    }

    private void stopLocationUpdates() {
        Log.v("LocationService", "Stopping location updates");
        curMode = MODE_IDLE;
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lm.removeUpdates(this);
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onLocationChanged(Location location) {

        Location l = location;

        if (currentLocation == null)
            currentLocation = location;

        double currentAcc = currentLocation.getAccuracy();
        double newAcc = location.getAccuracy();
        double oldAcc = currentAcc;

        // update the location IF:
        // we have a better accuracy
        // we have the same accuracy, but we've moved
        // the current accuracy is too old
        double distance = LocationHelper.distance(location, currentLocation);
        Log.v("LocationService", String.format("Movement: %fm (±%.1fm)", distance, l.getAccuracy()));

        if (newAcc <= ACCURACY_MIN) {
            if (distance >= 1 ||
                    newAcc < currentAcc ||
                    System.currentTimeMillis() - currentLocation.getTime() > EXPIRY ||
                    curMode == MODE_ACQUIRING) {
                currentLocation = location;
                currentAcc = currentLocation.getAccuracy();
                binder.fireLocationChanged();
            }
        } else if (System.currentTimeMillis() - currentLocation.getTime() > EXPIRY || currentAcc > ACCURACY_MIN){
            currentLocation = location;
            currentAcc = location.getAccuracy();
        }

        binder.fireAccuracyChanged(currentAcc);

        if (currentAcc <= ACCURACY_MIN && curMode == MODE_ACQUIRING) {
            // enter tracking mode
//            stopLocationUpdates();
            requestLocationUpdates(MODE_TRACKING);
        } else if (currentAcc > ACCURACY_MIN && curMode == MODE_TRACKING) {
            // drop back to acquiring until we have an accurate fix
//            stopLocationUpdates();
            requestLocationUpdates(MODE_ACQUIRING);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // handled in GnssStatus.Callback
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.v("LocationService", provider + " enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.v("LocationService", provider + " disabled");
    }

    @Override
    public void onGpsStatusChanged(int event) {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        switch (event) {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                //noinspection MissingPermission
                currentStatus = lm.getGpsStatus(null);
                binder.fireStatusChanged(currentStatus);
                break;
        }
    }

    public class LocationServiceBinder extends Binder {

        LocationChangedListener locationChanged;

        protected void fireLocationChanged() {
            if (locationChanged != null)
                locationChanged.onLocationChanged(currentLocation);
        }

        protected void fireAccuracyChanged(double newAccuracy) {
            if (locationChanged != null)
                locationChanged.onAccuracyChanged(newAccuracy);
        }

        protected void fireStatusChanged(GpsStatus status) {
            if (locationChanged != null)
                locationChanged.onStatusChanged(status);
        }

        public void setLocationChangedListener(LocationChangedListener listener) {
            locationChanged = listener;
        }

        public Location getCurrentLocation() {
            return currentLocation;
        }

        public boolean hasValidLocation() {
            return currentLocation.getAccuracy() <= ACCURACY_MIN && (System.currentTimeMillis() - currentLocation.getTime()) <= EXPIRY;
        }

        public GpsStatus getCurrentStatus() {
            return currentStatus;
        }
    }

    public interface LocationChangedListener {
        public void onLocationChanged(Location location);
        public void onStatusChanged(GpsStatus status);
        public void onAccuracyChanged(double accuracy);
    }
}
