package com.jso.tagit2.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.jso.tagit.backend.baitApi.BaitApi;
import com.jso.tagit.backend.baitApi.model.Bait;
import com.jso.tagit.backend.baitApi.model.CollectionResponseBait;
import com.jso.tagit2.SharedPrefsHelper;
import com.jso.tagit2.database.BaitsTable;
import com.jso.tagit2.provider.TagIt2Provider;

import java.io.IOException;
import java.util.List;

/**
 * Created by jshelley on 22/03/2017.
 */

public class SyncBaitsAsyncTask extends AsyncTask<Object, String, String> {

    private Context context;
    private ISyncComplete syncCompleteDelegate;

    public SyncBaitsAsyncTask(Context context, ISyncComplete syncCompleteDelegate) {
        this.context = context;
        this.syncCompleteDelegate = syncCompleteDelegate;
    }

    @Override
    protected String doInBackground(Object... params) {

        HttpTransport transport = HttpTransportFactory.getInstance().getTransport();
        JsonFactory jsonFactory = new JacksonFactory();

        BaitApi.Builder bob = new BaitApi.Builder(transport, jsonFactory, null)
                .setRootUrl("https://tagit-1247.appspot.com/_ah/api")
                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                    @Override
                    public void initialize(AbstractGoogleClientRequest<?> request) throws IOException {
                        request.setDisableGZipContent(true);
                    }
                })
                .setApplicationName("TagIt");

        BaitApi api = bob.build();

        SharedPrefsHelper prefs = new SharedPrefsHelper(context);
        long lastSync = prefs.getLastSync(BaitsTable.TABLE_NAME);

        try {
            BaitApi.GetModified mods = api.getModified(lastSync);
            CollectionResponseBait response = mods.execute();
            List<Bait> baits = response.getItems();

            ContentResolver resolver = context.getContentResolver();

            if (baits != null) {
                for (Bait bait : baits) {
                    Log.i("BaitSync", bait.getDescription());
                    ContentValues values = new ContentValues();
                    values.put(BaitsTable.COL_NAME, bait.getDescription());
                    values.put(BaitsTable.COL_BAIT_ID, bait.getId());
                    values.put(BaitsTable.COL_SELECTION_COUNT, bait.getSelectionCount());
                    values.put(BaitsTable.COL_IS_SYNCED, 1);
                    long lastModified = bait.getLastModified();
                    if (lastModified > lastSync) {
                        lastSync = lastModified;
                    }
                    values.put(BaitsTable.COL_LAST_MODIFIED, bait.getLastModified());
                    resolver.insert(TagIt2Provider.Contract.BAITS_URI, values);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        prefs.setLastSync(BaitsTable.TABLE_NAME, lastSync);

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        syncCompleteDelegate.onSyncComplete();
    }
}
