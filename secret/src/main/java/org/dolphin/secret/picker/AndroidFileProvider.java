package org.dolphin.secret.picker;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import org.dolphin.http.MimeType;
import org.dolphin.lib.util.ValueUtil;
import org.dolphin.secret.SecretApplication;
import org.dolphin.secret.file.FileAndUriHelper;
import org.dolphin.secret.file.FileAttributeReader;
import org.dolphin.secret.file.FileInfoAttributeReader;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hanyanan on 2016/2/16.
 */
public class AndroidFileProvider {
    public static final String TAG = "AndroidTypedFileProvider";
    public static final FileInfoAttributeReader FILE_INFO_ATTRIBUTE_READER =
            new FileInfoAttributeReader();

    public static Uri getMediaStoreContentUri(String type) {
        Uri contentUri = null;
        if ("image".equals(type)) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if ("video".equals(type)) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if ("audio".equals(type)) {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        return contentUri;
    }

    public static AndroidFileInfo requestFile(Context context, Uri fileUri) {
        return FileAndUriHelper.readAttributes(context, fileUri, FILE_INFO_ATTRIBUTE_READER);
    }

    public static List<AndroidFileInfo> requestType(Context context, String type) {
        // 指定要查询的uri资源
        Uri uri = getMediaStoreContentUri(type);
        if (null == uri) {
            return null;
        }
        // 获取ContentResolver
        ContentResolver contentResolver = context.getContentResolver();
        // 查询的字段
        String[] projection = {MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.TITLE,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.HEIGHT};
        // 排序
        String sortOrder = MediaStore.MediaColumns.DATE_MODIFIED + " desc";
        List<AndroidFileInfo> androidFileInfoList = new LinkedList<AndroidFileInfo>();
        // 查询sd卡上的图片
        Cursor cursor = contentResolver.query(uri, projection, null, null, sortOrder);
        if (null == cursor) {
            return null;
        }
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            androidFileInfoList.add(readAndroidFileInfoAttribute(cursor));
        }
        cursor.close();
        return androidFileInfoList;
    }

    public static String[] androidFileInfoColumns() {
        String[] projection = {MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.TITLE,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.HEIGHT};
        return projection;
    }

    public static AndroidFileInfo readAndroidFileInfoAttribute(Cursor cursor) {
        AndroidFileInfo androidFileInfo = new AndroidFileInfo();
        // 获得图片的id
        androidFileInfo.id = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
        // 获得图片显示的名称
        androidFileInfo.name = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
        // 获得图片的大小信息
        androidFileInfo.size = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE));
        // 获得图片所在的路径(可以使用路径构建URI)
        androidFileInfo.path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
        // 最后修改时间
        androidFileInfo.lastModifyTime = ValueUtil.parseInt(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)), -1);
        // mimeType
        androidFileInfo.mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
        // width(如果有)
        androidFileInfo.width = ValueUtil.parseInt(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)), -1);
        // height(如果有)
        androidFileInfo.height = ValueUtil.parseInt(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)), -1);
        return androidFileInfo;
    }

    public static AndroidFileInfo readAndroidFileInfoAttribute(String filePath) {
        MimeType mimeType = MimeType.createFromFileName(filePath);
        String type;
        if (mimeType.getMimeType().startsWith("image")) {
            type = "image";
        } else if (mimeType.getMimeType().startsWith("video")) {
            type = "video";
        } else if (mimeType.getMimeType().startsWith("audio")) {
            type = "audio";
        } else {
            // Not support type
            return null;
        }

        return FileAndUriHelper.readAttributes(SecretApplication.getInstance(), type, filePath, FILE_INFO_ATTRIBUTE_READER);
    }
}
