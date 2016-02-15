package org.dolphin.arch;

import android.os.Parcel;
import android.os.Parcelable;

import org.dolphin.job.Job;
import org.dolphin.job.Operator;
import org.dolphin.lib.ValueReference;

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


    /**
     * 从server下载的数据直接就是可以使用的json，直接转化成指定的viewModel type。
     *
     * @param url            请求的url
     * @param params         请求的参数
     * @param viewModelClazz 转化的最终的json
     */
    public <T extends PageViewModel> void registerQuest(String token, String url, Map<String, String> params, Class<T> viewModelClazz) {
        final ValueReference<Job> delayValueRef = new ValueReference<Job>();
        Job.Callback1<T> responseCallback = new Job.Callback1<T>() {
            @Override
            public void call(T result) {
                bindedPageView.foundViewModel(result);
                Job job = delayValueRef.getValue();
                if (null != job) {
                    remainderJobs.remove(job);
                }
            }
        };
        Job job = ArchJobs.parseHttpGetJob(url, params, token, viewModelClazz, responseCallback);
        delayValueRef.setValue(job);
        remainderJobs.add(job);
    }

//    public <T extends PageViewModel> void register(String token, Operator<? extends Serializable, T> operator) {
//        final ValueReference<Job> delayValueRef = new ValueReference<Job>();
//        Job.Callback1<T> responseCallback = new Job.Callback1<T>() {
//            @Override
//            public void call(T result) {
//                bindedPageView.foundViewModel(result);
//                Job job = delayValueRef.getValue();
//                if (null != job) {
//                    remainderJobs.remove(job);
//                }
//            }
//        };
//        delayValueRef.setValue(job);
//        remainderJobs.add(job);
//    }

    public <T extends PageView> void setBindPageView(T bindPageView) {
        this.bindedPageView = bindPageView;
    }

    public void start() {
        Util.checkThreadState();
        for (Job job : remainderJobs) {
            job.work();
        }
    }

    public void stop() {
        Util.checkThreadState();
        for (Job job : remainderJobs) {
            job.abort();
        }
    }

//    public final boolean supportReuse() {
//        return false;
//    }

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
