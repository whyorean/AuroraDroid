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
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.ui.generic.fragment.AboutFragment;
import com.aurora.adroid.ui.generic.fragment.InstalledFragment;
import com.aurora.adroid.ui.intro.RepoFragment;
import com.aurora.adroid.ui.generic.fragment.BlacklistFragment;
import com.aurora.adroid.ui.generic.fragment.FavouriteFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContainerActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private ActionBar actionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        ButterKnife.bind(this);
        setupActionBar();
        onNewIntent(getIntent());
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String fragmentName = intent.getStringExtra(Constants.FRAGMENT_NAME);
        Fragment fragment = null;

        switch (fragmentName) {
            case Constants.FRAGMENT_ABOUT:
                actionBar.setTitle(getString(R.string.action_about));
                fragment = new AboutFragment();
                break;
            case Constants.FRAGMENT_INSTALLED:
                actionBar.setTitle(getString(R.string.title_installed));
                fragment = new InstalledFragment();
                break;
            case Constants.FRAGMENT_BLACKLIST:
                actionBar.setTitle(getString(R.string.action_blacklist));
                fragment = new BlacklistFragment();
                break;
            case Constants.FRAGMENT_FAV_LIST:
                actionBar.setTitle(getString(R.string.action_favourites));
                fragment = new FavouriteFragment();
                break;
            case Constants.FRAGMENT_REPOSITORY:
                actionBar.setTitle(getString(R.string.title_repositories));
                fragment = new RepoFragment();
                break;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
    }


    private void setupActionBar() {
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
    }
}
