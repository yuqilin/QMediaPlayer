package com.wenjoyai.videoplayer.gui.video;

import android.content.Context;
import android.provider.MediaStore;
import android.support.annotation.MainThread;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wenjoyai.videoplayer.IEventsHandler;
import com.wenjoyai.videoplayer.R;
import com.wenjoyai.videoplayer.media.MediaWrapper;
import com.wenjoyai.videoplayer.util.AsyncImageLoader;
import com.wenjoyai.videoplayer.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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

    private List<MediaWrapper> mVideos = new ArrayList<>();

    private VideoComparator mVideoComparator = new VideoComparator();

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
            params.height = params.width * 13 / 16;
            Log.d(TAG, "GridLayout width = " + params.width + ", height = " + params.height);
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
        Log.d(TAG, "onBindViewHolder position[" + position + "] : " + media + " " + media.getFilePath());
        holder.mThumbnail.setImageBitmap(AsyncImageLoader.DEFAULT_COVER_VIDEO);
        holder.mThumbnail.setTag(media);
        AsyncImageLoader.loadPicture(holder.mThumbnail, media, mListMode ? MediaStore.Video.Thumbnails.MICRO_KIND : MediaStore.Video.Thumbnails.MINI_KIND);
        holder.mFileName.setText(media.getFilePath().substring(media.getFilePath().lastIndexOf('/') + 1));
        holder.mFileSize.setText(Strings.readableSize(media.getFileSize()));
        holder.mDuration.setText(Strings.millisToString(media.getLength()));
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
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        Log.d(TAG, "onAttachedToRecyclerView");
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        Log.d(TAG, "onViewDetachedFromWindow");
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        Log.d(TAG, "onViewRecycled layoutPosition = " + holder.getLayoutPosition() + ", adapterPosition = " + holder.getAdapterPosition());
        super.onViewRecycled(holder);
//        holder.binding.setVariable(BR.cover, AsyncImageLoader.DEFAULT_COVER_VIDEO_DRAWABLE);
//        holder.mThumbnail.setTag(holder.getLayoutPosition());
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

    public List<MediaWrapper> getVideos() {
        return mVideos;
    }

    public void updateVideos(List<MediaWrapper> videos) {
        Log.d(TAG, "updateVideos : " + videos.size());
        mVideos = videos;
        Collections.sort(mVideos, mVideoComparator);
        notifyDataSetChanged();
    }
    public void addVideo(int position, MediaWrapper video) {
        Log.d(TAG, "addVideo position " + position);
        mVideos.add(position, video);
        notifyItemInserted(position);
    }

    // TODO: 17/4/21 优化：排序动作在后台处理，以防数据量大时耗时过长
    public void sortVideos(Comparator<MediaWrapper> comparator) {
        if (mVideos.size() > 1) {
            Collections.sort(mVideos, comparator);
        }
        notifyDataSetChanged();
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
            if (mListMode) {
                mListItem = v.findViewById(R.id.list_item_view);
                mThumbnail = (ImageView) v.findViewById(R.id.item_video_list_thumbnail);
                mFileName = (TextView) v.findViewById(R.id.item_video_list_filename);
                mFileSize = (TextView) v.findViewById(R.id.item_video_list_filesize);
                mDuration = (TextView) v.findViewById(R.id.item_video_list_duration);
            } else {
                mListItem = v.findViewById(R.id.grid_item_view);
                mThumbnail = (ImageView) v.findViewById(R.id.item_video_grid_thumbnail);
                mFileName = (TextView) v.findViewById(R.id.item_video_grid_filename);
                mFileSize = (TextView) v.findViewById(R.id.item_video_grid_filesize);
                mDuration = (TextView) v.findViewById(R.id.item_video_grid_duration);
                mThumbnail.setLayoutParams(new LinearLayout.LayoutParams(mGridCardWidth, mGridCardWidth * 9 / 16));
            }
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

    int sortDirection(int sortDirection) {
        return mVideoComparator.sortDirection(sortDirection);
    }

    void sortBy(int sortby) {
        mVideoComparator.sortBy(sortby);
    }

    private class VideoComparator implements Comparator<MediaWrapper> {

        private int mSortDirection;
        private int mSortBy;

        VideoComparator() {
            mSortBy = SORT_BY_TITLE;
            mSortDirection = 1;
        }

        int sortDirection(int sortby) {
            if (sortby == mSortBy) {
                return mSortDirection;
            } else {
                return -1;
            }
        }

        void sortBy(int sortby) {
            if (mSortBy == sortby) {
                mSortDirection *= -1;
            } else {
                mSortBy = sortby;
                mSortDirection *= 1;
            }
            Collections.sort(mVideos, mVideoComparator);
            notifyDataSetChanged();
        }

        @Override
        public int compare(MediaWrapper item1, MediaWrapper item2) {
            if (item1 == null) {
                return item2 == null ? 0 : -1;
            } else if (item2 == null) {
                return 1;
            }
            int compare = 0;
            switch (mSortBy) {
                case SORT_BY_DATE:
                    compare = ((Long)item1.getDateTaken()).compareTo((Long)item2.getDateTaken());
                    break;
                case SORT_BY_TITLE:
                    compare = item1.getTitle().toUpperCase(Locale.ENGLISH).compareTo(
                            item2.getTitle().toUpperCase(Locale.ENGLISH));
                    break;
                case SORT_BY_LENGTH:
                    compare = ((Long)item1.getLength()).compareTo((Long)item2.getLength());
                    break;
            }
            return mSortDirection * compare;
        }
    }



}
