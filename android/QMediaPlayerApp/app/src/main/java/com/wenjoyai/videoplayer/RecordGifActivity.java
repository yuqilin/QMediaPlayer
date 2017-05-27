package com.wenjoyai.videoplayer;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.yuqilin.qmediaplayer.QMediaPlayerVideoView;
import com.wenjoyai.videoplayer.util.Strings;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yuqilin on 17/5/24.
 */

public class RecordGifActivity extends AppCompatActivity {
    private static final String TAG = "RecordGifActivity";

    private QMediaPlayerVideoView mVideoView;

    private SeekBar mSeekbarLeft;
    private SeekBar mSeekbarRight;
    private ImageView mRecordStop;
    private TextView mCancel;

    private TextView mCurrentPlayPos;

    private static final int UPDATE_PROGRESS = 1;
    private static final int RECORD_STOP = 2;

    private static final int DEFAULT_FPS = 5;
    private static final int DEFAULT_SCALE_WIDTH = -2;
    private static final int DEFAULT_SCALE_HEIGHT = 480;

    // MediaWrapper
    private String mVideoPath;
    private long mStartPlayPos = 0;

    private int mCurrentProgress = 0;
    private int mMinProgress = 3;
    private int mMaxProgress = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_gif);
        mVideoPath = getIntent().getStringExtra("videoPath");
        mStartPlayPos = getIntent().getLongExtra("startPos", 0);

        mVideoView = (QMediaPlayerVideoView) findViewById(R.id.record_gif_player_video_view);
        mSeekbarLeft = (SeekBar) findViewById(R.id.record_gif_seekbar_left);
        mSeekbarRight = (SeekBar) findViewById(R.id.record_gif_seekbar_right);
        mRecordStop = (ImageView) findViewById(R.id.record_gif_stop);
        mCancel = (TextView) findViewById(R.id.record_gif_cancel);

        mCurrentPlayPos = (TextView) findViewById(R.id.record_gif_current_play_pos);

        mSeekbarLeft.setMax(mMaxProgress);
        mSeekbarRight.setMax(mMaxProgress);
        mRecordStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.removeMessages(UPDATE_PROGRESS);
                mHandler.sendEmptyMessage(RECORD_STOP);
            }
        });
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.removeCallbacksAndMessages(null);
                mVideoView.stopPlayback();
                finish();
            }
        });

        mVideoView.setVideoPath(mVideoPath);
        mVideoView.seekTo(mStartPlayPos);
        mVideoView.start();
//        mCurrentPlayPos.setText(Strings.millisToString(mVideoView.getCurrentPosition()));
        mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        mVideoView.stopPlayback();
    }

    private Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {

        @Override
        public boolean handleMessage(Message message) {
            long pos = 0;
            switch (message.what) {
//                case SHOW_PROGRESS:
//                    pos = setPlayProgress();
//                    message = mHandler.obtainMessage(SHOW_PROGRESS);
//                    mHandler.sendMessageDelayed(message, 1000 - (pos % 1000));
//                    break;
                case UPDATE_PROGRESS:
                    mCurrentProgress++;
                    mSeekbarLeft.setProgress(mCurrentProgress);
                    mSeekbarRight.setProgress(mCurrentProgress);
                    if (mCurrentProgress <= mMaxProgress) {
                        mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
                    } else {
                        mHandler.sendEmptyMessage(RECORD_STOP);
                    }
                    mCurrentPlayPos.setText(Strings.millisToString(mVideoView.getCurrentPosition()));
                    break;
                case RECORD_STOP:
                    mVideoView.stopPlayback();
                    doRecordGif();
                    break;
            }
            return true;
        }
    });

    private void doRecordGif() {
        File gifDir = new File(QApplication.getGifStoragePath());
        if (!gifDir.exists()) {
            if (!gifDir.mkdirs()) {
                Log.e(TAG, "");
                return;
            }
        }
        String gifFile = QApplication.getGifStoragePath() + "/" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".gif";
        convertGif(mStartPlayPos / 1000, mCurrentProgress, mVideoPath, DEFAULT_FPS, DEFAULT_SCALE_WIDTH, DEFAULT_SCALE_HEIGHT, gifFile);

        // notify system image library
        MediaScannerConnection.scanFile(this, new String[]{ gifFile }, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String s, Uri uri) {
                Log.d(TAG, "onScanCompleted, s : " + s + ", uri : " + uri);
            }
        });
    }

    private void convertGif(long startTime, long duration, String videoPath, int fps, int scaleWidth, int scaleHeight, String output) {
        String command = "ffmpeg -ss " + startTime + " -t " + duration + " -i " + videoPath + " -f gif -vf fps=" + fps + ",scale=" + scaleWidth + ":" + scaleHeight + ":flags=lanczos -y " + output;
        Log.d(TAG, "convertGif run command : " + command);
//        new FFmpegAndroid().run(command.split(" "));
    }



}
