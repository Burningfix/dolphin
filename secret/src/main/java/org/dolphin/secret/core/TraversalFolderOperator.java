package org.dolphin.secret.core;

import android.util.Log;
import org.dolphin.job.Operator;
import org.dolphin.job.tuple.FourTuple;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by hanyanan on 2016/2/15.
 *
 * 遍历整个文件的所有的文件,分别计算出所有的类型，包括图片，视频，音频
 */
public class TraversalFolderOperator implements Operator<File,
        FourTuple<List<FileInfo>, List<FileInfo>, List<FileInfo>, List<String>>> {
    public static final String TAG = "TraversalFolderOperator";

    /**
     * 遍历出目录下所有的文件
     * @param rootDir 需要遍历的根目录，需要是一个文件夹
     * @return  按照顺序依次返回该目录下的图片，视频，音频，未加密文件
     * @throws Throwable
     */
    @Override
    public FourTuple<List<FileInfo>, List<FileInfo>, List<FileInfo>, List<String>> operate(File rootDir) throws Throwable {
        String[] files = rootDir.list();
        if (null == files || files.length <= 0) return null;
        List<String> originalFileNames = Arrays.asList(files);
        if (null == originalFileNames || originalFileNames.isEmpty()) return null;
        List<FileInfo> images = new ArrayList<FileInfo>();
        List<FileInfo> videos = new ArrayList<FileInfo>();
        List<FileInfo> audios = new ArrayList<FileInfo>();
        List<String> leaks = new ArrayList<String>();
        FileInfoReaderOperator fileInfoReaderOperator = FileInfoReaderOperator.DEFAULT;
        for (String name : originalFileNames) {
            try {
                File file = new File(rootDir, name);
                if (!file.exists() || !file.isFile() || file.isHidden() || file.isDirectory()) {
                    continue;
                }
                FileInfo fileInfo = fileInfoReaderOperator.operate(file);
                Log.d(TAG, "Found File " + fileInfo.toString());
                if (fileInfo.isPhotoType()) {
                    images.add(fileInfo);
                } else if (fileInfo.isVideoType()) {
                    videos.add(fileInfo);
                } else if (fileInfo.isAudioType()) {
                    audios.add(fileInfo);
                }
            } catch (Throwable e) {
                leaks.add(name);
            }
        }
        return new FourTuple<>(images, videos, audios, leaks);
    }
}
