package com.jso.tagit2.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.jso.tagit2.R;
import com.jso.tagit2.database.CatchesTable;
import com.jso.tagit2.database.IDatabaseTable;
import com.jso.tagit2.provider.TagIt2Provider;
import com.jso.tagit2.utils.ImageAsyncLoader;

public class EditCatchFragment extends Fragment {

    private static final String ARG_CATCH_ID = "catchId";
    ContentObserver observer;

    private Uri catchUri;
    private long catchId;

    private View rootView;
    private ImageButton browseButton;
    private ImageButton takePhotoButton;
    private ImageView imageView;

    public EditCatchFragment() {
        // Required empty public constructor
    }

    public static EditCatchFragment newInstance(long catchId) {
        EditCatchFragment fragment = new EditCatchFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CATCH_ID, catchId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            catchId = getArguments().getLong(ARG_CATCH_ID);
            catchUri = Uri.withAppendedPath(TagIt2Provider.Contract.CATCHES_URI, String.valueOf(catchId));
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
                    refreshView(rootView);
                }
            };

            resolver.registerContentObserver(catchUri, true, observer);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_edit_catch, container, false);

        refreshView(rootView);

        return rootView;
    }

    private void refreshView(View v) {
        browseButton = (ImageButton)v.findViewById(R.id.btn_browse_image);
        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        takePhotoButton = (ImageButton)v.findViewById(R.id.btn_take_photo);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        imageView = (ImageView)v.findViewById(R.id.image_catch);
        ContentResolver resolver = getContext().getContentResolver();
        Cursor c = resolver.query(catchUri, TagIt2Provider.Contract.CATCHES_PROJECTION, null, null, null);

        if (c.moveToFirst()) {
            String imagePath = c.getString(c.getColumnIndex(CatchesTable.COL_IMAGE_PATH));
            if (imagePath != null) {
                ImageAsyncLoader loader = new ImageAsyncLoader(resolver, imageView);
                loader.execute(Uri.parse(imagePath));
            } else
                imageView.setImageBitmap(null);
        } else
            imageView.setImageBitmap(null);
        c.close();
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED)
            return;

        if (requestCode == 1) {
            Uri uri = data.getData();
            ContentValues values = new ContentValues();
            values.put(CatchesTable.COL_IMAGE_PATH, uri.toString());
            getActivity().getContentResolver().update(Uri.withAppendedPath(TagIt2Provider.Contract.CATCHES_URI, String.valueOf(catchId)),
                    values, null, null);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
