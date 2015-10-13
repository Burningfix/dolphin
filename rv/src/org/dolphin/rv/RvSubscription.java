package org.dolphin.rv;


/**
 * Subscription returns from {@link RvObservable#subscribe(RvSubscriber)} to allow unsubscribing.
 * <p>
 * See the utilities in {@link RvSubscription} and the implementations in the {@code subscriptions} package.
 * <p>
 * This interface is the RxJava equivalent of {@code IDisposable} in Microsoft's Rx implementation.
 */
public interface RvSubscription {

    /**
     * Stops the receipt of notifications on the {@link RvSubscriber} that was registered when this Subscription
     * was received.
     * <p>
     * This allows unregistering an {@link RvSubscriber} before it has finished receiving all events (i.e. before
     * onCompleted is called).
     */
    void unsubscribe();

    /**
     * Indicates whether this {@code Subscription} is currently unsubscribed.
     *
     * @return {@code true} if this {@code Subscription} is currently unsubscribed, {@code false} otherwise
     */
    boolean isUnsubscribed();

}
