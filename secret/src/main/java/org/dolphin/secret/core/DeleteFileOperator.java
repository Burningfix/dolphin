package org.dolphin.secret.core;

import org.dolphin.job.Operator;
import org.dolphin.lib.util.IOUtil;

import java.io.File;

/**
 * Created by hanyanan on 2016/2/15.
 */
public class DeleteFileOperator implements Operator<ObscureFileInfo, Void> {
    public final static String TAG = "DeleteFileOperator";
    private final File rootDir;

    public DeleteFileOperator(File rootDir){
        this.rootDir = rootDir;
    }
    @Override
    public Void operate(ObscureFileInfo fileInfo) throws Throwable {
        IOUtil.safeDeleteIfExists(new File(rootDir, fileInfo.obscuredFileName));
        return null;
    }
}
