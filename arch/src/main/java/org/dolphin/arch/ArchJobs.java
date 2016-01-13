package org.dolphin.arch;

import org.dolphin.http.HttpRequest;
import org.dolphin.job.Job;
import org.dolphin.job.Operator;
import org.dolphin.job.operator.BytesToStringOperator;
import org.dolphin.job.operator.HttpPerformOperator;
import org.dolphin.job.operator.HttpResponseToBytes;
import org.dolphin.job.operator.StringToGson;
import org.dolphin.job.schedulers.Schedulers;

import java.util.Map;

/**
 * Created by hanyanan on 2016/1/13.
 */
public class ArchJobs {
    public static <T extends PageViewModel> Job parseHttpGetJob(String url, Map<String, String> params,
                                                                final String token, final Class<T> viewModelClazz,
                                                                final Job.Callback1<T> callback1) {
        HttpRequest request = new HttpRequest(url);
        request.params(params);
        Job<HttpRequest, T> job = new Job<HttpRequest, T>(request);
        job.then(new HttpPerformOperator())
                .then(new HttpResponseToBytes())
                .then(new BytesToStringOperator())
                .then(new StringToGson(viewModelClazz))
                .then(new Operator() {
                    @Override
                    public Object operate(Object input) throws Throwable {
                        if (PageViewModel.class.isInstance(input)) {
                            PageViewModel pageViewModel = (PageViewModel) input;
                            pageViewModel.token = token;
                        }
                        return input;
                    }
                })
                .workOn(Schedulers.computation())
                .callbackOn(AndroidMainScheduler.INSTANCE)
                .result(callback1)
                .error(new Job.Callback2() {
                    @Override
                    public void call(Throwable throwable, Object[] unexpectedResult) {
                        T pageModel = null;
                        try {
                            pageModel = viewModelClazz.newInstance();
                            pageModel.token = token;
                            pageModel.error = -1;
                            pageModel.msg = "网络错误";
                            callback1.call(pageModel);
                        } catch (Exception e) {
                            e.printStackTrace();
                            callback1.call(null);
                        }
                    }
                });
        return job;
    }


}
