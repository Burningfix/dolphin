package org.dolphin.secret.browser;

import org.dolphin.secret.core.ObscureFileInfo;

/**
 * Created by yananh on 2016/3/29.
 */
public interface ImportFileListener {
    void onImportSuccess(String filePath, ObscureFileInfo obscureFile);

    void onImportFailed(String filePath, Throwable error);
}
