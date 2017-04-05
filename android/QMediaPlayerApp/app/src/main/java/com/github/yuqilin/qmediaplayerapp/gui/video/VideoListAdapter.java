/*****************************************************************************
 * VideoListAdapter.java
 *****************************************************************************
 * Copyright © 2011-2012 VLC authors and VideoLAN
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package com.github.yuqilin.qmediaplayerapp.gui.video;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.yuqilin.qmediaplayerapp.IEventsHandler;
import com.github.yuqilin.qmediaplayerapp.R;
import com.github.yuqilin.qmediaplayerapp.media.MediaWrapper;
import com.github.yuqilin.qmediaplayerapp.util.AsyncImageLoader;

import java.util.ArrayList;
import java.util.List;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> {

    public final static String TAG = "VideoListAdapter";

    public final static int SORT_BY_TITLE = 0;
    public final static int SORT_BY_LENGTH = 1;
    public final static int SORT_BY_DATE = 2;

    final static int UPDATE_SELECTION = 0;
    final static int UPDATE_THUMB = 1;
    final static int UPDATE_TIME = 2;

    private boolean mListMode = true;
//    private VideoFragment mFragment;
    private IEventsHandler mEventsHandler;

//    private VideoComparator mVideoComparator = new VideoComparator();
//    private volatile SortedList<MediaWrapper> mVideos = new SortedList<>(MediaWrapper.class, mVideoComparator);
//    private volatile ArrayList<MediaInfo> mVideos = new ArrayList<>();


    private int mGridCardWidth = 0;

    private ArrayList<MediaWrapper> mVideos = new ArrayList<>();
    private ArrayList<MediaWrapper> mOriginalData = null;

    public VideoListAdapter(IEventsHandler eventsHandler) {
        super();
//        mFragment = fragment;
        mEventsHandler = eventsHandler;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder viewType " + viewType);
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(mListMode ? R.layout.item_video_list : R.layout.item_video_grid, parent, false);


        if (!mListMode) {
            GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) v.getLayoutParams();
            params.width = mGridCardWidth;
            params.height = params.width * 10 / 16;
            v.setLayoutParams(params);
        }

        return new ViewHolder(v);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            MediaWrapper media = (MediaWrapper) view.getTag();
            mEventsHandler.onClick(view, 0, media);
        }
    };

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder position " + position);
        MediaWrapper media = mVideos.get(position);
        if (media == null) {
            return;
        }
        Log.d(TAG, "position[" + position + "]: " + media.filePath);
        holder.mThumbnail.setImageBitmap(AsyncImageLoader.DEFAULT_COVER_VIDEO);
        AsyncImageLoader.loadPicture(holder.mThumbnail, media, position);
        holder.mFileName.setText(media.filePath.substring(media.filePath.lastIndexOf('/') + 1));
        holder.mFileSize.setText(media.fileSize);
        holder.mDuration.setText(media.duration);
        holder.mListItem.setTag(media);
        holder.mListItem.setOnClickListener(mOnClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            Log.d(TAG, "onBindViewHolder position " + position + ", payloads size " + payloads.size());
//            MediaWrapper media = mVideos.get(position);
            onBindViewHolder(holder, position);
//            for (Object data : payloads) {
//                switch ((int) data) {
//                    case UPDATE_THUMB:
////                        AsyncImageLoader.loadPicture(holder.thumbView, media);
//                        break;
//                    case UPDATE_TIME:
//                        fillView(holder, media);
//                        break;
//                    case UPDATE_SELECTION:
////                        boolean isSelected = media.hasStateFlags(MediaLibraryItem.FLAG_SELECTED);
////                        holder.setOverlay(isSelected);
////                        holder.binding.setVariable(BR.bgColor, ContextCompat.getColor(holder.itemView.getContext(), mListMode && isSelected ? R.color.orange200transparent : R.color.transparent));
//                        break;
//                }
//            }
        }
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        Log.d(TAG, "onViewDetachedFromWindow");
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        Log.d(TAG, "onViewRecycled");
        super.onViewRecycled(holder);
//        holder.binding.setVariable(BR.cover, AsyncImageLoader.DEFAULT_COVER_VIDEO_DRAWABLE);
        holder.mThumbnail.setTag(1001);
    }

    public void setListMode(boolean value) {
        mListMode = value;
    }

    public boolean isListMode() {
        return mListMode;
    }

    void setGridCardWidth(int gridCardWidth) {
        mGridCardWidth = gridCardWidth;
    }

    public void updateVideos(ArrayList<MediaWrapper> videos) {
        mVideos = videos;
        notifyDataSetChanged();
    }
    public void addVideo(int position, MediaWrapper video) {
        Log.d(TAG, "addVideo position " + position);
        mVideos.add(position, video);
        notifyItemInserted(position);
    }

//    @Override
//    public int getItemViewType(int position) {
//        int type = super.getItemViewType(position);
//        Log.d(TAG, "getItemViewType position = " + position + ", type = " + type);
//        return 1;
//    }

    @Override
    public long getItemId(int position) {
        return 0L;
    }

    @Override
    public int getItemCount() {
//        Log.d(TAG, "getItemCount : " + mVideos.size());
        return mVideos.size();
    }

    @MainThread
    public void clear() {
        mVideos.clear();
        mOriginalData = null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnFocusChangeListener {
        private View mListItem;
        private ImageView mThumbnail;
        private TextView mDuration;
        private TextView mFileName;
        private TextView mFileSize;

        public ViewHolder(View v) {
            super(v);
            v.setOnFocusChangeListener(this);
            mListItem = v.findViewById(R.id.list_item_view);
            mThumbnail = (ImageView) v.findViewById(R.id.item_video_list_thumbnail);
            mFileName = (TextView) v.findViewById(R.id.item_video_list_filename);
            mFileSize = (TextView) v.findViewById(R.id.item_video_list_filesize);
            mDuration = (TextView) v.findViewById(R.id.item_video_list_duration);
        }

        public void onClick(View v) {
            int position = getLayoutPosition();
            mEventsHandler.onClick(v, position, mVideos.get(position));
        }

        public void onMoreClick(View v){
            mEventsHandler.onCtxClick(v, getLayoutPosition(), null);
        }

        public boolean onLongClick(View v) {
            int position = getLayoutPosition();
            return mEventsHandler.onLongClick(v, position, mVideos.get(position));
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
        }
    }



}
