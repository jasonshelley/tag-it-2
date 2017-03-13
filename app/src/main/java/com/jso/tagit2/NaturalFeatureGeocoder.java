package com.jso.tagit2;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by JSHELLEY on 13/03/2017.
 */

public class NaturalFeatureGeocoder extends AsyncTask<LatLng, Void, String> {

    Context context;
    IGeocodeComplete delegate;

    public NaturalFeatureGeocoder(Context context, IGeocodeComplete delegate) {
        this.context = context;
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(LatLng... params) {

        String host = "maps.googleapis.com";
        String path = "maps/api/geocode/json";

        LatLng latlng = params[0];

        StringBuilder bob = new StringBuilder();
        bob.append("https://");
        bob.append(host);
        bob.append("/");
        bob.append(path);
        bob.append("?");
        bob.append(String.format("latlng=%1$f,%2$f", latlng.latitude, latlng.longitude));
        //bob.append("&");
        //bob.append("result_type=natural_feature");
        bob.append("&");
        bob.append("key=" + context.getResources().getString(R.string.google_api_key));

        HttpsURLConnection connection = null;

        try {
            URL url = new URL(bob.toString());
            connection = (HttpsURLConnection)url.openConnection();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()
                    )
            );
            bob = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                bob.append(line).append("\n");
            }
            reader.close();

            return bob.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        try {
            JSONObject result = new JSONObject(s);
            String status = result.getString("status");
            if (status.equals("OK")) {
                JSONArray results = result.getJSONArray("results");
                if (results.length() > 0) {
                    s = results.getJSONObject(0).getString("formatted_address");
                }
            }
        } catch (JSONException e) {
            s = "not found";
            e.printStackTrace();
        }

        delegate.onComplete(s);
    }

    public interface IGeocodeComplete {
        public void onComplete(String locationDesc);
    }
}
