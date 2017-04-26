package com.wenjoyai.videoplayer.util;

import android.annotation.TargetApi;
import android.content.SharedPreferences;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Util {
    public final static String TAG = "VLC/Util";

    public static String readAsset(String assetName, String defaultS) {
        InputStream is = null;
        BufferedReader r = null;
        try {
//            is = VLCApplication.getAppResources().getAssets().open(assetName);
            r = new BufferedReader(new InputStreamReader(is, "UTF8"));
            StringBuilder sb = new StringBuilder();
            String line = r.readLine();
            if(line != null) {
                sb.append(line);
                line = r.readLine();
                while(line != null) {
                    sb.append('\n');
                    sb.append(line);
                    line = r.readLine();
                }
            }
            return sb.toString();
        } catch (IOException e) {
            return defaultS;
        } finally {
            close(is);
            close(r);
        }
    }

    @TargetApi(android.os.Build.VERSION_CODES.GINGERBREAD)
    public static void commitPreferences(SharedPreferences.Editor editor){
        if (AndroidUtil.isGingerbreadOrLater())
            editor.apply();
        else
            editor.commit();
    }

    public static boolean close(Closeable closeable) {
        if (closeable != null)
            try {
                closeable.close();
                return true;
            } catch (IOException e) {}
        return false;
    }

//    public static boolean isCallable(Intent intent) {
//        List<ResolveInfo> list = VLCApplication.getAppContext().getPackageManager().queryIntentActivities(intent,
//                PackageManager.MATCH_DEFAULT_ONLY);
//        return list.size() > 0;
//    }
}
