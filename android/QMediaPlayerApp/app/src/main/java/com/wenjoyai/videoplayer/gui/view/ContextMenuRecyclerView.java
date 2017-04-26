package com.wenjoyai.videoplayer.gui.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;

public class ContextMenuRecyclerView extends RecyclerView {

    private ContextMenu.ContextMenuInfo mContextMenuInfo = null;

    public ContextMenuRecyclerView(Context context) {
        super(context);
    }

    public ContextMenuRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContextMenuRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

    public void openContextMenu(int position) {
        if (position >= 0)
            createContextMenuInfo(position);
        showContextMenu();
    }

    protected void createContextMenuInfo(int position) {
        if (mContextMenuInfo == null)
            mContextMenuInfo = new RecyclerContextMenuInfo(position);
        else
            ((RecyclerContextMenuInfo)mContextMenuInfo).setValues(position);
    }

    public static class RecyclerContextMenuInfo implements ContextMenu.ContextMenuInfo {

        public int position;

        public RecyclerContextMenuInfo(int position) {
            setValues(position);
        }

        public void setValues(int position){
            this.position = position;
        }
    }
}
