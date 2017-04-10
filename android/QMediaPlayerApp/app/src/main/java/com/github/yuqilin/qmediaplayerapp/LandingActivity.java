package com.github.yuqilin.qmediaplayerapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by yuqilin on 17/4/8.
 */

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startNextPage();
    }

    private void startNextPage() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LandingActivity.this, MainActivity.class);
                startActivity(intent);
                LandingActivity.this.finish();
            }
        }, 2000);
    }
}
