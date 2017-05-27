package com.wenjoyai.videoplayer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;

import com.wenjoyai.videoplayer.util.AndroidUtil;
import com.wenjoyai.videoplayer.util.BitmapCache;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by yuqilin on 17/3/16.
 */

public class QApplication extends Application {
    private static final String TAG = QApplication.class.getSimpleName();

    private static QApplication instance;

    private static SharedPreferences mSettings;

    /* Up to 2 threads maximum, inactive threads are killed after 2 seconds */
    int maxThreads = Math.max(AndroidUtil.isJellyBeanMR1OrLater() ? Runtime.getRuntime().availableProcessors() : 2, 1);
    private ThreadPoolExecutor mThreadPool = new ThreadPoolExecutor(Math.min(2, maxThreads), maxThreads, 2, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(), THREAD_FACTORY);
    public static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setPriority(Process.THREAD_PRIORITY_DEFAULT+Process.THREAD_PRIORITY_LESS_FAVORABLE);
            return thread;
        }
    };
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        instance = this;

    }

    /**
     * Called when the overall system is running low on memory
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w(TAG, "System is running low on memory");

        BitmapCache.getInstance().clear();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.w(TAG, "onTrimMemory, level: "+level);

        BitmapCache.getInstance().clear();
    }

    /**
     * @return the main context of the Application
     */
    public static Context getAppContext()
    {
        return instance;
    }

    /**
     * @return the main resources from the Application
     */
    public static Resources getAppResources()
    {
        return instance.getResources();
    }

    public static void runBackground(Runnable runnable) {
        instance.mThreadPool.execute(runnable);
    }

    public static void runOnMainThread(Runnable runnable) {
        instance.mHandler.post(runnable);
    }

    public static boolean removeTask(Runnable runnable) {
        return instance.mThreadPool.remove(runnable);
    }

    public static String getSnapshotStoragePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                getAppContext().getPackageName() + "/cutVideo/screenshot";
    }

    public static String getGifStoragePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                getAppContext().getPackageName() + "/cutVideo/gif";
    }

    public static String getVideoStoragePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                getAppContext().getPackageName() + "/cutVideo/video";
    }

}
