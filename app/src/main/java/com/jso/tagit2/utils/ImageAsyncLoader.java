package com.jso.tagit2.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
            InputStream input = resolver.openInputStream(params[0]);

            BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
            onlyBoundsOptions.inJustDecodeBounds = true;
            onlyBoundsOptions.inDither=true;//optional
            onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
            input.close();

            if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
                return null;
            }

            int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

            double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
            bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//
            input = resolver.openInputStream(params[0]);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
            input.close();

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
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
