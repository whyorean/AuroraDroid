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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.R;
import com.aurora.adroid.adapter.ViewPagerAdapter;
import com.aurora.adroid.database.AppDatabase;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.Events;
import com.aurora.adroid.fragment.AppsFragment;
import com.aurora.adroid.fragment.HomeFragment;
import com.aurora.adroid.fragment.SearchFragment;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.service.RepoSyncService;
import com.aurora.adroid.util.ContextUtil;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.TextUtil;
import com.aurora.adroid.util.ThemeUtil;
import com.aurora.adroid.util.Util;
import com.aurora.adroid.util.ViewUtil;
import com.aurora.adroid.view.CustomViewPager;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AuroraActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.viewpager)
    CustomViewPager viewPager;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;

    private ActionBar actionBar;
    private ThemeUtil themeUtil = new ThemeUtil();
    private CompositeDisposable disposable = new CompositeDisposable();

    @Nullable
    public ActionBar getDroidActionBar() {
        return actionBar;
    }

    public BottomNavigationView getBottomNavigationView() {
        return bottomNavigationView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeUtil.onCreate(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (Util.isFirstLaunch(this)) {
            startActivity(new Intent(this, IntroActivity.class));
            finish();
        } else if (!DatabaseUtil.isDatabaseAvailable(this)) {
            showSyncDialog(false);
        } else if (DatabaseUtil.isDatabaseObsolete(this)) {
            showSyncDialog(true);
        } else {
            checkPermissions();
        }

        init();

        disposable.add(AuroraApplication.getRxBus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event instanceof Event) {
                        Events eventEnum = ((Event) event).getEvent();
                        switch (eventEnum) {
                            case SYNC_COMPLETED:
                                clearSyncAnim();
                                showSyncCompletedDialog();
                                break;
                            case SYNC_EMPTY:
                                clearSyncAnim();
                                ContextUtil.toastLong(this, "Select at least one repo to sync");
                                break;
                        }
                    }
                }));

        onNewIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.action_sync);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_downloads:
                startActivity(new Intent(this, DownloadsActivity.class));
                return true;
            case R.id.action_sync:
                showSyncDialog(false);
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && !TextUtil.isEmpty(intent.getDataString())) {
            String repoDataString = intent.getDataString();
            if (repoDataString.contains("fingerprint") || repoDataString.contains("FINGERPRINT")) {
                try {
                    String[] ss = repoDataString.split("\\?");
                    Repo repo = new Repo();
                    repo.setRepoName(Util.getDomainName(ss[0]));
                    repo.setRepoId(String.valueOf(repo.getRepoName().hashCode()));
                    repo.setRepoUrl(ss[0]);
                    ss[1] = ss[1].replace("fingerprint=", "");
                    ss[1] = ss[1].replace("FINGERPRINT=", "");
                    repo.setRepoFingerprint(ss[1]);
                    showAddRepoDialog(repo);
                } catch (Exception ignored) {
                }
            } else if (intent.getData() != null
                    && intent.getData().getLastPathSegment() != null) {
                if (intent.getData().getLastPathSegment().equalsIgnoreCase("repo")) {
                    try {
                        Repo repo = new Repo();
                        repo.setRepoName(intent.getData().getPath());
                        repo.setRepoId(String.valueOf(repo.getRepoName().hashCode()));
                        repo.setRepoUrl(intent.getDataString());
                        showAddRepoDialog(repo);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        themeUtil.onResume(this);
        Util.toggleSoftInput(this, false);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        Util.toggleSoftInput(this, false);
    }

    @Override
    protected void onDestroy() {
        try {
            AppDatabase.destroyInstance();
            Glide.with(this).pauseAllRequests();
            disposable.clear();
            disposable.dispose();
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }

    private void init() {
        setupActionbar();
        setupViewPager();
        setupBottomNavigation();
    }

    private void setupActionbar() {
        setSupportActionBar(mToolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setElevation(0f);
            actionBar.setTitle(getString(R.string.app_name));
        }
    }

    private void setupViewPager() {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(0, new HomeFragment());
        viewPagerAdapter.addFragment(1, new AppsFragment());
        viewPagerAdapter.addFragment(2, new SearchFragment());
        viewPager.enableScroll(false);
        viewPager.setAdapter(viewPagerAdapter);
    }

    private void setupBottomNavigation() {
        @ColorInt
        int backGroundColor = ViewUtil.getStyledAttribute(this, android.R.attr.colorBackground);
        bottomNavigationView.setBackgroundColor(ColorUtils.setAlphaComponent(backGroundColor, 245));
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            viewPager.setCurrentItem(menuItem.getOrder(), true);
            switch (menuItem.getItemId()) {
                case R.id.action_home:
                    Util.toggleSoftInput(this, false);
                    actionBar.setTitle(getString(R.string.title_home));
                    break;
                case R.id.action_installed:
                    Util.toggleSoftInput(this, false);
                    actionBar.setTitle(getString(R.string.title_apps));
                    break;
                case R.id.action_search:
                    Util.toggleSoftInput(this, true);
                    actionBar.setTitle(getString(R.string.title_search));
                    break;
            }
            return true;
        });
    }

    protected void showSyncDialog(boolean obsolete) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.dialog_sync_title))
                .setMessage(obsolete ? getString(R.string.dialog_sync_desc_alt) : getString(R.string.dialog_sync_desc))
                .setPositiveButton(getString(R.string.dialog_sync_positive), (dialog, which) -> {
                    if (RepoSyncService.isServiceRunning())
                        return;
                    startRepoSyncService();
                    startSyncAnim();
                })
                .setNegativeButton(getString(R.string.dialog_sync_negative), (dialog, which) -> {
                    dialog.dismiss();
                });
        builder.create();
        builder.show();
    }

    protected void showSyncCompletedDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.dialog_sync_title))
                .setMessage(getString(R.string.dialog_sync_completed_desc))
                .setPositiveButton(getString(R.string.dialog_sync_complete_positive), (dialog, which) -> {
                    Util.restartApp(this);
                })
                .setNegativeButton(getString(R.string.action_later), (dialog, which) -> {
                    dialog.dismiss();
                });
        ;
        builder.create();
        builder.show();
    }

    protected void showAddRepoDialog(Repo repo) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.dialog_repo_title))
                .setMessage(new StringBuilder()
                        .append(getString(R.string.dialog_repo_desc))
                        .append(" ")
                        .append(repo.getRepoName())
                        .append(" ?"))
                .setPositiveButton(getString(R.string.action_add), (dialog, which) -> {
                    RepoListManager.addRepoToCustomList(this, repo);
                })
                .setNegativeButton(getString(R.string.action_cancel), (dialog, which) -> {
                    dialog.dismiss();
                });
        ;
        builder.create();
        builder.show();
    }

    private void startSyncAnim() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.sync_anim);
        View view = findViewById(R.id.action_sync);
        if (view != null)
            view.startAnimation(animation);
    }

    private void clearSyncAnim() {
        View view = findViewById(R.id.action_sync);
        if (view != null)
            view.clearAnimation();
    }

    private void startRepoSyncService() {
        Intent intent = new Intent(this, RepoSyncService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.CAMERA
                    },
                    1337);
        }
    }
}
