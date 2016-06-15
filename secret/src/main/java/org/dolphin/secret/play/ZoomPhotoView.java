package org.dolphin.secret.play;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.View;

import org.dolphin.arch.AndroidMainScheduler;
import org.dolphin.job.Job;
import org.dolphin.job.Operator;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.lib.util.IOUtil;
import org.dolphin.lib.util.ValueUtil;
import org.dolphin.secret.R;
import org.dolphin.secret.SecretApplication;
import org.dolphin.secret.core.FileInfo;
import org.dolphin.secret.core.ReadableFileInputStream;
import org.dolphin.secret.util.BitmapUtils;

import java.io.File;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by yananh on 2016/6/14.
 */
public class ZoomPhotoView extends PhotoView {
    private boolean attached = false;
    private boolean visible = false;
    private String filePath = null;
    private FileInfo fileInfo = null;
    private Job loadJob = null;

    public ZoomPhotoView(Context context) {
        super(context);
    }

    public ZoomPhotoView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public ZoomPhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }


    public void setFile(String path, FileInfo fileInfo) {
        this.fileInfo = fileInfo;
        this.filePath = path;
        this.setImageResource(R.drawable.default_thumbnail_loading);
        notifyPropertyChanged(this.attached, this.visible, path, fileInfo);
    }

    @Override
    protected void onAttachedToWindow() {
        notifyPropertyChanged(true, true, this.filePath, this.fileInfo);
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        notifyPropertyChanged(false, false, this.filePath, this.fileInfo);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        notifyPropertyChanged(this.attached, VISIBLE == visibility, this.filePath, this.fileInfo);
        super.onVisibilityChanged(changedView, visibility);
    }

    private void notifyPropertyChanged(boolean newAttached, boolean newVisible, String newPath, FileInfo newFileInfo) {
        int nextOperation = 0;  // 0什么都不做，1加载新的图片，2，终止所有操作
        do {
            if (!newAttached || null == newPath) {
                nextOperation = 3;
                break;
            }

            // attached and has path
            if (newAttached != this.attached) {
                nextOperation = 1;
            }

            if (!ValueUtil.isEquals(newPath, this.filePath)) {
                nextOperation = 1;
            }
        } while (false);

        switch (nextOperation) {
            case 0:
                break;
            case 1:
                if (loadJob != null) {
                    loadJob.abort();
                    loadJob = null;
                }
                loadThumbnail(newPath, newFileInfo);
                break;
            case 2:
                if (loadJob != null) {
                    loadJob.abort();
                    loadJob = null;
                }
                break;
        }

        this.visible = newVisible;
        this.attached = newAttached;
        this.fileInfo = newFileInfo;
        this.filePath = newPath;
    }


    private void loadThumbnail(final String filePath, final FileInfo fileInfo) {
        if (loadJob != null) {
            loadJob.abort();
            loadJob = null;
        }

        new Job(filePath)
                .then(new Operator<String, Bitmap>() {
                    @Override
                    public Bitmap operate(String filePath) throws Throwable {
                        ReadableFileInputStream fileInputStream = null;
                        try {
                            fileInputStream = new ReadableFileInputStream(new File(filePath), fileInfo);
                            int screenWidth = SecretApplication.getInstance().getWidth();
                            int screenHeight = SecretApplication.getInstance().getHeight();
                            fileInputStream.mark(0);
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeStream(fileInputStream, null, options);
                            if (!BitmapUtils.checkOptions(options)) {
                                return null;
                            }
                            fileInputStream.reset();
                            options.inJustDecodeBounds = false;
                            options.inSampleSize = BitmapUtils.calculateInSampleByCount(options.outWidth,
                                    options.outHeight, screenWidth, screenHeight);
                            return BitmapFactory.decodeStream(fileInputStream, null, options);
                        } finally {
                            IOUtil.closeQuietly(fileInputStream);
                        }
                    }
                })
                .workOn(Schedulers.computation())
                .callbackOn(AndroidMainScheduler.INSTANCE)
                .error(new Job.Callback2() {
                    @Override
                    public void call(Throwable throwable, Object[] unexpectedResult) {
                        // TODO
                    }
                })
                .result(new Job.Callback1<Bitmap>() {
                    @Override
                    public void call(Bitmap result) {
                        if (null == result) {
                            return;
                        }
                        ZoomPhotoView.this.setImageBitmap(result);
                    }
                })
                .work();
    }
}
