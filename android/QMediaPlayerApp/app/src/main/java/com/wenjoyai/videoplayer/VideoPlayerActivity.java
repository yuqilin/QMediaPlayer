package com.wenjoyai.videoplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.yuqilin.qmediaplayer.FFmpegInvoke;
import com.github.yuqilin.qmediaplayer.IMediaController;
import com.github.yuqilin.qmediaplayer.IMediaPlayer;
import com.github.yuqilin.qmediaplayer.IRenderView;
import com.github.yuqilin.qmediaplayer.QMediaPlayerVideoView;
import com.wenjoyai.videoplayer.util.AndroidDevices;
import com.wenjoyai.videoplayer.util.AndroidUtil;
import com.wenjoyai.videoplayer.util.BitmapUtil;
import com.wenjoyai.videoplayer.util.Permissions;
import com.wenjoyai.videoplayer.util.Tools;
import com.wenjoyai.videoplayer.util.Util;

import java.io.File;
import java.text.SimpleDateFormat;
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
    private static final int FADE_OUT_OVERLAY = 4;

    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int SEEK_TO_POST_DELAY_MILLIS = 200;
    private static final int FAST_REWIND_STEP = 5000;   // milliseconds
    private static final int FAST_FORWARD_STEP = 15000; // milliseconds

    private static final int OVERLAY_VOLUME = 1;
    private static final int OVERLAY_BRIGHTNESS = 2;
    private static final int OVERLAY_FAST_FORWARD = 3;
    private static final int OVERLAY_FAST_REWIND = 4;

    private String mVideoPath;
    //    private PlayerTitleView mTitleView;
//    private PlayerBottomView mBottomView;
    private QMediaPlayerVideoView mVideoView;
    private IMediaController.MediaPlayerControl mMediaPlayerControl;
    private int mVideoWidth;
    private int mVideoHeight;

    // title view
    View mTitleView;
    ImageView mBack;
    TextView mTitle;
    ImageView mBattery;
//    TextView mSysTime;
    ImageView mShowMore;

    // bottom view
    View mBottomView;
    TextView mCurrentTime;
    TextView mTotalTime;
    SeekBar mSeekBar;
//    ImageView mLockScreen;
    ImageView mFloatScreen;
    ImageView mForward;
    ImageView mPlayPause;
    ImageView mRewind;
    ImageView mDisplayRatio;
    ImageView mRotateScreen;
//    ImageView mPlaySpeed;
    ImageView mSnapshot;
    ImageView mRecord;

    // center tools view
    View mToolsView;
//    ImageView mSnapshot;
//    ImageView mTakeGif;
    ImageView mLockCenter;
    ImageView mScreenLock;

    View mOverlay;
    ImageView mOverlayIcon;
    TextView mOverlayText;

//    PopupMenu mPopupMenu;

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

    private static final int SEEKBAR_MAX = 1000;

    //stick event
    private static final int JOYSTICK_INPUT_DELAY = 300;
    private long mLastMove;

    private float mInitTouchY, mTouchY = -1f, mTouchX = -1f;

    // Brightness
    private boolean mIsFirstBrightnessGesture = true;
    private float mRestoreAutoBrightness = -1f;

    private SharedPreferences mSettings;

    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetectorCompat mDetector = null;

    DisplayMetrics mScreen = new DisplayMetrics();

    private float mFov;

    private int mTouchControls = 3;

    //Touch Events
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_VOLUME = 1;
    private static final int TOUCH_BRIGHTNESS = 2;
    private static final int TOUCH_MOVE = 3;
    private static final int TOUCH_SEEK = 4;
    private int mTouchAction = TOUCH_NONE;

    private int mSurfaceYDisplayRange, mSurfaceXDisplayRange;
    private boolean mPlayCompleted = false;

//    private PopupWindow mDisplayRatioPopup;
//    private ActionBar mActionBar;
    private boolean mPauseByUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        initView();

        initPlayer();

//        hideStatusBar();
        dimStatusBar(false);

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        mVideoPath = getIntent().getStringExtra("videoPath");
        mVideoView.setVideoPath(mVideoPath);
        mVideoView.start();

        mTitle.setText(getFileName(mVideoPath));
//
//        File file = new File(mVideoPath);
//        if (!file.exists()) {
//            Log.w(TAG, "File not exitsts or can not read");
//        }

        getWindowManager().getDefaultDisplay().getMetrics(mScreen);
        mSurfaceYDisplayRange = Math.min(mScreen.widthPixels, mScreen.heightPixels);
        mSurfaceXDisplayRange = Math.max(mScreen.widthPixels, mScreen.heightPixels);

        Log.d(TAG, "mScreen: " + mScreen.xdpi + " " + mScreen.ydpi + " " + mScreen.widthPixels + " " + mScreen.heightPixels);

    }

    private void initPlayer() {
        mVideoView.setMediaController(this);
        mVideoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                mVideoWidth = mp.getVideoWidth();
                mVideoHeight = mp.getVideoHeight();
                show();
            }
        });
        mVideoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
                mPlayCompleted = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 500);
            }
        });
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mPauseByUser) {
            mVideoView.resume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mVideoView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
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

        getWindowManager().getDefaultDisplay().getMetrics(mScreen);
        mSurfaceYDisplayRange = Math.min(mScreen.widthPixels, mScreen.heightPixels);
        mSurfaceXDisplayRange = Math.max(mScreen.widthPixels, mScreen.heightPixels);
    }

    private String getFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf('/') + 1, filePath.lastIndexOf('.'));
    }

    private void updateSysTime() {
//        if (mSysTime != null) {
//            mSysTime.setText(DateFormat.getTimeFormat(this).format(new Date(System.currentTimeMillis())));
//        }
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

//        mActionBar = getSupportActionBar();
//        if (mActionBar == null) {
//            Log.d(TAG, "mActionBar is null");
//        }

        mVideoView = (QMediaPlayerVideoView) findViewById(R.id.player_video_view);
//        mTitleView = (PlayerTitleView) findViewById(R.id.player_title_view);
//        mBottomView = (PlayerBottomView) findViewById(R.id.player_bottom_view);

        // title view
        mTitleView = findViewById(R.id.player_title_view);
        mBack = (ImageView) findViewById(R.id.view_player_back);
        mTitle = (TextView) findViewById(R.id.view_player_title);
//        mBattery = (ImageView) findViewById(R.id.view_player_battery);
//        mSysTime = (TextView) findViewById(R.id.view_player_systime);
//        mShowMore = (ImageView) findViewById(R.id.view_player_more);

        // bottom view
        mBottomView = findViewById(R.id.player_bottom_view);
        mCurrentTime = (TextView) findViewById(R.id.view_player_current_time);
        mTotalTime = (TextView) findViewById(R.id.view_player_total_time);
        mSeekBar = (SeekBar) findViewById(R.id.view_player_seekbar);
//        mLockScreen = (ImageView) findViewById(R.id.view_player_lock_screen);
        mFloatScreen = (ImageView) findViewById(R.id.view_player_float_screen);
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
//        mPlaySpeed = (ImageView) findViewById(R.id.view_player_play_speed);
        mScreenLock = (ImageView) findViewById(R.id.view_player_screen_lock);
        mSnapshot = (ImageView) findViewById(R.id.view_player_snapshot);
        mRecord = (ImageView) findViewById(R.id.view_player_record);

//        mBrightnessOverlay = findViewById(R.id.view_player_brightness_overlay);
//        mVolumeOverlay = findViewById(R.id.view_player_volume_overlay);
//        mBrightnessText = (TextView) findViewById(R.id.view_player_brightness_text);
//        mVolumeText = (TextView) findViewById(R.id.view_player_volume_text);
        mOverlay = findViewById(R.id.view_player_overlay_info);
        mOverlayIcon = (ImageView) findViewById(R.id.view_player_overlay_icon);
        mOverlayText = (TextView) findViewById(R.id.view_player_overlay_text);

//        View view = getLayoutInflater().inflate(R.layout.view_player_popup_ratio, null);
//        mDisplayRatioPopup = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
//        mDisplayRatioPopup.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
//        ContextThemeWrapper wrapper = new ContextThemeWrapper(this, R.style.PopupMenu);
//        mPopupMenu = new PopupMenu(this, mDisplayRatio);
//        mPopupMenu.inflate(R.menu.menu_display_ratio);
//        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                setMenuItemColor(item, getResources().getColor(R.color.colorPrimary));
//
//                switch (item.getItemId()) {
//                    case R.id.mi_ratio_best_fit:
//                        mCurrentRatioIndex = 0;
//                        mVideoView.setAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
//                        break;
//                    case R.id.mi_ratio_fit_horizontal:
//                        mCurrentRatioIndex = 1;
//                        mVideoView.setAspectRatio(IRenderView.AR_ASPECT_FILL_PARENT);
//                        break;
//                    case R.id.mi_ratio_fit_vertical:
//                        mCurrentRatioIndex = 2;
//                        mVideoView.setAspectRatio(IRenderView.AR_ASPECT_WRAP_CONTENT);
//                        break;
//                    case R.id.mi_ratio_fill:
//                        mCurrentRatioIndex = 3;
//                        mVideoView.setAspectRatio(IRenderView.AR_MATCH_PARENT);
//                        break;
//                    case R.id.mi_ratio_16_9:
//                        mCurrentRatioIndex = 4;
//                        mVideoView.setAspectRatio(IRenderView.AR_16_9_FIT_PARENT);
//                        break;
//                    case R.id.mi_ratio_4_3:
//                        mCurrentRatioIndex = 5;
//                        mVideoView.setAspectRatio(IRenderView.AR_4_3_FIT_PARENT);
//                        break;
//                }
//
//                return false;
//            }
//        });

        mBack.setOnClickListener(mOnClickListener);
//        mShowMore.setOnClickListener(mOnClickListener);
//        mLockScreen.setOnClickListener(mOnClickListener);
        mForward.setOnClickListener(mOnClickListener);
        mPlayPause.setOnClickListener(mOnClickListener);
        mRewind.setOnClickListener(mOnClickListener);
        mDisplayRatio.setOnClickListener(mOnClickListener);
        mRotateScreen.setOnClickListener(mOnClickListener);
//        mPlaySpeed.setOnClickListener(mOnClickListener);
        mScreenLock.setOnClickListener(mOnClickListener);
        mLockCenter.setOnClickListener(mOnClickListener);
        mFloatScreen.setOnClickListener(mOnClickListener);
        mSnapshot.setOnClickListener(mOnClickListener);
        mRecord.setOnClickListener(mOnClickListener);

        mSeekBar.setOnSeekBarChangeListener(mSeekListener);
        mSeekBar.setThumbOffset(1);
        mSeekBar.setMax(SEEKBAR_MAX);
        mSeekBar.setEnabled(!mDisableProgress);

//        mHandler.sendEmptyMessage(UPDATE_SYSTIME);
    }

    private void setMenuItemColor(MenuItem item, int color) {
        SpannableString spannableString = new SpannableString(item.getTitle());
        spannableString.setSpan(new ForegroundColorSpan(color), 0, spannableString.length(), 0);
        item.setTitle(spannableString);
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
        Log.d(TAG, "show timeout " + timeout + ", mShowing = " + mShowing);
        if (!mShowing || mIsLocked) {
            if (mPlayPause != null)
                mPlayPause.requestFocus();
            if (mIsLocked) {
                showLock();
            } else {
                showOverlay();
            }
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
        Log.d(TAG, "hide");
        if (mShowing) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.removeMessages(SHOW_PROGRESS);
            hideLock();
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
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if (mIsLoading)
            return false;
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

        if (System.currentTimeMillis() - mLastMove > JOYSTICK_INPUT_DELAY) {
            if (Math.abs(x) > 0.3) {
                seekDelta(x > 0.0f ? 10000 : -10000);
            } else if (Math.abs(y) > 0.3) {
                if (mIsFirstBrightnessGesture)
                    initBrightnessTouch();
                changeBrightness(-y / 10f);
            } else if (Math.abs(rz) > 0.3) {
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
            int brightness = (int) (mRestoreAutoBrightness * 255f);
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

    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mShowing)
                hideOverlay();
            else
                showOverlay();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (!mIsLocked) {
//                doPlayPause();
                return true;
            }
            return false;
        }
    };

    //    @Override
//    public boolean onScale(ScaleGestureDetector detector) {
//        float diff = DEFAULT_FOV * (1 - detector.getScaleFactor());
//        if (mService.updateViewpoint(0, 0, 0, diff, false)) {
//            mFov = Math.min(Math.max(MIN_FOV, mFov + diff), MAX_FOV);
//            return true;
//        }
//        return false;
//    }
//
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent : " + event.getAction());
//        if (true)
//            return true;
        if (mDetector == null) {
            mDetector = new GestureDetectorCompat(this, mGestureListener);
            mDetector.setOnDoubleTapListener(mGestureListener);
        }
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
        if (mTouchControls == 0 || mIsLocked) {
            // locked or swipe disabled, only handle show/hide & ignore all actions
            if (event.getAction() == MotionEvent.ACTION_UP) {
                show();
            }
            return false;
        }
//        if (mFov != 0f && mScaleGestureDetector != null)
//            mScaleGestureDetector.onTouchEvent(event);
//        if ((mScaleGestureDetector != null && mScaleGestureDetector.isInProgress()) ||
//                (mDetector != null && mDetector.onTouchEvent(event)))
//            return true;

        float x_changed, y_changed;
        if (mTouchX != -1f && mTouchY != -1f) {
            y_changed = event.getRawY() - mTouchY;
            x_changed = event.getRawX() - mTouchX;
        } else {
            x_changed = 0f;
            y_changed = 0f;
        }

        // coef is the gradient's move to determine a neutral zone
        float coef = Math.abs(y_changed / x_changed);
        float xgesturesize = ((x_changed / mScreen.xdpi) * 2.54f);
        float delta_y = Math.max(1f, (Math.abs(mInitTouchY - event.getRawY()) / mScreen.xdpi + 0.5f) * 2f);

        int xTouch = Math.round(event.getRawX());
        int yTouch = Math.round(event.getRawY());

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                // Audio
                mTouchY = mInitTouchY = event.getRawY();
                mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                mTouchAction = TOUCH_NONE;
                // Seek
                mTouchX = event.getRawX();
                // Mouse events for the core
//                sendMouseEvent(MotionEvent.ACTION_DOWN, 0, xTouch, yTouch);
                break;

            case MotionEvent.ACTION_MOVE:
                // Mouse events for the core
//                sendMouseEvent(MotionEvent.ACTION_MOVE, 0, xTouch, yTouch);

                if (mFov == 0f) {
                    // No volume/brightness action if coef < 2 or a secondary display is connected
                    //TODO : Volume action when a secondary display is connected
                    if (mTouchAction != TOUCH_SEEK && coef > 2 /*&& mPresentation == null*/) {
                        if (Math.abs(y_changed / mSurfaceYDisplayRange) < 0.05)
                            return false;
                        mTouchY = event.getRawY();
                        mTouchX = event.getRawX();
                        // Volume (Up or Down - Right side)
                        if (mTouchControls == 1 || (mTouchControls == 3 && (int) mTouchX > (4 * mScreen.widthPixels / 7f))) {
                            doVolumeTouch(y_changed);
//                            hideOverlay();
                        }
                        // Brightness (Up or Down - Left side)
                        if (mTouchControls == 2 || (mTouchControls == 3 && (int) mTouchX < (3 * mScreen.widthPixels / 7f))) {
                            doBrightnessTouch(y_changed);
//                            hideOverlay();
                        }
                    } else {
                        // Seek (Right or Left move)
                        doSeekTouch(Math.round(delta_y), xgesturesize, false);
                    }
                } else {
                    mTouchY = event.getRawY();
                    mTouchX = event.getRawX();
                    mTouchAction = TOUCH_MOVE;
                    float yaw = mFov * -x_changed / (float) mSurfaceXDisplayRange;
                    float pitch = mFov * -y_changed / (float) mSurfaceXDisplayRange;
//                    mService.updateViewpoint(yaw, pitch, 0, 0, false);
                }
                break;

            case MotionEvent.ACTION_UP:
                // Mouse events for the core
//                sendMouseEvent(MotionEvent.ACTION_UP, 0, xTouch, yTouch);
                // Seek
                if (mTouchAction == TOUCH_SEEK)
                    doSeekTouch(Math.round(delta_y), xgesturesize, true);
                mTouchX = -1f;
                mTouchY = -1f;
                break;
        }
        Log.d(TAG, "onTouchEvent mTouchAction : " + mTouchAction);
        return mTouchAction != TOUCH_NONE;
    }

    private void doBrightnessTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_BRIGHTNESS)
            return;
        if (mIsFirstBrightnessGesture) initBrightnessTouch();
        mTouchAction = TOUCH_BRIGHTNESS;

        // Set delta : 2f is arbitrary for now, it possibly will change in the future
        float delta = -y_changed / mSurfaceYDisplayRange;

        changeBrightness(delta);
    }

    private void doVolumeTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_VOLUME)
            return;
        float delta = -((y_changed / mSurfaceYDisplayRange) * mAudioMax);
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
        showOverlayInfo(OVERLAY_VOLUME, Integer.toString(vol) + '%', 1000);
    }

    private void seekto(long position, long length) {
//        mForcedTime = position;
//        mLastTime = mService.getTime();
//        mService.seek(position, length);
        mVideoView.seekTo(position);
    }

    private void doSeekTouch(int coef, float gesturesize, boolean seek) {
        if (coef == 0)
            coef = 1;
        // No seek action if coef > 0.5 and gesturesize < 1cm
        if (Math.abs(gesturesize) < 1 /*|| !mService.isSeekable()*/)
            return;

        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_SEEK)
            return;
        mTouchAction = TOUCH_SEEK;

        long length = mVideoView.getDuration();
        long time = getTime();

        // Size of the jump, 10 minutes max (600000), with a bi-cubic progression, for a 8cm gesture
        int jump = (int) ((Math.signum(gesturesize) * ((600000 * Math.pow((gesturesize / 8), 4)) + 3000)) / coef);

        // Adjust the jump
        if ((jump > 0) && ((time + jump) > length))
            jump = (int) (length - time);
        if ((jump < 0) && ((time + jump) < 0))
            jump = (int) -time;

        //Jump !
        if (seek && length > 0)
            seekto(time + jump, length);

//        if (length > 0)
//            //Show the jump's size
//            showInfo(String.format("%s%s (%s)%s",
//                    jump >= 0 ? "+" : "",
//                    Tools.millisToString(jump),
//                    Tools.millisToString(time + jump),
//                    coef > 1 ? String.format(" x%.1g", 1.0/coef) : ""), 50);
//        else
//            showInfo(R.string.unseekable_stream, 1000);
        showOverlayInfo(jump > 0 ? OVERLAY_FAST_FORWARD : OVERLAY_FAST_REWIND, String.format("%s/%s",
                Tools.millisToString(time + jump),
                Tools.millisToString(length)), 1000);
    }

    private void mute(boolean mute) {
        mMute = mute;
//        if (mMute)
//            mVolSave = mService.getVolume();
//        mService.setVolume(mMute ? 0 : mVolSave);
    }

    private void updateMute() {
        mute(!mMute);
//        showInfo(mMute ? R.string.sound_off : R.string.sound_on, 1000);
    }

    private void changeBrightness(float delta) {
        // Estimate and adjust Brightness
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        float brightness = Math.min(Math.max(lp.screenBrightness + delta, 0.01f), 1f);
        setWindowBrightness(brightness);
        brightness = Math.round(brightness * 100);
//        showInfoWithVerticalBar(getString(R.string.brightness) + "\n" + (int) brightness + '%', 1000, (int) brightness);
        showOverlayInfo(OVERLAY_BRIGHTNESS, (int) brightness + "%", 1000);
    }

    private void setWindowBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = brightness;
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
        long time = mVideoView.getCurrentPosition();
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
        return time;
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
                case FADE_OUT_OVERLAY:
                    fadeOutOverlay();
                    break;
            }
            return true;
        }
    });

    private void fadeOutOverlay() {
        if (mOverlay != null) {
            mOverlay.setVisibility(View.GONE);
        }
    }

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
                case R.id.view_player_screen_lock:
                    onScreenLock();
                    break;
                case R.id.view_player_lock_center:
                    onScreenLock();
                    break;
                case R.id.view_player_float_screen:
                    onFloatScreen();
                    break;
                case R.id.view_player_snapshot:
                    onSnapshot();
                    break;
                case R.id.view_player_record:
                    onRecord();
                    break;
            }
        }
    };

    private PopupWindow showPopupWindow(View anchorView, View popupView) {
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY,
                location[0] - popupView.getMeasuredWidth() - getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin),
                location[1] + (anchorView.getHeight() - popupView.getMeasuredHeight()) / 2);
        return popupWindow;
    }

    private void onSnapshot() {
//        PackageManager m = getPackageManager();
//        String dataDir = null;
//        PackageInfo packageInfo = null;
//        try {
//            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
//            dataDir = packageInfo.applicationInfo.dataDir;
//            Log.d(TAG, "data dir : " + dataDir);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//            Log.e(TAG, "onSnapshot NameNotFoundException : ", e);
//        }
        Log.d(TAG, "getFilesDir : " + getFilesDir()
                + ", getCacheDir : " + getCacheDir()
                + ", getExternalFilesDir : " + getExternalFilesDir(null)
                + ", getExternalCacheDir : " + getExternalCacheDir());

//        dataDir = getExternalFilesDir(null).getPath();
//
//        if (dataDir == null) {
//            return;
//        }
//
//        File snapshotDir = new File(dataDir + "/cutVideo/screenshot");
//        if (!snapshotDir.exists()) {
//            if (!snapshotDir.mkdirs()) {
//                Log.e(TAG, "snapshotDir mkdirs failed : " + snapshotDir.getPath());
//                return;
//            }
//        }

//        File snapshotFile = new File(snapshotDir + "/" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpg");
//        Log.d(TAG, "snapshotFile : " + snapshotFile.getAbsolutePath() + ", name : " + snapshotFile.getName());

        Bitmap bitmap = Bitmap.createBitmap(mVideoWidth, mVideoHeight, Bitmap.Config.ARGB_8888);
        if (!mVideoView.getCurrentFrame(bitmap)) {
            Log.e(TAG, "getCurrentFrame failed");
            return;
        }
        String externalStorage = Environment.getExternalStorageDirectory().getPath();
        Log.d(TAG, "external storage directory : " + externalStorage);
        File snapshotDirectory = new File(QApplication.getSnapshotStoragePath());
        if (!snapshotDirectory.exists()) {
            if (!snapshotDirectory.mkdirs()) {
                Log.e(TAG, "onSnapshot mkdirs failed : " + snapshotDirectory.getPath());
                return;
            }
        }
        File snapshotFile = new File(snapshotDirectory.getAbsolutePath() + "/"
                + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpg");
        Log.d(TAG, "onSnapshot snapshotFile : " + snapshotFile);
        if (!BitmapUtil.saveBitmap(snapshotFile, bitmap)) {
            Log.e(TAG, "saveBitmap failed : " + snapshotFile.getAbsolutePath());
            return;
        }

        // show share popup
//        View popupView = getLayoutInflater().inflate(R.layout.view_player_popup_snapshot, null);
//        final PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
//        popupWindow.setTouchable(true);
//        popupWindow.setOutsideTouchable(true);
//        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//
//        int[] location = new int[2];
//        mSnapshot.getLocationOnScreen(location);
//        popupWindow.showAtLocation(mSnapshot, Gravity.NO_GRAVITY, location[0] - popupView.getMeasuredWidth() - getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin), location[1] + (mSnapshot.getHeight() - popupView.getMeasuredHeight()) / 2);

        View popupView = getLayoutInflater().inflate(R.layout.view_player_popup_snapshot, null);
        final PopupWindow popupWindow = showPopupWindow(mSnapshot, popupView);
        popupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "popup snapshot clicked");
                popupWindow.dismiss();
            }
        });

        ImageView thumbnail = (ImageView)popupView.findViewById(R.id.view_popup_snapshot_thumbnail);
        thumbnail.setImageBitmap(bitmap);

        // notify system image library
        MediaScannerConnection.scanFile(this, new String[]{snapshotFile.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String s, Uri uri) {
                Log.d(TAG, "onScanCompleted, s : " + s + ", uri : " + uri);
            }
        });
    }

    private void onRecord() {
        View popupView = getLayoutInflater().inflate(R.layout.view_player_popup_record, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
//        popupView.setBackgroundDrawable(new BitmapDrawable());
        int[] recordLocation = new int[2];
        mRecord.getLocationOnScreen(recordLocation);
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        TextView recordGif = (TextView)popupView.findViewById(R.id.view_popup_record_gif);
        TextView recordVideo = (TextView)popupView.findViewById(R.id.view_popup_record_video);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                ((TextView)view).setTextColor(getResources().getColor(R.color.colorPrimary));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        popupWindow.dismiss();
                        switch (view.getId()) {
                            case R.id.view_popup_record_gif:
                                onRecordGif();
                                break;
                            case R.id.view_popup_record_video:
                                onRecordVideo();
                                break;
                        }
                    }
                }, 200);

            }
        };
        recordGif.setOnClickListener(onClickListener);
        recordVideo.setOnClickListener(onClickListener);

        Log.d(TAG, "recordLocation : " + recordLocation[0] + " , " + recordLocation[1]);
        Log.d(TAG, "record width : " + mRecord.getWidth() + " , height : " + mRecord.getHeight());
        Log.d(TAG, "popupView width : " + popupView.getMeasuredWidth() + " , height : " + popupView.getMeasuredHeight());

        popupWindow.showAtLocation(mRecord, Gravity.NO_GRAVITY, recordLocation[0] - popupView.getMeasuredWidth() - getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin), recordLocation[1] + (mRecord.getHeight() - popupView.getMeasuredHeight()) / 2);

//        Context wrapper = new ContextThemeWrapper(this, R.style.CustomPopupTheme);
//        final PopupMenu mPopupMenu = new PopupMenu(wrapper, mRecord, Gravity.LEFT);
//        mPopupMenu.inflate(R.menu.menu_record);
//        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                setMenuItemColor(item, getResources().getColor(R.color.colorPrimary));
//                switch (item.getItemId()) {
//                    case R.id.mi_record_gif:
//                        onRecordGif();
//                        break;
//                    case R.id.mi_record_video:
//                        onRecordVideo();
//                        break;
//                }
//                return false;
//            }
//        });
//        mPopupMenu.show();

    }

    private void onRecordGif() {
        Log.d(TAG, "onRecordGif");
    }

    private void onRecordVideo() {
        Log.d(TAG, "onRecordVideo");
    }

    private void onRewind() {
        long pos = mMediaPlayerControl.getCurrentPosition();
        if (pos - FAST_REWIND_STEP > 0) {
            pos -= FAST_REWIND_STEP;
        } else {
            pos = 0;
        }
        mMediaPlayerControl.seekTo(pos);
//        setProgress();
        show(DEFAULT_TIMEOUT);
    }

    private void onForward() {
        long pos = mMediaPlayerControl.getCurrentPosition();
        long duration = mMediaPlayerControl.getDuration();
        if (pos + FAST_FORWARD_STEP < duration) {
            pos += FAST_FORWARD_STEP;
        } else {
            pos = duration;
        }
        Log.d(TAG, "onForward pos = " + pos + ",duration = " + duration);
        mMediaPlayerControl.seekTo(pos);
//        setProgress();
        show(DEFAULT_TIMEOUT);
    }

    private void onPlayPause() {
        mPauseByUser = !mPauseByUser;
        doPauseResume();
        show(DEFAULT_TIMEOUT);
    }

    private int mCurrentRatioIndex = 0;
    private void onToggleDisplayRatio() {

//        ContextThemeWrapper wrapper = new ContextThemeWrapper(this, R.style.PopupMenu);
//        PopupMenu popupMenu = new PopupMenu(wrapper, mDisplayRatio);
//        popupMenu.inflate(R.menu.menu_display_ratio);
//        SpannableString spannableString = new SpannableString(popupMenu.getMenu().getItem(mCurrentRatioId).getTitle());
//        popupMenu.getMenu().getItem(mCurrentRatioId).setTitle(spannableString);

        Context wrapper = new ContextThemeWrapper(this, R.style.CustomPopupTheme);
        final PopupMenu mPopupMenu = new PopupMenu(wrapper, mDisplayRatio, Gravity.CENTER);
        mPopupMenu.inflate(R.menu.menu_display_ratio);
        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                setMenuItemColor(mPopupMenu.getMenu().getItem(mCurrentRatioIndex), Color.WHITE);
                setMenuItemColor(item, getResources().getColor(R.color.colorPrimary));

                switch (item.getItemId()) {
                    case R.id.mi_ratio_best_fit:
                        mCurrentRatioIndex = 0;
                        mVideoView.setAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
                        break;
                    case R.id.mi_ratio_fit_horizontal:
                        mCurrentRatioIndex = 1;
                        mVideoView.setAspectRatio(IRenderView.AR_ASPECT_FILL_PARENT);
                        break;
                    case R.id.mi_ratio_fit_vertical:
                        mCurrentRatioIndex = 2;
                        mVideoView.setAspectRatio(IRenderView.AR_ASPECT_WRAP_CONTENT);
                        break;
                    case R.id.mi_ratio_fill:
                        mCurrentRatioIndex = 3;
                        mVideoView.setAspectRatio(IRenderView.AR_MATCH_PARENT);
                        break;
                    case R.id.mi_ratio_16_9:
                        mCurrentRatioIndex = 4;
                        mVideoView.setAspectRatio(IRenderView.AR_16_9_FIT_PARENT);
                        break;
                    case R.id.mi_ratio_4_3:
                        mCurrentRatioIndex = 5;
                        mVideoView.setAspectRatio(IRenderView.AR_4_3_FIT_PARENT);
                        break;
                }

                return false;
            }
        });

        MenuItem item = mPopupMenu.getMenu().getItem(mCurrentRatioIndex);
        setMenuItemColor(item, getResources().getColor(R.color.colorPrimary));
//        hideDefaultControls();

        mPopupMenu.show();

//        mDisplayRatioPopup.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
//        mDisplayRatioPopup.showAtLocation(mDisplayRatio, Gravity.BOTTOM | Gravity.LEFT, 40, 40);

//        mVideoView.toggleAspectRatio();
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

    private void onScreenLock() {
        if (mIsLocked) {
            unlockScreen();
        } else {
            lockScreen();
        }
    }

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
        if (mMediaPlayerControl.isPlaying()) {
            mMediaPlayerControl.pause();
        } else {
            mMediaPlayerControl.start();
            mPlayCompleted = false;
        }
        updatePlayPause();
    }

    // FIXME: 17/4/22 completed current position incorrect
    private long setProgress() {
        if (mMediaPlayerControl == null || mDragging)
            return 0;

        long position = mMediaPlayerControl.getCurrentPosition();
        long duration = mMediaPlayerControl.getDuration();
        Log.d(TAG, "seekbar setProgress position = " + position + ",duration = " + duration);

        if (mDuration != duration) {
            mDuration = duration;
            if (mTotalTime != null)
                mTotalTime.setText(generateTime(mDuration));
        }
        if (mCurrentTime != null)
            mCurrentTime.setText(generateTime(position));

        if (mSeekBar != null) {
            if (duration > 0) {
                mSeekBar.setProgress((int)(SEEKBAR_MAX * (position / 1000) / (duration / 1000)));
            }
            int percent = mMediaPlayerControl.getBufferPercentage();
            mSeekBar.setSecondaryProgress(percent * 10);
        }

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

    private void showLock() {
//        mScreenLock.setVisibility(View.VISIBLE);
        Log.d(TAG, "showLock");
        mLockCenter.setVisibility(View.VISIBLE);
    }

    private void hideLock() {
        Log.d(TAG, "hideLock");
//        mScreenLock.setVisibility(View.INVISIBLE);
        mLockCenter.setVisibility(View.GONE);
    }

    private void lockScreen() {
        Log.d(TAG, "lockScreen");
//        mScreenLock.setImageResource(R.drawable.ic_lock_on);
        mLockCenter.setImageResource(R.drawable.ic_lock_on);
        mIsLocked = true;
        hideOverlay();
        show();
    }

    private void unlockScreen() {
        Log.d(TAG, "unlockScreen");
//        mScreenLock.setImageResource(R.drawable.ic_lock_open);
        mLockCenter.setImageResource(R.drawable.ic_lock_off);
        mIsLocked = false;
        showOverlay();
        show();
    }

    private void onFloatScreen() {
        Intent mIntent = new Intent();
        mIntent.putExtra("playUrl", mVideoPath);
        mIntent.putExtra("videoWidth", mVideoWidth);
        mIntent.putExtra("videoHeight", mVideoHeight);
        mIntent.setClass(VideoPlayerActivity.this, PlayBackService.class);
        startService(mIntent);
        finish();
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
        if (!AndroidUtil.isHoneycombOrLater())
            return;
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
        mTitleView.setVisibility(View.VISIBLE);
        mBottomView.setVisibility(View.VISIBLE);
        mSnapshot.setVisibility(View.VISIBLE);
        mRecord.setVisibility(View.VISIBLE);
        dimStatusBar(false);
    }

    private void hideOverlay() {
        mTitleView.setVisibility(View.INVISIBLE);
        mBottomView.setVisibility(View.INVISIBLE);
        mSnapshot.setVisibility(View.INVISIBLE);
        mRecord.setVisibility(View.INVISIBLE);
        dimStatusBar(true);
    }

    /**
     * Show text in the info view for "duration" milliseconds
     *
     * @param text
     * @param duration
     */
    private void showOverlayInfo(int kind, String text, int duration) {
        int orientation = LinearLayout.HORIZONTAL;
        if (kind == OVERLAY_VOLUME) {
            mOverlayIcon.setImageResource(R.drawable.ic_volume);
        } else if (kind == OVERLAY_BRIGHTNESS) {
            mOverlayIcon.setImageResource(R.drawable.ic_brightness);
        } else if (kind == OVERLAY_FAST_FORWARD) {
            orientation = LinearLayout.VERTICAL;
            mOverlayIcon.setImageResource(R.drawable.ic_fast_forward);
        } else if (kind == OVERLAY_FAST_REWIND) {
            orientation = LinearLayout.VERTICAL;
            mOverlayIcon.setImageResource(R.drawable.ic_fast_rewind);
        }
        ((LinearLayout)mOverlay).setOrientation(orientation);
        mOverlayText.setText(text);
        mOverlay.setVisibility(View.VISIBLE);
        mHandler.removeMessages(FADE_OUT_OVERLAY);
        mHandler.sendEmptyMessageDelayed(FADE_OUT_OVERLAY, 1000);
    }

}
