package com.github.yuqilin.qmediaplayerapp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.yuqilin.qmediaplayerapp.QApplication;
import com.github.yuqilin.qmediaplayerapp.media.MediaWrapper;

/**
 * Created by yuqilin on 17/3/10.
 */

public class AsyncImageLoader {
    public interface Callbacks {
        Bitmap getImage();
        void updateImage(Bitmap bitmap, View target);
    }

    public final static String TAG = "AsyncImageLoader";
    private static final Handler sHandler = new Handler(Looper.getMainLooper());


//    public static final BitmapDrawable DEFAULT_COVER_VIDEO = new BitmapDrawable(VLCApplication.getAppResources(), BitmapCache.getFromResource(VLCApplication.getAppResources(), R.drawable.icon));

    public static void loadPicture(View view, MediaWrapper media) {
        AsyncImageLoader.LoadImage(new MediaCoverFetcher(media), view);
    }

    public static void LoadImage(final Callbacks cbs, final View target) {
        QApplication.runBackground(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = cbs.getImage();
                cbs.updateImage(bitmap, target);
            }
        });
    }

    public abstract static class CoverFetcher implements AsyncImageLoader.Callbacks {

        public void updateBindImage(final Bitmap bitmap) {}
        public void updateImageView(final Bitmap bitmap, View target) {}

        @Override
        public void updateImage(final Bitmap bitmap, final View target) {
            sHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateImageView(bitmap, target);
                }
            });
        }
    }

    private static class MediaCoverFetcher extends AsyncImageLoader.CoverFetcher {
        final MediaWrapper media;

        MediaCoverFetcher(MediaWrapper media) {
            this.media = media;
        }

        @Override
        public Bitmap getImage() {
            return BitmapUtil.fetchPicture(media);
        }

        @Override
        public void updateImage(final Bitmap bitmap, final View target) {
            sHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (target instanceof ImageView) {
                        Log.d(TAG, "updateImage --- ");
//                        setCover((ImageView) target, media.getType(), bitmap, binding);
                        ImageView iv = (ImageView) target;
                        iv.setVisibility(View.VISIBLE);
                        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        iv.setImageBitmap(bitmap);
                    } else if (target instanceof TextView) {
//                        if (bitmap != null && (bitmap.getWidth() != 1 && bitmap.getHeight() != 1)) {
//                            if (binding != null) {
//                                binding.setVariable(BR.scaleType, ImageView.ScaleType.FIT_CENTER);
//                                binding.setVariable(BR.image, new BitmapDrawable(VLCApplication.getAppResources(), bitmap));
//                                binding.setVariable(BR.protocol, null);
//                            } else {
//                                target.setBackgroundDrawable(new BitmapDrawable(VLCApplication.getAppResources(), bitmap));
//                                ((TextView) target).setText(null);
//                            }
//                        }
                    }
                }
            });
        }
    }

}
