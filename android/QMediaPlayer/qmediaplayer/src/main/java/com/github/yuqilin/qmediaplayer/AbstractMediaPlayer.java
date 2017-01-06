package com.github.yuqilin.qmediaplayer;

/**
 * Created by yuqilin on 16/12/19.
 */

public abstract class AbstractMediaPlayer implements IMediaPlayer {
    private OnPreparedListener onPreparedListener;
    private OnCompletionListener onCompletionListener;
    private OnBufferingUpdateListener onBufferingUpdateListener;
    private OnSeekCompleteListener onSeekCompleteListener;
    private OnVideoSizeChangedListener onVideoSizeChangedListener;
    private OnErrorListener onErrorListener;
    private OnInfoListener onInfoListener;

    public final void setOnPreparedListener(OnPreparedListener listener) {
        onPreparedListener = listener;
    }

    public final void setOnCompletionListener(OnCompletionListener listener) {
        onCompletionListener = listener;
    }

    public final void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        onBufferingUpdateListener = listener;
    }

    public final void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        onSeekCompleteListener = listener;
    }

    public final void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        onVideoSizeChangedListener = listener;
    }

    public final void setOnErrorListener(OnErrorListener listener) {
        onErrorListener = listener;
    }

    public final void setOnInfoListener(OnInfoListener listener) {
        onInfoListener = listener;
    }

    protected final void notifyOnPrepared() {
        if (onPreparedListener != null) {
            onPreparedListener.onPrepared(this);
        }
    }

    protected final void notifyOnCompletion() {
        if (onCompletionListener != null) {
            onCompletionListener.onCompletion(this);
        }
    }

    protected final void notifyOnBufferingUpdate(int percent) {
        if (onBufferingUpdateListener != null) {
            onBufferingUpdateListener.onBufferingUpdate(this, percent);
        }
    }
    protected final boolean notifyOnInfo(int what, int extra) {
        boolean result = false;
        if (onInfoListener != null) {
            result = onInfoListener.onInfo(this, what, extra);
        }
        return result;
    }
    protected final boolean notifyOnError(int what, int extra) {
        boolean result = false;
        if (onErrorListener != null) {
            result = onErrorListener.onError(this, what, extra);
        }
        return result;
    }
    protected final void notifyOnVideoSizeChanged(int width, int height) {
        if (onVideoSizeChangedListener != null) {
            onVideoSizeChangedListener.onVideoSizeChanged(this, width, height);
        }
    }
    protected final void notifyOnSeekComplete() {
        if (onSeekCompleteListener != null) {
            onSeekCompleteListener.onSeekComplete(this);
        }
    }
}
