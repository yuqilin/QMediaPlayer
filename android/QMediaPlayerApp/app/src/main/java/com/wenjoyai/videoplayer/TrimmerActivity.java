package com.wenjoyai.videoplayer;

/**
 * Created by liwenfeng on 17/4/26.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

//import com.github.yuqilin.qmediaplayer.FFmpegAndroid;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import life.knowledge4.videotrimmer.K4LVideoTrimmer;
import life.knowledge4.videotrimmer.interfaces.OnK4LVideoListener;
import life.knowledge4.videotrimmer.interfaces.OnTrimVideoListener;
import life.knowledge4.videotrimmer.utils.FileUtils;

public class TrimmerActivity extends AppCompatActivity implements OnTrimVideoListener, OnK4LVideoListener {

    private static final String TAG = "TrimmerActivity";

    private K4LVideoTrimmer mVideoTrimmer;
    private ProgressDialog mProgressDialog;
    private String mVideoPath;
    private String mVideoSavePath;
    private Handler mHandler;

    private static final int CUT_START = 1;
    private static final int CUT_FINISH = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trimmer);

        Intent extraIntent = getIntent();
//        String path = "";
        long duration = 1000;

        if (extraIntent != null) {
            mVideoPath = extraIntent.getStringExtra("videoPath");
            duration = extraIntent.getLongExtra("duration", 1000);
        }

        //setting progressbar
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.trimming_progress));

        mVideoTrimmer = ((K4LVideoTrimmer) findViewById(R.id.timeLine));
        if (mVideoTrimmer != null) {
            mVideoTrimmer.setMaxDuration((int)duration);
            mVideoTrimmer.setOnTrimVideoListener(this);
            mVideoTrimmer.setOnK4LVideoListener(this);
            mVideoTrimmer.setVideoURI(Uri.parse(mVideoPath));
            mVideoTrimmer.setVideoInformationVisibility(true);
        }

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {

            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case CUT_START:
                        mProgressDialog.show();
                        break;
                    case CUT_FINISH:
                        mProgressDialog.cancel();
                        if (!TextUtils.isEmpty(mVideoSavePath) && new File(mVideoSavePath).exists()) {
                            Toast.makeText(TrimmerActivity.this, getString(R.string.video_saved_at) + mVideoSavePath, Toast.LENGTH_LONG).show();
                            // notify system image library
                            MediaScannerConnection.scanFile(TrimmerActivity.this, new String[]{mVideoSavePath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String s, Uri uri) {
                                    Log.d(TAG, "onScanCompleted, s : " + s + ", uri : " + uri);
                                }
                            });
                        } else {
                            Log.e(TAG, "mVideoSavePath not exits : " + mVideoSavePath);
                        }
                        TrimmerActivity.this.finish();
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTrimStarted() {
        File savePath = new File(QApplication.getVideoStoragePath());
        if (!savePath.exists()) {
            savePath.mkdirs();
        }
        mVideoSavePath = QApplication.getVideoStoragePath() + "/"
                + com.wenjoyai.videoplayer.util.FileUtils.getFileBaseNameFromPath(mVideoPath) + "_"
                + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".mp4";
        Log.d(TAG, "videoSavePath : " + mVideoSavePath);
        mVideoTrimmer.setDestinationPath(mVideoSavePath);

        mProgressDialog.show();
    }

    @Override
    public void getResult(final Uri uri) {
        mProgressDialog.cancel();

        if (!TextUtils.isEmpty(mVideoSavePath) && new File(mVideoSavePath).exists()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(TrimmerActivity.this, getString(R.string.video_saved_at) + mVideoSavePath, Toast.LENGTH_LONG).show();
                }
            });
            // notify system image library
            MediaScannerConnection.scanFile(TrimmerActivity.this, new String[]{mVideoSavePath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String s, Uri uri) {
                    Log.d(TAG, "onScanCompleted, s : " + s + ", uri : " + uri);
                }
            });
        } else {
            Log.e(TAG, "mVideoSavePath not exits : " + mVideoSavePath);
        }

//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(TrimmerActivity.this, getString(R.string.video_saved_at, uri.getPath()), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//        intent.setDataAndType(uri, "video/mp4");
//        startActivity(intent);
        finish();
    }

    @Override
    public void getTranscodeResult(final Uri uri, int duration, final int startPosition,
                                   final int endPositon, String bitrate, String type, int vbr) {

//        Intent intent = new Intent();
//        intent.putExtra("videoPath", uri.getPath());
//        intent.putExtra("vbr", vbr);
//        intent.putExtra("type", type);
//        intent.putExtra("bits", bitrate);
//        intent.putExtra("duration", duration);
//        intent.putExtra("startTime", startPosition );
//        intent.putExtra("endTime", endPositon );
//
//        setResult(RESULT_OK, intent);
//        finish();

        File savePath = new File(QApplication.getVideoStoragePath());
        if (!savePath.exists()) {
            savePath.mkdirs();
        }
        mVideoSavePath = QApplication.getVideoStoragePath() + "/"
                + com.wenjoyai.videoplayer.util.FileUtils.getFileBaseNameFromPath(mVideoPath) + "_"
                + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".mp4";
        Log.d(TAG, "videoSavePath : " + mVideoSavePath);
        mVideoTrimmer.setDestinationPath(mVideoSavePath);

        mHandler.sendEmptyMessage(CUT_START);

        QApplication.runBackground(new Runnable() {
            @Override
            public void run() {
                cutVideo(mVideoPath, mVideoSavePath, startPosition / 1000, (endPositon - startPosition) / 1000);
                mHandler.sendEmptyMessage(CUT_FINISH);
            }
        });
    }

    @Override
    public void cancelAction() {
        mProgressDialog.cancel();
        mVideoTrimmer.destroy();
        finish();
    }

    @Override
    public void onError(final String message) {
        mProgressDialog.cancel();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TrimmerActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onVideoPrepared() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TrimmerActivity.this, "video is prepared, please waiting, my friend", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cutVideo(String src, String dst, long startTime, long duration) {
        String command = "ffmpeg -i " + src + " -ss " + startTime + " -t " + duration + " -movflags faststart -vcodec copy -acodec copy -y " + dst;
        Log.d(TAG, "cutVideo command : " + command);
//        new FFmpegAndroid().run(command.split(" "));
    }
}
