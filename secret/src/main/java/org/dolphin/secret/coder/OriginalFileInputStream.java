package org.dolphin.secret.coder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hanyanan on 2016/1/15.
 */
public class OriginalFileInputStream extends InputStream {
    private final File file;

    public OriginalFileInputStream(File file) {
        this.file = file;
    }


    @Override
    public int read() throws IOException {
        return 0;
    }
}
