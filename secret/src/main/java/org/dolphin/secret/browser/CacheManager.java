package org.dolphin.secret.browser;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import org.dolphin.secret.core.FileInfoContentCache;

import java.io.File;
import java.util.Map;

/**
 * Created by hanyanan on 2016/1/20.
 */
public class CacheManager {
    private static final int MIN_MEM_CACHE_SIZE = 4 * 0x400 * 0x400; // 4m
    private static final int MAX_MEM_CACHE_SIZE = 16 * 0x400 * 0x400; // 16m
    private static final int MEM_CACHE_LIFETIME = -1; // unlimited
    //
//    static {
//        int cacheSize = MIN_MEM_CACHE_SIZE;
//        if (null != BDApplication.instance()) {
//            ActivityManager activityManager = (ActivityManager) BDApplication.instance()
//                    .getSystemService(Context.ACTIVITY_SERVICE);
//            if (null != activityManager) {
//                int heapSize = activityManager.getMemoryClass();
//                Log.i("heap", "heap size " + heapSize);
//                cacheSize = heapSize / 8;
//                cacheSize = cacheSize * 1024 * 1024;
//                cacheSize = cacheSize < MIN_MEM_CACHE_SIZE ? MIN_MEM_CACHE_SIZE : cacheSize;
//                cacheSize = cacheSize > MAX_MEM_CACHE_SIZE ? MAX_MEM_CACHE_SIZE : cacheSize;
//            }
//        }
//        Log.i("heap", "cache size " + cacheSize);
//        MEM_CACHE = new MemCache<String, Bitmap>(cacheSize, MEM_CACHE_LIFETIME) {
//            @Override
//            protected int sizeOf(Object object) {
//                if (object instanceof Bitmap) {
//                    Bitmap bmp = (Bitmap) object;
//                    return bmp.getHeight() * bmp.getRowBytes();
//                } else {
//                    return super.sizeOf(object);
//                }
//            }
//        };
//    }
//
//    public static Map<String, Bitmap> memcache() {
//        return MEM_CACHE;
//    }
//
    private static CacheManager sInstance = null;

    public synchronized static CacheManager getInstance() {
        if (null == sInstance) {
            sInstance = new CacheManager();
        }

        return sInstance;
    }

    private CacheManager() {

    }

    private final LruCache<String, FileInfoContentCache> lruCache = new LruCache<String, FileInfoContentCache>(MAX_MEM_CACHE_SIZE) {
        @Override
        protected int sizeOf(String key, FileInfoContentCache value) {
            if (null == value) return 0;
            int size = 0;
            if (value.headBodyContent != null) {
                size += value.headBodyContent.length;
            }

            if (value.footBodyContent != null) {
                size += value.footBodyContent.length;
            }

            if (null != value.thumbnail) {
                size += value.thumbnail.getHeight() * value.thumbnail.getRowBytes();
            }

            return size;
        }

        @Override
        protected void entryRemoved(boolean evicted, String key, FileInfoContentCache oldValue, FileInfoContentCache newValue) {
            super.entryRemoved(evicted, key, oldValue, newValue);
        }
    };

    public synchronized void putCache(String path, FileInfoContentCache cache) {
        lruCache.put(path, cache);
    }

    public synchronized FileInfoContentCache getCache(String path) {
        return lruCache.get(path);
    }
}
