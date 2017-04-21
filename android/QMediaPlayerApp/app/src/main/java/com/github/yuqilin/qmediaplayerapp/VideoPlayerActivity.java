package com.github.yuqilin.qmediaplayerapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.yuqilin.qmediaplayer.FFmpegInvoke;
import com.github.yuqilin.qmediaplayer.IMediaController;
import com.github.yuqilin.qmediaplayer.IMediaPlayer;
import com.github.yuqilin.qmediaplayer.QMediaPlayerVideoView;
import com.github.yuqilin.qmediaplayerapp.util.AndroidDevices;
import com.github.yuqilin.qmediaplayerapp.util.Permissions;
import com.github.yuqilin.qmediaplayerapp.util.Util;

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
    ImageView mPlaySpeed;

    // center tools view
    View mToolsView;
//    ImageView mSnapshot;
//    ImageView mTakeGif;
    ImageView mLockCenter;

    private AudioManager mAudioManager;
    private int mAudioMax;
    private boolean mMute = false;
    private int mVolSave;
    private float mVol;

    private boolean mShowing;
    private boolean mDragging;
    private long mDuration;
    private boolean mInstantSeeking = true;
    private Runnable mLastSeekBarRunnable;
    private boolean mDisableProgress = false;
    private boolean mIsLocked = false;
    private boolean mIsLoading;

    //stick event
    private static final int JOYSTICK_INPUT_DELAY = 300;
    private long mLastMove;

    //Touch Events
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_VOLUME = 1;
    private static final int TOUCH_BRIGHTNESS = 2;
    private static final int TOUCH_SEEK = 3;
    private int mTouchAction = TOUCH_NONE;
    private int mSurfaceYDisplayRange;
    private float mInitTouchY, mTouchY =-1f, mTouchX=-1f;

    // Brightness
    private boolean mIsFirstBrightnessGesture = true;
    private float mRestoreAutoBrightness = -1f;

    private SharedPreferences mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        initView();

        initPlayer();

        hideStatusBar();
//        dimStatusBar(true);

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        mVideoPath = getIntent().getStringExtra("videoPath");
        mVideoView.setVideoPath(mVideoPath);
        mVideoView.start();

        mTitle.setText(getFileName(mVideoPath));

        File file = new File(mVideoPath);
        if (!file.exists()) {
            Log.w(TAG, "File not exitsts or can not read");
        }

    }

    private void initPlayer() {
        mVideoView.setMediaController(this);
//        mVideoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(IMediaPlayer mp) {
//                mSeekBar.setProgress(mSeekBar.getMax());
//            }
//        });
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
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
//        mBattery = (ImageView) findViewById(R.id.view_player_battery);
        mSysTime = (TextView) findViewById(R.id.view_player_systime);
        mShowMore = (ImageView) findViewById(R.id.view_player_more);

        // bottom view
        mBottomView = findViewById(R.id.player_bottom_view);
        mCurrentTime = (TextView) findViewById(R.id.view_player_current_time);
        mTotalTime = (TextView) findViewById(R.id.view_player_total_time);
        mSeekBar = (SeekBar) findViewById(R.id.view_player_seekbar);
        mLockScreen = (ImageView) findViewById(R.id.view_player_lock_screen);
//        mFloatScreen = (ImageView) findViewById(R.id.view_player_float_screen);
        mForward = (ImageView) findViewById(R.id.view_player_forward);
        mPlayPause = (ImageView) findViewById(R.id.view_player_play_pause);
        mRewind = (ImageView) findViewById(R.id.view_player_rewind);
        mDisplayRatio = (ImageView) findViewById(R.id.view_player_display_ratio);
        mRotateScreen = (ImageView) findViewById(R.id.view_player_rotation);

        // center tools view
        mToolsView = findViewById(R.id.player_tools_view);
//        mSnapshot = (ImageView) findViewById(R.id.view_player_take_snapshot);
//        mTakeGif = (ImageView) findViewById(R.id.view_player_take_gif);
        mLockCenter = (ImageView) findViewById(R.id.view_player_lock_center);
        mPlaySpeed = (ImageView) findViewById(R.id.view_player_play_speed);

        mBack.setOnClickListener(mOnClickListener);
        mShowMore.setOnClickListener(mOnClickListener);
        mLockScreen.setOnClickListener(mOnClickListener);
        mForward.setOnClickListener(mOnClickListener);
        mPlayPause.setOnClickListener(mOnClickListener);
        mRewind.setOnClickListener(mOnClickListener);
        mDisplayRatio.setOnClickListener(mOnClickListener);
        mRotateScreen.setOnClickListener(mOnClickListener);
        mLockCenter.setOnClickListener(mOnClickListener);
        mPlaySpeed.setOnClickListener(mOnClickListener);

        mSeekBar.setOnSeekBarChangeListener(mSeekListener);
        mSeekBar.setThumbOffset(1);
        mSeekBar.setMax(1000);
        mSeekBar.setEnabled(!mDisableProgress);

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
        if (!mShowing || mIsLocked) {
            if (mPlayPause != null)
                mPlayPause.requestFocus();
            showOverlay();
            mShowing = true;
        }
        if (!mIsLocked) {
            updatePlayPause();
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT),
                    timeout);
        }
    }

    @Override
    public void hide() {
        if (mShowing || mIsLocked) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.removeMessages(SHOW_PROGRESS);
            hideOverlay();
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public boolean dispatchGenericMotionEvent(MotionEvent event){
        if (mIsLoading)
            return  false;
        //Check for a joystick event
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) !=
                InputDevice.SOURCE_JOYSTICK ||
                event.getAction() != MotionEvent.ACTION_MOVE)
            return false;

        InputDevice mInputDevice = event.getDevice();

        float dpadx = event.getAxisValue(MotionEvent.AXIS_HAT_X);
        float dpady = event.getAxisValue(MotionEvent.AXIS_HAT_Y);
        if (mInputDevice == null || Math.abs(dpadx) == 1.0f || Math.abs(dpady) == 1.0f)
            return false;

        float x = AndroidDevices.getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_X);
        float y = AndroidDevices.getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_Y);
        float rz = AndroidDevices.getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_RZ);

        if (System.currentTimeMillis() - mLastMove > JOYSTICK_INPUT_DELAY){
            if (Math.abs(x) > 0.3){
                seekDelta(x > 0.0f ? 10000 : -10000);
            } else if (Math.abs(y) > 0.3){
                if (mIsFirstBrightnessGesture)
                    initBrightnessTouch();
                changeBrightness(-y / 10f);
            } else if (Math.abs(rz) > 0.3){
                mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int delta = -(int) ((rz / 7) * mAudioMax);
                int vol = (int) Math.min(Math.max(mVol + delta, 0), mAudioMax);
                setAudioVolume(vol);
            }
            mLastMove = System.currentTimeMillis();
        }
        return true;
    }

    private void restoreBrightness() {
        if (mRestoreAutoBrightness != -1f) {
            int brightness = (int) (mRestoreAutoBrightness*255f);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightness);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        }
        // Save brightness if user wants to
        if (mSettings.getBoolean("save_brightness", false)) {
            float brightness = getWindow().getAttributes().screenBrightness;
            if (brightness != -1f) {
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putFloat("brightness_value", brightness);
                Util.commitPreferences(editor);
            }
        }
    }

    private void initBrightnessTouch() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        float brightnesstemp = lp.screenBrightness != -1f ? lp.screenBrightness : 0.6f;
        // Initialize the layoutParams screen brightness
        try {
            if (Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                if (!Permissions.canWriteSettings(this)) {
                    Permissions.checkWriteSettingsPermission(this, Permissions.PERMISSION_SYSTEM_BRIGHTNESS);
                    return;
                }
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                mRestoreAutoBrightness = android.provider.Settings.System.getInt(getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
            } else if (brightnesstemp == 0.6f) {
                brightnesstemp = android.provider.Settings.System.getInt(getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        lp.screenBrightness = brightnesstemp;
        getWindow().setAttributes(lp);
        mIsFirstBrightnessGesture = false;
    }

//    public boolean onTouchEvent(MotionEvent event) {
//        if (mService == null)
//            return false;
//        if (mDetector == null) {
//            mDetector = new GestureDetectorCompat(this, mGestureListener);
//            mDetector.setOnDoubleTapListener(mGestureListener);
//        }
//        if (mFov != 0f && mScaleGestureDetector == null)
//            mScaleGestureDetector = new ScaleGestureDetector(this, this);
//        if (mPlaybackSetting != DelayState.OFF) {
//            if (event.getAction() == MotionEvent.ACTION_UP)
//                endPlaybackSetting();
//            return true;
//        } else if (mPlaylist.getVisibility() == View.VISIBLE) {
//            togglePlaylist();
//            return true;
//        }
//        if (mTouchControls == 0 || mIsLocked) {
//            // locked or swipe disabled, only handle show/hide & ignore all actions
//            if (event.getAction() == MotionEvent.ACTION_UP) {
//                if (!mShowing) {
//                    showOverlay();
//                } else {
//                    hideOverlay(true);
//                }
//            }
//            return false;
//        }
//        if (mFov != 0f && mScaleGestureDetector != null)
//            mScaleGestureDetector.onTouchEvent(event);
//        if ((mScaleGestureDetector != null && mScaleGestureDetector.isInProgress()) ||
//                (mDetector != null && mDetector.onTouchEvent(event)))
//            return true;
//
//        float x_changed, y_changed;
//        if (mTouchX != -1f && mTouchY != -1f) {
//            y_changed = event.getRawY() - mTouchY;
//            x_changed = event.getRawX() - mTouchX;
//        } else {
//            x_changed = 0f;
//            y_changed = 0f;
//        }
//
//        // coef is the gradient's move to determine a neutral zone
//        float coef = Math.abs (y_changed / x_changed);
//        float xgesturesize = ((x_changed / mScreen.xdpi) * 2.54f);
//        float delta_y = Math.max(1f, (Math.abs(mInitTouchY - event.getRawY()) / mScreen.xdpi + 0.5f) * 2f);
//
//        int xTouch = Math.round(event.getRawX());
//        int yTouch = Math.round(event.getRawY());
//
//        switch (event.getAction()) {
//
//            case MotionEvent.ACTION_DOWN:
//                // Audio
//                mTouchY = mInitTouchY = event.getRawY();
//                mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//                mTouchAction = TOUCH_NONE;
//                // Seek
//                mTouchX = event.getRawX();
//                // Mouse events for the core
//                sendMouseEvent(MotionEvent.ACTION_DOWN, 0, xTouch, yTouch);
//                break;
//
//            case MotionEvent.ACTION_MOVE:
//                // Mouse events for the core
//                sendMouseEvent(MotionEvent.ACTION_MOVE, 0, xTouch, yTouch);
//
//                if (mFov == 0f) {
//                    // No volume/brightness action if coef < 2 or a secondary display is connected
//                    //TODO : Volume action when a secondary display is connected
//                    if (mTouchAction != TOUCH_SEEK && coef > 2 && mPresentation == null) {
//                        if (Math.abs(y_changed/mSurfaceYDisplayRange) < 0.05)
//                            return false;
//                        mTouchY = event.getRawY();
//                        mTouchX = event.getRawX();
//                        // Volume (Up or Down - Right side)
//                        if (mTouchControls == 1 || (mTouchControls == 3 && (int)mTouchX > (4 * mScreen.widthPixels / 7f))){
//                            doVolumeTouch(y_changed);
//                            hideOverlay(true);
//                        }
//                        // Brightness (Up or Down - Left side)
//                        if (mTouchControls == 2 || (mTouchControls == 3 && (int)mTouchX < (3 * mScreen.widthPixels / 7f))){
//                            doBrightnessTouch(y_changed);
//                            hideOverlay(true);
//                        }
//                    } else {
//                        // Seek (Right or Left move)
//                        doSeekTouch(Math.round(delta_y), xgesturesize, false);
//                    }
//                } else {
//                    mTouchY = event.getRawY();
//                    mTouchX = event.getRawX();
//                    mTouchAction = TOUCH_MOVE;
//                    float yaw = mFov * -x_changed/(float)mSurfaceXDisplayRange;
//                    float pitch = mFov * -y_changed/(float)mSurfaceXDisplayRange;
//                    mService.updateViewpoint(yaw, pitch, 0, 0, false);
//                }
//                break;
//
//            case MotionEvent.ACTION_UP:
//                // Mouse events for the core
//                sendMouseEvent(MotionEvent.ACTION_UP, 0, xTouch, yTouch);
//                // Seek
//                if (mTouchAction == TOUCH_SEEK)
//                    doSeekTouch(Math.round(delta_y), xgesturesize, true);
//                mTouchX = -1f;
//                mTouchY = -1f;
//                break;
//        }
//        return mTouchAction != TOUCH_NONE;
//    }


    private void doBrightnessTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_BRIGHTNESS)
            return;
        if (mIsFirstBrightnessGesture) initBrightnessTouch();
        mTouchAction = TOUCH_BRIGHTNESS;

        // Set delta : 2f is arbitrary for now, it possibly will change in the future
        float delta = - y_changed / mSurfaceYDisplayRange;

        changeBrightness(delta);
    }

    private void doVolumeTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_VOLUME)
            return;
        float delta = - ((y_changed / mSurfaceYDisplayRange) * mAudioMax);
        mVol += delta;
        int vol = (int) Math.min(Math.max(mVol, 0), mAudioMax);
        if (delta != 0f) {
            setAudioVolume(vol);
        }
    }

    private void setAudioVolume(int vol) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);

        /* Since android 4.3, the safe volume warning dialog is displayed only with the FLAG_SHOW_UI flag.
         * We don't want to always show the default UI volume, so show it only when volume is not set. */
        int newVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (vol != newVol)
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_SHOW_UI);

        mTouchAction = TOUCH_VOLUME;
        vol = vol * 100 / mAudioMax;
//        showInfoWithVerticalBar(getString(R.string.volume) + "\n" + Integer.toString(vol) + '%', 1000, vol);
    }

    private void mute(boolean mute) {
        mMute = mute;
//        if (mMute)
//            mVolSave = mService.getVolume();
//        mService.setVolume(mMute ? 0 : mVolSave);
    }

    private void updateMute () {
        mute(!mMute);
//        showInfo(mMute ? R.string.sound_off : R.string.sound_on, 1000);
    }

    private void changeBrightness(float delta) {
        // Estimate and adjust Brightness
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        float brightness =  Math.min(Math.max(lp.screenBrightness + delta, 0.01f), 1f);
        setWindowBrightness(brightness);
        brightness = Math.round(brightness * 100);
//        showInfoWithVerticalBar(getString(R.string.brightness) + "\n" + (int) brightness + '%', 1000, (int) brightness);
    }

    private void setWindowBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness =  brightness;
        // Set Brightness
        getWindow().setAttributes(lp);
    }

    private void seekDelta(int delta) {
        // unseekable stream
//        if(mVideoView.getDuration() <= 0 || !mVideoView.isSeekable()) return;

        long position = getTime() + delta;
        if (position < 0) position = 0;
        mVideoView.seekTo(position);
//        showInfo(Strings.millisToString(mService.getTime())+"/"+Strings.millisToString(mService.getLength()), 1000);
    }

    private long getTime() {
//        long time = mVideoView.getCurrentPosition();
//        if (mForcedTime != -1 && mLastTime != -1) {
//            /* XXX: After a seek, mService.getTime can return the position before or after
//             * the seek position. Therefore we return mForcedTime in order to avoid the seekBar
//             * to move between seek position and the actual position.
//             * We have to wait for a valid position (that is after the seek position).
//             * to re-init mLastTime and mForcedTime to -1 and return the actual position.
//             */
//            if (mLastTime > mForcedTime) {
//                if (time <= mLastTime && time > mForcedTime || time > mLastTime)
//                    mLastTime = mForcedTime = -1;
//            } else {
//                if (time > mForcedTime)
//                    mLastTime = mForcedTime = -1;
//            }
//        } else if (time == 0)
//            time = (int) mService.getCurrentMediaWrapper().getTime();
//        return mForcedTime == -1 ? time : mForcedTime;
        return 0;
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

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.view_player_rewind:
                    onRewind();
                    break;
                case R.id.view_player_forward:
                    onForward();
                    break;
                case R.id.view_player_play_pause:
                    onPlayPause();
                    break;
                case R.id.view_player_display_ratio:
                    onToggleDisplayRatio();
                    break;
                case R.id.view_player_rotation:
                    onRotateScreen();
                    break;
                case R.id.view_player_back:
                    onGoBack();
                    break;
                case R.id.view_player_lock_screen:
                    onLockScreen();
                    break;
            }
        }
    };

    private void onRewind() {
        long pos = mMediaPlayerControl.getCurrentPosition();
        if (pos > 0) {
            pos = Math.max(pos - FAST_REWIND_STEP, 0);
            mMediaPlayerControl.seekTo(pos);
            setProgress();
            show(DEFAULT_TIMEOUT);
        }
    }

    private void onForward() {
        long pos = mMediaPlayerControl.getCurrentPosition();
        long duration = mMediaPlayerControl.getDuration();
        if (pos < duration) {
            pos = Math.max(pos + FAST_FORWARD_STEP, duration);
            mMediaPlayerControl.seekTo(pos);
            setProgress();
            show(DEFAULT_TIMEOUT);
        }
    }

    private void onPlayPause() {
        doPauseResume();
        show(DEFAULT_TIMEOUT);
    }

    private void onToggleDisplayRatio() {
        mVideoView.toggleAspectRatio();
    }

    private void onRotateScreen() {
        rotateScreen();
    }

    private void onGoBack() {
        goBack();
    }

    private void onShowMore() {
        showMore();
    }

    private void onLockScreen() {
        if (mIsLocked) {
            unlockScreen();
        } else {
            lockScreen();
        }
    }

//    private View.OnClickListener mFloatScreenListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            floatScreen();
//        }
//    };

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
                long pos = 1000L * (position + 500) / duration;
                Log.d(TAG, "seekbar setProgress " + pos);
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
        mLockCenter.setVisibility(View.VISIBLE);
        hideOverlay();
        mIsLocked = true;
        show();
    }

    private void unlockScreen() {
        mLockCenter.setVisibility(View.GONE);
        mIsLocked = false;
        showOverlay();
        show();
    }

//    private void floatScreen() {
//        Intent mIntent = new Intent();
//        mIntent.putExtra("playUrl", mVideoPath);
//        mIntent.setClass(VideoPlayerActivity.this, PlayBackService.class);
//        VideoPlayerActivity.this.startService(mIntent);
//        VideoPlayerActivity.this.finish();
//    }

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

    private void showOverlay() {
        if (mIsLocked) {
            mLockCenter.setVisibility(View.VISIBLE);
//            mTitleView.setVisibility(View.INVISIBLE);
//            mBottomView.setVisibility(View.INVISIBLE);
//            mToolsView.setVisibility(View.INVISIBLE);
        } else {
//            mLockCenter.setVisibility(View.INVISIBLE);
            mTitleView.setVisibility(View.VISIBLE);
            mBottomView.setVisibility(View.VISIBLE);
            mToolsView.setVisibility(View.VISIBLE);
        }
    }

    private void hideOverlay() {
        if (mIsLocked) {
            mLockCenter.setVisibility(View.INVISIBLE);
        } else {
            mTitleView.setVisibility(View.INVISIBLE);
            mBottomView.setVisibility(View.INVISIBLE);
            mToolsView.setVisibility(View.INVISIBLE);
        }

    }

}
