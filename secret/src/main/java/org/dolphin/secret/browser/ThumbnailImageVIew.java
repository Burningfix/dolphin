package org.dolphin.secret.browser;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import org.dolphin.job.Job;
import org.dolphin.lib.ValueUtil;
import org.dolphin.secret.core.FileInfo;

/**
 * Created by yananh on 2016/1/23.
 */
public class ThumbnailImageVIew extends ImageView {
    private boolean attached = false;
    private boolean visible = false;
    private String filePath = null;
    private FileInfo fileInfo = null;
    private Job loadJob = null;

    public ThumbnailImageVIew(Context context) {
        super(context);
    }

    public ThumbnailImageVIew(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThumbnailImageVIew(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ThumbnailImageVIew(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onAttachedToWindow() {
        attached = true;
        visible = true;
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        attached = false;
        visible = false;
        super.onDetachedFromWindow();
    }


    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        visible = VISIBLE == visibility;
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
                loadJob = getLoadJob(newPath, newFileInfo);
                loadJob.work();
                break;
            case 2:
                if (loadJob != null) {
                    loadJob.abort();
                    loadJob = null;
                }
                break;
        }

        this.visible = visible;
        this.attached = attached;
        this.fileInfo = fileInfo;
        this.filePath = filePath;
    }


    private Job getLoadJob(String filePath, FileInfo fileInfo) {

        return null;
    }


}
