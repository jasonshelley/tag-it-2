package com.jso.tagit2.models;


import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by jshelley on 21/03/2017.
 */

public class TagIt2ClusterItem implements ClusterItem{

    LatLng position;
    String title;
    String snippet;

    public TagIt2ClusterItem(LatLng position, String title, String snippet) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public String getTitle() {
        return title;
    }

    public String getSnippet() {
        return snippet;
    }

}
