package com.wenjoyai.videoplayer;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wenjoyai.videoplayer.util.ShareUtils;

/**
 * Created by yuqilin on 17/4/18.
 */

public class AboutActivity extends AppCompatActivity {
    private ImageView mBack;
    private TextView mVersion;
    private View mContact;
    private View mRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mBack = (ImageView) findViewById(R.id.about_back);
        mVersion = (TextView) findViewById(R.id.about_version);
        mContact = findViewById(R.id.about_contact);
        mRate = findViewById(R.id.about_rate);

        mVersion.setText(String.format(getString(R.string.about_version),getVersion()));
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareUtils.adviceEmail(AboutActivity.this);
            }
        });

        mRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareUtils.launchAppDetail(AboutActivity.this, getPackageName(), "com.android.vending");
            }
        });
    }

    private String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
