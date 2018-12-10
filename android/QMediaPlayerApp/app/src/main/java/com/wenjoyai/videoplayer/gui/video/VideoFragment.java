package com.wenjoyai.videoplayer.gui.video;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.wenjoyai.videoplayer.BaseFragment;
import com.wenjoyai.videoplayer.IEventsHandler;
import com.wenjoyai.videoplayer.R;
import com.wenjoyai.videoplayer.VideoPlayerActivity;
import com.wenjoyai.videoplayer.gui.view.AutoFitRecyclerView;
import com.wenjoyai.videoplayer.media.MediaWrapper;

import java.util.Comparator;
import java.util.List;

/**
 * Created by yuqilin on 17/2/11.
 */

public class VideoFragment extends BaseFragment implements IEventsHandler {
    public static final String TAG = "VideoFragment";

    private static final String PAGE_TITLE = "VIDEOS";

    public static final int SCAN_START = 1;
    public static final int SCAN_FINISH = 2;
    public static final int SCAN_CANCEL = 3;
    public static final int SCAN_ADD_ITEM = 4;

    protected AutoFitRecyclerView mGridView;
    private VideoListAdapter mVideoAdapter;
    private boolean mListMode;

//    private VideoLoader mVideoLoader;
//    private Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case SCAN_START:
//                    mVideoLoader.scanStart();
//                    break;
//                case SCAN_FINISH:
//                    mVideoAdapter.updateVideos(mVideoLoader.getVideos());
////                    if (isVisible()) {
////                        mVideoAdapter.notifyDataSetChanged();
////                    }
//                    break;
//                case SCAN_CANCEL:
//                    break;
//                case SCAN_ADD_ITEM:
//                    mVideoAdapter.addVideo(msg.arg1, (MediaWrapper)msg.obj);
//                    break;
//                default:
//                    super.handleMessage(msg);
//            }
//        }
//    };

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
//        mVideoLoader = new VideoLoader(this);

        Resources res = getResources();
        mListMode = res.getBoolean(R.bool.list_mode);
        mListMode |= res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT &&
                PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("force_list_portrait", false);

        mGridView = (AutoFitRecyclerView) view.findViewById(R.id.video_grid);
//        mGridView.setHasFixedSize(true);

        mVideoAdapter = new VideoListAdapter(this);

        mGridView.setAdapter(mVideoAdapter);

//        mHandler.sendEmptyMessage(SCAN_START);
    }

    @Override
    public void onStart() {
        super.onStart();
//        mFabPlay.setImageResource(R.drawable.ic_fab_play);
//        registerForContextMenu(mGridView);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        updateViewMode();
//        if (mMediaLibrary.isInitiated())
//            fillView();
//        else
//            setupMediaLibraryReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
//        unregisterForContextMenu(mGridView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putString(KEY_GROUP, mGroup);
    }

    @Override
    public void onDestroyView() {
//        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(messageReceiverVideoListFragment);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVideoAdapter.clear();
    }

    public void notifyDataChanged() {
        if (!isVisible()) {
            Log.d(TAG, "notifyDataChanged but not visible");
            return;
        }
        if (mVideoAdapter != null) {
            mVideoAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_video;
    }

    @Override
    protected String getPageTitle() {
        return PAGE_TITLE;
    }

    @Override
    public void onClick(View v, int position, MediaWrapper item) {
        jumpToPlayerActivity(item.getFilePath());
    }

    @Override
    public boolean onLongClick(View v, int position, MediaWrapper item) {
        return false;
    }

    @Override
    public void onCtxClick(View v, int position, MediaWrapper item) {

    }

    @Override
    public void onUpdateFinished(RecyclerView.Adapter adapter) {

    }

    private void jumpToPlayerActivity(String videoPath) {
        Intent intent = new Intent(getContext(), VideoPlayerActivity.class);
        intent.putExtra("videoPath", videoPath);
        startActivity(intent);
    }

    private void updateViewMode() {
        if (getView() == null || getActivity() == null) {
            Log.w(TAG, "Unable to setup the view");
            return;
        }
        Resources res = getResources();
        // Compute the left/right padding dynamically
        DisplayMetrics outMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(outMetrics);

        // Select between grid or list
        if (!mListMode) {
            int thumbnailWidth = res.getDimensionPixelSize(R.dimen.grid_card_thumb_width);
            mGridView.setColumnWidth(mGridView.getPerfectColumnWidth(thumbnailWidth, res.getDimensionPixelSize(R.dimen.default_margin)));
            if (mVideoAdapter != null) {
                mVideoAdapter.setGridCardWidth(mGridView.getColumnWidth());
            }
        }
        mGridView.setNumColumns(mListMode ? 1 : 2);
        if (mVideoAdapter != null && mVideoAdapter.isListMode() != mListMode) {
//            if (listMode)
//                mGridView.addItemDecoration(mDividerItemDecoration);
//            else
//                mGridView.removeItemDecoration(mDividerItemDecoration);
            mVideoAdapter.setListMode(mListMode);
        }
    }

//    @Override
//    public void onLoadItem(int position, MediaWrapper video) {
////        mHandler.sendMessage(Message.obtain(mHandler, SCAN_ADD_ITEM, position, 0, video));
//    }
//
//    @Override
//    public void onLoadCompleted(ArrayList<MediaWrapper> videos) {
//        mHandler.sendEmptyMessage(SCAN_FINISH);
//    }
    public boolean isListMode() {
        return mListMode;
    }

    public void toggleMode() {
        Log.d(TAG, "toggleMode");
        mListMode = !mListMode;
//        List<MediaWrapper> videos = null;
//        if (mVideoAdapter != null) {
//            videos = mVideoAdapter.getVideos();
//        }
//        mVideoAdapter = new VideoListAdapter(this);
        updateViewMode();
        mVideoAdapter.notifyDataSetChanged();
//        mGridView.setAdapter(mVideoAdapter);
//        mGridView.requestLayout();
//        mGridView.invalidate();
//        if (videos != null && videos.size() > 0) {
//            mVideoAdapter.updateVideos(videos);
//        }
    }

    public void updateVideos(List<MediaWrapper> videos) {
        if (mVideoAdapter != null) {
            mVideoAdapter.updateVideos(videos);
        }
    }

    public void sortVideos(Comparator<MediaWrapper> comparator) {
        mVideoAdapter.sortVideos(comparator);
    }

    public void sortBy(int sortby) {
        mVideoAdapter.sortBy(sortby);
    }

    public int sortDirection(int sortby) {
        return mVideoAdapter.sortDirection(sortby);
    }

}
