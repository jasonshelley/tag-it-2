package com.jso.tagit2.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jshelley on 23/03/2017.
 */

public class BitmapHelper {
    public static Bitmap resizeBitmap(Context context, Uri uri, int size) throws IOException {

        InputStream is = context.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither=true;//optional
        onlyBoundsOptions.inPreferredConfig= Bitmap.Config.RGB_565;//optional
        BitmapFactory.decodeStream(is, new Rect(), onlyBoundsOptions);
        is.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
            return null;
        }

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > size) ? (originalSize / size) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inPreferredConfig=Bitmap.Config.RGB_565;  // no need for an alpha channel... this is a jpg

        is = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(is, new Rect(), bitmapOptions);

        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }

    public static String getThumbnailPath(String imagePath) {
        Matcher m = Pattern.compile("(.+2\\.\\w+.)(\\w+)").matcher(imagePath);

        if (m.find())
            return m.group(1) + "thumbnail." + m.group(2);  // will always be png but may not for ever

        return imagePath;
    }

    public static Uri getFileProviderUri(Context context, File f) {
        Uri uri = null;
        try {
            uri = FileProvider.getUriForFile(context,
                    "com.jso.tagit2.fileprovider",
                    f);
        } catch (Exception e) {
            return null;
        }

        return uri;
    }

    public static Uri getFileProviderUri(Context context, String path) {
        File f = new File(path);
        return getFileProviderUri(context, f);
    }

    public static String getImageName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return String.format("tagit2.%s.jpg", timeStamp);

    }
}
