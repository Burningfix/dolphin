package org.dolphin.secret.browser;

/**
 * Created by yananh on 2016/3/29.
 */
public interface ImportCallback {
    public void onImportSucced(String originalPath, String obscurePath);
    public void onImportFailed(String originalPath, Exception error);
}
