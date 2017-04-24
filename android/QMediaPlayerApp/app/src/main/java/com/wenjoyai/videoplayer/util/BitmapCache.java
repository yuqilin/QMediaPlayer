package com.wenjoyai.videoplayer.util;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.wenjoyai.videoplayer.QApplication;

import java.lang.ref.SoftReference;

/**
 * Created by yuqilin on 17/3/13.
 */

public class BitmapCache {

    public final static String TAG = "BitmapCache";
    private final static boolean LOG_ENABLED = false;

//    private static final String CONE_KEY = "res:"+ R.drawable.cone;
//    private static final String CONE_O_KEY = "res:"+ R.drawable.ic_cone_o;
    private static BitmapCache mInstance;
    private final LruCache<String, CacheableBitmap> mMemCache;

    public synchronized static BitmapCache getInstance() {
        if (mInstance == null)
            mInstance = new BitmapCache();
        return mInstance;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private BitmapCache() {

        // Get memory class of this device, exceeding this amount will throw an
        // OutOfMemory exception.
        final ActivityManager am = ((ActivityManager) QApplication.getAppContext().getSystemService(Context.ACTIVITY_SERVICE));
        final int memClass = AndroidUtil.isHoneycombOrLater() ? am.getLargeMemoryClass() : am.getMemoryClass();

        // Use 1/5th of the available memory for this memory cache.
        final int cacheSize = 1024 * 1024 * memClass / 5;

        Log.i(TAG, "LRUCache size set to " + cacheSize);

        mMemCache = new LruCache<String, CacheableBitmap>(cacheSize) {

            @Override
            protected int sizeOf(String key, CacheableBitmap value) {
                return value.getSize();
            }
        };
    }

    public synchronized Bitmap getBitmapFromMemCache(String key) {
        final CacheableBitmap cacheableBitmap = mMemCache.get(key);
        if (cacheableBitmap == null)
            return null;
        Bitmap b = cacheableBitmap.get();
        if (b == null){
            mMemCache.remove(key);
            return null;
        }
        if (LOG_ENABLED)
            Log.d(TAG, (b == null) ? "Cache miss" : "Cache found");
        return b;
    }

    public synchronized void addBitmapToMemCache(String key, Bitmap bitmap) {
        if (key != null && bitmap != null && getBitmapFromMemCache(key) == null) {
            final CacheableBitmap cacheableBitmap = new CacheableBitmap(bitmap);
            mMemCache.put(key, cacheableBitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(int resId) {
        return getBitmapFromMemCache("res:" + resId);
    }

    private void addBitmapToMemCache(int resId, Bitmap bitmap) {
        addBitmapToMemCache("res:" + resId, bitmap);
    }

    public synchronized void clear() {
        mMemCache.evictAll();
    }

    public static Bitmap getFromResource(Resources res, int resId) {
        BitmapCache cache = BitmapCache.getInstance();
        Bitmap bitmap = cache.getBitmapFromMemCache(resId);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(res, resId);
            cache.addBitmapToMemCache(resId, bitmap);
        }
        return bitmap;
    }

    private static class CacheableBitmap {
        final int mSize;
        final SoftReference<Bitmap> mReference;

        CacheableBitmap(Bitmap bitmap){
            mReference = new SoftReference<Bitmap>(bitmap);
            mSize = bitmap == null ? 0 : bitmap.getRowBytes() * bitmap.getHeight();
        }

        public SoftReference<Bitmap> getReference(){
            return mReference;
        }

        public Bitmap get(){
            if (mReference != null)
                return mReference.get();
            return null;
        }

        public int getSize(){
            return mSize;
        }
    }
}

