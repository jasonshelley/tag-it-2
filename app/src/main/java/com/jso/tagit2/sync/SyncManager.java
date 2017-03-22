package com.jso.tagit2.sync;

import android.content.Context;
import android.os.AsyncTask;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by jshelley on 22/03/2017.
 */

public class SyncManager implements ISyncComplete {

    private static SyncManager _this;

    public static SyncManager getInstance(Context context) {

        if (_this == null)
            _this = new SyncManager();

        _this.context = context;

        return _this;
    }

    private Context context;
    private Queue<AsyncTask> tasks;

    public void sync() {
        tasks = new LinkedList<AsyncTask>();
        tasks.add(new SyncBaitsAsyncTask(context, this));
        tasks.add(new SyncFishersAsyncTask(context, this));
        tasks.add(new SyncSpeciesAsyncTask(context, this));
        tasks.add(new SyncCatchesAsyncTask(context, this));

        doNext();
    }

    private void doNext() {
        AsyncTask task = tasks.poll();
        if (task != null)
            task.execute();
    }

    @Override
    public void onSyncComplete() {
        doNext();
    }

}
