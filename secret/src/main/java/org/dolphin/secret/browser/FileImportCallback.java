package org.dolphin.secret.browser;

import org.dolphin.secret.core.FileInfo;

import java.util.List;

/**
 * Created by hanyanan on 2016/3/29.
 */
public interface FileImportCallback {
    void onImportSuccess(String originalPath, FileInfo obscureFile);

    void onImportFailed(String originalPath, Exception exception);
}
