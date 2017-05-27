package com.wenjoyai.videoplayer.gui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by yuqilin on 17/5/25.
 */

public class RecordVideoRecyclerView extends RecyclerView {

    private LinearLayoutManager mLayoutManager;

    public RecordVideoRecyclerView(Context context) {
        super(context);
    }

    public RecordVideoRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordVideoRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs) {
        mLayoutManager = new LinearLayoutManager(getContext(), HORIZONTAL, false);
        setLayoutManager(mLayoutManager);
    }
}
