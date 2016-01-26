package org.dolphin.secret.browser;

import android.util.Log;

import org.dolphin.arch.AndroidMainScheduler;
import org.dolphin.job.Job;
import org.dolphin.job.Operator;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.job.tuple.FourTuple;
import org.dolphin.job.tuple.TwoTuple;
import org.dolphin.secret.core.FileInfo;
import org.dolphin.secret.core.FileInfoReaderOperator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by hanyanan on 2016/1/20.
 */
public class BrowserManager {
    public static File sRootDir = new File("/sdcard/");
    private static BrowserManager sInstance = null;

    public synchronized static BrowserManager getInstance() {
        if (null == sInstance) {
            sInstance = new BrowserManager();
        }

        return sInstance;
    }

    private File rootDir;
    private Job scanerJob = null;

    private BrowserManager() {
        this.rootDir = sRootDir;
        if (!rootDir.isDirectory()) {
            throw new IllegalArgumentException("");
        }
    }

    private final List<FileChangeListener> imageFileChangeListeners = new ArrayList<FileChangeListener>();
    private final List<FileChangeListener> videoFileChangeListeners = new ArrayList<FileChangeListener>();
    private final List<FileChangeListener> audioFileChangeListeners = new ArrayList<FileChangeListener>();

    private final List<TwoTuple<String, FileInfo>> imageFileList = new ArrayList<TwoTuple<String, FileInfo>>();
    private final List<TwoTuple<String, FileInfo>> videoFileList = new ArrayList<TwoTuple<String, FileInfo>>();
    private final List<TwoTuple<String, FileInfo>> audioFileList = new ArrayList<TwoTuple<String, FileInfo>>();
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
                .then(new Operator<List<String>, FourTuple<List<TwoTuple<String, FileInfo>>, List<TwoTuple<String, FileInfo>>, List<TwoTuple<String, FileInfo>>, List<String>>>() {
                    @Override
                    public FourTuple<List<TwoTuple<String, FileInfo>>, List<TwoTuple<String, FileInfo>>, List<TwoTuple<String, FileInfo>>, List<String>> operate(List<String> input) throws Throwable {
                        if (null == input || input.isEmpty()) return null;
                        List<TwoTuple<String, FileInfo>> images = new ArrayList<TwoTuple<String, FileInfo>>();
                        List<TwoTuple<String, FileInfo>> videos = new ArrayList<TwoTuple<String, FileInfo>>();
                        List<TwoTuple<String, FileInfo>> audios = new ArrayList<TwoTuple<String, FileInfo>>();
                        List<String> leaks = new ArrayList<String>();
                        FileInfoReaderOperator fileInfoReaderOperator = new FileInfoReaderOperator();
                        for (String name : input) {
                            try {
                                File file = new File(rootDir, name);
                                if (!file.exists() || !file.isFile() || file.isHidden() || file.isDirectory()) {
                                    continue;
                                }
                                FileInfo fileInfo = fileInfoReaderOperator.operate(file);
                                if (fileInfo.originalMimeType.startsWith("image")) {
                                    images.add(new TwoTuple<String, FileInfo>(name, fileInfo));
                                } else if (fileInfo.originalMimeType.startsWith("video")) {
                                    videos.add(new TwoTuple<String, FileInfo>(name, fileInfo));
                                } else if (fileInfo.originalMimeType.startsWith("audio")) {
                                    audios.add(new TwoTuple<String, FileInfo>(name, fileInfo));
                                } else {
                                    leaks.add(name);
                                }
                            } catch (Throwable e) {
                                leaks.add(name);
                            }
                        }
                        return new FourTuple<List<TwoTuple<String, FileInfo>>, List<TwoTuple<String, FileInfo>>, List<TwoTuple<String, FileInfo>>, List<String>>(images, videos, audios, leaks);
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
                .result(new Job.Callback1<FourTuple<List<TwoTuple<String, FileInfo>>, List<TwoTuple<String, FileInfo>>, List<TwoTuple<String, FileInfo>>, List<String>>>() {
                    @Override
                    public void call(FourTuple<List<TwoTuple<String, FileInfo>>, List<TwoTuple<String, FileInfo>>, List<TwoTuple<String, FileInfo>>, List<String>> result) {
                        if (null == result) {
                            onImageFileFound(result.value1);
                            onVideoFileFound(result.value2);
                            onAudioFileFound(result.value3);
                            onLeakedFile(result.value4);
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

    private synchronized void onImageFileFound(List<TwoTuple<String, FileInfo>> files) {
        if (null == files || files.isEmpty()) {
            return;
        }
        this.imageFileList.addAll(files);
        notifyFileChanged(this.imageFileList, this.imageFileChangeListeners);
    }

    private synchronized void onVideoFileFound(List<TwoTuple<String, FileInfo>> files) {
        if (null == files || files.isEmpty()) {
            return;
        }
        this.videoFileList.addAll(files);
        notifyFileChanged(this.videoFileList, this.videoFileChangeListeners);
    }

    private synchronized void onAudioFileFound(List<TwoTuple<String, FileInfo>> files) {
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
    }

    private synchronized void onScanFailed(Throwable throwable) {

    }

    public List<TwoTuple<String, FileInfo>> getImageFileList() {
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

    private void notifyFileChanged(List<TwoTuple<String, FileInfo>> files, List<FileChangeListener> listeners) {
        final List<FileChangeListener> imageFileChangeListeners = new ArrayList<FileChangeListener>();
        synchronized (BrowserManager.class) {
            imageFileChangeListeners.addAll(listeners);
        }
        for (FileChangeListener listener : imageFileChangeListeners) {
            listener.onFileList(files);
        }
    }


    public interface FileChangeListener {
        public void onFileList(List<TwoTuple<String, FileInfo>> files);
    }
}
