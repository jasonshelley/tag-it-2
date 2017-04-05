package com.jso.tagit2.fragments;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jso.tagit2.interfaces.IStateManager;
import com.jso.tagit2.R;
import com.jso.tagit2.database.CatchesTable;
import com.jso.tagit2.models.State;
import com.jso.tagit2.provider.TagIt2Provider;
import com.jso.tagit2.utils.BitmapHelper;
import com.jso.tagit2.utils.ImageAsyncLoader;
import com.jso.tagit2.utils.ImageAsyncLoaderTag;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class FishDetailsFragment extends Fragment {

    private IStateManager stateManager;

    public final static String ARG_CATCH_ID = "CATCH_ID";

    public long catchId = -1;

    public View rootView;

    public static FishDetailsFragment newInstance(long catchId)
    {
        FishDetailsFragment fragment = new FishDetailsFragment();

        Bundle args = new Bundle();
        args.putLong(ARG_CATCH_ID, catchId);

        fragment.setArguments(args);

        return fragment;
    }

    public FishDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            catchId = getArguments().getLong(ARG_CATCH_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fish_tile, null);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    stateManager.go(State.EDIT_CATCH, new JSONObject("{ id: " + catchId + "}"), false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        refreshView();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IStateManager) {
            stateManager = (IStateManager) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement IStateManager");
        }
    }

    public void setCatchId(long id)
    {
        catchId = id;
        refreshView();
    }

    private void refreshView() {
        if (rootView.findViewById(R.id.image_fish) != null) {
            TextView textSpecies = (TextView)rootView.findViewById(R.id.text_species);
            TextView textFisher = (TextView)rootView.findViewById(R.id.text_fisher);
            TextView textDateTime = (TextView)rootView.findViewById(R.id.text_datetime);
            TextView textLocation = (TextView)rootView.findViewById(R.id.text_location);
            TextView textLength = (TextView)rootView.findViewById(R.id.text_length);
            TextView textWeight = (TextView)rootView.findViewById(R.id.text_weight);
            ImageView imageView = (ImageView)rootView.findViewById(R.id.image_fish);

            Context context = getActivity();

            Cursor cursor = context.getContentResolver().query(Uri.withAppendedPath(TagIt2Provider.Contract.CATCHES_URI, String.valueOf(catchId)), TagIt2Provider.Contract.CATCHES_PROJECTION, null, null, null);
            if (!cursor.moveToFirst())
                return;

            String species = cursor.getString(cursor.getColumnIndex(CatchesTable.COL_SPECIES));
            String fisher = cursor.getString(cursor.getColumnIndex(CatchesTable.COL_FISHER));
            long datetime = cursor.getLong(cursor.getColumnIndex(CatchesTable.COL_TIMESTAMP));
            String locationDesc = cursor.getString(cursor.getColumnIndex(CatchesTable.COL_LOCATION_DESC));
            String imagePath = cursor.getString(cursor.getColumnIndex(CatchesTable.COL_THUMBNAIL_PATH));

            float length = cursor.getFloat(cursor.getColumnIndex(CatchesTable.COL_LENGTH));
            float weight = cursor.getFloat(cursor.getColumnIndex(CatchesTable.COL_WEIGHT));

            cursor.close();

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MMM-yy");
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = calendar.getTimeZone();
            sdf.setTimeZone(tz);

            textSpecies.setText(species);
            textFisher.setText(fisher);
            textDateTime.setText(sdf.format(new Date(datetime * 1000L)));
            textLocation.setText(locationDesc);
            textLength.setText(String.format("%.0f  cm", length));
            textWeight.setText(String.format("%.1f kg", weight));
            Drawable drawable = imageView.getDrawable();

            if (imagePath != null) {
                Object o = imageView.getTag();
                if (o instanceof ImageAsyncLoaderTag) {
                    imageView.setImageBitmap(null);
                    ImageAsyncLoaderTag currentTag = (ImageAsyncLoaderTag)o;
                    if (currentTag.bmp != null)
                        currentTag.bmp.recycle();
                }
                ImageAsyncLoaderTag tag = new ImageAsyncLoaderTag(imagePath, imageView);
                imageView.setTag(tag);
                ImageAsyncLoader loader = new ImageAsyncLoader(context.getContentResolver(), imageView);
                loader.execute(BitmapHelper.getFileProviderUri(getContext(), imagePath));
            } else
                imageView.setImageBitmap(null);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stateManager = null;
    }
}
