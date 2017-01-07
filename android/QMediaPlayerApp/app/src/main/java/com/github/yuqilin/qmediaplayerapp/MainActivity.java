package com.github.yuqilin.qmediaplayerapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "QMediaPlayer-MainActivity";

    private EditText mVideoPathEdit;
    private Button mPlayBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVideoPathEdit = (EditText) findViewById(R.id.video_path_edit);
        mPlayBtn = (Button) findViewById(R.id.play_btn);

        mVideoPathEdit.setText("/sdcard/BigBuckBunny_320x180.mp4");

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClickPlay(View v) {
        String videoPath = mVideoPathEdit.getText().toString();
        if (!TextUtils.isEmpty(videoPath)) {
            jumpToPlayerActivity(videoPath);
        }
    }

    private void jumpToPlayerActivity(String videoPath) {
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra("videoPath", videoPath);
        startActivity(intent);
    }
}
