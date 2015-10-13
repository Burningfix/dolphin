package org.dolphin.rx;

import org.dolphin.rx.functions.Action;
import org.dolphin.rx.functions.Function;

/**
 * Created by hanyanan on 2015/9/17.
 * <p/>
 * <p/>
 * <table border="1">
 * <tr>
 *      <td>operator</td>
 *      <td>Description</td>
 *      <td>Sample</td>
 * </tr>
 * <tr>
 *      <td>map</td>
 *      <td>transform one emitted item into another</td>
 *      <td>map(s -> s + " -Dan")</td>
 * </tr>
 * <tr>
 *      <td>flatMap</td>
 *      <td>(new Func1<List<String>, Observable<String>)takes the emissions of one Observable
 * and returns the emissions of another Observable to take its place.它接收一系列单个的String，
 * 然后通过Observable.from()返回，简而言之就是flatMap()能够将包含多个的对象打散成一个一个单独的对象。
 * </td>
 *      <td width=400>{@code
 * query("Hello, world!")
 * .flatMap(urls -> Observable.from(urls))
 * .flatMap(new Func1<String, Observable<String>>() {
 *      @Override
 *      public Observable<String> call(String url) {
 *          return getTitle(url);
 *      }
 * })
 * .subscribe(title -> System.out.println(title));}
 *      </td>
 * </tr>
 * <tr>
 *      <td>from</td>
 *      <td>transform one emitted item into another</td>
 *      <td>transform one emitted item into another</td>
 * </tr>
 * <tr>
 *      <td>filter</td>
 *      <td>emits the same item it received, but only if it passes the boolean check.</td>
 *      <td>Observer.filter(title -> title != null)</td>
 * </tr>
 * <tr>
 *      <td>doOnNext</td>
 *      <td>allows us to add extra behavior each time an item is emitted, in this case saving the title.</td>
 *      <td>transform one emitted item into another</td>
 * </tr>
 * <tr>
 *      <td>concat</td>
 *      <td>concatenate two or more Observables sequentially</td>
 *      <td>transform one emitted item into another</td>
 * </tr>
 * <tr>
 *      <td>from</td>
 *      <td>transform one emitted item into another</td>
 *      <td>transform one emitted item into another</td>
 * </tr>
 * <tr>
 *      <td>from</td>
 *      <td>transform one emitted item into another</td>
 *      <td>transform one emitted item into another</td>
 * </tr>
 * <tr>
 *      <td>from</td>
 *      <td>transform one emitted item into another</td>
 *      <td>transform one emitted item into another</td>
 * </tr>
 * <tr>
 *      <td>from</td>
 *      <td>transform one emitted item into another</td>
 *      <td>transform one emitted item into another</td>
 * </tr>
 * <tr>
 *      <td>from</td>
 *      <td>transform one emitted item into another</td>
 *      <td>transform one emitted item into another</td>
 * </tr>
 * <tr>
 *      <td>from</td>
 *      <td>transform one emitted item into another</td>
 *      <td>transform one emitted item into another</td>
 * </tr>
 *
 * <tr>
 *      <td>from</td>
 *      <td>transform one emitted item into another</td>
 *      <td>transform one emitted item into another</td>
 * </tr>
 *
 * <tr>
 *      <td>from</td>
 *      <td>transform one emitted item into another</td>
 *      <td>transform one emitted item into another</td>
 * </tr>
 *
 * <tr>
 *      <td>from</td>
 *      <td>transform one emitted item into another</td>
 *      <td>transform one emitted item into another</td>
 * </tr>
 *
 * <tr>
 *      <td>from</td>
 *      <td>transform one emitted item into another</td>
 *      <td>transform one emitted item into another</td>
 * </tr>
 *
 * <tr>
 *      <td>from</td>
 *      <td>transform one emitted item into another</td>
 *      <td>transform one emitted item into another</td>
 * </tr>
 *
 * <tr>
 *      <td>zip</td>
 *      <td>transform one emitted item into another</td>
 *      <td>transform one emitted item into another</td>
 * </tr>
 * <tr>
 *      <td>merge</td>
 *      <td>transform one emitted item into another</td>
 * </tr>
 * </table>
 */
public class Observable {


    /**
     * Read from input param and output a new value.
     *
     * @param function
     * @return
     */
    public final Observable map(Function function) {

        return this;
    }

    /**
     * @param functions
     * @return
     */
    public final Observable maps(Function... functions) {

        return this;
    }

    /**
     * takes a collection of items and emits each them one at a time:
     *
     * @param inputs a collection of items will be processed.
     * @return
     */
    public final static <T> Observable from(T ... inputs) {

        return null;
    }

    public final Observable subscribe(Action action) {

        return this;
    }

    public final Observable exceptionHandler(){

        return this;
    }


}
