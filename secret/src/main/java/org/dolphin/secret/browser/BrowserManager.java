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
    private static BrowserManager sInstance = null;

    public synchronized static BrowserManager getInstance(File rootDir) {
        if (null == sInstance) {
            sInstance = new BrowserManager(rootDir);
        }

        return sInstance;
    }

    private File rootDir;
    private Job scanerJob = null;

    private BrowserManager(File rootDir) {
        if (!rootDir.isDirectory()) {
            throw new IllegalArgumentException("");
        }
        this.rootDir = rootDir;
    }

    private final List<ImageFileChangeListener> imageFileChangeListeners = new ArrayList<ImageFileChangeListener>();
    private final List<ImageFileChangeListener> videoFileChangeListeners = new ArrayList<ImageFileChangeListener>();
    private final List<ImageFileChangeListener> audioFileChangeListeners = new ArrayList<ImageFileChangeListener>();

    private final List<TwoTuple<String, FileInfo>> imageFileList = new ArrayList<TwoTuple<String, FileInfo>>();
    private final List<TwoTuple<String, FileInfo>> videoFileList = new ArrayList<TwoTuple<String, FileInfo>>();
    private final List<TwoTuple<String, FileInfo>> audioFileList = new ArrayList<TwoTuple<String, FileInfo>>();
    private final List<String> leakedFileList = new ArrayList<String>();

    public void start() {
        imageFileList.clear();
        videoFileList.clear();
        audioFileList.clear();
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
            Log.d("DDD", "No image file found!");
            return;
        }
        for (TwoTuple<String, FileInfo> tuple : files) {
            Log.d("DDD", "Found image file " + tuple.value1 + "\tOriginal file Name: " + tuple.value2.originalFileName);
        }
    }

    private synchronized void onVideoFileFound(List<TwoTuple<String, FileInfo>> files) {

    }

    private synchronized void onAudioFileFound(List<TwoTuple<String, FileInfo>> files) {

    }

    private synchronized void onLeakedFile(List<String> leakedFileList) {

    }

    private synchronized void onScanFailed(Throwable throwable) {

    }

    public void stop() {
        if (scanerJob != null) {
            scanerJob.abort();
            scanerJob = null;
        }
    }


    public synchronized void addImageFileChangeListener(ImageFileChangeListener listener) {
        if (!imageFileChangeListeners.contains(listener)) {
            imageFileChangeListeners.add(listener);
        }
    }

    private void notifyImageFileChanged(List<TwoTuple<String, FileInfo>> files) {
        final List<ImageFileChangeListener> imageFileChangeListeners = new ArrayList<ImageFileChangeListener>();
        synchronized (BrowserManager.class) {
            imageFileChangeListeners.addAll(this.imageFileChangeListeners);
        }
        for (ImageFileChangeListener listener : imageFileChangeListeners) {
            listener.onImageFileList(files);
        }
    }


    public interface ImageFileChangeListener {
        public void onImageFileList(List<TwoTuple<String, FileInfo>> files);
    }

    public interface VideoFileChangeListener {
        public void onImageFileList(List<TwoTuple<String, FileInfo>> files);
    }

    public interface AudioFileChangeListener {
        public void onImageFileList(List<TwoTuple<String, FileInfo>> files);
    }
}
