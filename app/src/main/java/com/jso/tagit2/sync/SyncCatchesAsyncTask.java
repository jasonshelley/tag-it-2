package com.jso.tagit2.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.jso.tagit.backend.fishApi.FishApi;
import com.jso.tagit.backend.fishApi.model.CollectionResponseFish;
import com.jso.tagit.backend.fishApi.model.Fish;
import com.jso.tagit2.SharedPrefsHelper;
import com.jso.tagit2.database.CatchesTable;
import com.jso.tagit2.database.FishersTable;
import com.jso.tagit2.provider.TagIt2Provider;

import java.io.IOException;
import java.util.List;

/**
 * Created by jshelley on 22/03/2017.
 */

public class SyncCatchesAsyncTask extends AsyncTask<Object, String, String> {

    private Context context;
    private ISyncComplete syncCompleteDelegate;

    public SyncCatchesAsyncTask(Context context, ISyncComplete syncCompleteDelegate) {
        this.context = context;
        this.syncCompleteDelegate = syncCompleteDelegate;
    }

    @Override
    protected String doInBackground(Object... params) {

        HttpTransport transport = HttpTransportFactory.getInstance().getTransport();
        JsonFactory jsonFactory = new JacksonFactory();

        FishApi.Builder bob = new FishApi.Builder(transport, jsonFactory, null)
                .setRootUrl("https://tagit-1247.appspot.com/_ah/api")
                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                    @Override
                    public void initialize(AbstractGoogleClientRequest<?> request) throws IOException {
                        request.setDisableGZipContent(true);
                    }
                })
                .setApplicationName("TagIt");

        FishApi api = bob.build();

        SharedPrefsHelper prefs = new SharedPrefsHelper(context);
        long lastSync = prefs.getLastSync(FishersTable.TABLE_NAME);
        String userId = prefs.getLoggedInUser().id;

        try {
            FishApi.GetModified mods = api.getModified(userId, lastSync);
            CollectionResponseFish response = mods.execute();
            List<Fish> fishes = response.getItems();

            ContentResolver resolver = context.getContentResolver();


            if (fishes != null) {
                for (Fish fish : fishes) {
                    Log.i("FishSync", fish.getLocationDescription());
                    ContentValues values = new ContentValues();
                    values.put(CatchesTable.COL_CATCH_ID, fish.getId());
                    values.put(CatchesTable.COL_IS_SYNCED, 1);
                    long lastModified = fish.getLastModified();
                    if (lastModified > lastSync) {
                        lastSync = lastModified;
                    }
                    values.put(CatchesTable.COL_LAST_MODIFIED, fish.getLastModified());

                    String fisherId = fish.getFishermanId();
                    Cursor fisherCursor = resolver.query(TagIt2Provider.Contract.FISHERS_URI, TagIt2Provider.Contract.FISHER_PROJECTION, FishersTable.COL_FISHER_ID + "='" + fisherId + "'", null, null);
                    String fisher = "";
                    if (fisherCursor.moveToFirst()) {
                        fisher = fisherCursor.getString(fisherCursor.getColumnIndex(FishersTable.COL_NAME));
                    }

                    values.put(CatchesTable.COL_FISHER, fisher);
                    values.put(CatchesTable.COL_BAIT, fish.getBait());
                    values.put(CatchesTable.COL_SPECIES, fish.getSpecies());

                    values.put(CatchesTable.COL_LATITUDE, fish.getLatitude());
                    values.put(CatchesTable.COL_LONGITUDE, fish.getLongitude());
                    values.put(CatchesTable.COL_LOCATION_DESC, fish.getLocationDescription());

                    values.put(CatchesTable.COL_LENGTH, fish.getLength());
                    values.put(CatchesTable.COL_WEIGHT, fish.getWeight());

                    values.put(CatchesTable.COL_IMAGE_PATH, fish.getPhotoPath());

                    values.put(CatchesTable.COL_TIMESTAMP, fish.getTimestamp());

                    resolver.insert(TagIt2Provider.Contract.CATCHES_URI, values);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        prefs.setLastSync(CatchesTable.TABLE_NAME, lastSync);

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        syncCompleteDelegate.onSyncComplete();
    }
}
