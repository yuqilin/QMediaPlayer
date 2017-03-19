package com.github.yuqilin.qmediaplayerapp.media;

import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import com.github.yuqilin.qmediaplayerapp.QApplication;

import java.util.ArrayList;

/**
 * Created by yuqilin on 17/3/16.
 */

public class VideoLoader {

    public static final String TAG = "VideoLoader";

    public interface VideoLoaderListener {
        void onLoadItem(int position, MediaWrapper video);
        void onLoadCompleted(ArrayList<MediaWrapper> videos);
    }

    private ArrayList<MediaWrapper> mVideos = new ArrayList<>();

    private VideoLoaderListener mVideoLoaderListener;

    public VideoLoader(VideoLoaderListener videoLoaderListener) {
        mVideoLoaderListener = videoLoaderListener;
    }

    public void scanStart() {
        QApplication.runBackground(new Runnable() {
            @Override
            public void run() {
                loadVideos();
                if (mVideoLoaderListener != null) {
                    mVideoLoaderListener.onLoadCompleted(mVideos);
                }
            }
        });
    }

    public ArrayList<MediaWrapper> getVideos() {
        return mVideos;
    }

    private void loadVideos() {
        String[] thumbColumns = new String[]{
                MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Thumbnails.VIDEO_ID
        };

        String[] mediaColumns = new String[]{
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE
        };

        //首先检索SDcard上所有的video
        Cursor cursor = QApplication.getAppContext().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mediaColumns, null, null, null);

        if(cursor.moveToFirst()){
            do{
                MediaWrapper media = new MediaWrapper();

                media.filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                media.mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                media.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                media.duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                media.fileSize = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));

                //获取当前Video对应的Id，然后根据该ID获取其Thumb
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                String selection = MediaStore.Video.Thumbnails.VIDEO_ID +"=?";
                String[] selectionArgs = new String[]{
                        id+""
                };
//                Cursor thumbCursor = getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, thumbColumns, selection, selectionArgs, null);
//
//                if(thumbCursor != null && thumbCursor.moveToFirst()){
//                    info.thumbPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
//                }
                media.videoId = id;

                Log.d(TAG, "====Scanned : [" + mVideos.size() + "] " + media.filePath + " " + media.title + " " + media.videoId);

                //然后将其加入到videoList
                mVideos.add(media);

                if (mVideoLoaderListener != null) {
                    mVideoLoaderListener.onLoadItem(mVideos.size() - 1, media);
                }

//                mVideoFragment.notifyDataChanged();

            } while(cursor.moveToNext());
        }
    }
}
