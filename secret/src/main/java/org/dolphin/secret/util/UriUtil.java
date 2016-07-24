package org.dolphin.secret.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by hanyanan on 2016/7/22.
 */
public class UriUtil {
    /**
     * Try to return the absolute file path from the given Uri
     *
     * @param context
     * @param uri,    such as content://media/external/images/media/62026
     * @return the file path or null such as /sdcard/0/a.jpg
     */
    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            String pathColumnName = MediaStore.MediaColumns.DATA;
            Cursor cursor = context.getContentResolver().query(uri, new String[]{pathColumnName}, null, null, null);
            if (null != cursor && cursor.moveToFirst()) {
                data = cursor.getString(cursor.getColumnIndex(pathColumnName));
            }
            if (null != cursor) {
                cursor.close();
            }
        }
        return data;
    }

    String type = Utils.ensureNotNull(intent.getType());
    Log.d(TAG, "uri is " + uri);
    if (uri.getScheme().equals("file") && (type.contains("image/"))) {
        String path = uri.getEncodedPath();
        Log.d(TAG, "path1 is " + path);
        if (path != null) {
            path = Uri.decode(path);
            Log.d(TAG, "path2 is " + path);
            ContentResolver cr = this.getContentResolver();
            StringBuffer buff = new StringBuffer();
            buff.append("(")
                    .append(Images.ImageColumns.DATA)
                    .append("=")
                    .append("'" + path + "'")
                    .append(")");
            Cursor cur = cr.query(
                    Images.Media.EXTERNAL_CONTENT_URI,
                    new String[] { Images.ImageColumns._ID },
                    buff.toString(), null, null);
            int index = 0;
            for (cur.moveToFirst(); !cur.isAfterLast(); cur
                    .moveToNext()) {
                index = cur.getColumnIndex(Images.ImageColumns._ID);
                // set _id value
                index = cur.getInt(index);
            }
            if (index == 0) {
                //do nothing
            } else {
                Uri uri_temp = Uri
                        .parse("content://media/external/images/media/"
                                + index);
                Log.d(TAG, "uri_temp is " + uri_temp);
                if (uri_temp != null) {
                    uri = uri_temp;
                }
            }
        }
    }
}
