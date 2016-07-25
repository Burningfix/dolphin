package org.dolphin.secret.file;

import android.database.Cursor;

/**
 * Created by hanyanan on 2016/7/25.
 */
public interface FileAttributeReader<T> {
    String selection();

    String[] selectionArgs();

    String[] projection();

    T read(Cursor cursor);

    T read(String filePath);
}
