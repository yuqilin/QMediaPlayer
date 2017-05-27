package com.wenjoyai.videoplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.wenjoyai.videoplayer.util.AsyncImageLoader;
import com.wenjoyai.videoplayer.util.FileUtils;

import java.io.File;

/**
 * Created by yuqilin on 17/5/25.
 */

public class RecordVideoActivity extends AppCompatActivity {

    private static final String TAG = "RecordVideoActivity";

    private String mVideoPath;
    private RecyclerView mSnapshotView;
    private long mDuration;
    private SnapshotAdapter mSnapshotAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_video);

        mVideoPath = getIntent().getStringExtra("videoPath");
        mDuration = getIntent().getLongExtra("duration", 0);

        Log.d(TAG, "mVideoPath = " + mVideoPath + ", mDuration = " + mDuration);

        mSnapshotView = (RecyclerView) findViewById(R.id.record_video_snapshots);
        mSnapshotAdapter = new SnapshotAdapter();
        mSnapshotView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mSnapshotView.setAdapter(mSnapshotAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class SnapshotAdapter extends RecyclerView.Adapter<SnapshotAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record_video, null);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.mSnapshot.setImageBitmap(AsyncImageLoader.DEFAULT_COVER_VIDEO);
            AsyncImageLoader.LoadImage(new AsyncImageLoader.Callbacks() {
                @Override
                public Bitmap getImage(int kind) {
                    String baseName = FileUtils.getFileNameFromPath(mVideoPath).substring(0, FileUtils.getFileNameFromPath(mVideoPath).lastIndexOf('.'));
                    File cacheDir = new File(getApplicationContext().getExternalCacheDir() + "/snapshot/");
                    if (!cacheDir.exists()) {
                        cacheDir.mkdirs();
                    }
                    String cache = getApplicationContext().getExternalCacheDir() + "/snapshot/" + baseName + "_" + String.format("%03d", position) + ".bmp";
                    ffmpegExtractFrame(mVideoPath, position * 2, cache);
                    Bitmap bitmap = BitmapFactory.decodeFile(cache);
                    return bitmap;
                }

                @Override
                public void updateImage(Bitmap bitmap, View target, int kind) {

                    if (target instanceof ImageView) {
                        ((ImageView) target).setImageBitmap(bitmap);
                    }

                }
            }, holder.mSnapshot, 0);
        }

        @Override
        public int getItemCount() {
            return (int)mDuration / 2000;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView mSnapshot;

            public ViewHolder(View itemView) {
                super(itemView);
                mSnapshot = (ImageView) itemView.findViewById(R.id.item_record_video_snapshot);
            }
        }

        private boolean ffmpegExtractFrame(String videoPath, long startTime, String output) {
            String command = "ffmpeg -ss " + startTime + " -i " + videoPath + " -vframes 1 -y " + output;
            Log.d(TAG, "ffmpegExtractFrame command : " + command);
//            new FFmpegAndroid().run(command.split(" "));
            return true;
        }
    }
}
