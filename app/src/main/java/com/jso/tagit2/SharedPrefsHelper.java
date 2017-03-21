package com.jso.tagit2;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.jso.tagit2.models.State;
import com.jso.tagit2.models.User;

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
        return getPreference(PREF_USER, User.class, null);
    }

    public State getState() {
        return getPreference(PREF_STATE, State.class, new State());
    }

    public void setState(int state, JSONObject args) {
        setPreference(PREF_STATE, new State(state, args));
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
