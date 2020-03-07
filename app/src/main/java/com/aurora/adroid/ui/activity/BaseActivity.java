package com.aurora.adroid.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aurora.adroid.util.ThemeUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;

public abstract class BaseActivity extends AppCompatActivity {

    protected int intExtra;
    protected String stringExtra;
    protected Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).create();

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
