package com.jso.tagit2.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.BaseImplementation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jso.tagit2.R;
import com.jso.tagit2.database.CatchesTable;
import com.jso.tagit2.provider.TagIt2Provider;

/**
 * Created by JSHELLEY on 13/03/2017.
 */

public class TagItMapFragment extends Fragment implements OnMapReadyCallback {

    MapView mapView;
    GoogleMap map;
    ContentObserver observer;

    Bundle args;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment_map, container, false);

        mapView = (MapView)v.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        args = savedInstanceState != null ? savedInstanceState : getArguments();

        return v;
    }

    private void loadCatch(long catchId)
    {
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
        map.addMarker(new MarkerOptions()
        .position(latlng)
        .title(species));

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 13), 2000, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {

            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void loadCatches()
    {

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

        long catchId;
        if ((catchId = args.getLong("CATCH_ID", -1)) == -1)
            loadCatches();
        else
            loadCatch(catchId);
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

}
