package org.dolphin.arch;

import android.os.Parcel;
import android.os.Parcelable;

import org.dolphin.http.HttpRequest;
import org.dolphin.job.Job;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Created by hanyanan on 2015/12/4.
 * <p/>
 * 保存状态，请求成功后就是
 */
public class PageModel implements Parcelable {
    private final LinkedList<Job> backgroundJobList = new LinkedList<Job>();

    public <T> void addHttpJsonJob() {

    }

    public <T> void addHttpJsonJob(HttpRequest request, Class<T> clazz) {

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

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);

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
