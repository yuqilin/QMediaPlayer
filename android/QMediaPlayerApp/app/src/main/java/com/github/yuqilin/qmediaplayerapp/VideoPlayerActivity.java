package com.github.yuqilin.qmediaplayerapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.github.yuqilin.qmediaplayer.QMediaPlayerVideoView;

import java.io.File;

/**
 * Created by yuqilin on 17/1/6.
 */

public class VideoPlayerActivity extends Activity {

    private static final String TAG = "VideoPlayerActivity";

    private String mVideoPath;
    private PlayerTitleView mTitleView;
    private PlayerBottomView mBottomView;
    private QMediaPlayerVideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        mVideoView = (QMediaPlayerVideoView) findViewById(R.id.player_video_view);
        mTitleView = (PlayerTitleView) findViewById(R.id.player_title_view);
        mBottomView = (PlayerBottomView) findViewById(R.id.player_bottom_view);

        mVideoPath = getIntent().getStringExtra("videoPath");

        mTitleView.setTitle(getFileName(mVideoPath));

        File file = new File(mVideoPath);
        if (!file.exists()) {
            Log.w(TAG, "File not exitsts or can not read");
        }

        mVideoView.setVideoPath(mVideoPath);
        mVideoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.resume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mVideoView.stopPlayback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoView.stopPlayback();
    }

    private String getFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf('/')+1, filePath.lastIndexOf('.'));
    }

}
