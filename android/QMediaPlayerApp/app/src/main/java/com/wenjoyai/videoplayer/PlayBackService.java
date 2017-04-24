package com.wenjoyai.videoplayer;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

/**
 * Created by yuqilin on 17/1/17.
 */

public class PlayBackService extends Service {

    private final static int PlayBackServiceID = 127001;

    private BiliFloatingView mFloatingView;

    private void createView(String playUrl) {
        mFloatingView = new BiliFloatingView(this, playUrl);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String playUrl = null;
        if (null != intent) {
            Bundle bundle = intent.getExtras();
            if (null != bundle) {
                playUrl = bundle.getString("playUrl");
            }
        }
        if (mFloatingView == null) {
            NotificationCompat.Builder NBuilder = new NotificationCompat.Builder(this).setSubText("QMediaPlayerFloatingPlay");
            startForeground(PlayBackServiceID, NBuilder.build());

            createView(playUrl);
        } else {
            mFloatingView.changeUrl(playUrl);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mFloatingView != null) {
            mFloatingView.fixViewPostion();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
