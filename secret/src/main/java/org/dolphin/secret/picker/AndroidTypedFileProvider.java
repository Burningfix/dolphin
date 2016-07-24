package org.dolphin.secret.picker;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import org.dolphin.lib.util.ValueUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hanyanan on 2016/2/16.
 */
public class AndroidTypedFileProvider {
    public static final String TAG = "AndroidTypedFileProvider";
    private final String mimeType;
    private final Context context;

    public AndroidTypedFileProvider(String mimeType, Context context) {
        this.mimeType = mimeType;
        this.context = context;
    }

    public Uri getDatabaseUri() {
        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    public static List<AndroidFileInfo> requestSpec(Context context, Uri uri) {
        String[] projection = {MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.TITLE,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.HEIGHT};
        final List<AndroidFileInfo> fileInfos = new LinkedList<AndroidFileInfo>();
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        try {
            if (null != cursor && cursor.moveToFirst()) {
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
                fileInfos.add(androidFileInfo);
            }
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return fileInfos;
    }

    public List<AndroidFileInfo> request() {
        // 指定要查询的uri资源
        Uri uri = getDatabaseUri();
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
//        // 条件
//        String selection = MediaStore.Images.Media.MIME_TYPE + " like ? ";
//        // 条件值(這裡的参数不是图片的格式，而是标准，所有不要改动)
//        String[] selectionArgs = {"image"};
        // 排序
        String sortOrder = MediaStore.MediaColumns.DATE_MODIFIED + " desc";
        List<AndroidFileInfo> androidFileInfoList = new LinkedList<AndroidFileInfo>();
        // 查询sd卡上的图片
        Cursor cursor = contentResolver.query(uri, projection, null, null, sortOrder);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
                while (cursor.moveToNext()) {
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
                    androidFileInfoList.add(androidFileInfo);
                }
            }
        } finally {
            // 关闭cursor
            cursor.close();
        }

        return androidFileInfoList;
    }
}
