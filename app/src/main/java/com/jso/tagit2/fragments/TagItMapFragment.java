package com.jso.tagit2.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.jso.tagit2.interfaces.ICatchPagerScrolled;
import com.jso.tagit2.interfaces.ICatchSelected;
import com.jso.tagit2.R;
import com.jso.tagit2.database.CatchesTable;
import com.jso.tagit2.database.IDatabaseTable;
import com.jso.tagit2.models.Catch;
import com.jso.tagit2.models.TagIt2ClusterItem;
import com.jso.tagit2.provider.TagIt2Provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by JSHELLEY on 13/03/2017.
 */

public class TagItMapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        ICatchSelected,
        ICatchPagerScrolled,
        ClusterManager.OnClusterItemClickListener {

    MapView mapView;
    GoogleMap map;
    ClusterManager<TagIt2ClusterItem> clusterManager;
    ContentObserver observer;
    long selectedCatchId;
    HashMap<Long, Catch> catches = new HashMap<Long, Catch>();  // maps content provider id to catch

    Map<TagIt2ClusterItem, Long> markerToCatchIdMap = new HashMap<>();

    View rootView;

    Bundle args;

    public static TagItMapFragment newInstance(long catchId) {
        Bundle args = new Bundle();
        args.putLong("CATCH_ID", catchId);

        TagItMapFragment fragment = new TagItMapFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            rootView = inflater.inflate(R.layout.fragment_map, container, false);

            mapView = (MapView) rootView.findViewById(R.id.mapview);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);

            args = savedInstanceState != null ? savedInstanceState : getArguments();

            selectedCatchId = args.getLong("CATCH_ID", -1);

            return rootView;
        } catch (Exception e) {
            return null;
        }
    }

    private void loadCatch(long catchId, Boolean animate)
    {
        if (catchId != -1) {
            Fragment f = getChildFragmentManager().findFragmentById(R.id.fragment_fish_details);
            if (f instanceof CatchesPagerFragment) {
                ((CatchesPagerFragment) f).setCatchId(catchId);
            }
        }

        Uri uri = Uri.withAppendedPath(TagIt2Provider.Contract.CATCHES_URI, String.valueOf(catchId));
        Cursor c = getActivity().getContentResolver().query(uri, TagIt2Provider.Contract.CATCHES_PROJECTION, null, null, null);

        double latitude = 0, longitude = 0;
        String species = "";
        if (c.moveToFirst())
        {
            latitude = c.getDouble(c.getColumnIndex(CatchesTable.COL_LATITUDE));
            longitude = c.getDouble(c.getColumnIndex(CatchesTable.COL_LONGITUDE));
            species = c.getString(c.getColumnIndex(CatchesTable.COL_SPECIES));
        }

        c.close();

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
        if (map == null)
            return;
        CameraUpdate update = null;
        if (map.getCameraPosition().zoom < 12)
            update = CameraUpdateFactory.newLatLngZoom(latlng, 12);
        else
            update = CameraUpdateFactory.newLatLng(latlng);

        if (animate) {
            if (isAnimating)
                map.stopAnimation();
            isAnimating = true;
            map.animateCamera(update, 250, cancelableCallback);
        } else
            map.moveCamera(update);
    }

    private void loadCatches(ClusterManager<TagIt2ClusterItem> clusterManager)
    {
        map.clear();
        clusterManager.clearItems();
        catches.clear();

        Uri uri = TagIt2Provider.Contract.CATCHES_URI;
        Cursor c = getActivity().getContentResolver().query(uri, TagIt2Provider.Contract.CATCHES_PROJECTION, null, null, null);
        String species = "";
        while (c.moveToNext())
        {
            Catch newCatch = CatchesTable.fromCursor(c);

            LatLng latlng = new LatLng(newCatch.latitude, newCatch.longitude);

            TagIt2ClusterItem item = new TagIt2ClusterItem(latlng, species, species + " (" + newCatch.fisher + ")");
            markerToCatchIdMap.put(item, newCatch._id);
            catches.put(newCatch._id, newCatch);

            clusterManager.addItem(item);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();

        MapsInitializer.initialize(activity);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // do map init stuff here
        map = googleMap;
        UiSettings settings = map.getUiSettings();
        settings.setZoomControlsEnabled(true);

        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//        map.setOnMarkerClickListener(this);

        clusterManager = new ClusterManager<TagIt2ClusterItem>(this.getActivity(), map);
        clusterManager.setRenderer(new CustomRenderer<TagIt2ClusterItem>(this.getContext(), map, clusterManager));
        clusterManager.setAlgorithm(new Algorithm<TagIt2ClusterItem>() {
            List<TagIt2ClusterItem> items = new ArrayList<TagIt2ClusterItem>();

            @Override
            public void addItem(TagIt2ClusterItem tagIt2ClusterItem) {
                items.add(tagIt2ClusterItem);
            }

            @Override
            public void addItems(Collection<TagIt2ClusterItem> collection) {
                items.addAll(collection);
            }

            @Override
            public void clearItems() {
                items.clear();
            }

            @Override
            public void removeItem(TagIt2ClusterItem tagIt2ClusterItem) {
                items.remove(tagIt2ClusterItem);
            }

            @Override
            public Set<? extends Cluster<TagIt2ClusterItem>> getClusters(double v) {
                Set<Cluster<TagIt2ClusterItem>> clusters = new HashSet<Cluster<TagIt2ClusterItem>>();

                List<TagIt2ClusterItem> itemsCopy = new ArrayList<TagIt2ClusterItem>();
                itemsCopy.addAll(items);

                float[] distance = new float[3];

                while (itemsCopy.size() > 0) {
                    TagIt2ClusterItem zeroItem = itemsCopy.get(0);
                    LatLng zeroLocation = zeroItem.getPosition();

                    TagIt2Cluster cluster = new TagIt2Cluster();
                    cluster.addItem(itemsCopy.get(0));
                    clusters.add(cluster);

                    for (int i = 1; i < itemsCopy.size(); i++) {
                        TagIt2ClusterItem idxItem =itemsCopy.get(i);
                        LatLng idxLocation = idxItem.getPosition();

                        double multiplier = Math.pow(2, v) / 10000.0;

                        Location.distanceBetween(zeroLocation.latitude, zeroLocation.longitude, idxLocation.latitude, idxLocation.longitude, distance);

                        if (distance[0] * multiplier < 500) {
                            cluster.addItem(itemsCopy.get(i));
                            itemsCopy.remove(i);
                            i--;    // will be incremented again shortly
                        }

                    }
                    itemsCopy.remove(0);
                }
                return clusters;
            }

            @Override
            public Collection<TagIt2ClusterItem> getItems() {
                return items;
            }
        });

        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
        clusterManager.setOnClusterItemClickListener(this);

        loadCatches(clusterManager);
        if (selectedCatchId != -1)
            loadCatch(selectedCatchId, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null)
            mapView.onResume();

        ContentResolver resolver = getActivity().getContentResolver();
        observer = new ContentObserver(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return false;
            }
        })) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                loadCatches(clusterManager);
            }
        };

        resolver.registerContentObserver(TagIt2Provider.Contract.CATCHES_URI, true, observer);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        ContentResolver resolver = getActivity().getContentResolver();
        if (observer != null)
            resolver.unregisterContentObserver(observer);
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
        if (!markerToCatchIdMap.containsKey(marker))
            return false;

        long id = markerToCatchIdMap.get(marker);

        loadCatch(id, true);

        clusterManager.onMarkerClick(marker);

        return true;
    }

    @Override
    public void onCatchSelected(long id) {
        loadCatch(id, true);
    }

    @Override
    public boolean onClusterItemClick(ClusterItem clusterItem) {
        if (!markerToCatchIdMap.containsKey(clusterItem))
            return false;

        long id = markerToCatchIdMap.get(clusterItem);

        loadCatch(id, true);

        return true;
    }

    @Override
    public void onPageScrolled(long fromCatchId, long toCatchId, float offset) {
        // let's try and scroll the map with the pager
        Catch fromCatch = catches.get(fromCatchId);
        Catch toCatch = catches.get(toCatchId);

        if (fromCatch == null || toCatch == null)
            return;

        LatLng frompos = fromCatch.getPosition();
        LatLng topos = toCatch.getPosition();

        double dlong = Math.pow(frompos.longitude - topos.longitude, 2);
        double dlat = Math.pow(frompos.latitude - topos.latitude, 2);
        double distance = Math.sqrt(dlat + dlong);

        LatLng newpos = new LatLng(frompos.latitude + (topos.latitude - frompos.latitude) * offset,
                                   frompos.longitude + (topos.longitude - frompos.longitude) * offset);


        MoveCamera(newpos, false);
    }

    private class TagIt2Cluster implements Cluster<TagIt2ClusterItem> {

        private List<TagIt2ClusterItem> items = new ArrayList<TagIt2ClusterItem>();

        public void addItem(TagIt2ClusterItem item) {
            items.add(item);
        }

        @Override
        public LatLng getPosition() {

            if (items.size() == 0)
                return new LatLng(0,0);

            if (items.size() == 1)
                return items.get(0).getPosition();

            TagIt2ClusterItem firstItem = items.get(0);
            LatLng latlng = firstItem.getPosition();

            LatLng secondLatLng = items.get(1).getPosition();
            LatLngBounds.Builder bob = new LatLngBounds.Builder();
            for (TagIt2ClusterItem item : items) {
                bob.include(item.getPosition());
            }

            return bob.build().getCenter();
        }

        @Override
        public Collection<TagIt2ClusterItem> getItems() {
            return items;
        }

        @Override
        public int getSize() {
            return items.size();
        }
    }

    private class CustomRenderer<T extends  ClusterItem> extends DefaultClusterRenderer<T>{

        public CustomRenderer(Context context, GoogleMap map, ClusterManager<T> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<T> cluster) {
            return cluster.getSize() > 1;
        }
    }
}
