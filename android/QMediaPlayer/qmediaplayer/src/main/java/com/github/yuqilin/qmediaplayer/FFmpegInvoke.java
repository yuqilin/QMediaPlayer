package com.github.yuqilin.qmediaplayer;

/**
 * Created by yuqilin on 17/1/13.
 */

public final class FFmpegInvoke {
    static {
        System.loadLibrary("ffmpeg_invoke");
    }

    public static void help() {
        FFmpegInvoke ffmpeg = new FFmpegInvoke();
        ffmpeg.run(new String[]{
                "ffmpeg", "-h"
        });
    }

    public native void run(String[] args);
}
