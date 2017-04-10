package com.jso.tagit2;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.media.Image;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Iterators;
import com.jso.tagit2.database.BaitsTable;
import com.jso.tagit2.database.CatchesTable;
import com.jso.tagit2.database.FishersTable;
import com.jso.tagit2.database.SpeciesTable;
import com.jso.tagit2.fragments.EditCatchFragment;
import com.jso.tagit2.fragments.FishListFragment;
import com.jso.tagit2.fragments.LoginFragment;
import com.jso.tagit2.fragments.QrMeasurementFragment;
import com.jso.tagit2.fragments.TagItMapFragment;
import com.jso.tagit2.interfaces.IGoogleApiClient;
import com.jso.tagit2.interfaces.IStateManager;
import com.jso.tagit2.models.Catch;
import com.jso.tagit2.models.State;
import com.jso.tagit2.models.User;
import com.jso.tagit2.provider.TagIt2Provider;
import com.jso.tagit2.services.LocationService;
import com.jso.tagit2.sync.SyncManager;
import com.jso.tagit2.utils.AsyncGeocoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements IStateManager,
        GoogleApiClient.OnConnectionFailedListener,
        IGoogleApiClient,
        LocationService.LocationChangedListener {

    private final String TAG = "TagIt2.MainActivity";

    public final String CUR_FRAG_PARAM = "CurrentFragment";
    public final String CUR_CATCH_PARAM = "CurrentCatch";

    public final int RC_SIGN_IN = 0x1000;

    private final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 0x01;

    TextView textAccuracy;
    ImageView imageStatus;
    ImageView imageFixStatus;

    GpsStatus gpsStatus;

    LocationService.LocationServiceBinder locationServiceBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_pane);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textAccuracy = (TextView)findViewById(R.id.text_accuracy);
        textAccuracy.setText("");

        imageStatus = (ImageView)findViewById(R.id.image_status);
        imageFixStatus = (ImageView)findViewById(R.id.image_fix_status);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertNewFishAndEdit();

                Snackbar.make(view, "Fill in the details for your new catch!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        View v = findViewById(R.id.main_frame);
        v.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
/*
        ContentResolver resolver = getContentResolver();
        resolver.delete(TagIt2Provider.Contract.BAITS_URI, null, null);
        resolver.delete(TagIt2Provider.Contract.SPECIES_URI, null, null);
        resolver.delete(TagIt2Provider.Contract.FISHERS_URI, null, null);
        resolver.delete(TagIt2Provider.Contract.CATCHES_URI, null, null);
*/
    }

    ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            View v = findViewById(R.id.main_frame);
            v.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);

            SharedPrefsHelper prefs = new SharedPrefsHelper(MainActivity.this);
            State savedState = prefs.getState();
            User user = prefs.getLoggedInUser();
            if (!user.isLoggedIn())
                savedState.state = State.LOGIN;

            go(savedState.state, savedState.args, false);

            if (user.isLoggedIn()) {
                SyncManager sm = SyncManager.getInstance(MainActivity.this);
                sm.sync();
            }

            int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
            } else {
                startLocationService();
            }

            View logo = findViewById(R.id.image_logo);
            logo.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (locationServiceBinder != null) {
            unbindService(locationServiceConnection);
            locationServiceBinder = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationService();
                }
                break;
        }
    }

    private void startLocationService() {
        Intent locationServiceIntent = new Intent(this, LocationService.class);
        this.startService(locationServiceIntent);
        this.bindService(locationServiceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            User user = new User();
            user.id = acct.getId();
            user.name = acct.getGivenName();
            user.provider = "google";

            SharedPrefsHelper prefs = new SharedPrefsHelper(this);
            prefs.setLoggedInUser(user);

            SyncManager sm = SyncManager.getInstance(this);
            sm.sync();

            go(State.CATCH_LIST, null, false);
        } else {
        }
    }

    private Bundle getState(Bundle savedInstanceState)
    {
        if (savedInstanceState != null)
            return savedInstanceState;

        SharedPreferences prefs = getSharedPreferences("fragment", MODE_PRIVATE);

        Bundle bundle = new Bundle();
        bundle.putString(CUR_FRAG_PARAM, prefs.getString(CUR_FRAG_PARAM, FishListFragment.class.toString()));
        bundle.putString(CUR_CATCH_PARAM, prefs.getString(CUR_CATCH_PARAM, FishListFragment.class.toString()));

        return bundle;
   }

   private void insertNewFishAndEdit() {
       Catch c = new Catch();
       ContentResolver resolver = getContentResolver();
       ContentValues values = CatchesTable.getContentValues(c);
       Uri uri = resolver.insert(TagIt2Provider.Contract.CATCHES_URI, values);

       String catchId = uri.getLastPathSegment();
       try {
           go(State.EDIT_CATCH, new JSONObject("{ id: " + catchId + "}"), false);
       } catch (JSONException e) {
           e.printStackTrace();
       }
   }

    private void insertDummyFish() throws IOException {
        String [] species = new String[] {
                "Snapper",
                "Bream",
                "Flathead",
                "Kingfish"
        };

        String[] baits = new String[] {
                "Prawn",
                "Squid",
                "Lure",
                "Pilchards"
        };

        String[] fishers = new String[] {
                "Jase",
                "Jess",
                "Amy",
                "Matt"
        };

        LatLng[] locations = new LatLng[] {
                new LatLng(-33.980068, 151.195658),
                new LatLng(-34.006056, 151.193045),
                new LatLng(-34.005253, 151.146385)
        };

        final ContentResolver resolver = getContentResolver();
        
        // delete the existing baits
//        resolver.delete(TagIt2Provider.Contract.CATCHES_VIEW_URI, null, null);
//        resolver.delete(TagIt2Provider.Contract.BAITS_URI, null, null);
//        resolver.delete(TagIt2Provider.Contract.FISHERS_URI, null, null);
//        resolver.delete(TagIt2Provider.Contract.SPECIES_URI, null, null);

        if (true) {
            Random random = new Random((new Date()).getTime());
            ContentValues values;
            Uri result;

            String bait = baits[random.nextInt(4)];
            Cursor c = resolver.query(TagIt2Provider.Contract.BAITS_URI,
                    TagIt2Provider.Contract.BAIT_PROJECTION,
                    BaitsTable.COL_NAME + "='" + bait + "'",
                    null,
                    null);

            if (!c.moveToFirst()) {
                c.close();
                // Insert a bait
                values = new ContentValues();
                values.put(BaitsTable.COL_BAIT_ID, UUID.randomUUID().toString());
                values.put(BaitsTable.COL_NAME, "Prawn");
                result = resolver.insert(TagIt2Provider.Contract.BAITS_URI, values);
                c = resolver.query(result, TagIt2Provider.Contract.BAIT_PROJECTION, null, null, null, null);
                c.moveToFirst();
            }

            final String baitId = c.getString(c.getColumnIndex(BaitsTable.COL_BAIT_ID));

            String fisher = fishers[random.nextInt(4)];
            c = resolver.query(TagIt2Provider.Contract.FISHERS_URI,
                    TagIt2Provider.Contract.FISHER_PROJECTION,
                    FishersTable.COL_NAME + "='" + fisher + "'",
                    null,
                    null);
            if (!c.moveToFirst()) {
                c.close();
                // Insert a fisher
                values = new ContentValues();
                values.put(FishersTable.COL_FISHER_ID, UUID.randomUUID().toString());
                values.put(FishersTable.COL_NAME, "Matt");
                result = resolver.insert(TagIt2Provider.Contract.FISHERS_URI, values);
                c = resolver.query(result, TagIt2Provider.Contract.FISHER_PROJECTION, null, null, null, null);
                c.moveToFirst();
            }
            final String fisherId = c.getString(c.getColumnIndex(FishersTable.COL_FISHER_ID));


            String thisSpecies = species[random.nextInt(4)];
            c = resolver.query(TagIt2Provider.Contract.SPECIES_URI,
                    TagIt2Provider.Contract.SPECIES_PROJECTION,
                    SpeciesTable.COL_NAME + "='" + thisSpecies + "'",
                    null,
                    null);

            if (!c.moveToFirst()) {
                c.close();
                // Insert a species
                values = new ContentValues();
                values.put(SpeciesTable.COL_SPECIES_ID, UUID.randomUUID().toString());
                values.put(SpeciesTable.COL_NAME, thisSpecies);
                result = resolver.insert(TagIt2Provider.Contract.SPECIES_URI, values);
                c = resolver.query(result, TagIt2Provider.Contract.SPECIES_PROJECTION, null, null, null, null);
                c.moveToFirst();
            }
            final String speciesId = c.getString(c.getColumnIndex(SpeciesTable.COL_SPECIES_ID));

            final LatLng location = locations[random.nextInt(locations.length)];

            Geocoder localGeocoder = new Geocoder(this);
            List<Address> addresses = localGeocoder.getFromLocation(location.latitude, location.longitude, 10);

            String featureName = "";
            for (Address a : addresses) {
                String thisName = a.getFeatureName();
                if (thisName.endsWith("Ocean")
                        || thisName.endsWith("Bay")
                        || thisName.endsWith("River")
                        || thisName.endsWith("Inlet")
                        || thisName.startsWith("Lake")) {
                    featureName = thisName;
                    break;
                }
            }

            Catch newCatch = new Catch();
            newCatch.bait = bait;
            newCatch.catchId = UUID.randomUUID().toString();
            newCatch.fisher = fisher;
            newCatch.species = thisSpecies;
            newCatch.length = 20 + random.nextInt(30);
            newCatch.weight = 0.5f + random.nextInt(1000)/1000.0f;
            newCatch.locationDescription = featureName;
            newCatch.latitude = location.latitude;
            newCatch.longitude = location.longitude;
            // now that we have everything, let's stick in a catch
            values = CatchesTable.getContentValues(newCatch);
            resolver.insert(TagIt2Provider.Contract.CATCHES_URI, values);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fish_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_measure) {
            Catch c = new SharedPrefsHelper(this).getCurrentCatch();
            try {
                go(State.MEASURE, new JSONObject("{id:" +  String.valueOf(c._id) + "}"), false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        State state = prefs.getState();
        switch (state.state) {
            case State.MAP:
                go(State.CATCH_LIST, null, true);
                break;
            case State.EDIT_CATCH:
                go(State.MAP, state.args, true);  // same arg
                break;

            case State.MEASURE:
                go(State.MAP, state.args, true);  // same arg
                break;

            default:
                long last = prefs.getLastBackPressTime();
                long now = System.currentTimeMillis();
                if (now - last < 2000) {
                    finish();
                } else {
                    View v = findViewById(R.id.main_frame);
                    Snackbar.make(v, "Press back again to exit", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    prefs.setLastBackPressTime(now);
                }
                break;
        }
    }

    private void swapFragments(Fragment newFragment, boolean back)
    {
        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction ft =  fm.beginTransaction();

        if (newFragment instanceof EditCatchFragment) {
            // can only be forward
            ft.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        } else if (newFragment instanceof TagItMapFragment) {
            if (back)   // we're coming from the edit catch fragment
                ft.setCustomAnimations(R.anim.slide_in_down, R.anim.slide_out_down);
            else
                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (newFragment instanceof FishListFragment) {
            if (back)   // we're coming from the map fragment
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
            else    // we're coming from the login fragment
                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            if (!back)
                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
            else
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        }

        ft.replace(R.id.fragment_container, newFragment);

//        ft.addToBackStack(null);

        ft.commit();
    }

    @Override
    public void go(int newState, JSONObject args, boolean back) {

        long catchId;
        try {
            switch (newState) {
                case State.LOGIN:
                    swapFragments(LoginFragment.newInstance(), back);
                    break;

                case State.CATCH_LIST:
                    swapFragments(FishListFragment.newInstance(), back);
                    break;

                case State.MAP:
                    catchId = args.getLong("id");
                    swapFragments(TagItMapFragment.newInstance(catchId), back);
                    break;

                case State.EDIT_CATCH:
                    catchId = args.getLong("id");
                    swapFragments(EditCatchFragment.newInstance(catchId), back);
                    break;

                case State.MEASURE:
                    catchId = args.getLong("id");
                    swapFragments(QrMeasurementFragment.newInstance(catchId), false);
                    break;
            }
        }
        catch (JSONException jsonException) {
            Log.e("StateManager", "Invalid args: " + args);
        }

        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        prefs.setState(newState, args);
    }

    GoogleApiClient googleApiClient;

    private void initGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId()
                .requestProfile()
                .requestEmail()
                .requestIdToken("813651815469-ov9i00icnoatto84033ct4s07cakt828.apps.googleusercontent.com")
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void SignIn() {
        initGoogleSignIn();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private ServiceConnection locationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            locationServiceBinder = (LocationService.LocationServiceBinder)service;
            locationServiceBinder.setLocationChangedListener(MainActivity.this);

            GpsStatus status = locationServiceBinder.getCurrentStatus();
            Location location = locationServiceBinder.getCurrentLocation();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            unbindService(locationServiceConnection);
        }
    };

    public LocationService.LocationServiceBinder getLocationServiceBinder() { return locationServiceBinder; }

    @Override
    public void onLocationChanged(Location location)  {

        if (location == null)
            return;

        setAccuracy(location.getAccuracy());

        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        Catch currentCatch = prefs.getCurrentCatch();
        if (currentCatch != null && currentCatch.latitude == 0) {
            currentCatch.setPosition(location);

            ContentResolver resolver = getContentResolver();
            ContentValues values = CatchesTable.getContentValues(currentCatch);
            // update the location only
            resolver.update(prefs.getCurrentCatchUri(), values, null, null);

            AsyncGeocoder ag = new AsyncGeocoder(this);
            ag.execute(currentCatch);
        }
    }

    private void setStatus(GpsStatus status) {
        gpsStatus = status;
        if (status == null)
            imageStatus.setImageResource(android.R.drawable.presence_busy);
        else {
            if (status.getTimeToFirstFix() == 0) {
                textAccuracy.setText(String.format("%d satellites", Iterators.size(gpsStatus.getSatellites().iterator())));
                imageStatus.setImageResource(android.R.drawable.presence_away);
            } else
                imageStatus.setImageResource(android.R.drawable.presence_online);
        }
    }

    public void setAccuracy(double accuracy) {
        if (locationServiceBinder == null) {
            imageFixStatus.setImageResource(android.R.drawable.presence_busy);
            textAccuracy.setText("");

            return;
        }

        int count= 0;
        if (gpsStatus != null && gpsStatus.getTimeToFirstFix() == 0) {
            imageFixStatus.setImageResource(android.R.drawable.presence_busy);
            textAccuracy.setText(String.format("%d satellites", Iterators.size(gpsStatus.getSatellites().iterator())));
        } else {
            if (locationServiceBinder.hasValidLocation())
                imageFixStatus.setImageResource(android.R.drawable.presence_online);
            else
                imageFixStatus.setImageResource(android.R.drawable.presence_away);
            textAccuracy.setText(String.format("± %.0fm", accuracy));
        }
    }

    @Override
    public void onStatusChanged(GpsStatus status) {
        setStatus(status);
    }

    @Override
    public void onAccuracyChanged(double accuracy) {
        setAccuracy(accuracy);
    }
}
