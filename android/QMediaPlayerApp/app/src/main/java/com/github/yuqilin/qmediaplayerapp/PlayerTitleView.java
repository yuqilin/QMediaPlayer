package com.github.yuqilin.qmediaplayerapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by yuqilin on 17/1/6.
 */

public class PlayerTitleView extends FrameLayout {

    View mBack;
    TextView mTitleText;
    ImageView mBattery;
    TextView mSysTime;
    ImageView mShowMore;

    public PlayerTitleView(Context context) {
        super(context);
        initView();
    }

    public PlayerTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PlayerTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlayerTitleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        Context context = getContext();
        LayoutInflater.from(context).inflate(R.layout.view_player_title, this);
        mBack = findViewById(R.id.view_player_title_back);
        mTitleText = (TextView) findViewById(R.id.view_player_title_text);
    }

    public void setTitle(String title) {
        mTitleText.setText(title);
    }


}
