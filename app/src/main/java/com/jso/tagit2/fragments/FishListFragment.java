package com.jso.tagit2.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;

import com.jso.tagit2.INavigator;
import com.jso.tagit2.MainActivity;
import com.jso.tagit2.R;
import com.jso.tagit2.controllers.FishCursorAdapter;
import com.jso.tagit2.provider.TagIt2Provider;

/**
 * A placeholder fragment containing a simple view.
 */
public class FishListFragment extends ListFragment implements OnItemClickListener, LoaderCallbacks<Cursor> {

    private Activity parentActivity;
    private Context context;
    FishCursorAdapter adapter;

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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        INavigator nav = (INavigator)parentActivity;
        nav.showCatch(id);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new CursorLoader(parentActivity,
                TagIt2Provider.Contract.CATCHES_URI,
                TagIt2Provider.Contract.CATCHES_VIEW_PROJECTION,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
