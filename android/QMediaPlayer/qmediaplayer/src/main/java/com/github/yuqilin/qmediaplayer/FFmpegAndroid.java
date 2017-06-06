package com.github.yuqilin.qmediaplayer;

/**
 * Created by yuqilin on 17/1/13.
 */

public final class FFmpegAndroid {
    static {
        System.loadLibrary("ffmpeg-android");
    }

    public static void help() {
        FFmpegAndroid ffmpeg = new FFmpegAndroid();
        ffmpeg.run(new String[]{
                "ffmpeg", "-h"
        });
    }

    public native void run(String[] args);
}
