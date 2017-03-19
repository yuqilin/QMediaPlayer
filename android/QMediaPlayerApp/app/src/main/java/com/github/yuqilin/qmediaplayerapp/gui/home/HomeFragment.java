package com.github.yuqilin.qmediaplayerapp.gui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.github.yuqilin.qmediaplayerapp.BaseFragment;
import com.github.yuqilin.qmediaplayerapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuqilin on 17/1/24.
 */

public class HomeFragment extends BaseFragment {

    private static final String PAGE_TITLE = "HOME";

    private List<HomeData> mDatas = new ArrayList<>();
    private GridView mGridView;
    private HomeAdapter mAdapter;

    @Override
    protected void initView(View view, Bundle savedInstanceState) {

        mGridView = (GridView) view.findViewById(R.id.home_grid);

        mAdapter = new HomeAdapter(mActivity);
        mGridView.setAdapter(mAdapter);

        if (mDatas.size() == 0) {
            mDatas.add(new HomeData(R.drawable.ic_movie, getString(R.string.home_item_movies),
                    getString(R.string.home_item_movies_sub)));
            mDatas.add(new HomeData(R.drawable.ic_lock_outline_24_px, getString(R.string.home_item_private),
                    getString(R.string.home_item_private_sub)));
            mDatas.add(new HomeData(R.drawable.ic_works, getString(R.string.home_item_works),
                    getString(R.string.home_item_works_sub)));
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected String getPageTitle() {
        return PAGE_TITLE;
    }

    private class HomeData {
        int resId;
        String mainText;
        String subText;
        public HomeData(int resId, String mainText, String subText) {
            this.resId = resId;
            this.mainText = mainText;
            this.subText = subText;
        }
    }

    private class HomeAdapter extends BaseAdapter {
        private Context mContext;

        public HomeAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int i) {
            return mDatas.get(i);
        }

        @Override
        public long getItemId(int i) {
            return mDatas.get(i).resId;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_home, null);
            }
            ImageView image = (ImageView) convertView.findViewById(R.id.home_item_image);
            TextView main = (TextView) convertView.findViewById(R.id.home_item_main_text);
            TextView sub = (TextView) convertView.findViewById(R.id.home_item_sub_text);

            image.setImageResource(mDatas.get(i).resId);
            main.setText(mDatas.get(i).mainText);
            sub.setText(mDatas.get(i).subText);

            return convertView;
        }
    }
}
