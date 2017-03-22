package com.jso.tagit2.fragments;

import android.app.Activity;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.Context;
import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;

import com.jso.tagit2.IStateManager;
import com.jso.tagit2.R;
import com.jso.tagit2.controllers.FishCursorAdapter;
import com.jso.tagit2.database.CatchesTable;
import com.jso.tagit2.database.IDatabaseTable;
import com.jso.tagit2.models.State;
import com.jso.tagit2.provider.TagIt2Provider;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A placeholder fragment containing a simple view.
 */
public class FishListFragment extends ListFragment implements OnItemClickListener, LoaderCallbacks<Cursor> {

    private Activity parentActivity;
    private Context context;
    FishCursorAdapter adapter;

    IStateManager stateManager;

    long selectedFishId;

    public static FishListFragment newInstance() {
        FishListFragment fragment = new FishListFragment();

        return fragment;
    }

    public FishListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_fish_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        parentActivity = getActivity();

        adapter = new FishCursorAdapter(parentActivity, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        setListAdapter(adapter);
        getLoaderManager().initLoader(0, null, this);

        getListView().setOnItemClickListener(this);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedFishId = id;

        try {
            stateManager.go(State.MAP, new JSONObject(String.format("{id: %s}", id)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void SelectImage() {
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
            values.put(IDatabaseTable.COL_ID, selectedFishId);
            values.put(CatchesTable.COL_IMAGE_PATH, uri.toString());
            getActivity().getContentResolver().update(TagIt2Provider.Contract.CATCHES_URI, values, null, null);
        }
    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new CursorLoader(parentActivity,
                TagIt2Provider.Contract.CATCHES_URI,
                TagIt2Provider.Contract.CATCHES_PROJECTION,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
