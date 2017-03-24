package com.jso.tagit2.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PatternMatcher;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.RegEx;

/**
 * Created by jshelley on 20/03/2017.
 */

public class ImageAsyncLoader extends AsyncTask<Uri, Void, Bitmap> {

    private final int THUMBNAIL_SIZE = 256;

    ContentResolver resolver;
    ImageView imageView;
    ImageAsyncLoaderTag orgTag;

    public ImageAsyncLoader(ContentResolver resolver, ImageView imageView) {
        this.resolver = resolver;
        this.imageView = imageView;
        Object o = imageView.getTag();
        if (o instanceof ImageAsyncLoaderTag)
            orgTag = (ImageAsyncLoaderTag)o;
    }

    @Override
    protected Bitmap doInBackground(Uri... params) {
        try {
            Uri thumbUri = params[0];
            resolver.openInputStream(thumbUri);
            InputStream is = resolver.openInputStream(thumbUri);
            Bitmap bitmap = null;
            if (is != null) {
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            }

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        Object tag = imageView.getTag();

        if (tag != null) {
            if (tag instanceof ImageAsyncLoaderTag) {
                ImageAsyncLoaderTag itag = (ImageAsyncLoaderTag)tag;
                if (orgTag.path == itag.path) { // still the same view... let's do stuff
                    imageView.setImageBitmap(bitmap);
                    Log.d("ImageAsyncLoader", "Recycling bitmap");
                    if (orgTag.bmp != null)
                        orgTag.bmp.recycle();
                    itag.bmp = bitmap;
                    imageView.setTag(itag);
                } else { // too slow
                    Log.d("ImageAsyncLoader", "Not Loading Image... too slow");
                }
            }
        } else {  // no restrictions
            imageView.setImageBitmap(bitmap);
        }
    }

}
