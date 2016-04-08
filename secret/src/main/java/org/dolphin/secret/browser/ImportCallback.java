package org.dolphin.secret.browser;

import org.dolphin.secret.core.FileInfo;

/**
 * Created by yananh on 2016/3/29.
 */
public interface ImportCallback {
    public void onImportSucced(String originalPath, FileInfo obscureFile);
    public void onImportFailed(String originalPath, Throwable error);
}
