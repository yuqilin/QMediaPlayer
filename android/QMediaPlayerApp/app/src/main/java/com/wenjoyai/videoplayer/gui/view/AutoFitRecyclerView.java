package com.wenjoyai.videoplayer.gui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.wenjoyai.videoplayer.QApplication;

public class AutoFitRecyclerView extends ContextMenuRecyclerView {

    public static final String TAG = "AutoFitRecyclerView";

    private GridLayoutManager mGridLayoutManager;
    private int mColumnWidth = -1;
    private int mSpanCount = -1;

    public AutoFitRecyclerView(Context context) {
        super(context);
        init(context, null);
    }

    public AutoFitRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AutoFitRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            int[] attrsArray = {
                    android.R.attr.columnWidth
            };
            TypedArray array = context.obtainStyledAttributes(attrs, attrsArray);
            mColumnWidth = array.getDimensionPixelSize(0, -1);
            array.recycle();
        }

        mGridLayoutManager = new GridLayoutManager(getContext(), 1);
        setLayoutManager(mGridLayoutManager);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        Log.d(TAG, "onMeasure widthSpec = " + widthSpec + ", heightSpec = " + heightSpec + ", mSpanCount = " + mSpanCount);
        super.onMeasure(widthSpec, heightSpec);
        if (mSpanCount == -1 && mColumnWidth > 0) {
            int ratio = getMeasuredWidth() / mColumnWidth;
            int spanCount = Math.max(1, ratio);
            mGridLayoutManager.setSpanCount(spanCount);
        } else if (mSpanCount > 0){
            mGridLayoutManager.setSpanCount(mSpanCount);
        }
    }

    public void setColumnWidth(int width) {
        Log.d(TAG, "setColumnWidth width " + width);
        mColumnWidth = width;
    }

    public int getPerfectColumnWidth(int columnWidth, int margin) {

        WindowManager wm = (WindowManager) QApplication.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int displayWidth = display.getWidth() - margin;

        int remainingSpace = displayWidth % columnWidth;
        int ratio = displayWidth / columnWidth;
        int spanCount = Math.max(1, ratio);

        return (columnWidth + (remainingSpace / spanCount));
    }

    public int getColumnWidth() {
        return mColumnWidth;
    }

    public void setNumColumns(int spanCount) {
        Log.d(TAG, "setNumColumns spanCount " + spanCount);
        mSpanCount = spanCount;
    }

    public void setSpanSizeLookup(GridLayoutManager.SpanSizeLookup spanSizeLookup) {
        mGridLayoutManager.setSpanSizeLookup(spanSizeLookup);
    }

}
