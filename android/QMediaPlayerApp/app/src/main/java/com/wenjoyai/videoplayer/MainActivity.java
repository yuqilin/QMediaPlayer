package com.wenjoyai.videoplayer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;

import com.wenjoyai.videoplayer.gui.video.VideoFragment;
import com.wenjoyai.videoplayer.media.MediaComparators;
import com.wenjoyai.videoplayer.media.MediaWrapper;
import com.wenjoyai.videoplayer.media.VideoLoader;
import com.wenjoyai.videoplayer.util.FileUtils;
import com.wenjoyai.videoplayer.util.ShareUtils;
import com.wenjoyai.videoplayer.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private static String[] sPageTitles = {"HOME", "VIDEOS", "FOLDERS"};

    private final static String FOLDER_ALL_VIDEOS = "All Videos";

    private ViewPager mViewPager;
//    private SmartTabLayout mViewPagerTab;
    private FragmentPagerAdapter mAdpter;

    private Spinner mSpinner;

    private List<BaseFragment> mFragments = new ArrayList<>();

    private VideoFragment mVideoFragment;

    private VideoLoader mVideoLoader;

    private Map<String, List<MediaWrapper>> mAllVideos = new TreeMap<>();

    private ArrayAdapter<String> mArrayAdapter;

    private int mCurrentFolderSelected = 0;

    private List<String> mFolderNames = new ArrayList<>();

    private boolean mLoadCompleted = false;

    private static final int SCAN_START = 1;
    private static final int SCAN_FINISH = 2;
    private static final int SCAN_CANCEL = 3;
    private static final int SCAN_ADD_ITEM = 4;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCAN_START:
                    mVideoLoader.scanStart();
                    break;
                case SCAN_FINISH:
                    mVideoFragment.updateVideos(mVideoLoader.getVideos());
                    break;
                case SCAN_CANCEL:
                    break;
                case SCAN_ADD_ITEM:
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private long mPressedTime = 0;

    private VideoLoader.VideoLoaderListener mVideoLoaderListener = new VideoLoader.VideoLoaderListener() {
        @Override
        public void onLoadItem(int position, MediaWrapper video) {

        }

        @Override
        public void onLoadCompleted(final List<MediaWrapper> videos) {

            for (MediaWrapper video : videos) {
                String folderPath = FileUtils.getParent(video.getFilePath());
                String folderName = FileUtils.getFolderName(folderPath);
                List<MediaWrapper> folderVideos = null;
                if (mAllVideos.containsKey(folderName)) {
                    folderVideos = mAllVideos.get(folderName);
                } else {
                    folderVideos = new ArrayList<>();
                }
                folderVideos.add(video);
                Log.d(TAG, "video.filePath " + video.getFilePath() + ", folderName : " + folderName);
                mAllVideos.put(folderName, folderVideos);
            }

//            Collections.sort(mAllVideos, new Comparator<Map.Entry<String, MediaWrapper>>() {
//                @Override
//                public int compare(Map.Entry<String, MediaWrapper> stringMediaWrapperEntry, Map.Entry<String, MediaWrapper> t1) {
//                    return 0;
//                }
//            });

//            for (String folderPath : mAllVideos.keySet()) {
//                String folderName = FileUtils.getFolderName(folderPath);
//                Log.d(TAG, "folderName : " + folderName);
//                mFolderNames.add(folderName);
//            }
            mFolderNames.addAll(mAllVideos.keySet());

            Log.d(TAG, "mFolderNames : " + mFolderNames);

            mLoadCompleted = true;

            mHandler.sendEmptyMessage(SCAN_FINISH);
//            new Handler().post(new Runnable() {
//                @Override
//                public void run() {
//                    mArrayAdapter.notifyDataSetChanged();
//                    mVideoFragment.updateVideos(videos);
//                }
//            });
        }
    };

    private AdapterView.OnItemSelectedListener mOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            Log.d(TAG, "onItemSelected, position = " + position + ", id = " + id);
            if (!mLoadCompleted)
                return;
            List<MediaWrapper> selectedVideos = null;
            if (position == 0) {
                selectedVideos = mVideoLoader.getVideos();
            } else {
                selectedVideos = mAllVideos.get(mFolderNames.get(position));
            }
            mVideoFragment.updateVideos(selectedVideos);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            Log.d(TAG, "onNothingSelected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mSpinner = (Spinner) findViewById(R.id.main_spinner);
        mFolderNames.add(FOLDER_ALL_VIDEOS);
        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mFolderNames);
        mArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mArrayAdapter);
//        mSpinner.setMinimumWidth(500);
//        mSpinner.setDropDownWidth(900);
        mSpinner.setOnItemSelectedListener(mOnItemSelectedListener);

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
//        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mAdpter);

        mVideoLoader = new VideoLoader(mVideoLoaderListener);

        mHandler.sendEmptyMessage(SCAN_START);

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
    protected void onStart() {
        super.onStart();
//        mVideoLoader.scanStart();
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

    private void jumpToAboutActivity() {
        Intent intent= new Intent(this, AboutActivity.class);
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
        Log.d(TAG, "onOptionsItemSelected, item id = " + item.getItemId());

        switch (item.getItemId()) {
            case R.id.mi_switchmode:
                mVideoFragment.toggleMode();
                item.setIcon(mVideoFragment.isListMode() ? R.drawable.ic_list_mode : R.drawable.ic_grid_mode);
                break;
            case R.id.mi_sortby:
                break;
            case R.id.mi_sortby_name:
                mVideoFragment.sortVideos(MediaComparators.byName);
                break;
            case R.id.mi_sortby_length:
                mVideoFragment.sortVideos(MediaComparators.byLength);
                break;
            case R.id.mi_sortby_date:
                mVideoFragment.sortVideos(MediaComparators.byDate);
                break;
            case R.id.mi_about:
                jumpToAboutActivity();
                break;
            case R.id.mi_rateus:
                ShareUtils.launchAppDetail(MainActivity.this, getPackageName(), "com.android.vending");
                break;
            case R.id.mi_invite:
                ShareUtils.shareAppText(MainActivity.this);
                break;
            case R.id.mi_help:
                ShareUtils.adviceEmail(MainActivity.this);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu");
        super.onPrepareOptionsMenu(menu);

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

    private PopupMenu.OnMenuItemClickListener onMoreMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.mi_sortby:
//                    showPopupMenu(findViewById(R.id.mi_sortby), R.menu.menu_sortby, );
                    break;
                case R.id.mi_about:
                    jumpToAboutActivity();
                    break;
                case R.id.mi_rateus:
                    ShareUtils.launchAppDetail(MainActivity.this, getPackageName(), "com.android.vending");
                    break;
                case R.id.mi_invite:
                    break;
                case R.id.mi_help:
                    ShareUtils.adviceEmail(MainActivity.this);
                    break;
            }
            return true;
        }
    };

    private PopupMenu.OnMenuItemClickListener onSortbyMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
            }
            return true;
        }
    };

    private void showPopupMenu(View anchor, int menuRes, PopupMenu.OnMenuItemClickListener onMenuItemClickListener) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(menuRes, popup.getMenu());
        popup.setOnMenuItemClickListener(onMenuItemClickListener);
        popup.show();
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
