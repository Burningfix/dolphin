package org.dolphin.secret.file;

import android.database.Cursor;

import org.dolphin.secret.picker.AndroidFileInfo;
import org.dolphin.secret.picker.AndroidFileProvider;

/**
 * Created by hanyanan on 2016/7/25.
 */
public class FileInfoAttributeReader implements FileAttributeReader<AndroidFileInfo> {
    @Override
    public String selection() {
        return null;
    }

    @Override
    public String[] selectionArgs() {
        return null;
    }

    @Override
    public String[] projection() {
        return AndroidFileProvider.androidFileInfoColumns();
    }

    @Override
    public AndroidFileInfo read(Cursor cursor) {
        return AndroidFileProvider.readAndroidFileInfoAttribute(cursor);
    }

    @Override
    public AndroidFileInfo read(String filePath) {
        return AndroidFileProvider.readAndroidFileInfoAttribute(filePath);
    }
}
