package org.dolphin.secret.browser;

import android.util.Log;

import org.dolphin.arch.AndroidMainScheduler;
import org.dolphin.job.Job;
import org.dolphin.job.Operator;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.job.tuple.FourTuple;
import org.dolphin.job.tuple.ThreeTuple;
import org.dolphin.job.tuple.TwoTuple;
import org.dolphin.secret.core.FileEncodeOperator;
import org.dolphin.secret.core.FileInfo;
import org.dolphin.secret.core.FileInfoContentCache;
import org.dolphin.secret.core.FileInfoReaderOperator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by hanyanan on 2016/1/20.
 */
public class BrowserManager {
    private static final String TAG = "BrowserManager";
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
        if (scanerJob != null) {
            scanerJob.abort();
        }
        scanerJob = new Job(this.rootDir);
        scanerJob.then(new Operator<File, List<String>>() {
            public List<String> operate(File input) throws Throwable {
                String[] files = input.list();
                if (null == files || files.length <= 0) return null;
                return Arrays.asList(files);
            }
        })
                .then(new Operator<List<String>, FourTuple<List<FileInfo>, List<FileInfo>, List<FileInfo>, List<String>>>() {
                    @Override
                    public FourTuple<List<FileInfo>, List<FileInfo>, List<FileInfo>, List<String>> operate(List<String> input) throws Throwable {
                        if (null == input || input.isEmpty()) return null;
                        List<FileInfo> images = new ArrayList<FileInfo>();
                        List<FileInfo> videos = new ArrayList<FileInfo>();
                        List<FileInfo> audios = new ArrayList<FileInfo>();
                        List<String> leaks = new ArrayList<String>();
                        FileInfoReaderOperator fileInfoReaderOperator = FileInfoReaderOperator.DEFAULT;
                        for (String name : input) {
                            try {
                                File file = new File(rootDir, name);
                                if (!file.exists() || !file.isFile() || file.isHidden() || file.isDirectory()) {
                                    continue;
                                }
                                FileInfo fileInfo = fileInfoReaderOperator.operate(file);
                                Log.d(TAG, "Found File " + fileInfo.toString());
                                if (fileInfo.originalMimeType.startsWith("image")) {
                                    images.add(fileInfo);
                                } else if (fileInfo.originalMimeType.startsWith("video")) {
                                    videos.add(fileInfo);
                                } else if (fileInfo.originalMimeType.startsWith("audio")) {
                                    audios.add(fileInfo);
                                }
                            } catch (Throwable e) {
                                leaks.add(name);
                            }
                        }
                        return new FourTuple<List<FileInfo>, List<FileInfo>, List<FileInfo>, List<String>>(images, videos, audios, leaks);
                    }
                })
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
                            return;
                        }
                        onImageFileFound(result.value1);
                        onVideoFileFound(result.value2);
                        onAudioFileFound(result.value3);
                        onLeakedFile(result.value4);
                    }
                })
                .work();
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
        encodeJob.then(new Operator<List<String>, List<TwoTuple<FileInfo, FileInfoContentCache>>>() {
            @Override
            public List<TwoTuple<FileInfo, FileInfoContentCache>> operate(List<String> input) throws Throwable {
                if (null == input || input.isEmpty()) {
                    return null;
                }
                FileEncodeOperator operator = new FileEncodeOperator();
                List<TwoTuple<FileInfo, FileInfoContentCache>> res = new ArrayList<TwoTuple<FileInfo, FileInfoContentCache>>();
                for (String fileName : input) {
                    try {
                        TwoTuple<FileInfo, FileInfoContentCache> tuple = operator.operate(new File(rootDir, fileName));
                        res.add(new TwoTuple<FileInfo, FileInfoContentCache>(tuple.value1, tuple.value2));
                        Log.i(TAG, "Encode file success: " + fileName);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                        Log.w(TAG, "Failed encode file " + fileName);
                    }
                }
                return res;
            }
        })
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
                            if (tuple.value1.originalMimeType.startsWith("image")) {
                                images.add(tuple.value1);
                            } else if (tuple.value1.originalMimeType.startsWith("video")) {
                                video.add(tuple.value1);
                            } else if (tuple.value1.originalMimeType.startsWith("audio")) {
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
