package org.dolphin.job.schedulers;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by hanyanan on 2015/10/28.
 */
public class ExecutorScheduler implements Scheduler {
    private final Timer timer = new Timer();
    private final Executor executor;

    public ExecutorScheduler(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public Subscription schedule(final Runnable runnable) {
        final Subscription subscription = new Subscription() {
            private final AtomicBoolean isUnsubscription = new AtomicBoolean(false);

            @Override
            public void unsubscription() {
                if (isUnsubscription.get()) {
                    return;
                }
                isUnsubscription.set(true);
            }

            @Override
            public boolean isUnsubscription() {
                return isUnsubscription.get();
            }
        };
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (subscription.isUnsubscription()) {
                    return;
                }
                runnable.run();
            }
        });

        return subscription;
    }

    @Override
    public Subscription schedule(Runnable runnable, long delayTime, TimeUnit unit) {
        SubscriptionTimeTask subscriptionTimeTask = new SubscriptionTimeTask(runnable);

        timer.schedule(subscriptionTimeTask, unit.toMillis(delayTime));

        return subscriptionTimeTask;
    }

    @Override
    public Subscription schedulePeriodically(final Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        SubscriptionTimeTask subscriptionTimeTask = new SubscriptionTimeTask(runnable);

        timer.schedule(subscriptionTimeTask, unit.toMillis(initialDelay), unit.toMillis(period));

        return subscriptionTimeTask;
    }


    private class SubscriptionTimeTask extends TimerTask implements Subscription {
        Runnable runnable;
        AtomicBoolean isUnsubscription = new AtomicBoolean(false);

        private SubscriptionTimeTask(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void unsubscription() {
            synchronized (this) {
                if (!isUnsubscription.get()) {
                    this.cancel();
                }
                isUnsubscription.set(true);
            }
        }

        @Override
        public boolean isUnsubscription() {
            synchronized (this) {
                return isUnsubscription.get();
            }
        }

        @Override
        public void run() {
            if (isUnsubscription()) {
                return;
            }
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (isUnsubscription()) {
                        return;
                    }
                    runnable.run();
                }
            });

        }
    }
}
