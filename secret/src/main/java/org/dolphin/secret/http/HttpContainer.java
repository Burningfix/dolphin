package org.dolphin.secret.http;

import org.dolphin.secret.core.ObscureFileInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Created by hanyanan on 2016/5/10.
 * <p/>
 * 用于http
 */
public class HttpContainer {
    private static HttpContainer instance = null;

    public static synchronized HttpContainer getInstance() {
        if (null == instance) {
            instance = new HttpContainer();
        }
        return instance;
    }

    private HttpContainer() {

    }

    private final List<String> ids = new LinkedList<String>();
    private final WeakHashMap<String, ObscureFileInfo> map = new WeakHashMap<String, ObscureFileInfo>();
    private final WeakHashMap<ObscureFileInfo, String> remap = new WeakHashMap<ObscureFileInfo, String>();

    public synchronized String deliveryId(ObscureFileInfo fileInfo) {
        String pre = remap.get(fileInfo);
        if (null != pre) {
            return pre;
        }
        String id = "" + System.nanoTime() * (System.currentTimeMillis() % 1000);
        remap.put(fileInfo, id);
        map.put(id, fileInfo);
        ids.add(id);
        trim();
        return id;
    }

    public synchronized ObscureFileInfo getFileInfo(String id) {
        return map.get(id);
    }

    private synchronized void trim() {
        while (ids.size() > 10) {
            remap.remove(map.remove(ids.remove(0)));
        }
    }
}
