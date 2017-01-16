package com.github.yuqilin.qmediaplayerapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.yuqilin.qmediaplayer.FFmpegInvoke;
import com.github.yuqilin.qmediaplayer.IMediaController;
import com.github.yuqilin.qmediaplayer.QMediaPlayerVideoView;

import java.io.File;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yuqilin on 17/1/6.
 */

public class VideoPlayerActivity extends AppCompatActivity implements IMediaController {

    private static final String TAG = "VideoPlayerActivity";

    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private static final int UPDATE_SYSTIME = 3;

    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int SEEK_TO_POST_DELAY_MILLIS = 200;
    private static final int FAST_REWIND_STEP = 5000;   // milliseconds
    private static final int FAST_FORWARD_STEP = 15000; // milliseconds

    private String mVideoPath;
//    private PlayerTitleView mTitleView;
//    private PlayerBottomView mBottomView;
    private QMediaPlayerVideoView mVideoView;
    private IMediaController.MediaPlayerControl mMediaPlayerControl;

    // title view
    View mTitleView;
    ImageView mBack;
    TextView mTitle;
    ImageView mBattery;
    TextView mSysTime;
    ImageView mShowMore;

    // bottom view
    View mBottomView;
    TextView mCurrentTime;
    TextView mTotalTime;
    SeekBar mSeekBar;
    ImageView mLockScreen;
    ImageView mFloatScreen;
    ImageView mForward;
    ImageView mPlayPause;
    ImageView mRewind;
    ImageView mDisplayRatio;
    ImageView mRotateScreen;

    // center tools view
    View mToolsView;
    ImageView mSnapshot;
    ImageView mTakeGif;

    private AudioManager mAudioManager;
    private boolean mShowing;
    private boolean mDragging;
    private long mDuration;
    private boolean mInstantSeeking = true;
    private Runnable mLastSeekBarRunnable;
    private boolean mDisableProgress = false;
    private boolean mIsLocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);


        initView();

        hideStatusBar();
//        dimStatusBar(true);

        mVideoPath = getIntent().getStringExtra("videoPath");
        mVideoView.setVideoPath(mVideoPath);
        mVideoView.start();

        mTitle.setText(getFileName(mVideoPath));

        File file = new File(mVideoPath);
        if (!file.exists()) {
            Log.w(TAG, "File not exitsts or can not read");
        }

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

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);

        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Toast.makeText(MainActivity.this, "竖屏模式", 3000).show();
        } else {
//            Toast.makeText(MainActivity.this, "横屏模式", 3000).show();
        }

    }

    private String getFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf('/')+1, filePath.lastIndexOf('.'));
    }
    private void updateSysTime() {
        if (mSysTime != null) {
            mSysTime.setText(DateFormat.getTimeFormat(this).format(new Date(System.currentTimeMillis())));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void hideStatusBar() {
        int systemUiVisibility = 0;
        if (Build.VERSION.SDK_INT >= 14) {
            systemUiVisibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        if (Build.VERSION.SDK_INT >= 16) {
            systemUiVisibility |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        }

        if (Build.VERSION.SDK_INT >= 18) {
            systemUiVisibility |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                Log.i(TAG, "onSystemUiVisibilityChange, i=" + i);
            }
        });

        getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);
    }

    private void initView() {
        mVideoView = (QMediaPlayerVideoView) findViewById(R.id.player_video_view);
//        mTitleView = (PlayerTitleView) findViewById(R.id.player_title_view);
//        mBottomView = (PlayerBottomView) findViewById(R.id.player_bottom_view);

        // title view
        mTitleView = findViewById(R.id.player_title_view);
        mBack = (ImageView) findViewById(R.id.view_player_back);
        mTitle = (TextView) findViewById(R.id.view_player_title);
        mBattery = (ImageView) findViewById(R.id.view_player_battery);
        mSysTime = (TextView) findViewById(R.id.view_player_systime);
        mShowMore = (ImageView) findViewById(R.id.view_player_more);

        // bottom view
        mBottomView = findViewById(R.id.player_bottom_view);
        mCurrentTime = (TextView) findViewById(R.id.view_player_current_time);
        mTotalTime = (TextView) findViewById(R.id.view_player_total_time);
        mSeekBar = (SeekBar) findViewById(R.id.view_player_seekbar);
        mLockScreen = (ImageView) findViewById(R.id.view_player_lock_screen);
        mFloatScreen = (ImageView) findViewById(R.id.view_player_float_screen);
        mForward = (ImageView) findViewById(R.id.view_player_forward);
        mPlayPause = (ImageView) findViewById(R.id.view_player_play_pause);
        mRewind = (ImageView) findViewById(R.id.view_player_rewind);
        mDisplayRatio = (ImageView) findViewById(R.id.view_player_display_ratio);
        mRotateScreen = (ImageView) findViewById(R.id.view_player_rotation);

        // center tools view
        mToolsView = findViewById(R.id.player_tools_view);
        mSnapshot = (ImageView) findViewById(R.id.view_player_take_snapshot);
        mTakeGif = (ImageView) findViewById(R.id.view_player_take_gif);

        mBack.setOnClickListener(mBackListener);
        mShowMore.setOnClickListener(mShowMoreListener);
        mLockScreen.setOnClickListener(mLockScreenListener);
        mFloatScreen.setOnClickListener(mFloatScreenListener);
        mForward.setOnClickListener(mForwardListener);
        mPlayPause.setOnClickListener(mPlayPauseListener);
        mRewind.setOnClickListener(mRewindListener);
        mDisplayRatio.setOnClickListener(mDisplayRatioListener);
        mRotateScreen.setOnClickListener(mRotateScreenListener);
        mSnapshot.setOnClickListener(mSnapshotListener);
        mTakeGif.setOnClickListener(mTakeGifListener);
        mSeekBar.setOnSeekBarChangeListener(mSeekListener);

        mSeekBar.setThumbOffset(1);
        mSeekBar.setMax(1000);
        mSeekBar.setEnabled(!mDisableProgress);

        mVideoView.setMediaController(this);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mHandler.sendEmptyMessage(UPDATE_SYSTIME);
    }

    @Override
    public void setMediaPlayer(MediaPlayerControl playerControl) {
        mMediaPlayerControl = playerControl;
    }

    @Override
    public void show() {
        show(DEFAULT_TIMEOUT);
    }

    @Override
    public void show(int timeout) {
        if (!mShowing) {
//            if (mAnchor != null && mAnchor.getWindowToken() != null) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//                    mAnchor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
//                }
//            }
            if (mPlayPause != null)
                mPlayPause.requestFocus();
//            disableUnsupportedButtons();

//            if (mFromXml) {
//                setVisibility(View.VISIBLE);
//            } else {
//                int[] location = new int[2];
//
//                if (mAnchor != null) {
//                    mAnchor.getLocationOnScreen(location);
//                    Rect anchorRect = new Rect(location[0], location[1],
//                            location[0] + mAnchor.getWidth(), location[1]
//                            + mAnchor.getHeight());
//
//                    mWindow.setAnimationStyle(mAnimStyle);
//                    mWindow.showAtLocation(mAnchor, Gravity.BOTTOM,
//                            anchorRect.left, 0);
//                } else {
//                    Rect anchorRect = new Rect(location[0], location[1],
//                            location[0] + mRoot.getWidth(), location[1]
//                            + mRoot.getHeight());
//
//                    mWindow.setAnimationStyle(mAnimStyle);
//                    mWindow.showAtLocation(mRoot, Gravity.BOTTOM,
//                            anchorRect.left, 0);
//                }
//            }
            mTitleView.setVisibility(View.VISIBLE);
            mBottomView.setVisibility(View.VISIBLE);
            mToolsView.setVisibility(View.VISIBLE);

            mShowing = true;
//            if (mShownListener != null)
//                mShownListener.onShown();
        }
        updatePlayPause();
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT),
                    timeout);
        }
    }

    @Override
    public void hide() {
        if (mShowing) {
//            if (mAnchor != null) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//                    //mAnchor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//                }
//            }
            mHandler.removeMessages(FADE_OUT);
            mHandler.removeMessages(SHOW_PROGRESS);

            mTitleView.setVisibility(View.INVISIBLE);
            mBottomView.setVisibility(View.INVISIBLE);
            mToolsView.setVisibility(View.INVISIBLE);

            mShowing = false;
        }
    }

    @Override
    public boolean isShowing() {
        return mShowing;
    }

    @Override
    public void setEnabled(boolean enabled) {

    }

    @Override
    public void setAnchorView(View anchorView) {

    }

    private Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            long pos;
            switch (message.what) {
                case FADE_OUT:
                    hide();
                    break;
                case SHOW_PROGRESS:
                    pos = setProgress();
                    if (!mDragging && mShowing) {
                        message = mHandler.obtainMessage(SHOW_PROGRESS);
                        mHandler.sendMessageDelayed(message, 1000 - (pos % 1000));
                        updatePlayPause();
                    }
                    break;
                case UPDATE_SYSTIME:
                    updateSysTime();
                    message = mHandler.obtainMessage(UPDATE_SYSTIME);
                    mHandler.sendMessageDelayed(message, 1000);
                    break;
            }
            return true;
        }
    });

    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser)
                return;

            final long newPosition = (long) (mDuration * progress) / 1000;
            String time = generateTime(newPosition);
            if (mInstantSeeking) {
                mHandler.removeCallbacks(mLastSeekBarRunnable);
                mLastSeekBarRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mMediaPlayerControl.seekTo(newPosition);
                    }
                };
                mHandler.postDelayed(mLastSeekBarRunnable, SEEK_TO_POST_DELAY_MILLIS);
            }
            if (mCurrentTime != null)
                mCurrentTime.setText(time);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mDragging = true;
            show(3600000);
            mHandler.removeMessages(SHOW_PROGRESS);
            if (mInstantSeeking)
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (!mInstantSeeking)
                mMediaPlayerControl.seekTo(mDuration * seekBar.getProgress() / 1000);

            show(DEFAULT_TIMEOUT);
            mHandler.removeMessages(SHOW_PROGRESS);
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            mDragging = false;
            mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 1000);
        }
    };

    private View.OnClickListener mRewindListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            long pos = mMediaPlayerControl.getCurrentPosition();
            pos -= FAST_REWIND_STEP;
            mMediaPlayerControl.seekTo(pos);
            setProgress();

            show(DEFAULT_TIMEOUT);
        }
    };

    private View.OnClickListener mForwardListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            long pos = mMediaPlayerControl.getCurrentPosition();
            pos += FAST_FORWARD_STEP;
            mMediaPlayerControl.seekTo(pos);
            setProgress();

            show(DEFAULT_TIMEOUT);
        }
    };

    private View.OnClickListener mPlayPauseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            doPauseResume();
            show(DEFAULT_TIMEOUT);
        }
    };

    private View.OnClickListener mDisplayRatioListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mVideoView.toggleAspectRatio();
        }
    };

    private View.OnClickListener mRotateScreenListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            rotateScreen();
        }
    };

    private View.OnClickListener mBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            goBack();
        }
    };

    private View.OnClickListener mShowMoreListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showMore();
        }
    };

    private View.OnClickListener mLockScreenListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            lockScreen();
        }
    };

    private View.OnClickListener mFloatScreenListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            floatScreen();
        }
    };

    private View.OnClickListener mSnapshotListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            takeSnapshot();
        }
    };

    private View.OnClickListener mTakeGifListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            takeGif();
        }
    };

    private void updatePlayPause() {
        if (mPlayPause == null)
            return;

        if (mMediaPlayerControl.isPlaying()) {
            mPlayPause.setImageResource(R.drawable.ic_pause);
//            Log.i(TAG, "isPlaying setImageResource pause");
        } else {
            mPlayPause.setImageResource(R.drawable.ic_play);
//            Log.i(TAG, "!isPlaying setImageResource play");
        }
    }

    private void doPauseResume() {
        if (mMediaPlayerControl.isPlaying())
            mMediaPlayerControl.pause();
        else
            mMediaPlayerControl.start();
        updatePlayPause();
    }

    private long setProgress() {
        if (mMediaPlayerControl == null || mDragging)
            return 0;

        long position = mMediaPlayerControl.getCurrentPosition();
        long duration = mMediaPlayerControl.getDuration();
        if (mSeekBar != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mSeekBar.setProgress((int) pos);
            }
            int percent = mMediaPlayerControl.getBufferPercentage();
            mSeekBar.setSecondaryProgress(percent * 10);
        }

        mDuration = duration;

//        Log.i(TAG, "duration=" + duration + ", position=" + position);

        if (mTotalTime != null)
            mTotalTime.setText(generateTime(mDuration));
        if (mCurrentTime != null)
            mCurrentTime.setText(generateTime(position));

        return position;
    }

    private static String generateTime(long position) {
        int totalSeconds = (int) (position / 1000);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes,
                    seconds).toString();
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds)
                    .toString();
        }
    }

    private void rotateScreen() {
        int currentOrientation = getRequestedOrientation();
        if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private void goBack() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            finish();
        }
    }

    private void showMore() {

    }

    private void lockScreen() {

    }

    private void floatScreen() {

    }

    private void takeSnapshot() {
        FFmpegInvoke.help();
    }

    private void takeGif() {

    }

    /**
     * Dim the status bar and/or navigation icons when needed on Android 3.x.
     * Hide it on Android 4.0 and later
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void dimStatusBar(boolean dim) {
//        if (dim || mIsLocked)
//            mActionBar.hide();
//        else
//            mActionBar.show();
//        if (!AndroidUtil.isHoneycombOrLater() || mIsNavMenu)
//            return;
        int visibility = 0;
        int navbar = 0;

        if (true) {
            visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            navbar = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        if (dim || mIsLocked) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (true)
                navbar |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
            else
                visibility |= View.STATUS_BAR_HIDDEN;
            if (true) {
                navbar |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                if (true)
                    visibility |= View.SYSTEM_UI_FLAG_IMMERSIVE;
                if (true)
                    visibility |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            }
        } else {
//            mActionBar.show();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (true)
                visibility |= View.SYSTEM_UI_FLAG_VISIBLE;
            else
                visibility |= View.STATUS_BAR_VISIBLE;
        }

        if (true)
            visibility |= navbar;
        getWindow().getDecorView().setSystemUiVisibility(visibility);
    }

    private void hideOverlay() {

    }

}
