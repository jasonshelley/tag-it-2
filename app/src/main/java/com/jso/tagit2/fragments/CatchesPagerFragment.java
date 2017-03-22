package com.jso.tagit2.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jso.tagit2.ICatchSelected;
import com.jso.tagit2.R;
import com.jso.tagit2.database.CatchesTable;
import com.jso.tagit2.provider.TagIt2Provider;

import java.util.ArrayList;
import java.util.Arrays;

public class CatchesPagerFragment extends Fragment implements ViewPager.OnPageChangeListener {

    ViewPager pager;
    CatchesPagerAdapter adapter;
    ICatchSelected catchSelectedDelegate;

    ContentObserver observer;

    public CatchesPagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment parent = getParentFragment();
        if (parent instanceof ICatchSelected) {
            catchSelectedDelegate = (ICatchSelected)parent;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_catch_pager, container, false);

        pager = (ViewPager)v.findViewById(R.id.catches_pager);
        adapter = new CatchesPagerAdapter(getContext(), getFragmentManager());
        adapter.init();
        pager.setAdapter(adapter);

        pager.addOnPageChangeListener(this);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
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
                adapter.init();
            }
        };

        resolver.registerContentObserver(TagIt2Provider.Contract.CATCHES_URI, true, observer);
    }

    @Override
    public void onPause() {
        super.onPause();
        ContentResolver resolver = getActivity().getContentResolver();
        if (observer != null)
            resolver.unregisterContentObserver(observer);
    }

    public void setCatchId(long id)
    {
        int pos = adapter.getItemPos(id);
        pager.setCurrentItem(pos);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (positionOffsetPixels == 0) {
            if (catchSelectedDelegate != null)
                catchSelectedDelegate.onCatchSelected(adapter.getItemId(position));
        }
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class CatchesPagerAdapter extends FragmentStatePagerAdapter {

        private Context context;
        private ArrayList<Long> ids;


        public CatchesPagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            this.context = context;
        }

        public int getItemPos(long id) {
            return ids.indexOf(id);
        }

        public long getItemId(int position) {
            return ids.get(position);
        }

        public void init()
        {
            ContentResolver resolver = context.getContentResolver();
            Cursor c = resolver.query(TagIt2Provider.Contract.CATCHES_URI, new String[] {CatchesTable.COL_ID }, null, null, CatchesTable.COL_TIMESTAMP + " DESC");
            ids = new ArrayList<>(c.getCount());
            while (c.moveToNext()) {
                ids.add(c.getLong(0));
            }
            c.close();

            this.notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            return FishDetailsFragment.newInstance(ids.get(position));
        }

        @Override
        public int getCount() {
            return ids.size();
        }
    }
}
