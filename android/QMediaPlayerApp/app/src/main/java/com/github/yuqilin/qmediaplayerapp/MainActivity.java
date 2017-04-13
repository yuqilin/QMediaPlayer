package com.github.yuqilin.qmediaplayerapp;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.github.yuqilin.qmediaplayerapp.gui.home.HomeFragment;
import com.github.yuqilin.qmediaplayerapp.gui.video.VideoFragment;
import com.github.yuqilin.qmediaplayerapp.media.MediaWrapper;
import com.github.yuqilin.qmediaplayerapp.util.ToastUtil;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private static String[] sPageTitles = {"HOME", "VIDEOS", "FOLDERS"};

    private ViewPager mViewPager;
//    private SmartTabLayout mViewPagerTab;
    private FragmentPagerAdapter mAdpter;

    private Spinner mSpinner;

    private List<BaseFragment> mFragments = new ArrayList<>();

    private VideoFragment mVideoFragment;

    private long mPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // TODO: 17/4/11 spinner add adapter
        String[] folders = { "All Videos", "Camera", "WeiXin" };
        mSpinner = (Spinner) findViewById(R.id.main_spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.item_video_spinner, R.id.item_video_spinner_text, folders);
        mSpinner.setAdapter(arrayAdapter);

//        mFragments.add(new HomeFragment());
        mVideoFragment = new VideoFragment();
        mFragments.add(mVideoFragment);

        mAdpter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mFragments.get(position).getPageTitle();
            }
        };

        mViewPager = (ViewPager) findViewById(R.id.main_viewpager);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mAdpter);

//        mViewPagerTab = (SmartTabLayout) findViewById(R.id.main_viewpagertab);
////        mViewPagerTab.setCustomTabView(R.layout.custom_tab, R.id.custom_text);
//        mViewPagerTab.setDividerColors(getResources().getColor(R.color.transparent));
//        mViewPagerTab.setViewPager(mViewPager);

//        LinkedBlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<Runnable>();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            ExecutorService exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, blockingQueue);
//            mTask = new LoadVideoTask().executeOnExecutor(exec);
//        } else {
//            mTask = new LoadVideoTask().execute();
//        }

    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClickPlay(View v) {
//        String videoPath = mVideoPathEdit.getText().toString();
//        if (!TextUtils.isEmpty(videoPath)) {
//            jumpToPlayerActivity(videoPath);
//        }
    }

    private void jumpToPlayerActivity(String videoPath) {
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra("videoPath", videoPath);
        startActivity(intent);
    }

    @Override
    protected int getContentViewId() {
        return 0;
    }

    @Override
    protected int getFragmentContentId() {
        return 0;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_switchmode:
                mVideoFragment.toggleMode();
                break;
            case R.id.mi_more:
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        long now = System.currentTimeMillis();
        if ((now - mPressedTime) > 2000) {
            ToastUtil.makeToastAndShow(this, " Press back key again to exit!");
            mPressedTime = now;
        } else {
            this.finish();
            System.exit(0);
        }
    }

    private class MySpinnerAdapter extends BaseAdapter {

        private List<String> mData;

        public void setData(List<String> data) {
            mData = data;
        }


        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int i) {
            return mData.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return null;
        }
    }



}
