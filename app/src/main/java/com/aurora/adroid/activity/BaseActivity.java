package com.aurora.adroid.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aurora.adroid.util.ThemeUtil;

public abstract class BaseActivity extends AppCompatActivity {

    private ThemeUtil themeUtil = new ThemeUtil();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeUtil.onCreate(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        themeUtil.onResume(this);
    }
}
