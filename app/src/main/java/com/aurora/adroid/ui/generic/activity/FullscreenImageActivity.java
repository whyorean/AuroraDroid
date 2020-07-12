/*
 * Aurora Droid
 * Copyright (C) 2019-20, Rahul Kumar Patel <whyorean@gmail.com>
 *
 * Aurora Droid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora Droid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Aurora Droid.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.aurora.adroid.ui.generic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.adapter.BigScreenshotsAdapter;
import com.aurora.adroid.util.ViewUtil;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FullscreenImageActivity extends BaseActivity {

    static public final String INTENT_SCREENSHOT_NUMBER = "INTENT_SCREENSHOT_NUMBER";

    @BindView(R.id.gallery)
    RecyclerView recyclerView;

    private List<String> urlList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.activity_fullscreen_screenshots);
        ButterKnife.bind(this);
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {

            stringExtra = intent.getStringExtra(Constants.STRING_EXTRA);
            intExtra = intent.getIntExtra(INTENT_SCREENSHOT_NUMBER, 0);

            if (stringExtra != null) {
                final Type type = new TypeToken<List<String>>() {
                }.getType();
                urlList = gson.fromJson(stringExtra, type);
                setupRecycler();
            }
        } else {
            finishAfterTransition();
        }
    }

    private void setupRecycler() {
        final SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        recyclerView.setBackgroundColor(ViewUtil.getStyledAttribute(this, android.R.attr.colorBackground));
        recyclerView.setAdapter(new BigScreenshotsAdapter(urlList, this));
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        recyclerView.scrollToPosition(intExtra);
    }
}