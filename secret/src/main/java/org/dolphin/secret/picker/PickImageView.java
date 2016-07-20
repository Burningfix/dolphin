package org.dolphin.secret.picker;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.dolphin.arch.AndroidMainScheduler;
import org.dolphin.job.Job;
import org.dolphin.job.Operator;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.secret.util.BitmapUtils;

/**
 * Created by hanyanan on 2016/2/17.
 */
public class PickImageView extends ImageView {
    public static final String TAG = "PickImageView";
    private static final BitmapUtils.ImageThumbnailUtils DECODER = new BitmapUtils.ImageThumbnailUtils(false, 0.2F, 0.2F);
    private AndroidFileInfo requireDisplay = null;
    private AndroidFileInfo currenDisplay = null;
    private boolean isAttached = true;
    private Job job;

    public PickImageView(Context context) {
        super(context);
    }

    public PickImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PickImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PickImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void display(AndroidFileInfo entry) {
        this.requireDisplay = entry;
        requestIfNeed();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttached = true;
        requestIfNeed();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttached = false;
        if (null != job) {
            job.abort();
            job = null;
        }
    }

    public void requestIfNeed() {
        if (!isAttached) {
            if (null != job) {
                job.abort();
                job = null;
            }
            return;
        }

        if (this.requireDisplay == this.currenDisplay) return;
        if (null != job) {
            job.abort();
        }
        final AndroidFileInfo fileInfo = this.requireDisplay;
        job = new Job(this.requireDisplay);
        job.then(new Operator<AndroidFileInfo, Bitmap>() {
            @Override
            public Bitmap operate(AndroidFileInfo fileInfo) throws Throwable {
                return getThumbnail(fileInfo);
            }
        })
                .workOn(Schedulers.computation())
                .callbackOn(AndroidMainScheduler.INSTANCE)
                .error(new Job.Callback2() {
                    @Override
                    public void call(Throwable throwable, Object[] unexpectedResult) {
                        job = null;
                    }
                })
                .result(new Job.Callback1<Bitmap>() {
                    @Override
                    public void call(Bitmap result) {
                        if (null == result) {
                            // TODO
                            return;
                        }
                        PickImageView.this.setImageBitmap(result);
                        currenDisplay = fileInfo;
                        job = null;
                    }
                })
                .work();
    }

    public Bitmap getThumbnail(AndroidFileInfo fileInfo) {
        return DECODER.extractThumbnail(fileInfo.path, 200, 200, null);
    }

//    public Bitmap getThumbnail(AndroidTypedFileProvider.FileEntry fileEntry) {
//        if (!isAttached) return null;
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        try {
//            MediaStore.Images.Thumbnails.getThumbnail(getContext().getContentResolver(), Long.parseLong(fileEntry.id),
//                    MediaStore.Images.Thumbnails.MINI_KIND, options);
//        }catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
//        if (options.outWidth <= 0 || options.outHeight <= 0) {
//            BitmapFactory.decodeFile(fileEntry.path, options);
//        }
//        options.inJustDecodeBounds = false;
//        options.inSampleSize = FileConstants.calculateSampleSize(options.outWidth, options.outHeight, 400, 400);
//        Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(getContext().getContentResolver(),
//                Long.parseLong(fileEntry.id), MediaStore.Images.Thumbnails.MINI_KIND, options);
//        if (null == thumbnail) {
//            thumbnail = BitmapFactory.decodeFile(fileEntry.path, options);
//        }
//        return thumbnail;
//    }
}
