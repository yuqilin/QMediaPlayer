package com.wenjoyai.videoplayer.util;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.wenjoyai.videoplayer.QApplication;
import com.wenjoyai.videoplayer.media.MediaWrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by yuqilin on 17/3/13.
 */

public class BitmapUtil {
    public final static String TAG = "BitmapUtil";

    public static Bitmap cropBorders(Bitmap bitmap, int width, int height)
    {
        int top = 0;
        for (int i = 0; i < height / 2; i++) {
            int pixel1 = bitmap.getPixel(width / 2, i);
            int pixel2 = bitmap.getPixel(width / 2, height - i - 1);
            if ((pixel1 == 0 || pixel1 == -16777216) &&
                    (pixel2 == 0 || pixel2 == -16777216)) {
                top = i;
            } else {
                break;
            }
        }

        int left = 0;
        for (int i = 0; i < width / 2; i++) {
            int pixel1 = bitmap.getPixel(i, height / 2);
            int pixel2 = bitmap.getPixel(width - i - 1, height / 2);
            if ((pixel1 == 0 || pixel1 == -16777216) &&
                    (pixel2 == 0 || pixel2 == -16777216)) {
                left = i;
            } else {
                break;
            }
        }

        if (left >= width / 2 - 10 || top >= height / 2 - 10)
            return bitmap;

        // Cut off the transparency on the borders
        return Bitmap.createBitmap(bitmap, left, top,
                (width - (2 * left)), (height - (2 * top)));
    }

    public static Bitmap scaleDownBitmap(Context context, Bitmap bitmap, int width) {
        /*
         * This method can lead to OutOfMemoryError!
         * If the source size is more than twice the target size use
         * the optimized version available in AudioUtil::readCoverBitmap
         */
        if (bitmap != null) {
            final float densityMultiplier = context.getResources().getDisplayMetrics().density;
            int w = (int) (width * densityMultiplier);
            int h = (int) (w * bitmap.getHeight() / ((double) bitmap.getWidth()));
            bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
        }
        return bitmap;
    }

    public static Bitmap getPictureFromCache(MediaWrapper media) {
        // mPicture is not null only if passed through
        // the ctor which is deprecated by now.
//        Bitmap b = media.getPicture();
//        if (b == null) {
//            final BitmapCache cache = BitmapCache.getInstance();
//            return cache.getBitmapFromMemCache(media.getLocation());
//        } else {
//            return b;
//        }
        return null;
    }

    public static Bitmap fetchPicture(MediaWrapper media, int kind) {
//        final BitmapCache cache = BitmapCache.getInstance();
//
//        Bitmap picture = readCoverBitmap(media.getArtworkURL());
//        if (picture == null) {
//            picture = MediaDatabase.getInstance().getPicture(media.getUri());
//        }
//        cache.addBitmapToMemCache(media.getLocation(), picture);

        ContentResolver contentResolver = QApplication.getAppContext().getContentResolver();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap picture = MediaStore.Video.Thumbnails.getThumbnail(contentResolver,
                media.getVideoId(), kind, options);
        return picture;
    }

//    public static Bitmap getPicture(MediaWrapper media) {
//        final Bitmap picture = getPictureFromCache(media);
//        if (picture != null)
//            return picture;
//        else
//            return fetchPicture(media);
//    }

    private static Bitmap readCoverBitmap(String path) {
        if (path == null)
            return null;
        Resources res = null;//VLCApplication.getAppResources();
        String uri = Uri.decode(path);
        if (uri.startsWith("file://"))
            uri = uri.substring(7);
        Bitmap cover = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        int height = 0;//res.getDimensionPixelSize(R.dimen.grid_card_thumb_height);
        int width = 0;//res.getDimensionPixelSize(R.dimen.grid_card_thumb_width);

        /* Get the resolution of the bitmap without allocating the memory */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uri, options);

        if (options.outWidth > 0 && options.outHeight > 0) {
            if (options.outWidth > width) {
                options.outWidth = width;
                options.outHeight = height;
            }
            options.inJustDecodeBounds = false;

            // Decode the file (with memory allocation this time)
            try {
                cover = BitmapFactory.decodeFile(uri, options);
            } catch (OutOfMemoryError e) {
                cover = null;
            }
        }

        return cover;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    static boolean canUseForInBitmap(
            Bitmap candidate, BitmapFactory.Options targetOptions) {

        if (candidate == null)
            return false;

        if (AndroidUtil.isKitKatOrLater()) {
            if (targetOptions.inSampleSize == 0)
                return false;
            // From Android 4.4 (KitKat) onward we can re-use if the byte size of
            // the new bitmap is smaller than the reusable bitmap candidate
            // allocation byte count.
            int width = targetOptions.outWidth / targetOptions.inSampleSize;
            int height = targetOptions.outHeight /  targetOptions.inSampleSize;
            int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
            return byteCount <= candidate.getAllocationByteCount();
        }

        // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
        return candidate.getWidth() == targetOptions.outWidth
                && candidate.getHeight() == targetOptions.outHeight
                && targetOptions.inSampleSize == 1;
    }

    /**
     * A helper function to return the byte usage per pixel of a bitmap based on its configuration.
     */
    static int getBytesPerPixel(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else if (config == Bitmap.Config.RGB_565) {
            return 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2;
        } else if (config == Bitmap.Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }

    public static boolean saveBitmap(String path, Bitmap bitmap) {
        return saveBitmap(new File(path), bitmap);
    }

    public static boolean saveBitmap(File file, Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return false;
        }

        FileOutputStream outputStream = null;
        try {
            if (file.exists())
                file.createNewFile();
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}