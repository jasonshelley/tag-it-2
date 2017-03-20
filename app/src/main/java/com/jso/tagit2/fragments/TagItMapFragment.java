package com.jso.tagit2.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.BaseImplementation;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jso.tagit2.R;
import com.jso.tagit2.database.CatchesTable;
import com.jso.tagit2.database.IDatabaseTable;
import com.jso.tagit2.provider.TagIt2Provider;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by JSHELLEY on 13/03/2017.
 */

public class TagItMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    MapView mapView;
    GoogleMap map;
    ContentObserver observer;
    long selectedCatchId;

    Map<Marker, Long> markerToCatchIdMap = new HashMap<Marker, Long>();

    Bundle args;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment_map, container, false);

        mapView = (MapView)v.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        args = savedInstanceState != null ? savedInstanceState : getArguments();

        selectedCatchId = args.getLong("CATCH_ID", -1);

        return v;
    }

    private void loadCatch(long catchId, Boolean animate)
    {
        if (catchId != -1) {
            Fragment f = getChildFragmentManager().findFragmentById(R.id.fragment_fish_details);
            if (f instanceof FishDetailsFragment) {
                ((FishDetailsFragment) f).setCatchId(catchId);
            }
        }

        Uri uri = Uri.withAppendedPath(TagIt2Provider.Contract.CATCHES_URI, String.valueOf(catchId));
        Cursor c = getActivity().getContentResolver().query(uri, TagIt2Provider.Contract.CATCHES_VIEW_PROJECTION, null, null, null);

        double latitude = 0, longitude = 0;
        String species = "";
        if (c.moveToFirst())
        {
            latitude = c.getDouble(c.getColumnIndex(CatchesTable.COL_LATITUDE));
            longitude = c.getDouble(c.getColumnIndex(CatchesTable.COL_LONGITUDE));
            species = c.getString(c.getColumnIndex("Species"));
        }

        c.close();

        observer = new ContentObserver(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return false;
            }
        })) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
            }
        };

        LatLng latlng = new LatLng(latitude, longitude);

        MoveCamera(latlng, animate);
    }

    Boolean isAnimating = false;
    GoogleMap.CancelableCallback cancelableCallback =
        new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                isAnimating = false;
            }

            @Override
            public void onCancel() {
                isAnimating = false;
            }
        };

    private void MoveCamera(LatLng latlng, Boolean animate) {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latlng, 13);
        if (animate) {
            if (isAnimating)
                map.stopAnimation();
            isAnimating = true;
            map.animateCamera(update, 1000, cancelableCallback);
        } else
            map.moveCamera(update);
    }

    private void loadCatches()
    {
        Uri uri = TagIt2Provider.Contract.CATCHES_URI;
        Cursor c = getActivity().getContentResolver().query(uri, TagIt2Provider.Contract.CATCHES_VIEW_PROJECTION, null, null, null);
        double latitude = 0, longitude = 0;
        String species = "";
        while (c.moveToNext())
        {
            latitude = c.getDouble(c.getColumnIndex(CatchesTable.COL_LATITUDE));
            longitude = c.getDouble(c.getColumnIndex(CatchesTable.COL_LONGITUDE));
            species = c.getString(c.getColumnIndex("Species"));
            long catchId = c.getLong(c.getColumnIndex(IDatabaseTable.COL_ID));

            LatLng latlng = new LatLng(latitude, longitude);
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(latlng)
                    .title(species));
            markerToCatchIdMap.put(marker, catchId);

        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();

        MapsInitializer.initialize(activity);

        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // do map init stuff here
        map = googleMap;

        map.setOnMarkerClickListener(this);

        loadCatches();
        if (selectedCatchId != -1)
            loadCatch(selectedCatchId, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (observer != null)
            getActivity().getContentResolver().unregisterContentObserver(observer);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        long id = markerToCatchIdMap.get(marker);

        loadCatch(id, true);

        return true;
    }
}
