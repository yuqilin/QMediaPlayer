package com.wenjoyai.videoplayer.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by yuqilin on 17/4/13.
 */

public class ToastUtil {
    private static String oldMsg;
    private static Toast toast = null;
    private static long oneTime = 0;
    private static long twoTime = 0;

    public static void makeToastAndShow(Context context, String text) {
        if (context == null) return;
        if (toast == null) {
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
            toast.setText(text);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();
            if (text.equals(oldMsg)) {
                if (twoTime - oneTime > 3000) {
                    toast.setText(text);
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();
                    oneTime = twoTime;
                }
            } else {
                oldMsg = text;
                toast.setText(text);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
                oneTime = twoTime;
            }
        }
    }
    public static void makeToastAndShowLong(Context context, String text) {
        if (context == null) return;
        if (toast == null) {
            toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            toast.setText(text);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();
            if (text.equals(oldMsg)) {
                if (twoTime - oneTime > 3000) {
                    toast.setText(text);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.show();
                    oneTime = twoTime;
                }
            } else {
                oldMsg = text;
                toast.setText(text);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.show();
                oneTime = twoTime;
            }
        }
    }
    public static void cancel() {
        if (toast != null) {
            toast.cancel();
        }
    }
}
