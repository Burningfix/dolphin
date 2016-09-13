package org.dolphin.secret.browser;

import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.dolphin.arch.AndroidMainScheduler;
import org.dolphin.job.Job;
import org.dolphin.job.Operator;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.job.tuple.FourTuple;
import org.dolphin.job.tuple.TwoTuple;
import org.dolphin.lib.util.ValueUtil;
import org.dolphin.secret.BuildConfig;
import org.dolphin.secret.SecretApplication;
import org.dolphin.secret.core.DeleteFileOperator;
import org.dolphin.secret.core.DeobscureOperator;
import org.dolphin.secret.core.EncodeLeakFileOperator;
import org.dolphin.secret.core.FileInfoContentCache;
import org.dolphin.secret.core.ObscureFileInfo;
import org.dolphin.secret.core.ObscureOperator;
import org.dolphin.secret.core.TraversalFolderOperator;
import org.dolphin.secret.picker.AndroidFileInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by hanyanan on 2016/1/20.
 */
public class BrowserManager {
    public static final String TAG = "BrowserManager";
    public static final Comparator<ObscureFileInfo> fileInfoComparator = new Comparator<ObscureFileInfo>() {
        @Override
        public int compare(ObscureFileInfo lhs, ObscureFileInfo rhs) {
            return lhs.encodeTime > rhs.encodeTime ? 1 : -1;
        }
    };
    public static File sRootDir = new File(Environment.getExternalStorageDirectory(), "se");
    private static BrowserManager sInstance = null;
    private final List<FileChangeListener> imageFileChangeListeners = new ArrayList<FileChangeListener>();
    private final List<FileChangeListener> videoFileChangeListeners = new ArrayList<FileChangeListener>();
    private final List<FileChangeListener> audioFileChangeListeners = new ArrayList<FileChangeListener>();
    private final List<ObscureFileInfo> imageFileList = new ArrayList<ObscureFileInfo>();
    private final List<ObscureFileInfo> videoFileList = new ArrayList<ObscureFileInfo>();
    private final List<ObscureFileInfo> audioFileList = new ArrayList<ObscureFileInfo>();
    private File rootDir;
    private Job scannerJob = null;
    private Job obscureJob = null;

    private BrowserManager() {
        this.rootDir = sRootDir;
        checkEnvironment();
    }

    public synchronized static BrowserManager getInstance() {
        if (null == sInstance) {
            sInstance = new BrowserManager();
        }

        return sInstance;
    }

    private static void onTypedFileFound(List<ObscureFileInfo> newFiles, List<ObscureFileInfo> out,
                                         List<FileChangeListener> fileChangeListeners) {
        if (null == out || null == fileChangeListeners) {
            throw new NullPointerException("");
        }

        CopyOnWriteArrayList<ObscureFileInfo> copyOnWriteArrayList = null;
        CopyOnWriteArrayList<FileChangeListener> listeners = null;
        synchronized (out) {
            if (null == newFiles || newFiles.isEmpty()) {
                return;
            }
            out.addAll(newFiles);

            copyOnWriteArrayList = new CopyOnWriteArrayList<ObscureFileInfo>(out);
            listeners = new CopyOnWriteArrayList<FileChangeListener>(fileChangeListeners);
        }

        notifyFileChanged(copyOnWriteArrayList, listeners);
    }

    private static void onTypedFileRemoved(List<ObscureFileInfo> rejectFiles, List<ObscureFileInfo> out,
                                           List<FileChangeListener> fileChangeListeners) {
        if (null == out || null == fileChangeListeners) {
            throw new NullPointerException("");
        }

        CopyOnWriteArrayList<ObscureFileInfo> copyOnWriteArrayList = null;
        CopyOnWriteArrayList<FileChangeListener> listeners = null;
        synchronized (out) {
            if (null == rejectFiles || rejectFiles.isEmpty()) {
                return;
            }
            out.removeAll(rejectFiles);
            copyOnWriteArrayList = new CopyOnWriteArrayList<ObscureFileInfo>(out);
            listeners = new CopyOnWriteArrayList<FileChangeListener>(fileChangeListeners);
        }

        notifyFileChanged(copyOnWriteArrayList, listeners);
    }

    private static void notifyFileChanged(List<ObscureFileInfo> files, List<FileChangeListener> listeners) {
        for (FileChangeListener listener : listeners) {
            listener.onFileListChanged(files);
        }
    }

    private void checkEnvironment() {
        if (this.rootDir.exists()) {
            if (!this.rootDir.isDirectory()) {
                FileUtils.deleteQuietly(this.rootDir);
                checkEnvironment();
            }
        } else {
            if (!this.rootDir.mkdirs()) {
                // Double-check that some other thread or process hasn't made
                // the directory in the background
                if (!this.rootDir.isDirectory()) {
                    FileUtils.deleteQuietly(this.rootDir);
                    checkEnvironment();
                }
            }
        }
    }

    public File getRootDir() {
        return this.rootDir;
    }

    public synchronized void start() {
        imageFileList.clear();
        videoFileList.clear();
        audioFileList.clear();
        if (scannerJob != null && !scannerJob.isAborted()) {
            scannerJob.abort();
        }
        scannerJob = new Job(this.rootDir)
                .then(new TraversalFolderOperator())
                .workOn(Schedulers.io())
                .callbackOn(AndroidMainScheduler.INSTANCE)
                .error(new Job.Callback2() {
                    @Override
                    public void call(Throwable throwable, Object[] unexpectedResult) {
                        // TODO
                    }
                })
                .result(new Job.Callback1<FourTuple<List<ObscureFileInfo>, List<ObscureFileInfo>, List<ObscureFileInfo>, List<String>>>() {
                    @Override
                    public void call(FourTuple<List<ObscureFileInfo>, List<ObscureFileInfo>, List<ObscureFileInfo>, List<String>> result) {
                        onImageFileFound(null == result ? null : result.value1);
                        onVideoFileFound(null == result ? null : result.value2);
                        onAudioFileFound(null == result ? null : result.value3);
                        onLeakFileFound(null == result ? null : result.value4);
                    }
                })
                .work();
    }

    private void onImageFileFound(List<ObscureFileInfo> files) {
        onTypedFileFound(files, this.imageFileList, this.imageFileChangeListeners);
    }

    private void onVideoFileFound(List<ObscureFileInfo> files) {
        onTypedFileFound(files, this.videoFileList, this.videoFileChangeListeners);
    }

    private void onAudioFileFound(List<ObscureFileInfo> files) {
        onTypedFileFound(files, this.audioFileList, this.audioFileChangeListeners);
    }

    private void onImageFileRemoved(List<ObscureFileInfo> files) {
        onTypedFileRemoved(files, this.imageFileList, this.imageFileChangeListeners);
    }

    private void onVideoFileRemoved(List<ObscureFileInfo> files) {
        onTypedFileRemoved(files, this.videoFileList, this.videoFileChangeListeners);
    }

    private void onAudioFileRemoved(List<ObscureFileInfo> files) {
        onTypedFileRemoved(files, this.audioFileList, this.audioFileChangeListeners);
    }

    private void onLeakFileFound(List<String> leakedFileList) {
        if (null == leakedFileList || leakedFileList.isEmpty()) {
            return;
        }
        if (BuildConfig.DEBUG) {
            StringBuilder sb = new StringBuilder();
            for (String fileName : leakedFileList) {
                sb.append(fileName).append(" ");
            }
            Log.d(TAG, "Found leak file[" + sb.toString() + "]");
        }

        if (null != obscureJob) {
            obscureJob.abort();
            obscureJob = null;
        }
        obscureJob = new Job(leakedFileList);
        obscureJob.then(new EncodeLeakFileOperator(rootDir))
                .workOn(Schedulers.io())
                .callbackOn(AndroidMainScheduler.INSTANCE)
                .error(new Job.Callback2() {
                    @Override
                    public void call(Throwable throwable, Object[] unexpectedResult) {
                        // TODO
                    }
                })
                .result(new Job.Callback1<List<TwoTuple<ObscureFileInfo, FileInfoContentCache>>>() {
                    @Override
                    public void call(List<TwoTuple<ObscureFileInfo, FileInfoContentCache>> result) {
                        if (null == result || result.isEmpty()) {
                            return;
                        }
                        List<ObscureFileInfo> images = new ArrayList<ObscureFileInfo>();
                        List<ObscureFileInfo> video = new ArrayList<ObscureFileInfo>();
                        List<ObscureFileInfo> audio = new ArrayList<ObscureFileInfo>();

                        for (TwoTuple<ObscureFileInfo, FileInfoContentCache> tuple : result) {
                            CacheManager.getInstance().putCache(tuple.value1, tuple.value2);
                            if (tuple.value1.isPhotoType()) {
                                images.add(tuple.value1);
                            } else if (tuple.value1.isVideoType()) {
                                video.add(tuple.value1);
                            } else if (tuple.value1.isAudioType()) {
                                audio.add(tuple.value1);
                            }
                        }
                        onImageFileFound(images);
                        onVideoFileFound(video);
                        onAudioFileFound(audio);
                    }
                })
                .work();
    }

    void onObscureFileFound(ObscureFileInfo fileInfo) {
        if (null == fileInfo) return;
        List<ObscureFileInfo> files = new ArrayList<>();
        files.add(fileInfo);
        if (fileInfo.isPhotoType()) {
            onImageFileFound(files);
        } else if (fileInfo.isVideoType()) {
            onVideoFileFound(files);
        } else if (fileInfo.isAudioType()) {
            onAudioFileFound(files);
        }
    }

    void onFileRemoved(Iterable<ObscureFileInfo> fileInfos) {
        Iterator<ObscureFileInfo> infoIterator = null;
        if (null == fileInfos || null == (infoIterator = fileInfos.iterator())) {
            return;
        }
        ArrayList<ObscureFileInfo> images = new ArrayList<ObscureFileInfo>();
        ArrayList<ObscureFileInfo> audios = new ArrayList<ObscureFileInfo>();
        ArrayList<ObscureFileInfo> videos = new ArrayList<ObscureFileInfo>();
        while (infoIterator.hasNext()) {
            ObscureFileInfo fileInfo = infoIterator.next();
            if (fileInfo.isVideoType()) {
                videos.add(fileInfo);
            } else if (fileInfo.isPhotoType()) {
                images.add(fileInfo);
            } else if (fileInfo.isAudioType()) {
                audios.add(fileInfo);
            }
        }
        onImageFileRemoved(images);
        onVideoFileRemoved(videos);
        onAudioFileFound(audios);
    }

    /**
     * 导入新的文件，需要扫描整个目录完成之后才能继续进行, 该文件必须是未加密的文件
     * <br>
     * 操作步骤：
     * 1. 如果在扫描中，则会等待等待扫描结束再添加
     * 2. 删除已经存在的相同文件，则删除原有的
     * 3. 加密文件，添加到仓库中
     *
     * @param fileName
     */
    public void obscureFile(String fileName) {
        new Job(new File(this.rootDir, fileName))
                .then(new ObscureOperator())
                .workOn(Schedulers.io())
                .callbackOn(AndroidMainScheduler.INSTANCE)
                .error(new Job.Callback2() {
                    @Override
                    public void call(Throwable throwable, Object[] unexpectedResult) {
                        // TODO
                    }
                })
                .result(new Job.Callback1<TwoTuple<ObscureFileInfo, FileInfoContentCache>>() {
                    @Override
                    public void call(TwoTuple<ObscureFileInfo, FileInfoContentCache> result) {
                        CacheManager.getInstance().putCache(result.value1, result.value2);
                        onObscureFileFound(result.value1);
                    }
                })
                .work();
    }

    public synchronized void deleteFiles(Collection<ObscureFileInfo> fileInfos) {
        if (null == fileInfos || fileInfos.isEmpty()) {
            return;
        }

        for (ObscureFileInfo fileInfo : fileInfos) {
            new Job(fileInfo)
                    .then(new DeleteFileOperator(rootDir))
                    .workOn(Schedulers.computation())
                    .work();
            CacheManager.getInstance().remove(fileInfo);
            if (fileInfo.isPhotoType()) {
                this.imageFileList.remove(fileInfo);

            }
            if (fileInfo.isAudioType()) {
                this.audioFileList.remove(fileInfo);

            }
            if (fileInfo.isVideoType()) {
                this.videoFileList.remove(fileInfo);
            }
        }

        notifyFileChanged(this.imageFileList, this.imageFileChangeListeners);
        notifyFileChanged(this.audioFileList, this.audioFileChangeListeners);
        notifyFileChanged(this.videoFileList, this.videoFileChangeListeners);
    }

    public void importFiles(final List<AndroidFileInfo> fileList, final ImportFileListener callback) {
        for (final AndroidFileInfo fileEntry : fileList) {
            final File originalFile = new File(fileEntry.path);
            final File destFile = new File(getInstance().getRootDir(), originalFile.getName());
            new Job(fileEntry)
                    .workOn(Schedulers.io())
                    .callbackOn(AndroidMainScheduler.INSTANCE)
                    .then(new Operator<AndroidFileInfo, File>() {
                        @Override
                        public File operate(AndroidFileInfo input) throws Throwable {
                            FileUtils.moveFile(originalFile, destFile);
                            MediaScannerConnection.scanFile(SecretApplication.getInstance(),
                                    new String[]{originalFile.getAbsolutePath()}, null, null);
                            return destFile;
                        }
                    })
                    .then(ObscureOperator.INSTANCE)
                    .result(new Job.Callback1<TwoTuple<ObscureFileInfo, FileInfoContentCache>>() {
                        @Override
                        public void call(TwoTuple<ObscureFileInfo, FileInfoContentCache> result) {
                            if (null == result || null == result.value1) {
                                // do nothing
                            } else {
                                CacheManager.getInstance().putCache(result.value1, result.value2);
                                callback.onImportSuccess(fileEntry.path, result.value1);
                            }
                        }
                    })
                    .error(new Job.Callback2() {
                        @Override
                        public void call(Throwable throwable, Object[] unexpectedResult) {
                            callback.onImportFailed(fileEntry.path, throwable);
                        }
                    })
                    .work();
        }
    }

    public void exportFiles(final Collection<ObscureFileInfo> fileList) {
        if (ValueUtil.isEmpty(fileList)) {
            return;
        }
        onFileRemoved(fileList);

        for (final ObscureFileInfo fileInfo : fileList) {
            new Job(fileInfo)
                    .workOn(Schedulers.io())
                    .then(DeobscureOperator.DEFAULT)
                    .then(new Operator<File, Void>() {
                        @Override
                        public Void operate(File input) throws Throwable {
                            File exportFile = getTypedFileDirector(fileInfo);
                            FileUtils.moveFile(input, new File(exportFile, input.getName()));
                            MediaScannerConnection.scanFile(SecretApplication.getInstance(),
                                    new String[]{exportFile.getAbsolutePath()}, null, null);
                            return null;
                        }
                    })
                    .work();
        }
    }

    public List<ObscureFileInfo> getImageFileList() {
        return new CopyOnWriteArrayList<ObscureFileInfo>(imageFileList);
    }

    public List<ObscureFileInfo> getVideoFileList() {
        return new CopyOnWriteArrayList<ObscureFileInfo>(videoFileList);
    }

    public List<ObscureFileInfo> getAudioFileList() {
        return new CopyOnWriteArrayList<ObscureFileInfo>(audioFileList);
    }

    public synchronized void addImageFileChangeListener(FileChangeListener listener) {
        if (!imageFileChangeListeners.contains(listener)) {
            imageFileChangeListeners.add(listener);
        }
    }

    public synchronized void removeImageFileChangeListener(FileChangeListener listener) {
        if (imageFileChangeListeners.contains(listener)) {
            imageFileChangeListeners.remove(listener);
        }
    }

    public synchronized void addVideoFileChangeListener(FileChangeListener listener) {
        if (!videoFileChangeListeners.contains(listener)) {
            videoFileChangeListeners.add(listener);
        }
    }

    public synchronized void removeVideoFileChangeListener(FileChangeListener listener) {
        if (videoFileChangeListeners.contains(listener)) {
            videoFileChangeListeners.remove(listener);
        }
    }

    public synchronized void addAudioFileChangeListener(FileChangeListener listener) {
        if (!audioFileChangeListeners.contains(listener)) {
            audioFileChangeListeners.add(listener);
        }
    }

    public synchronized void removeAudioFileChangeListener(FileChangeListener listener) {
        if (audioFileChangeListeners.contains(listener)) {
            audioFileChangeListeners.remove(listener);
        }
    }

    public File getTypedFileDirector(ObscureFileInfo fileInfo) throws IOException {
        if (null == fileInfo) {
            return null;
        }

        File file = null;

        if (fileInfo.isPhotoType()) {
            file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        } else if (fileInfo.isVideoType()) {
            file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        } else if (fileInfo.isAudioType()) {
            file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        } else {
            file = Environment.getExternalStorageDirectory();
        }

        if (!file.exists()) {
            // create director
            if (file.mkdir()) {
                return file;
            } else {
                // create failed
                throw new IOException("Create director " + file.getAbsolutePath() + " failed!");
            }
        }

        if (!file.isDirectory()) {
            throw new IOException("File " + file.getAbsolutePath() + " is not a director!");
        }

        return file;
    }

    public interface FileChangeListener {
        void onFileListChanged(List<ObscureFileInfo> files);
    }
}
