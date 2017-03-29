package com.jso.tagit2;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import com.google.gson.Gson;
import com.jso.tagit2.database.CatchesTable;
import com.jso.tagit2.models.Catch;
import com.jso.tagit2.models.State;
import com.jso.tagit2.models.User;
import com.jso.tagit2.provider.TagIt2Provider;

import org.json.JSONObject;

/**
 * Created by jshelley on 21/03/2017.
 */

public class SharedPrefsHelper {

    public final String PREF_USER = "User";
    public final String PREF_STATE = "State";

    Context context;
    SharedPreferences preferences;

    public SharedPrefsHelper(Context context) {
        this.context = context;
    }

    private SharedPreferences getSharedPrefs() {
        if (preferences == null) {
            preferences = context.getSharedPreferences("Common", Context.MODE_PRIVATE);
        }

        return preferences;
    }

    public void setLoggedInUser(User user) {
        setPreference(PREF_USER, user);
    }

    public User getLoggedInUser() {
        return getPreference(PREF_USER, User.class, new User());
    }

    public State getState() {
        return getPreference(PREF_STATE, State.class, new State());
    }

    public void setState(int state, JSONObject args) {
        setPreference(PREF_STATE, new State(state, args));
    }

    public long getLastSync(String tableName) {
        return getPreference("LastSync" + tableName, long.class, 0L) + 1;
//        return 0L;
    }

    public void setLastSync(String tableName, long timestamp) {
        setPreference("LastSync" + tableName, timestamp);
    }

    public void setNewPhotoUri(Uri uri) {
        setPreference("NewPhotoPath", uri);
    }

    public Uri getNewPhotoUri() {
        return getPreference("NewPhotoPath", Uri.class, null);
    }

    public long getLastBackPressTime() {
        return getPreference("LastBackPressTime", Long.class, 0L);
    }

    public void setLastBackPressTime(long time){
        setPreference("LastBackPressTime", time);
    }

    public Catch getCurrentCatch()
    {
        long id = getPreference("CurrentCatchId", Long.class, -1L);
        if (id == -1)
            return null;

        ContentResolver resolver = context.getContentResolver();
        Cursor c = resolver.query(Uri.withAppendedPath(TagIt2Provider.Contract.CATCHES_URI, String.valueOf(id)),
                TagIt2Provider.Contract.CATCHES_PROJECTION, null, null, null);
        Catch currentCatch = null;
        if (c.moveToFirst())
            currentCatch = CatchesTable.fromCursor(c);
        c.close();

        return currentCatch;
    }

    public Uri getCurrentCatchUri() {
        long id = getPreference("CurrentCatchId", Long.class, -1L);
        return Uri.withAppendedPath(TagIt2Provider.Contract.CATCHES_URI, String.valueOf(id));
    }

    public void setCurrentCatchId(long id) {
        setPreference("CurrentCatchId", id);
    }

    private void setPreference(String key, Object pref) {
        SharedPreferences prefs = getSharedPrefs();
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(pref);

        editor.putString(key, json);

        editor.commit();
    }

    private <T> T getPreference(String key, Class<T> classT, T defaultValue) {
        SharedPreferences prefs = getSharedPrefs();

        String pref = prefs.getString(key, "");

        if (pref == null)
            return defaultValue;

        Gson gson = new Gson();
        T o = gson.fromJson(pref, classT);
        if (o == null)
            return defaultValue;

        return o;
    }
}
