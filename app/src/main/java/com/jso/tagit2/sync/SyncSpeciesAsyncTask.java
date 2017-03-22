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
import com.jso.tagit.backend.baitApi.BaitApi;
import com.jso.tagit.backend.baitApi.model.Bait;
import com.jso.tagit.backend.baitApi.model.CollectionResponseBait;
import com.jso.tagit.backend.speciesApi.SpeciesApi;
import com.jso.tagit.backend.speciesApi.model.CollectionResponseSpecies;
import com.jso.tagit.backend.speciesApi.model.Species;
import com.jso.tagit2.SharedPrefsHelper;
import com.jso.tagit2.database.BaitsTable;
import com.jso.tagit2.database.SpeciesTable;
import com.jso.tagit2.provider.TagIt2Provider;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by jshelley on 22/03/2017.
 */

public class SyncSpeciesAsyncTask extends AsyncTask<Object, String, String> {

    private Context context;
    private ISyncComplete syncCompleteDelegate;

    public SyncSpeciesAsyncTask(Context context, ISyncComplete syncCompleteDelegate) {
        this.context = context;
        this.syncCompleteDelegate = syncCompleteDelegate;
    }

    @Override
    protected String doInBackground(Object... params) {

        HttpTransport transport = HttpTransportFactory.getInstance().getTransport();
        JsonFactory jsonFactory = new JacksonFactory();

        SpeciesApi.Builder bob = new SpeciesApi.Builder(transport, jsonFactory, null)
                .setRootUrl("https://tagit-1247.appspot.com/_ah/api")
                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                    @Override
                    public void initialize(AbstractGoogleClientRequest<?> request) throws IOException {
                        request.setDisableGZipContent(true);
                    }
                })
                .setApplicationName("TagIt");

        SpeciesApi api = bob.build();

        SharedPrefsHelper prefs = new SharedPrefsHelper(context);
        long lastSync = prefs.getLastSync(SpeciesTable.TABLE_NAME);

        try {
            SpeciesApi.GetModified mods = api.getModified(lastSync);
            CollectionResponseSpecies response = mods.execute();
            List<Species> speciess = response.getItems();

            ContentResolver resolver = context.getContentResolver();

            if (speciess != null) {
                for (Species species : speciess) {
                    Log.i("SpeciesSync", species.getName());
                    ContentValues values = new ContentValues();
                    values.put(SpeciesTable.COL_NAME, species.getName());
                    values.put(SpeciesTable.COL_SPECIES_ID, species.getId());
                    values.put(SpeciesTable.COL_SELECTION_COUNT, species.getSelectionCount());
                    values.put(SpeciesTable.COL_IS_SYNCED, 1);
                    long lastModified = species.getLastModified();
                    if (lastModified > lastSync) {
                        lastSync = lastModified;
                    }
                    values.put(SpeciesTable.COL_LAST_MODIFIED, species.getLastModified());
                    resolver.insert(TagIt2Provider.Contract.SPECIES_URI, values);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        prefs.setLastSync(SpeciesTable.TABLE_NAME, lastSync);

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        syncCompleteDelegate.onSyncComplete();
    }
}
