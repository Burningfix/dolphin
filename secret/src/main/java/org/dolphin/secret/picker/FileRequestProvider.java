package org.dolphin.secret.picker;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import org.dolphin.lib.ValueUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hanyanan on 2016/2/16.
 */
public class FileRequestProvider {
    public static final String TAG = "FileRequestProvider";
    private final String mimeType;
    private final Context context;

    public FileRequestProvider(String mimeType, Context context) {
        this.mimeType = mimeType;
        this.context = context;
    }

    public List<FileEntry> request() {
        // 指定要查询的uri资源
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
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
        // 条件
        String selection = MediaStore.Images.Media.MIME_TYPE + "=?";
        // 条件值(這裡的参数不是图片的格式，而是标准，所有不要改动)
        String[] selectionArgs = {this.mimeType};
        // 排序
        String sortOrder = MediaStore.MediaColumns.DATE_MODIFIED + " desc";
        List<FileEntry> fileEntryList = new LinkedList<FileEntry>();
        // 查询sd卡上的图片
        Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
                while (cursor.moveToNext()) {
                    FileEntry fileEntry = new FileEntry();
                    // 获得图片的id
                    fileEntry.id = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                    // 获得图片显示的名称
                    fileEntry.name = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                    // 获得图片的大小信息
                    fileEntry.size = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE));
                    // 获得图片所在的路径(可以使用路径构建URI)
                    fileEntry.path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                    // 最后修改时间
                    fileEntry.lastModifyTime = ValueUtil.parseInt(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)), -1);
                    // mimeType
                    fileEntry.mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
                    // width(如果有)
                    fileEntry.width = ValueUtil.parseInt(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)), -1);
                    // height(如果有)
                    fileEntry.height = ValueUtil.parseInt(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)), -1);
                    fileEntryList.add(fileEntry);
                }
            }
        } finally {
            // 关闭cursor
            cursor.close();
        }

        return fileEntryList;
    }


    public static class FileEntry {
        public String id;
        public String name;
        public String path;
        public long size;
        public long lastModifyTime;
        public String mimeType;
        public boolean isFolder;
        public int width;
        public int height;

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
}
