package com.jso.tagit2;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.jso.tagit2.database.BaitsTable;
import com.jso.tagit2.database.CatchesTable;
import com.jso.tagit2.database.FishersTable;
import com.jso.tagit2.database.SpeciesTable;
import com.jso.tagit2.fragments.EditCatchFragment;
import com.jso.tagit2.fragments.FishListFragment;
import com.jso.tagit2.fragments.LoginFragment;
import com.jso.tagit2.fragments.TagItMapFragment;
import com.jso.tagit2.models.State;
import com.jso.tagit2.models.User;
import com.jso.tagit2.provider.TagIt2Provider;
import com.jso.tagit2.sync.SyncManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements IStateManager, GoogleApiClient.OnConnectionFailedListener, IGoogleApiClient {

    private final String TAG = "TagIt2.MainActivity";

    public final String CUR_FRAG_PARAM = "CurrentFragment";
    public final String CUR_CATCH_PARAM = "CurrentCatch";

    public final int RC_SIGN_IN = 0x1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_pane);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    insertDummyFish();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Snackbar.make(view, "Adding random fish", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        State savedState = prefs.getState();
        User user = prefs.getLoggedInUser();
        if (!user.isLoggedIn())
            savedState.state = State.LOGIN;
        go(savedState.state, savedState.args);

        SyncManager sm = SyncManager.getInstance(this);
        sm.sync();
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

            go(State.CATCH_LIST, null);
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

            // now that we have everything, let's stick in a catch
            values = new ContentValues();
            values.put(CatchesTable.COL_BAIT, bait);
            values.put(CatchesTable.COL_CATCH_ID, UUID.randomUUID().toString());
            values.put(CatchesTable.COL_FISHER, fisher);
            values.put(CatchesTable.COL_SPECIES, thisSpecies);
            values.put(CatchesTable.COL_LENGTH, random.nextInt(30));
            values.put(CatchesTable.COL_WEIGHT, random.nextInt(1000)/1000.0f);
            values.put(CatchesTable.COL_LOCATION_DESC, featureName);
            values.put(CatchesTable.COL_LATITUDE, location.latitude);
            values.put(CatchesTable.COL_LONGITUDE, location.longitude);
            values.put(CatchesTable.COL_TIMESTAMP, (new Date()).getTime());
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        State state = prefs.getState();
        switch (state.state) {
            case State.MAP:
                go(State.CATCH_LIST, null);
                break;
            case State.EDIT_CATCH:
                go(State.MAP, state.args);  // same arg
                break;

            default:
                super.onBackPressed();
                break;
        }
    }

    private void swapFragments(Fragment newFragment)
    {
        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction ft =  fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        ft.replace(R.id.fragment_container, newFragment);

        ft.addToBackStack(null);

        ft.commit();
    }

    @Override
    public void go(int newState, JSONObject args) {

        long catchId;
        try {
            switch (newState) {
                case State.LOGIN:
                    swapFragments(LoginFragment.newInstance());
                    break;

                case State.CATCH_LIST:
                    swapFragments(FishListFragment.newInstance());
                    break;

                case State.MAP:
                    catchId = args.getLong("id");
                    swapFragments(TagItMapFragment.newInstance(catchId));
                    break;

                case State.EDIT_CATCH:
                    catchId = args.getLong("id");
                    swapFragments(EditCatchFragment.newInstance(catchId));
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
}
