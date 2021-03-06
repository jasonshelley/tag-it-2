package com.jso.tagit2.controllers;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jso.tagit2.R;
import com.jso.tagit2.database.CatchesTable;
import com.jso.tagit2.utils.BitmapHelper;
import com.jso.tagit2.utils.ImageAsyncLoader;
import com.jso.tagit2.utils.ImageAsyncLoaderTag;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by JSHELLEY on 8/03/2017.
 */

public class FishCursorAdapter extends CursorAdapter {

    private LayoutInflater layoutInflater;
    private Context context;

    public FishCursorAdapter(Context context, Cursor cursor, int flags){
        super(context, cursor, flags);
        this.context = context;
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.fish_tile, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textSpecies = (TextView)view.findViewById(R.id.text_species);
        TextView textFisher = (TextView)view.findViewById(R.id.text_fisher);
        TextView textDateTime = (TextView)view.findViewById(R.id.text_datetime);
        TextView textLocation = (TextView)view.findViewById(R.id.text_location);
        TextView textLength = (TextView)view.findViewById(R.id.text_length);
        TextView textWeight = (TextView)view.findViewById(R.id.text_weight);
        ImageView imageView = (ImageView)view.findViewById(R.id.image_fish);

        String species = cursor.getString(cursor.getColumnIndex("Species"));
        String fisher = cursor.getString(cursor.getColumnIndex("Fisher"));
        long datetime = cursor.getLong(cursor.getColumnIndex("Timestamp"));
        String locationDesc = cursor.getString(cursor.getColumnIndex("LocationDesc"));
        String imagePath = cursor.getString(cursor.getColumnIndex(CatchesTable.COL_THUMBNAIL_PATH));

        float length = cursor.getFloat(cursor.getColumnIndex(CatchesTable.COL_LENGTH));
        float weight = cursor.getFloat(cursor.getColumnIndex(CatchesTable.COL_WEIGHT));

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MMM-yy");
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = calendar.getTimeZone();
        sdf.setTimeZone(tz);

        textSpecies.setText(species);
        textFisher.setText(fisher);
        textDateTime.setText(sdf.format(new Date(datetime * 1000L)));
        textLocation.setText(locationDesc);
        textLength.setText(String.format("%.0f cm", length));
        textWeight.setText(String.format("%.1f kg", weight));
        Object o = imageView.getTag();
        if (o instanceof ImageAsyncLoaderTag) {
            imageView.setImageBitmap(null);
            ImageAsyncLoaderTag currentTag = (ImageAsyncLoaderTag)o;
            if (currentTag.bmp != null)
                currentTag.bmp.recycle();
        }
        if (imagePath != null) {
            ImageAsyncLoaderTag tag = new ImageAsyncLoaderTag(imagePath, imageView);
            imageView.setTag(tag);
            ImageAsyncLoader loader = new ImageAsyncLoader(context.getContentResolver(), imageView);
            loader.execute(BitmapHelper.getFileProviderUri(context, imagePath));
        } else {
            ImageAsyncLoaderTag tag = new ImageAsyncLoaderTag(null, imageView);
            imageView.setTag(tag);
            imageView.setImageBitmap(null);
        }
    }
}
