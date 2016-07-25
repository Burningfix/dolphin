package org.dolphin.secret.file;

import android.database.Cursor;

/**
 * Created by hanyanan on 2016/7/25.
 */
public class FileAttributeReaderWrapper<T> implements FileAttributeReader<T> {
    private final FileAttributeReader<T> fileAttributeReader;

    public FileAttributeReaderWrapper(FileAttributeReader<T> fileAttributeReader) {
        this.fileAttributeReader = fileAttributeReader;
    }

    @Override
    public String selection() {
        return this.fileAttributeReader.selection();
    }

    @Override
    public String[] selectionArgs() {
        return this.fileAttributeReader.selectionArgs();
    }

    @Override
    public String[] projection() {
        return this.fileAttributeReader.projection();
    }

    @Override
    public T read(Cursor cursor) {
        return this.fileAttributeReader.read(cursor);
    }

    @Override
    public T read(String filePath) {
        return this.fileAttributeReader.read(filePath);
    }
}
