package org.dolphin.secret.file;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by hanyanan on 2016/7/25.
 */
public class FileAndUriHelper {
    public static final String TAG = "AndroidFileInfoOperation";

    public static <T> T readAttributes(Context context, String type, String path, final FileAttributeReader<T> fileAttributeReader) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Uri contentUri = null;
        if ("image".equals(type)) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if ("video".equals(type)) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if ("audio".equals(type)) {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else {
            return null;
        }
        path = Uri.decode(path);
        ContentResolver cr = context.getContentResolver();
        final StringBuilder sb = new StringBuilder();
        sb.append("(")
                .append(MediaStore.MediaColumns.DATA)
                .append("=")
                .append("'").append(path).append("'")
                .append(")");
        final FileAttributeReader<T> fileAttributeReaderProxy = new FileAttributeReaderWrapper<T>(fileAttributeReader) {
            @Override
            public String selection() {
                return sb.toString();
            }
        };
        return readAttributes(context, contentUri, fileAttributeReaderProxy);
    }

    /**
     * 根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换
     * http://stackoverflow.com/questions/20067508/get-real-path-from-uri-android-kitkat-new-storage-access-framework
     * http://stackoverflow.com/questions/19834842/android-gallery-on-kitkat-returns-different-uri-for-intent-action-get-content
     * <p/>
     * Before Kitkat (or before the new Gallery) the {@link Intent#ACTION_GET_CONTENT} returned a Uri like this:
     * <b>content://media/external/images/media/3951</b>
     * Using the ContentResolver and quering for  MediaStore.MediaColumns.DATA returned the file URL.
     * In Kitkat however the Gallery returns a Uri (via "Last") like this:
     * <b>content://com.android.providers.media.documents/document/image:3951</b>
     *
     * @param context
     * @param uri
     */
    public static <T> T readAttributes(Context context, Uri uri, final FileAttributeReader<T> fileAttributeReader) {
        if (context == null || uri == null || null == fileAttributeReader) {
            return null;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return fileAttributeReader.read(Environment.getExternalStorageDirectory() + File.separator + split[1]);
                }
            } else if (isDownloadsDocument(uri)) {
                String id = DocumentsContract.getDocumentId(uri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return readAttribute(context, contentUri, fileAttributeReader);
            } else if (isMediaDocument(uri)) {
                // MediaProvider
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = MediaStore.MediaColumns._ID + "=?";
                final String[] selectionArgs = new String[]{split[1]};
                return readAttribute(context, contentUri, new FileAttributeReaderWrapper<T>(fileAttributeReader) {
                    @Override
                    public String[] selectionArgs() {
                        // TODO
                        return selectionArgs;
                    }

                    @Override
                    public String selection() {
                        // TODO
                        return selection;
                    }
                });
            }
        } else {
            final String scheme = uri.getScheme();
            if (TextUtils.isEmpty(scheme)) {
                return fileAttributeReader.read(uri.getPath());
            } else if (ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(scheme)) {
                if (isGooglePhotosUri(uri)) { // Return the remote address
                    return fileAttributeReader.read(uri.getLastPathSegment());
                }
                return readAttribute(context, uri, fileAttributeReader);
            } else if (ContentResolver.SCHEME_FILE.equalsIgnoreCase(scheme)) {
                return fileAttributeReader.read(uri.getPath());
            }
        }
        return fileAttributeReader.read(uri.toString());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static <T> T readAttribute(Context context, Uri uri, FileAttributeReader<T> fileAttributeReader) {
        Cursor cursor = null;
        String column = MediaStore.MediaColumns.DATA;
        String[] projection = fileAttributeReader.projection();
        String selection = fileAttributeReader.selection();
        String[] selectionArgs = fileAttributeReader.selectionArgs();
        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
        if (null == cursor) {
            return null;
        }
        T res = null;
        if (cursor.moveToFirst()) {
            res = fileAttributeReader.read(cursor);
        }
        cursor.close();
        return res;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
