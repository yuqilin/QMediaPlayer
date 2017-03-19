package com.github.yuqilin.qmediaplayerapp;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.github.yuqilin.qmediaplayerapp.media.MediaWrapper;

/**
 * Created by yuqilin on 17/3/16.
 */

public interface IEventsHandler {
    void onClick(View v, int position, MediaWrapper item);
    boolean onLongClick(View v, int position, MediaWrapper item);
    void onCtxClick(View v, int position, MediaWrapper item);
    void onUpdateFinished(RecyclerView.Adapter adapter);
}
