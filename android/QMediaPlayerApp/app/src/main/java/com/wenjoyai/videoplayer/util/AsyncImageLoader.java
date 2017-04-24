package com.wenjoyai.videoplayer.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wenjoyai.videoplayer.QApplication;
import com.wenjoyai.videoplayer.R;
import com.wenjoyai.videoplayer.media.MediaWrapper;

/**
 * Created by yuqilin on 17/3/10.
 */

public class AsyncImageLoader {
    public interface Callbacks {
        Bitmap getImage(int kind);
        void updateImage(Bitmap bitmap, View target, int kind);
    }

    public final static String TAG = "AsyncImageLoader";
    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    public static final Bitmap DEFAULT_COVER_VIDEO = BitmapFactory.decodeResource(QApplication.getAppResources(), R.drawable.ic_play_grid);
//    public static final BitmapDrawable DEFAULT_COVER_VIDEO_DRAWABLE = new BitmapDrawable(QApplication.getAppResources(), BitmapCache.getFromResource(QApplication.getAppResources(), R.drawable.ic_play_arrow_24_px));

    public static void loadPicture(View view, MediaWrapper media, int kind) {
        AsyncImageLoader.LoadImage(new MediaCoverFetcher(media), view, kind);
    }

    public static void LoadImage(final Callbacks cbs, final View target, final int kind) {
        QApplication.runBackground(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = cbs.getImage(kind);
                cbs.updateImage(bitmap, target, kind);
            }
        });
    }

    public abstract static class CoverFetcher implements AsyncImageLoader.Callbacks {

        public void updateBindImage(final Bitmap bitmap) {}
        public void updateImageView(final Bitmap bitmap, View target) {}

        @Override
        public void updateImage(final Bitmap bitmap, final View target, int kind) {
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
        public Bitmap getImage(int kind) {
            return BitmapUtil.fetchPicture(media, kind);
        }

        @Override
        public void updateImage(final Bitmap bitmap, final View target, final int kind) {
            sHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (target instanceof ImageView) {
//                        setCover((ImageView) target, media.getType(), bitmap, binding);
                        ImageView iv = (ImageView) target;
//                        CancelTag cancelTag = (CancelTag)iv.getTag();
//                        if (cancelTag != null) {
//                            ImageView imageView = (ImageView)cancelTag.getData();
//                            if (imageView == iv) {
//                                Log.d(TAG, "cancelTag set [" + position + "]");
//                            }
//                        }
                        if (iv.getTag() == media) {
//                            iv.setScaleType(kind == MediaStore.Video.Thumbnails.MICRO_KIND ? ImageView.ScaleType.FIT_XY : ImageView.ScaleType.CENTER_CROP);
                            iv.setImageBitmap(bitmap);
                        }
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

    public class CancelTag {
        private Object mData;
        public CancelTag(Object data) {
            this.mData = data;
        }
        public Object getData() {
            return mData;
        }
    }

}
