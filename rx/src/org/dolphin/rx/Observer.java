package org.dolphin.rx;

/**
 * Created by hanyanan on 2015/9/17.
 *
 *
 */
public interface Observer<T> {

    /**
     *
     * The {@link Observable} will not call this method if it calls {@link #onError}.
     */
    public void onComplete();


    /**
     * Notifies the Observer that the {@link Observable} has experienced an error condition.
     * @param throwable the exception encountered by the Observable
     */
    public void onError(Throwable throwable);


    /**
     * Provides the Observer with new item to observe.
     * <p>
     *
     *
     * @param t
     *          the item emitted by the Observable
     */
    public void onNext(T t);

}
