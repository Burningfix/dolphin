package org.dolphin.secret.browser;

import android.media.MediaScannerConnection;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.dolphin.arch.AndroidMainScheduler;
import org.dolphin.job.Job;
import org.dolphin.job.Operator;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.job.tuple.FourTuple;
import org.dolphin.job.tuple.TwoTuple;
import org.dolphin.secret.SecretApplication;
import org.dolphin.secret.core.DeleteFileOperator;
import org.dolphin.secret.core.EncodeLeakFileOperator;
import org.dolphin.secret.core.FileInfo;
import org.dolphin.secret.core.FileInfoContentCache;
import org.dolphin.secret.core.ObscureOperator;
import org.dolphin.secret.core.TraversalFolderOperator;
import org.dolphin.secret.picker.FileRequestProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by hanyanan on 2016/1/20.
 */
public class BrowserManager {
    public static final String TAG = "BrowserManager";
    public static File sRootDir = new File("/sdcard/se");
    private static BrowserManager sInstance = null;

    public synchronized static BrowserManager getInstance() {
        if (null == sInstance) {
            sInstance = new BrowserManager();
        }

        return sInstance;
    }

    private File rootDir;
    private Job scanerJob = null;
    private Job encodeJob = null;

    private BrowserManager() {
        this.rootDir = sRootDir;
        if (!rootDir.isDirectory()) {
            throw new IllegalArgumentException("");
        }
    }

    private final List<FileChangeListener> imageFileChangeListeners = new ArrayList<FileChangeListener>();
    private final List<FileChangeListener> videoFileChangeListeners = new ArrayList<FileChangeListener>();
    private final List<FileChangeListener> audioFileChangeListeners = new ArrayList<FileChangeListener>();

    private final List<FileInfo> imageFileList = new ArrayList<FileInfo>();
    private final List<FileInfo> videoFileList = new ArrayList<FileInfo>();
    private final List<FileInfo> audioFileList = new ArrayList<FileInfo>();
    private final List<String> leakedFileList = new ArrayList<String>();

    public synchronized void startScan() {
        imageFileList.clear();
        videoFileList.clear();
        audioFileList.clear();
        leakedFileList.clear();
        if (scanerJob != null) {
            scanerJob.abort();
        }
        scanerJob = new Job(this.rootDir);
        scanerJob.then(new TraversalFolderOperator())
                .workOn(Schedulers.io())
                .callbackOn(AndroidMainScheduler.INSTANCE)
                .error(new Job.Callback2() {
                    @Override
                    public void call(Throwable throwable, Object[] unexpectedResult) {
                        onScanFailed(throwable);
                    }
                })
                .result(new Job.Callback1<FourTuple<List<FileInfo>, List<FileInfo>, List<FileInfo>, List<String>>>() {
                    @Override
                    public void call(FourTuple<List<FileInfo>, List<FileInfo>, List<FileInfo>, List<String>> result) {
                        if (null == result) {
                            onImageFileFound(null);
                            onVideoFileFound(null);
                            onAudioFileFound(null);
                            onLeakedFile(null);
                        } else {
                            onImageFileFound(result.value1);
                            onVideoFileFound(result.value2);
                            onAudioFileFound(result.value3);
                            onLeakedFile(result.value4);
                        }
                    }
                })
                .work();
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
    public synchronized void obscureFile(String fileName) {
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
                .result(new Job.Callback1<TwoTuple<FileInfo, FileInfoContentCache>>() {
                    @Override
                    public void call(TwoTuple<FileInfo, FileInfoContentCache> result) {
                        if (null == result || null == result.value1) {
                            // do nothing
                        } else {
                            onFileFound(result.value1);
                        }
                    }
                })
                .work();
    }

    public synchronized void deleteFile(FileInfo fileInfo) {
        new Job(fileInfo)
                .then(new DeleteFileOperator(rootDir))
                .workOn(Schedulers.io())
                .work();
        CacheManager.getInstance().remove(fileInfo);
        if (fileInfo.isPhotoType()) {
            this.imageFileList.remove(fileInfo);
            notifyFileChanged(this.imageFileList, this.imageFileChangeListeners);
            return;
        }
        if (fileInfo.isAudioType()) {
            this.audioFileList.remove(fileInfo);
            notifyFileChanged(this.audioFileList, this.audioFileChangeListeners);
            return;
        }
        if (fileInfo.isVideoType()) {
            this.videoFileList.remove(fileInfo);
            notifyFileChanged(this.videoFileList, this.videoFileChangeListeners);
            return;
        }
    }

    private synchronized void onLeakedFile(List<String> leakedFileList) {
        if (null == leakedFileList || leakedFileList.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (String fileName : leakedFileList) {
            sb.append(fileName).append(" ");
        }
        Log.d(TAG, "Found leak file[" + sb.toString() + "]");

        if (null != encodeJob) {
            encodeJob.abort();
            encodeJob = null;
        }
        encodeJob = new Job(leakedFileList);
        encodeJob.then(new EncodeLeakFileOperator(rootDir))
                .workOn(Schedulers.io())
                .callbackOn(AndroidMainScheduler.INSTANCE)
                .error(new Job.Callback2() {
                    @Override
                    public void call(Throwable throwable, Object[] unexpectedResult) {

                    }
                })
                .result(new Job.Callback1<List<TwoTuple<FileInfo, FileInfoContentCache>>>() {
                    @Override
                    public void call(List<TwoTuple<FileInfo, FileInfoContentCache>> result) {
                        if (null == result || result.isEmpty()) {
                            return;
                        }
                        List<FileInfo> images = new ArrayList<FileInfo>();
                        List<FileInfo> video = new ArrayList<FileInfo>();
                        List<FileInfo> audio = new ArrayList<FileInfo>();

                        for (TwoTuple<FileInfo, FileInfoContentCache> tuple : result) {
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


    public void stop() {
        if (scanerJob != null) {
            scanerJob.abort();
            scanerJob = null;
        }
    }


    public void importFiles(final List<FileRequestProvider.FileEntry> fileList, final ImportCallback callback) {
        for (final FileRequestProvider.FileEntry fileEntry : fileList) {
            final File originalFile = new File(fileEntry.path);
            final File destFile = new File(rootDir, originalFile.getName());
            new Job(fileEntry)
                    .workOn(Schedulers.io())
                    .callbackOn(AndroidMainScheduler.INSTANCE)
                    .then(new Operator<FileRequestProvider.FileEntry, File>() {
                        @Override
                        public File operate(FileRequestProvider.FileEntry input) throws Throwable {
                            FileUtils.moveFile(originalFile, destFile);
                            MediaScannerConnection.scanFile(SecretApplication.getInstance(),
                                    new String[]{originalFile.getAbsolutePath()}, null, null);
                            Thread.sleep(3000);
                            return destFile;
                        }
                    })
                    .then(ObscureOperator.INSTANCE)
                    .result(new Job.Callback1<TwoTuple<FileInfo, FileInfoContentCache>>() {
                        @Override
                        public void call(TwoTuple<FileInfo, FileInfoContentCache> result) {
                            if (null == result || null == result.value1) {
                                // do nothing
                            } else {
                                CacheManager.getInstance().putCache(result.value1, result.value2);
                                callback.onImportSucced(fileEntry.path, result.value1);
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


        return;
    }


    synchronized void onFileFound(FileInfo fileInfo) {
        if (null == fileInfo) return;
        List<FileInfo> files = new ArrayList<>();
        files.add(fileInfo);
        if (fileInfo.isPhotoType()) {
            onImageFileFound(files);
            return;
        }
        if (fileInfo.isVideoType()) {
            onVideoFileFound(files);
            return;
        }
        if (fileInfo.isAudioType()) {
            onAudioFileFound(files);
            return;
        }
    }

    synchronized void onFileListFound(List<FileInfo> obscureFileList) {
        if (null == obscureFileList || obscureFileList.isEmpty()) {
            return;
        }
        final List<FileInfo> images = new ArrayList<FileInfo>();
        final List<FileInfo> videos = new ArrayList<FileInfo>();
        final List<FileInfo> audios = new ArrayList<FileInfo>();
        for (FileInfo fileInfo : obscureFileList) {
            if (fileInfo.isPhotoType()) {
                images.add(fileInfo);
            } else if (fileInfo.isVideoType()) {
                videos.add(fileInfo);
            } else if (fileInfo.isAudioType()) {
                audios.add(fileInfo);
            }
        }

        if (!images.isEmpty()) {
            onImageFileFound(images);
        }
        if (!videos.isEmpty()) {
            onVideoFileFound(videos);
        }
        if (!audios.isEmpty()) {
            onAudioFileFound(audios);
        }
    }

    private synchronized void onImageFileFound(List<FileInfo> files) {
        if (null == files || files.isEmpty()) {
            return;
        }
        this.imageFileList.addAll(files);
        notifyFileChanged(this.imageFileList, this.imageFileChangeListeners);
    }

    private synchronized void onVideoFileFound(List<FileInfo> files) {
        if (null == files || files.isEmpty()) {
            return;
        }
        this.videoFileList.addAll(files);
        notifyFileChanged(this.videoFileList, this.videoFileChangeListeners);
    }

    private synchronized void onAudioFileFound(List<FileInfo> files) {
        if (null == files || files.isEmpty()) {
            return;
        }
        this.audioFileList.addAll(files);
        notifyFileChanged(this.audioFileList, this.audioFileChangeListeners);
    }


    private synchronized void onScanFailed(Throwable throwable) {
        // TODO
    }

    public List<FileInfo> getImageFileList() {
        return imageFileList;
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

    private void notifyFileChanged(List<FileInfo> files, List<FileChangeListener> listeners) {
        final List<FileChangeListener> imageFileChangeListeners = new ArrayList<FileChangeListener>();
        synchronized (BrowserManager.class) {
            imageFileChangeListeners.addAll(listeners);
        }
        for (FileChangeListener listener : imageFileChangeListeners) {
            listener.onFileList(files);
        }
    }

    public static final Comparator<FileInfo> fileInfoComparator = new Comparator<FileInfo>() {
        @Override
        public int compare(FileInfo lhs, FileInfo rhs) {
            long res = lhs.encodeTime - rhs.encodeTime;
            return res < 0 ? -1 : 1;
        }
    };

    public interface FileChangeListener {
        void onFileList(List<FileInfo> files);
    }
}
