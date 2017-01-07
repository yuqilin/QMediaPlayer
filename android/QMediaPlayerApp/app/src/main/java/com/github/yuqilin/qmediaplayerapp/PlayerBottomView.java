package com.github.yuqilin.qmediaplayerapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

/**
 * Created by yuqilin on 17/1/6.
 */

public class PlayerBottomView extends FrameLayout {
    public PlayerBottomView(Context context) {
        super(context);
        initView();
    }

    public PlayerBottomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PlayerBottomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlayerBottomView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        Context context = getContext();
        LayoutInflater.from(context).inflate(R.layout.view_player_bottom, this);
    }
}
