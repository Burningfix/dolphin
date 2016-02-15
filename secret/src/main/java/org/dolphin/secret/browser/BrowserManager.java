package org.dolphin.secret.browser;

import android.util.Log;

import org.dolphin.arch.AndroidMainScheduler;
import org.dolphin.job.Job;
import org.dolphin.job.Operator;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.job.tuple.FourTuple;
import org.dolphin.job.tuple.TwoTuple;
import org.dolphin.secret.core.DeleteFileOperator;
import org.dolphin.secret.core.EncodeLeakFileOperator;
import org.dolphin.secret.core.FileEncodeOperator;
import org.dolphin.secret.core.FileInfo;
import org.dolphin.secret.core.FileInfoContentCache;
import org.dolphin.secret.core.FileInfoReaderOperator;
import org.dolphin.secret.core.TraversalFolderOperator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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

    public void start() {
        imageFileList.clear();
        videoFileList.clear();
        audioFileList.clear();
        leakedFileList.clear();
        scan();
    }

    private synchronized void scan() {
        if (scanerJob != null) {
            scanerJob.abort();
        }
        scanerJob = new Job(this.rootDir);
        scanerJob.then(new TraversalFolderOperator())
                .workOn(Schedulers.computation())
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

    public synchronized void addFile(String fileName) {
        if (scanerJob != null) {
            scanerJob.abort();
        }
        scanerJob = new Job(new File(this.rootDir, fileName));
        scanerJob.then(new FileEncodeOperator())
                .workOn(Schedulers.computation())
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

    public synchronized void removeFile(FileInfo fileInfo) {
        new Job(fileInfo)
                .then(new DeleteFileOperator(rootDir))
                .workOn(Schedulers.computation())
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

    private synchronized void onFileFound(FileInfo fileInfo) {
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
                .workOn(Schedulers.computation())
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

    private synchronized void onScanFailed(Throwable throwable) {
        // TODO
    }

    public List<FileInfo> getImageFileList() {
        return imageFileList;
    }

    public void stop() {
        if (scanerJob != null) {
            scanerJob.abort();
            scanerJob = null;
        }
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
        public void onFileList(List<FileInfo> files);
    }
}
