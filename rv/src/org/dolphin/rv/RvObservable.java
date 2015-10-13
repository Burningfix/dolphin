package org.dolphin.rv;

import org.dolphin.rv.functions.Action1;
import org.dolphin.rv.functions.Func;

import java.util.WeakHashMap;

/**
 * Created by hanyanan on 2015/9/18.
 */

/**
 * Created by hanyanan on 2015/9/17.
 * <p/>
 * <p/>
 * <table border="1">
 * <tr>
 * <td>operator</td>
 * <td>Description</td>
 * <td>Sample</td>
 * </tr>
 * <tr>
 * <td>map</td>
 * <td>transform one emitted item into another</td>
 * <td>map(s -> s + " -Dan")</td>
 * </tr>
 * <tr>
 * <td>flatMap</td>
 * <td>(new Func1<List<String>, Observable<String>)takes the emissions of one Observable
 * and returns the emissions of another Observable to take its place.它接收一系列单个的String，
 * 然后通过Observable.from()返回，简而言之就是flatMap()能够将包含多个的对象打散成一个一个单独的对象。
 * </td>
 * <td width=400>{@code
 * query("Hello, world!")
 * .flatMap(urls -> Observable.from(urls))
 * .flatMap(new Func1<String, Observable<String>>() {
 *
 * @Override public Observable<String> call(String url) {
 * return getTitle(url);
 * }
 * })
 * .subscribe(title -> System.out.println(title));}
 * </td>
 * </tr>
 * <p/>
 * <tr>
 * <td>from</td>
 * <td>transform one emitted item into another</td>
 * <td>transform one emitted item into another</td>
 * </tr>
 * <p/>
 * <tr>
 * <td>filter</td>
 * <td>emits the same item it received, but only if it passes the boolean check.</td>
 * <td>Observer.filter(title -> title != null)</td>
 * </tr>
 * <p/>
 * <tr>
 * <td>doOnNext</td>
 * <td>allows us to add extra behavior each time an item is emitted, in this case saving the title.</td>
 * <td>transform one emitted item into another</td>
 * </tr>
 * <p/>
 * <tr>
 * <td>concat</td>
 * <td>concatenate two or more Observables sequentially</td>
 * <td>transform one emitted item into another</td>
 * </tr>
 * <p/>
 * <tr>
 * <td>from</td>
 * <td>transform one emitted item into another</td>
 * <td>transform one emitted item into another</td>
 * </tr>
 * <p/>
 * <tr>
 * <td>from</td>
 * <td>transform one emitted item into another</td>
 * <td>transform one emitted item into another</td>
 * </tr>
 * <p/>
 * <tr>
 * <td>from</td>
 * <td>transform one emitted item into another</td>
 * <td>transform one emitted item into another</td>
 * </tr>
 * <p/>
 * <tr>
 * <td>from</td>
 * <td>transform one emitted item into another</td>
 * <td>transform one emitted item into another</td>
 * </tr>
 * <p/>
 * <tr>
 * <td>from</td>
 * <td>transform one emitted item into another</td>
 * <td>transform one emitted item into another</td>
 * </tr>
 * <p/>
 * <tr>
 * <td>from</td>
 * <td>transform one emitted item into another</td>
 * <td>transform one emitted item into another</td>
 * </tr>
 * <p/>
 * <tr>
 * <td>from</td>
 * <td>transform one emitted item into another</td>
 * <td>transform one emitted item into another</td>
 * </tr>
 * <p/>
 * <tr>
 * <td>from</td>
 * <td>transform one emitted item into another</td>
 * <td>transform one emitted item into another</td>
 * </tr>
 * <p/>
 * <tr>
 * <td>from</td>
 * <td>transform one emitted item into another</td>
 * <td>transform one emitted item into another</td>
 * </tr>
 * <p/>
 * <tr>
 * <td>from</td>
 * <td>transform one emitted item into another</td>
 * <td>transform one emitted item into another</td>
 * </tr>
 * <p/>
 * <tr>
 * <td>from</td>
 * <td>transform one emitted item into another</td>
 * <td>transform one emitted item into another</td>
 * </tr>
 * <p/>
 * <tr>
 * <td>zip</td>
 * <td>transform one emitted item into another</td>
 * <td>transform one emitted item into another</td>
 * </tr>
 * <tr>
 * <td>merge</td>
 * <td>transform one emitted item into another</td>
 * </tr>
 * </table>
 */
public class RvObservable<T, R> implements Cloneable{


//    public static <T> RvObservable from(T... input) {
//
//        return null;
//    }
//
//    /**
//     * convert a Runnable into an Observable that invokes the runnable and emits its result when a Subscriber subscribes
//     *
//     * @param runnable
//     * @return
//     */
//    public static RvObservable fromRunnable(Runnable runnable) {
//
//        return null;
//    }
//
//    /**
//     * convert a Callable into an Observable that invokes the callable and emits its result or exception when a
//     * Subscriber subscribes
//     *
//     * @param callable
//     * @return
//     */
//    public static RvObservable fromCallable(Callable callable) {
//
//        return null;
//    }
//
//    /**
//     * combine multiple Observables into one
//     * You can combine the output of multiple Observables so that they act like a single Observable, by using the Merge
//     * operator.
//     * <br/>
//     * Ay
//     * @param rvObservables all rvObservers need merged.
//     * @return
//     */
//    public static RvObservable merge(RvObservable ... rvObservables){
//
//
//
//        return null;
//    }
//
//    /**
//     *
//     * @param rvObservables
//     * @return
//     */
//    public static RvObservable mergeDelayError(RvObservable ... rvObservables){
//
//        return null;
//    }
//
//    /**
//     * emit the emissions from two or more Observables without interleaving them.<br/>
//     * The Concat operator concatenates the output of multiple Observables so that they act like a single Observable,
//     * with all of the items emitted by the first Observable being emitted before any of the items emitted by the second
//     * Observable (and so forth, if there are more than two).
//     * @param observable
//     * @return
//     */
//    public static RvObservable concat(RvObservable observable) {
//
//        return null;
//    }
//
//
//    public static RvObservable until(RvObservable ... observables){
//        return null;
//    }

    private static final WeakHashMap<?, ? extends RvObservable> OBSERVABLE_BUFFER =
            new WeakHashMap<Object, RvObservable>();
    public static <T> RvObservable just(T input) {
        return null;
    }

    public static <T> RvObservable query(T key){

        return null;
    }


    private final RvOnSubscribe<T, R>  onSubscribe;

    public RvObservable(RvOnSubscribe<T, R> onSubscribe){
        this.onSubscribe = onSubscribe;
    }


    public final RvObservable errorHandler(RvErrorHandler errorHandler) {

        return this;
    }

    public final RvObservable map(Func func) {

        return this;
    }

    public final RvObservable subscribe(Action1 nextAction) {

        return this;
    }

    public final RvObservable subscribe(Action1 nextAction, Action1 completeAction) {

        return this;
    }

    public final RvObservable subscribe(Action1 nextAction, Action1 completeAction, Action1<Throwable> errorAction) {

        return this;
    }


    /**
     * 从buffer中尝试得到指定的RvObservable
     * @param key 唯一的标识
     * @return
     */
    public static synchronized RvObservable fetch(Object key){

        return null;
    }


    public RvObservable clone(){

        return null;
    }



    public interface RvOnSubscribe<T, R> extends Action1<RvSubscriber<T, R>>{
        void call(RvSubscriber<T, R> t);
    }
}
