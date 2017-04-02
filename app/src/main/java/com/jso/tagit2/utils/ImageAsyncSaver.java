package com.jso.tagit2.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import com.jso.tagit2.database.CatchesTable;
import com.jso.tagit2.provider.TagIt2Provider;
import com.jso.tagit2.sync.ISyncComplete;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * Created by jshelley on 23/03/2017.
 */

public class ImageAsyncSaver extends AsyncTask<Void, Void, Void> {

    Context context;
    Uri uri;
    long catchId;
    boolean thumbnailOnly;  // set to true if this comes from a photo that we took, so we already have the saved image

    public ImageAsyncSaver(Context context, long catchId, Uri uri, boolean thumbnailOnly) {
        this.context = context;
        this.uri = uri;
        this.catchId = catchId;
        this.thumbnailOnly = thumbnailOnly;
    }

    @Override
    protected Void doInBackground(Void... params) {

        ContentResolver resolver = context.getContentResolver();
        String filePath = "";
        Uri outputUri = null;

        try {
            FileProvider fp = new FileProvider();
            if (!thumbnailOnly) {   // we need to copy the file from the uri provided
                InputStream is = resolver.openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                is.close();

                File file = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                String dataPath = file.getPath();

                String fileName = BitmapHelper.getImageName();
                File output = new File(dataPath, fileName);
                outputUri = BitmapHelper.getFileProviderUri(context, output);
                filePath = output.getPath();

                OutputStream fos = resolver.openOutputStream(outputUri);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
            } else {
                outputUri = uri;
                filePath = uri.getPath();
            }

            Bitmap thumbnail = BitmapHelper.resizeBitmap(context, uri, 256);
            String thumbnailPath = BitmapHelper.getThumbnailPath(context, filePath);
            File thumbnailFile = new File(thumbnailPath);
            Uri thumbnailUri = BitmapHelper.getFileProviderUri(context, thumbnailPath);
            OutputStream fos = resolver.openOutputStream(thumbnailUri);
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            ContentValues values = new ContentValues();
            values.put(CatchesTable.COL_IMAGE_PATH, filePath);
            values.put(CatchesTable.COL_THUMBNAIL_PATH, thumbnailPath);
            context.getContentResolver().update(Uri.withAppendedPath(TagIt2Provider.Contract.CATCHES_URI, String.valueOf(catchId)),
                    values, null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
