package com.jso.tagit2.controllers;

import android.content.Context;
import android.database.Cursor;
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

public class GenericListCursorAdapter extends CursorAdapter {

    private LayoutInflater layoutInflater;
    private Context context;

    public GenericListCursorAdapter(Context context, Cursor cursor, int flags){
        super(context, cursor, flags);
        this.context = context;
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.single_line, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = (TextView)view.findViewById(R.id.text);

        String text = cursor.getString(cursor.getColumnIndex("Name"));

        textView.setText(text);
    }
}
