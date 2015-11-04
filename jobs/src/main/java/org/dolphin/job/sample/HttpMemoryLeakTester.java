package org.dolphin.job.sample;

import org.dolphin.http.HttpRequest;
import org.dolphin.http.HttpResponse;
import org.dolphin.http.HttpResponseHeader;
import org.dolphin.job.Job;
import org.dolphin.job.Jobs;
import org.dolphin.job.Observer;
import org.dolphin.job.Operator;
import org.dolphin.job.http.HttpJobs;
import org.dolphin.job.operator.*;
import org.dolphin.job.tuple.TwoTuple;
import org.dolphin.job.util.Log;
import org.dolphin.lib.FileInfo;
import org.dolphin.lib.ValueUtil;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by hanyanan on 2015/10/29.
 */
public class HttpMemoryLeakTester {

    public static Job printMemoryUsed() {
        Job job = new Job(null);
        job.append(new Operator() {
            @Override
            public Object operate(Object input) throws Throwable {
                final Runtime run = Runtime.getRuntime();
                long total = run.totalMemory() - run.freeMemory();
                System.out.println("已分配内存 = " + FileInfo.sizeString(total));
                return input;
            }
        });

        return job;
    }

    public static final String[] urls = new String[]{
            "http://jaist.dl.sourceforge.net/project/shadowsocksgui/dist/Shadowsocks-win-2.5.2.zip", // 187K
            "http://oss.reflected.net/jenkins/86465/cm-11-20141008-SNAPSHOT-M11-n7100.zip", // 214M
            "http://mirror.cyanogenmod.org/jenkins/128821/cm-11-20151004-NIGHTLY-n7100-recovery.img", // 7.3M
            "http://dl_dir.qq.com/invc/qqplayer/QQPlayerMini_Setup_3.2.845.500.exe", //
            "http://dl2.itools.hk/dl/itools3/iToolsSetup_3.2.0.6.exe", // 20.8M
            "http://war3down1.uuu9.com/war3/201410/201410281152.rar", // error
            "http://www.baidu.com",
            "http://img.wallpapersking.com/800/2015-10/2015102907311.jpg", //
            "http://www.google.com.hk",
            "http://img.99118.com/800px/201510/01658001025607B5.jpg",
            "http://img.daimg.com/uploads/allimg/151027/3-15102F00512.jpg",
    };

    public static Job[] jobs() {
        Job[] res = new Job[urls.length];
        int index = 0;
        for (String url : urls) {
            final int defaultName = index;
            HttpRequest request = HttpJobs.create(url);
            Job job = new Job(request);
            job.append(new HttpPerformOperator());
            Operator read = new Operator<HttpResponse, InputStream>() {

                @Override
                public InputStream operate(HttpResponse input) throws Throwable {
//                    return input.body().getResource().openStream();
                    return input.body();
                }
            };

            Operator write = new Operator<HttpResponse, OutputStream>() {

                @Override
                public OutputStream operate(HttpResponse input) throws Throwable {
                    HttpResponseHeader responseHeader = input.getResponseHeader();
                    String fileName = null;
                    if (null != responseHeader && !ValueUtil.isEmpty(responseHeader.getDisposition())) {
                        fileName = responseHeader.getDisposition();
                    } else {
                        fileName = "" + defaultName;
                    }

                    return new FileOutputStream("D:\\tmp\\" + fileName);
                }
            };

            Operator size = new Operator<HttpResponse, Long>() {

                @Override
                public Long operate(HttpResponse input) throws Throwable {
//                    return input.body().getResource().size();
                    return input.getTransportLength();
                }
            };

            List<Operator> operatorList = new ArrayList<Operator>();
            operatorList.add(read);
            operatorList.add(write);
            operatorList.add(size);
            job.merge(operatorList);
            job.until(new StreamCopyOperator(), true);
            job.observer(new Observer<TwoTuple<Long, Long>, Object>() {
                @Override
                public void onNext(Job job, TwoTuple<Long, Long> next) {
                    if (null != next) {
                        Log.d("runBlockDownload", defaultName + " next [" + next.value1 + " - " + next.value2 + "]");
                    }
                }

                @Override
                public void onCompleted(Job job, Object result) {
                    Log.d("runBlockDownload", defaultName + " onCompleted");
                }

                @Override
                public void onFailed(Job job, Throwable error) {
                    Log.d("runBlockDownload", defaultName + " onFailed");
                }

                @Override
                public void onCancellation(Job job) {
                    Log.d("runBlockDownload", defaultName + " onCancellation");
                }
            });

            res[index++] = job;
        }
        return res;
    }


    public static void main(String[] argv) {
        Job printJob = printMemoryUsed();
        printJob.workPeriodic(1000, 1000, TimeUnit.MILLISECONDS);
        Job[] jobs = jobs();
        for (Job job : jobs) {
            job.workPeriodic(1000, 1000, TimeUnit.MILLISECONDS);
//            job.work();
        }
    }
}
