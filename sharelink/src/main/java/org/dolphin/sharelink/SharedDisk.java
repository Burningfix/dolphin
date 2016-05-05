package org.dolphin.sharelink;

import com.google.gson.Gson;

import org.dolphin.http.HttpRequest;
import org.dolphin.http.server.FileBean;
import org.dolphin.http.server.Main;
import org.dolphin.http.server.QueryFilesRequestHandler;
import org.dolphin.job.HttpJobs;
import org.dolphin.job.Job;
import org.dolphin.job.Log;
import org.dolphin.job.Operator;
import org.dolphin.job.operator.BytesToStringOperator;
import org.dolphin.job.operator.HttpPerformOperator;
import org.dolphin.job.operator.HttpResponseToBytes;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.lib.util.ValueUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yananh on 2015/11/7.
 */
public class SharedDisk {
    public static final String TAG = "SharedDisk";
    private final String serverIp;
    private final String serverPort;

    public SharedDisk(String ip, String port) {
        this.serverIp = ip;
        this.serverPort = port;
    }

    public String getServerIp() {
        return serverIp;
    }

    public String getServerPort() {
        return serverPort;
    }

    protected String parseQueryUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append("http://")
                .append(getServerIp())
                .append(':')
                .append(getServerPort())
                .append(Main.QUERY_FILE_LIST_PATH);
        return sb.toString();
    }

    public Job queryFileList(String mimeType, Job.Callback1<List<FileBean>> queryObserver) {
        String url = parseQueryUrl();
        Log.d(TAG, "Load Url " + url);
        final HttpRequest request = HttpJobs.create(url);
        Job job = new Job(request)
                .then(new HttpPerformOperator())
                .then(new HttpResponseToBytes())
                .then(new BytesToStringOperator())
                .then(new Operator<String, QueryFilesRequestHandler.FileTreeBean>() {
                    @Override
                    public QueryFilesRequestHandler.FileTreeBean operate(String input) throws Throwable {
                        Log.d(TAG, "Get Response " + input);
                        Gson gson = new Gson();
                        return gson.fromJson(input, QueryFilesRequestHandler.FileTreeBean.class);
                    }
                }).then(new Operator<QueryFilesRequestHandler.FileTreeBean, List<FileBean>>() {
                    @Override
                    public List<FileBean> operate(QueryFilesRequestHandler.FileTreeBean input) throws Throwable {
                        if (null == input) throw new Throwable("网络错误");
                        if (input.error != 0) {
                            if (!ValueUtil.isEmpty(input.msg)) {
                                throw new Throwable(input.msg);
                            }
                            throw new Throwable("网络错误");
                        }

                        if (input.files == null || input.files.length == 0) {
                            return new LinkedList<FileBean>();
                        }

                        LinkedList<FileBean> res = new LinkedList<FileBean>();
                        for (FileBean fileBean : input.files) {
                            fileBean.url = "http://" + getServerIp() + ":" + getServerPort() + fileBean.url;
                            res.add(fileBean);
                        }
                        return res;
                    }
                })
                .result(queryObserver)
                .workOn(Schedulers.computation())
                .callbackOn(AndroidMainThreadScheduler.INSTANCE)
                .work();
        return job;
    }

    @Override
    public String toString() {
        return getServerIp()+":"+getServerPort();
    }
}
