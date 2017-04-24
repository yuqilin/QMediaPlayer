package com.wenjoyai.videoplayer.util;


import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.wenjoyai.videoplayer.media.MediaWrapper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class Tools {

    private static StringBuilder sb = new StringBuilder();
    private static DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.US);
    static {
        format.applyPattern("00");
    }

    /*
     * Convert file:// uri from real path to emulated FS path.
     */
    public static Uri convertLocalUri(Uri uri) {
        if (!TextUtils.equals(uri.getScheme(), "file") || !uri.getPath().startsWith("/sdcard"))
            return uri;
        String path = uri.toString();
        return Uri.parse(path.replace("/sdcard", Environment.getExternalStorageDirectory().getPath()));
    }

    public static boolean isArrayEmpty(@Nullable Object[] array) {
        return array == null || array.length == 0;
    }
//    public static String getProgressText(MediaWrapper media) {
//        long lastTime = media.getTime();
//        if (lastTime == 0L)
//            return "";
//        return String.format("%s / %s",
//                millisToString(lastTime, true),
//                millisToString(media.getLength(), true));
//    }

    /**
     * Convert time to a string
     * @param millis e.g.time/length from file
     * @return formated string (hh:)mm:ss
     */
    public static String millisToString(long millis) {
        return millisToString(millis, false);
    }

    /**
     * Convert time to a string
     * @param millis e.g.time/length from file
     * @return formated string "[hh]h[mm]min" / "[mm]min[s]s"
     */
    public static String millisToText(long millis) {
        return millisToString(millis, true);
    }

    public static String getResolution(MediaWrapper media) {
        if (media.getWidth() > 0 && media.getHeight() > 0)
            return String.format(Locale.US, "%dx%d", media.getWidth(), media.getHeight());
        return "";
    }

    public static String millisToString(long millis, boolean text) {
        sb.setLength(0);
        if (millis < 0) {
            millis = -millis;
            sb.append("-");
        }

        millis /= 1000;
        int sec = (int) (millis % 60);
        millis /= 60;
        int min = (int) (millis % 60);
        millis /= 60;
        int hours = (int) millis;

        if (text) {
            if (millis > 0)
                sb.append(hours).append('h').append(format.format(min)).append("min");
            else if (min > 0)
                sb.append(min).append("min");
            else
                sb.append(sec).append("s");
        } else {
            if (millis > 0)
                sb.append(hours).append(':').append(format.format(min)).append(':').append(format.format(sec));
            else
                sb.append(min).append(':').append(format.format(sec));
        }
        return sb.toString();
    }

}
