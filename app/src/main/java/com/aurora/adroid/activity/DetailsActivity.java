/*
 * Aurora Droid
 * Copyright (C) 2019, Rahul Kumar Patel <whyorean@gmail.com>
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
 */

package com.aurora.adroid.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aurora.adroid.R;
import com.aurora.adroid.fragment.DetailsFragment;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.ThemeUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity {

    public static final String INTENT_PACKAGE_NAME = "INTENT_APK_FILE_NAME";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private String packageName;
    private DetailsFragment detailsFragment;
    private ThemeUtil themeUtil = new ThemeUtil();

    static public Intent getDetailsIntent(Context context, String packageName) {
        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra(DetailsActivity.INTENT_PACKAGE_NAME, packageName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeUtil.onCreate(this);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        setupActionBar();
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        packageName = getIntentPackageName(intent);
        if (TextUtils.isEmpty(packageName)) {
            Log.e("No package name provided");
            finish();
            return;
        }
        Log.i("Getting info about %s", packageName);
        grabDetails(packageName);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onResume() {
        super.onResume();
        themeUtil.onResume(this);
    }

    @Override
    public void onBackPressed() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments.get(0) instanceof DetailsFragment) {
            FragmentManager fm = fragments.get(0).getChildFragmentManager();
            if (!fm.getFragments().isEmpty())
                fm.popBackStack();
            else
                super.onBackPressed();
        } else
            super.onBackPressed();
    }

    private void setupActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private String getIntentPackageName(Intent intent) {
        if (intent.hasExtra(INTENT_PACKAGE_NAME)) {
            return intent.getStringExtra(INTENT_PACKAGE_NAME);
        } else if (intent.getScheme() != null
                && (intent.getScheme().equals("market")
                || intent.getScheme().equals("http")
                || intent.getScheme().equals("https"))) {
            return intent.getData().getQueryParameter("id");
        } else if (intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            return bundle.getString(INTENT_PACKAGE_NAME);
        }
        return null;
    }

    public void grabDetails(String packageName) {
        detailsFragment = new DetailsFragment();
        Bundle arguments = new Bundle();
        arguments.putString("PackageName", packageName);
        detailsFragment.setArguments(arguments);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, detailsFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commitAllowingStateLoss();
    }

    public void redrawButtons() {
        if (detailsFragment != null)
            detailsFragment.drawButtons();
    }
}
