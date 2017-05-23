package com.github.yuqilin.qmediaplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Map;

/**
 * Created by yuqilin on 16/10/27.
 */

public interface IMediaPlayer {
    /*
     * Do not change these values without updating their counterparts in native
     */
    int MEDIA_INFO_UNKNOWN = 1;
    int MEDIA_INFO_STARTED_AS_NEXT = 2;
    int MEDIA_INFO_VIDEO_RENDERING_START = 3;
    int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
    int MEDIA_INFO_BUFFERING_START = 701;
    int MEDIA_INFO_BUFFERING_END = 702;
    int MEDIA_INFO_NETWORK_BANDWIDTH = 703;
    int MEDIA_INFO_BAD_INTERLEAVING = 800;
    int MEDIA_INFO_NOT_SEEKABLE = 801;
    int MEDIA_INFO_METADATA_UPDATE = 802;
    int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
    int MEDIA_INFO_UNSUPPORTED_SUBTITLE = 901;
    int MEDIA_INFO_SUBTITLE_TIMED_OUT = 902;

    int MEDIA_INFO_VIDEO_ROTATION_CHANGED = 10001;
    int MEDIA_INFO_AUDIO_RENDERING_START = 10002;

    int MEDIA_ERROR_UNKNOWN = 1;
    int MEDIA_ERROR_SERVER_DIED = 100;
    int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
    int MEDIA_ERROR_IO = -1004;
    int MEDIA_ERROR_MALFORMED = -1007;
    int MEDIA_ERROR_UNSUPPORTED = -1010;
    int MEDIA_ERROR_TIMED_OUT = -110;

    void setDebugLoggingEnabled(boolean enabled);
    void setScreenOnWhilePlaying(boolean screenOn);
    void setWakeMode(Context context, int mode);

    void setDisplay(SurfaceHolder surfaceHolder);
    void setSurface(Surface surface);

    void setAudioStreamType(int audioStreamType);

    void setDataSource(String path)
     throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;
    void	setDataSource(FileDescriptor fd)
     throws IOException, IllegalArgumentException, IllegalStateException;
    void setDataSource(Context context, Uri uri)
     throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void setDataSource(Context context, Uri uri, Map<String, String> headers)
     throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    void prepare() throws IllegalStateException;
    void prepareAsync() throws IllegalStateException;
    void start() throws IllegalStateException;
    void stop() throws IllegalStateException;
    void pause() throws IllegalStateException;
    void release();
    void reset();

    void seekTo(long ms) throws IllegalStateException;
    long getCurrentPosition();
    long getDuration();
    int getVideoWidth();
    int getVideoHeight();
    int getVideoSarNum();
    int getVideoSarDen();

    boolean getCurrentFrame(Bitmap bitmap);

    boolean isPlaying();

    void setLooping(boolean looping);
    boolean isLooping();

    void setVolume(int volume);
    int getVolume();

    public interface OnPreparedListener {
        void onPrepared(IMediaPlayer mp);
    }
    public interface OnCompletionListener {
        void onCompletion(IMediaPlayer mp);
    }
    public interface OnBufferingUpdateListener {
        void onBufferingUpdate(IMediaPlayer mp, int percent);
    }
    public interface OnSeekCompleteListener {
        public void onSeekComplete(IMediaPlayer mp);
    }
    public interface OnVideoSizeChangedListener {
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height);
    }
    public interface OnInfoListener {
        boolean onInfo(IMediaPlayer mp, int what, int extra);
    }
    public interface OnErrorListener {
        boolean onError(IMediaPlayer mp, int what, int extra);
    }

    void setOnPreparedListener(OnPreparedListener listener);
    void setOnCompletionListener(OnCompletionListener listener);
    void setOnBufferingUpdateListener(OnBufferingUpdateListener listener);
    void setOnSeekCompleteListener(OnSeekCompleteListener listener);
    void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener);
    void setOnInfoListener(OnInfoListener listener);
    void setOnErrorListener(OnErrorListener listener);

}
