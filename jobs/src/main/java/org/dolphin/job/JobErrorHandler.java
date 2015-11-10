package org.dolphin.job;

/**
 * Created by hanyanan on 2015/10/13.
 */
public interface JobErrorHandler {
    JobRunningResult handleError(Job job, Throwable throwable);

    public static JobErrorHandler TERMINATE_HANDLER = new JobErrorHandler(){
        @Override
        public JobRunningResult handleError(Job job, Throwable throwable) {
            return JobRunningResult.TERMINATE;
        }
    };

    public static JobErrorHandler CONTINUE_HANDLER = new JobErrorHandler(){
        @Override
        public JobRunningResult handleError(Job job, Throwable throwable) {
            return JobRunningResult.CONTINUE;
        }
    };
}
