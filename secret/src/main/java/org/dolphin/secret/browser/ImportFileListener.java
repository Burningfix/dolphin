package org.dolphin.secret.browser;

import org.dolphin.secret.core.FileInfo;

/**
 * Created by yananh on 2016/3/29.
 */
public interface ImportFileListener {
    void onImportSuccess(String filePath, FileInfo obscureFile);

    void onImportFailed(String filePath, Throwable error);
}
