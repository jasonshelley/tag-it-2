package com.jso.tagit2;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.jso.tagit2.database.BaitsTable;
import com.jso.tagit2.database.CatchesTable;
import com.jso.tagit2.database.FishersTable;
import com.jso.tagit2.database.SpeciesTable;
import com.jso.tagit2.fragments.FishListFragment;
import com.jso.tagit2.fragments.TagItMapFragment;
import com.jso.tagit2.provider.TagIt2Provider;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements INavigator {

    public final String CUR_FRAG_PARAM = "CurrentFragment";
    public final String CUR_CATCH_PARAM = "CurrentCatch";

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

        Bundle extras = getState(savedInstanceState);

        String curFrag = extras.getString(CUR_FRAG_PARAM);
        if (curFrag.equals(FishListFragment.class.toString())) {
            FishListFragment fishListFragment = new FishListFragment();
            getFragmentManager().beginTransaction().add(R.id.fragment_container, fishListFragment).commit();
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

        LatLng[] locations = new LatLng[] {
                new LatLng(-33.980068, 151.195658),
                new LatLng(-34.006056, 151.193045),
                new LatLng(-34.005253, 151.146385)
        };

        final ContentResolver resolver = getContentResolver();
        
        // delete the existing baits
        resolver.delete(TagIt2Provider.Contract.CATCHES_URI, null, null);
        resolver.delete(TagIt2Provider.Contract.BAITS_URI, null, null);
        resolver.delete(TagIt2Provider.Contract.FISHERS_URI, null, null);
        resolver.delete(TagIt2Provider.Contract.SPECIES_URI, null, null);

        if (true) {
            // Insert a bait
            final ContentValues values = new ContentValues();
            values.put(BaitsTable.COL_BAIT_ID, UUID.randomUUID().toString());
            values.put(BaitsTable.COL_NAME, "Squid");
            Uri result = resolver.insert(TagIt2Provider.Contract.BAITS_URI, values);
            String id = result.getLastPathSegment();
            Cursor c = resolver.query(result, TagIt2Provider.Contract.BAIT_PROJECTION, null, null, null, null);
            c.moveToFirst();
            final String baitId = c.getString(c.getColumnIndex(BaitsTable.COL_BAIT_ID));

            // Insert a fisher
            values.clear();
            values.put(FishersTable.COL_FISHER_ID, UUID.randomUUID().toString());
            values.put(FishersTable.COL_NAME, "Jase");
            result = resolver.insert(TagIt2Provider.Contract.FISHERS_URI, values);
            id = result.getLastPathSegment();
            c = resolver.query(result, TagIt2Provider.Contract.FISHER_PROJECTION, null, null, null, null);
            c.moveToFirst();
            final String fisherId = c.getString(c.getColumnIndex(FishersTable.COL_FISHER_ID));

            Random random = new Random((new Date()).getTime());
            int speciesIdx = random.nextInt(4);

            // Insert a species
            values.clear();
            values.put(SpeciesTable.COL_SPECIES_ID, UUID.randomUUID().toString());
            values.put(SpeciesTable.COL_NAME, species[speciesIdx]);
            result = resolver.insert(TagIt2Provider.Contract.SPECIES_URI, values);
            id = result.getLastPathSegment();
            c = resolver.query(result, TagIt2Provider.Contract.SPECIES_PROJECTION, null, null, null, null);
            c.moveToFirst();
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
            values.clear();
            values.put(CatchesTable.COL_BAIT_ID, baitId);
            values.put(CatchesTable.COL_CATCH_ID, UUID.randomUUID().toString());
            values.put(CatchesTable.COL_FISHER_ID, fisherId);
            values.put(CatchesTable.COL_SPECIES_ID, speciesId);
            values.put(CatchesTable.COL_LENGTH, 24);
            values.put(CatchesTable.COL_WEIGHT, 0.8);
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
    public void showCatchList() {

    }

    @Override
    public void showCatch(long catchId) {
        Bundle args = new Bundle();
        args.putLong("CATCH_ID", catchId);

        swapFragments(TagItMapFragment.class.toString(), args);
    }

    private void swapFragments(String newFragmentClass, Bundle args)
    {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft =  fm.beginTransaction();

        Fragment fragment = null;

        if (newFragmentClass.equals(FishListFragment.class.toString()))
            fragment = new FishListFragment();
        else if (newFragmentClass.equals(TagItMapFragment.class.toString())) {
            fragment = new TagItMapFragment();
        }

        fragment.setArguments(args);
        ft.replace(R.id.fragment_container, fragment);

        ft.addToBackStack(null);

        ft.commit();
    }
}
