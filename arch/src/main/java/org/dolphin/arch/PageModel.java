package org.dolphin.arch;

import android.os.Parcel;
import android.os.Parcelable;
import org.dolphin.job.Job;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by hanyanan on 2015/12/4.
 * <p/>
 * 保存状态，请求成功后就是
 */
public class PageModel implements Parcelable {
    /**
     * 剩余需要执行的job
     */
    private final LinkedList<Job<? extends Serializable, ?>> remainderJobs = new LinkedList<Job<? extends Serializable, ?>>();
    /**
     * 绑定的pagevView
     */
    private PageView bindedPageView;

    public <T extends PageViewModel> void addHttpJsonJob(String url, Map<String, String> params, Class<T> viewModelClazz) {

    }

    public <T extends PageViewModel> void addHttpJsonJob(String url, Map<String, String> params) {

    }

    public void addJob(Job<? extends Serializable, ?> job) {

    }

    public <T extends PageView> void setBindedPageView(T bindedPageView) {
        this.bindedPageView = bindedPageView;
    }

    public void start() {
        Util.checkThreadState();

    }

    public void stop() {
        Util.checkThreadState();

    }

    public void reset() {
        Util.checkThreadState();

    }


    @Override
    public int describeContents() {
        return 0;
    }


    private PageModel(Parcel in) {
        int size = in.readInt();
        // 恢复未完成的job
        for (int i = 0; i < size; ++i) {
            Serializable input = in.readSerializable();
            Job job = (Job) in.readSerializable();
            job.setInput(job);
            remainderJobs.add(job);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        final LinkedList<Job<? extends Serializable, ?>> remainderJobs = new LinkedList<Job<? extends Serializable, ?>>(this.remainderJobs);
        int size = remainderJobs.size();
        dest.writeInt(size);
        for (Job<? extends Serializable, ?> job : remainderJobs) {
            dest.writeSerializable(job.getInput());
            dest.writeSerializable(job);
        }
        dest.writeStrongBinder();
    }

    public static final Parcelable.Creator<PageModel> CREATOR = new Parcelable.Creator<PageModel>() {
        public PageModel createFromParcel(Parcel in) {
            return new PageModel(in);
        }

        public PageModel[] newArray(int size) {
            return new PageModel[size];
        }
    };
}
