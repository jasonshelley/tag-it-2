package com.jso.tagit2.controllers;

import android.content.Context;
import android.database.Cursor;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.jso.tagit2.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by JSHELLEY on 8/03/2017.
 */

public class FishCursorAdapter extends CursorAdapter {

    private LayoutInflater layoutInflater;

    public FishCursorAdapter(Context context, Cursor cursor, int flags){
        super(context, cursor, flags);
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.fish_row, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textSpecies = (TextView)view.findViewById(R.id.text_species);
        TextView textFisher = (TextView)view.findViewById(R.id.text_fisher);
        TextView textDateTime = (TextView)view.findViewById(R.id.text_datetime);
        TextView textLocation = (TextView)view.findViewById(R.id.text_location);

        String species = cursor.getString(cursor.getColumnIndex("Species"));
        String fisher = cursor.getString(cursor.getColumnIndex("Fisher"));
        long datetime = cursor.getLong(cursor.getColumnIndex("Timestamp"));
        String locationDesc = cursor.getString(cursor.getColumnIndex("LocationDesc"));

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MMM-yy");

        textSpecies.setText(species);
        textFisher.setText(fisher);
        textDateTime.setText(sdf.format(new Date(datetime)));
        textLocation.setText(locationDesc);
    }
}
