package com.github.yuqilin.qmediaplayer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Map;

import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.TextureMediaPlayer;

/**
 * Created by yuqilin on 16/12/20.
 */

public class QMediaPlayer extends AbstractMediaPlayer {

    public static class Options {

        public static final int PLAYER_TYPE_Auto = 0;
        public static final int PLAYER_TYPE_AndroidMediaPlayer = 1;
        public static final int PLAYER_TYPE_IjkMediaPlayer = 2;
        public static final int PLAYER_TYPE_IjkExoMediaPlayer = 3;

        private int playerType;
        private boolean usingMediaCodec;
        private boolean usingMediaCodecAutoRotate;
        private boolean isMediaCodecHandleResolutionChange;
        private boolean usingOpenSLES;
        private boolean usingMediaDataSource;
        private String pixelFormat;
        private boolean enableSurfaceView;
        private boolean enableTextureView;
        private boolean enableNoView;
        private boolean enableBackgroundPlay;

        public Options() {

        }

        public void setPlayerType(int playerType) {
            this.playerType = playerType;
        }

        public int getPlayerType() {
            return playerType;
        }

        public boolean getUsingMediaCodec() {
            return usingMediaCodec;
        }

        public void setUsingMediaCodec(boolean usingMediaCodec) {
            this.usingMediaCodec = usingMediaCodec;
        }

        public boolean getUsingMediaCodecAutoRotate() {
            return usingMediaCodecAutoRotate;
        }

        public void setUsingMediaCodecAutoRotate(boolean usingMediaCodecAutoRotate) {
            this.usingMediaCodecAutoRotate = usingMediaCodecAutoRotate;
        }

        public boolean getMediaCodecHandleResolutionChange() {
            return isMediaCodecHandleResolutionChange;
        }

        public void setMediaCodecHandleResolutionChange(boolean mediaCodecHandleResolutionChange) {
            isMediaCodecHandleResolutionChange = mediaCodecHandleResolutionChange;
        }

        public boolean getUsingOpenSLES() {
            return usingOpenSLES;
        }

        public void setUsingOpenSLES(boolean usingOpenSLES) {
            this.usingOpenSLES = usingOpenSLES;
        }

        public String getPixelFormat() {
            return pixelFormat;
        }

        public void setPixelFormat(String pixelFormat) {
            this.pixelFormat = pixelFormat;
        }

        public boolean getUsingMediaDataSource() {
            return usingMediaDataSource;
        }

        public void setUsingMediaDataSource(boolean usingMediaDataSource) {
            this.usingMediaDataSource = usingMediaDataSource;
        }

        public boolean getEnableSurfaceView() {
            return enableSurfaceView;
        }

        public void setEnableSurfaceView(boolean enableSurfaceView) {
            this.enableSurfaceView = enableSurfaceView;
        }

        public boolean getEnableTextureView() {
            return enableTextureView;
        }

        public void setEnableTextureView(boolean enableTextureView) {
            this.enableTextureView = enableTextureView;
        }

        public boolean getEnableNoView() {
            return enableNoView;
        }

        public void setEnableNoView(boolean enableNoView) {
            this.enableNoView = enableNoView;
        }

        public boolean getEnableBackgroundPlay() {
            return enableBackgroundPlay;
        }

        public void setEnableBackgroundPlay(boolean enableBackgroundPlay) {
            this.enableBackgroundPlay = enableBackgroundPlay;
        }
    }

    private Options playerOptions;
    private tv.danmaku.ijk.media.player.IMediaPlayer ijkIMediaPlayer;

    public tv.danmaku.ijk.media.player.IMediaPlayer getIjkIMediaPlayer() {
        return ijkIMediaPlayer;
    }
    private tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener ijkOnPreparedListener = new tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(tv.danmaku.ijk.media.player.IMediaPlayer mp) {
            notifyOnPrepared();
        }
    };
    private tv.danmaku.ijk.media.player.IMediaPlayer.OnBufferingUpdateListener ijkOnBufferingUpdateListener = new tv.danmaku.ijk.media.player.IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(tv.danmaku.ijk.media.player.IMediaPlayer mp, int percent) {
            notifyOnBufferingUpdate(percent);
        }
    };
    private tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener ijkOnCompletionListener = new tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(tv.danmaku.ijk.media.player.IMediaPlayer mp) {
            notifyOnCompletion();
        }
    };
    private tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener ijkOnInfoListener = new tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(tv.danmaku.ijk.media.player.IMediaPlayer mp, int what, int extra) {
            return notifyOnInfo(what, extra);
        }
    };
    private tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener ijkOnErrorListener = new tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {
            return notifyOnError(what, extra);
        }
    };
    private tv.danmaku.ijk.media.player.IMediaPlayer.OnSeekCompleteListener ijkOnSeekCompleteListener = new tv.danmaku.ijk.media.player.IMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            notifyOnSeekComplete();
        }
    };
    private tv.danmaku.ijk.media.player.IMediaPlayer.OnVideoSizeChangedListener ijkOnVideoSizeChangedListener = new tv.danmaku.ijk.media.player.IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
            notifyOnVideoSizeChanged(width, height);
        }
    };

    public QMediaPlayer(Options options) {
        playerOptions = options;
        ijkIMediaPlayer = createPlayer(playerOptions.getPlayerType());
        ijkIMediaPlayer.setOnPreparedListener(ijkOnPreparedListener);
        ijkIMediaPlayer.setOnBufferingUpdateListener(ijkOnBufferingUpdateListener);
        ijkIMediaPlayer.setOnCompletionListener(ijkOnCompletionListener);
        ijkIMediaPlayer.setOnInfoListener(ijkOnInfoListener);
        ijkIMediaPlayer.setOnErrorListener(ijkOnErrorListener);
        ijkIMediaPlayer.setOnVideoSizeChangedListener(ijkOnVideoSizeChangedListener);
        ijkIMediaPlayer.setOnSeekCompleteListener(ijkOnSeekCompleteListener);
    }

    @Override
    public void setDebugLoggingEnabled(boolean enabled) {

    }

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {
        if (ijkIMediaPlayer != null) {
            ijkIMediaPlayer.setScreenOnWhilePlaying(screenOn);
        }
    }

    @Override
    public void setWakeMode(Context context, int mode) {
        if (ijkIMediaPlayer != null) {
            ijkIMediaPlayer.setWakeMode(context, mode);
        }
    }

    @Override
    public void setDisplay(SurfaceHolder surfaceHolder) {
        if (ijkIMediaPlayer != null) {
            ijkIMediaPlayer.setDisplay(surfaceHolder);
        }
    }

    @Override
    public void setSurface(Surface surface) {
        if (ijkIMediaPlayer != null) {
            ijkIMediaPlayer.setSurface(surface);
        }
    }

    @Override
    public void setAudioStreamType(int audioStreamType) {
        if (ijkIMediaPlayer != null) {
            ijkIMediaPlayer.setAudioStreamType(audioStreamType);
        }
    }

    @Override
    public void setDataSource(FileDescriptor fd) throws IOException, IllegalArgumentException, IllegalStateException {
        if (ijkIMediaPlayer != null) {
            ijkIMediaPlayer.setDataSource(fd);
        }
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        if (ijkIMediaPlayer != null) {
            ijkIMediaPlayer.setDataSource(path);
        }
    }

    @Override
    public void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        if (ijkIMediaPlayer != null) {
            ijkIMediaPlayer.setDataSource(context, uri);
        }
    }

    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        if (ijkIMediaPlayer != null) {
            ijkIMediaPlayer.setDataSource(context, uri, headers);
        }
    }

    @Override
    public void prepare() throws IllegalStateException {

    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        if (ijkIMediaPlayer != null) {
            ijkIMediaPlayer.prepareAsync();
        }
    }

    @Override
    public void start() throws IllegalStateException {
        if (ijkIMediaPlayer != null) {
            ijkIMediaPlayer.start();
        }
    }

    @Override
    public void stop() throws IllegalStateException {
        if (ijkIMediaPlayer != null) {
            ijkIMediaPlayer.stop();
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        if (ijkIMediaPlayer != null) {
            ijkIMediaPlayer.pause();
        }
    }

    @Override
    public void release() {
        if (ijkIMediaPlayer != null) {
            ijkIMediaPlayer.release();
        }
    }

    @Override
    public void reset() {
        if (ijkIMediaPlayer != null) {
            ijkIMediaPlayer.reset();
        }
    }

    @Override
    public void seekTo(long ms) throws IllegalStateException {
        if (ijkIMediaPlayer != null) {
            ijkIMediaPlayer.seekTo(ms);
        }
    }

    @Override
    public long getCurrentPosition() {
        long currentPosition = 0;
        if (ijkIMediaPlayer != null) {
            currentPosition = ijkIMediaPlayer.getCurrentPosition();
        }
        return currentPosition;
    }

    @Override
    public long getDuration() {
        long duration = 0;
        if (ijkIMediaPlayer != null) {
            duration = ijkIMediaPlayer.getDuration();
        }
        return duration;
    }

    @Override
    public int getVideoWidth() {
        int width = 0;
        if (ijkIMediaPlayer != null) {
            width = ijkIMediaPlayer.getVideoWidth();
        }
        return width;
    }

    @Override
    public int getVideoHeight() {
        int height = 0;
        if (ijkIMediaPlayer != null) {
            height = ijkIMediaPlayer.getVideoHeight();
        }
        return height;
    }

    @Override
    public int getVideoSarNum() {
        int num = 0;
        if (ijkIMediaPlayer != null) {
            num = ijkIMediaPlayer.getVideoSarNum();
        }
        return num;
    }

    @Override
    public int getVideoSarDen() {
        int den = 0;
        if (ijkIMediaPlayer != null) {
            den = ijkIMediaPlayer.getVideoSarDen();
        }
        return den;
    }

    @Override
    public boolean isPlaying() {
        boolean playing = false;
        if (ijkIMediaPlayer != null) {
            playing = ijkIMediaPlayer.isPlaying();
        }
        return playing;
    }

    @Override
    public void setLooping(boolean looping) {
        if (ijkIMediaPlayer != null) {
            ijkIMediaPlayer.setLooping(looping);
        }
    }

    @Override
    public boolean isLooping() {
        boolean looping = false;
        if (ijkIMediaPlayer != null) {
            looping = ijkIMediaPlayer.isLooping();
        }
        return looping;
    }

    @Override
    public void setVolume(int volume) {
        if (ijkIMediaPlayer != null) {
//            ijkIMediaPlayer.setVolume(volume);
        }
    }

    @Override
    public int getVolume() {
        return 0;
    }


    private tv.danmaku.ijk.media.player.IMediaPlayer createPlayer(int playerType) {
        tv.danmaku.ijk.media.player.IMediaPlayer iMediaPlayer = null;
        switch (playerType) {
            case Options.PLAYER_TYPE_IjkExoMediaPlayer: {
//                IjkExoMediaPlayer IjkExoMediaPlayer = new IjkExoMediaPlayer(mAppContext);
//                mediaPlayer = IjkExoMediaPlayer;
            }
            break;
            case Options.PLAYER_TYPE_AndroidMediaPlayer: {
                AndroidMediaPlayer androidMediaPlayer = new AndroidMediaPlayer();
                iMediaPlayer = androidMediaPlayer;
            }
            break;
            case Options.PLAYER_TYPE_IjkMediaPlayer:
            default: {
                IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();
                ijkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);

                if (playerOptions.getUsingMediaCodec()) {
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
                    if (playerOptions.getUsingMediaCodecAutoRotate()) {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
                    } else {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 0);
                    }
                    if (playerOptions.getMediaCodecHandleResolutionChange()) {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
                    } else {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 0);
                    }
                } else {
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
                }

                if (playerOptions.getUsingOpenSLES()) {
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1);
                } else {
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
                }

                String pixelFormat = playerOptions.getPixelFormat();
                if (TextUtils.isEmpty(pixelFormat)) {
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
                } else {
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", pixelFormat);
                }

                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);

                iMediaPlayer = ijkMediaPlayer;
                break;
            }
        }

//        if (playerOptions.getEnableDetachedSurfaceTextureView()) {
            iMediaPlayer = new TextureMediaPlayer(iMediaPlayer);
//        }

        return iMediaPlayer;
    }

}