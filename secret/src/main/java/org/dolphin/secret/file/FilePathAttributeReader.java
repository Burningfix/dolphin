package org.dolphin.secret.file;

import android.database.Cursor;
import android.provider.MediaStore;

/**
 * Created by hanyanan on 2016/7/25.
 * <p/>
 * 得到文件的路径
 */
public class FilePathAttributeReader implements FileAttributeReader<String> {
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
        return new String[]{MediaStore.MediaColumns.DATA};
    }

    @Override
    public String read(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
    }

    @Override
    public String read(String filePath) {
        return filePath;
    }
}
