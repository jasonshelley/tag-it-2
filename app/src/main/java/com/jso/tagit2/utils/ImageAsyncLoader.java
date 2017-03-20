package com.jso.tagit2.utils;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jshelley on 20/03/2017.
 */

public class ImageAsyncLoader extends AsyncTask<Uri, Void, Bitmap> {

    private final int THUMBNAIL_SIZE = 300;

    ContentResolver resolver;
    ImageView imageView;
    String tag;

    public ImageAsyncLoader(ContentResolver resolver, ImageView imageView) {
        this.resolver = resolver;
        this.imageView = imageView;
        tag = (String)imageView.getTag();
    }

    @Override
    protected Bitmap doInBackground(Uri... params) {
        Bitmap bmp = null;
        try {
            bmp = MediaStore.Images.Media.getBitmap(resolver, params[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmp;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageView.getTag() == tag)
            imageView.setImageBitmap(bitmap);
        else
            Log.i("ImageAsyncLoader", "Took too long... different item now.");
    }

}
