package org.dolphin.rx;

/**
 * Created by hanyanan on 2015/9/17.
 */
public interface Subscription {

    void unsubscribe();

    /**
     * Indicates whether this {@code Subscription} is currently unsubscribed.
     *
     * @return {@code true} if this {@code Subscription} is currently unsubscribed, {@code false} otherwise
     */
    boolean isUnsubscribed();
}
