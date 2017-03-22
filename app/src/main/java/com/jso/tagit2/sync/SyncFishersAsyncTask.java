package com.jso.tagit2.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.jso.tagit.backend.fishermanApi.FishermanApi;
import com.jso.tagit.backend.fishermanApi.model.CollectionResponseFisherman;
import com.jso.tagit.backend.fishermanApi.model.Fisherman;
import com.jso.tagit2.SharedPrefsHelper;
import com.jso.tagit2.database.FishersTable;
import com.jso.tagit2.provider.TagIt2Provider;

import java.io.IOException;
import java.util.List;

/**
 * Created by jshelley on 22/03/2017.
 */

public class SyncFishersAsyncTask extends AsyncTask<Object, String, String> {

    private Context context;
    private ISyncComplete syncCompleteDelegate;

    public SyncFishersAsyncTask(Context context, ISyncComplete syncCompleteDelegate) {
        this.context = context;
        this.syncCompleteDelegate = syncCompleteDelegate;
    }

    @Override
    protected String doInBackground(Object... params) {

        HttpTransport transport = HttpTransportFactory.getInstance().getTransport();
        JsonFactory jsonFactory = new JacksonFactory();

        FishermanApi.Builder bob = new FishermanApi.Builder(transport, jsonFactory, null)
                .setRootUrl("https://tagit-1247.appspot.com/_ah/api")
                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                    @Override
                    public void initialize(AbstractGoogleClientRequest<?> request) throws IOException {
                        request.setDisableGZipContent(true);
                    }
                })
                .setApplicationName("TagIt");

        FishermanApi api = bob.build();

        SharedPrefsHelper prefs = new SharedPrefsHelper(context);
        long lastSync = prefs.getLastSync(FishersTable.TABLE_NAME);
        String userId = prefs.getLoggedInUser().id;

        try {
            FishermanApi.GetModified mods = api.getModified(userId, lastSync);
            CollectionResponseFisherman response = mods.execute();
            List<Fisherman> fishers = response.getItems();

            ContentResolver resolver = context.getContentResolver();

            if (fishers != null) {
                for (Fisherman fisher : fishers) {
                    Log.i("FisherSync", fisher.getName());
                    ContentValues values = new ContentValues();
                    values.put(FishersTable.COL_NAME, fisher.getName());
                    values.put(FishersTable.COL_FISHER_ID, fisher.getId());
                    values.put(FishersTable.COL_IS_SYNCED, 1);

                    long lastModified = fisher.getLastModified();
                    if (lastModified > lastSync) {
                        lastSync = lastModified;
                    }
                    values.put(FishersTable.COL_LAST_MODIFIED, fisher.getLastModified());

                    resolver.insert(TagIt2Provider.Contract.FISHERS_URI, values);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        prefs.setLastSync(FishersTable.TABLE_NAME, lastSync);

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        syncCompleteDelegate.onSyncComplete();
    }
}
