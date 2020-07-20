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

package com.aurora.adroid.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.model.StaticRepo;
import com.aurora.adroid.service.SyncService;
import com.aurora.adroid.ui.generic.activity.BaseActivity;
import com.aurora.adroid.ui.generic.activity.ContainerActivity;
import com.aurora.adroid.ui.generic.activity.DownloadsActivity;
import com.aurora.adroid.ui.generic.activity.SearchActivity;
import com.aurora.adroid.ui.intro.IntroActivity;
import com.aurora.adroid.ui.setting.SettingsActivity;
import com.aurora.adroid.ui.view.Floaty;
import com.aurora.adroid.ui.view.MultiTextLayout;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.PrefUtil;
import com.aurora.adroid.util.Util;
import com.aurora.adroid.util.ViewUtil;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

public class AuroraActivity extends BaseActivity {
    @BindView(R.id.action1)
    AppCompatImageView action1;
    @BindView(R.id.multi_text_layout)
    MultiTextLayout multiTextLayout;
    @BindView(R.id.action2)
    AppCompatImageView action2;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.navigation)
    NavigationView navigation;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.floaty)
    Floaty floaty;


    private CompositeDisposable disposable = new CompositeDisposable();
    private int fragmentCur = 0;

    static boolean matchDestination(@NonNull NavDestination destination, @IdRes int destId) {
        NavDestination currentDestination = destination;
        while (currentDestination.getId() != destId && currentDestination.getParent() != null) {
            currentDestination = currentDestination.getParent();
        }
        return currentDestination.getId() == destId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (!DatabaseUtil.isDatabaseAvailable(this)) {
            showSyncDialog(false);
        } else if (DatabaseUtil.isDatabaseObsolete(this)) {
            showSyncDialog(true);
        }

        setupToolbar();
        setupSearch();
        setupDrawer();
        setupNavigation();

        checkPermissions();

        if (Util.isMiui(this) && !Util.isMiuiOptimizationDisabled()) {
            PrefUtil.putString(this, Constants.PREFERENCE_INSTALLATION_METHOD, "0");
        }

        onNewIntent(getIntent());
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START, true);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && StringUtils.isNotEmpty(intent.getDataString())) {
            String repoDataString = intent.getDataString();
            if (repoDataString.contains("fingerprint") || repoDataString.contains("FINGERPRINT")) {
                try {
                    final String[] stringArray = repoDataString.split("\\?");
                    final StaticRepo staticRepo = new StaticRepo();
                    staticRepo.setRepoName(Util.getDomainName(stringArray[0]));
                    staticRepo.setRepoId(String.valueOf(System.currentTimeMillis()));
                    staticRepo.setRepoUrl(stringArray[0]);
                    stringArray[1] = stringArray[1].replace("fingerprint=", "");
                    stringArray[1] = stringArray[1].replace("FINGERPRINT=", "");
                    staticRepo.setRepoFingerprint(stringArray[1]);
                    showAddRepoDialog(staticRepo);
                } catch (Exception ignored) {
                }
            } else if (intent.getData() != null
                    && intent.getData().getLastPathSegment() != null) {
                if (intent.getData().getLastPathSegment().equalsIgnoreCase("repo")) {
                    try {
                        final StaticRepo staticRepo = new StaticRepo();
                        staticRepo.setRepoName(intent.getData().getPath());
                        staticRepo.setRepoId(String.valueOf(staticRepo.getRepoName().hashCode()));
                        staticRepo.setRepoUrl(intent.getDataString());
                        showAddRepoDialog(staticRepo);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Util.toggleSoftInput(this, false);
        //Check & start notification service
        Util.startNotificationService(this);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        Util.toggleSoftInput(this, false);
    }

    @Override
    protected void onDestroy() {
        try {
            Glide.with(this).pauseAllRequests();
            disposable.clear();
            disposable.dispose();
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }

    private void setupToolbar() {
        multiTextLayout.setTxtPrimary(getString(R.string.string_aurora));
        multiTextLayout.setTxtSecondary(getString(R.string.string_droid));
    }

    private void setupSearch() {
        floaty.setClickListener(view -> {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(this, view, "transition");
            Intent intent = new Intent(this, SearchActivity.class);
            ActivityCompat.startActivity(this, intent, options.toBundle());
        });
    }

    private void setupNavigation() {
        int backGroundColor = ViewUtil.getStyledAttribute(this, android.R.attr.colorBackground);

        bottomNavigationView.setBackgroundColor(ColorUtils.setAlphaComponent(backGroundColor, 245));
        navigation.setBackgroundColor(backGroundColor);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_main);

        //Avoid Adding same fragment to NavController, if clicked on current BottomNavigation item
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == bottomNavigationView.getSelectedItemId())
                return false;
            NavigationUI.onNavDestinationSelected(item, navController);
            return true;
        });

        //Check correct BottomNavigation item, if nav_graph_main is done programmatically
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            final Menu menu = bottomNavigationView.getMenu();
            final int size = menu.size();
            for (int i = 0; i < size; i++) {
                MenuItem item = menu.getItem(i);
                if (matchDestination(destination, item.getItemId())) {
                    item.setChecked(true);
                }
            }
        });

        //Check default tab to open, if configured
        switch (fragmentCur) {
            case 0:
                navController.navigate(R.id.welcomeFragment);
                break;
            case 1:
                navController.navigate(R.id.permissionFragment);
                break;
            case 2:
                navController.navigate(R.id.repoFragment);
                break;
        }
    }

    private void setupDrawer() {
        action1.setOnClickListener(v -> {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START))
                drawerLayout.openDrawer(GravityCompat.START, true);
        });

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        navigation.setNavigationItemSelectedListener(item -> {
            Intent intent = new Intent(this, ContainerActivity.class);
            switch (item.getItemId()) {
                case R.id.action_all_apps:
                    intent.putExtra(Constants.FRAGMENT_NAME, Constants.FRAGMENT_INSTALLED);
                    startActivity(intent, ViewUtil.getEmptyActivityBundle(this));
                    break;
                case R.id.action_download:
                    startActivity(new Intent(this, DownloadsActivity.class),
                            ViewUtil.getEmptyActivityBundle(this));
                    break;
                case R.id.action_setting:
                    startActivity(new Intent(this, SettingsActivity.class),
                            ViewUtil.getEmptyActivityBundle(this));
                    break;
                case R.id.action_about:
                    intent.putExtra(Constants.FRAGMENT_NAME, Constants.FRAGMENT_ABOUT);
                    startActivity(intent, ViewUtil.getEmptyActivityBundle(this));
                    break;
                case R.id.action_favourite:
                    intent.putExtra(Constants.FRAGMENT_NAME, Constants.FRAGMENT_FAV_LIST);
                    startActivity(intent, ViewUtil.getEmptyActivityBundle(this));
                    break;
                case R.id.action_blacklist:
                    intent.putExtra(Constants.FRAGMENT_NAME, Constants.FRAGMENT_BLACKLIST);
                    startActivity(intent, ViewUtil.getEmptyActivityBundle(this));
                    break;
                case R.id.action_repository:
                    Intent introIntent = new Intent(this, IntroActivity.class);
                    introIntent.putExtra(Constants.INT_EXTRA, 3 /*TO MOVE DIRECTLY TO REPO*/);
                    startActivity(introIntent, ViewUtil.getEmptyActivityBundle(this));
                    break;
            }
            return false;
        });
    }

    protected void showSyncDialog(boolean obsolete) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.dialog_sync_title))
                .setMessage(obsolete ? getString(R.string.dialog_sync_desc_alt) : getString(R.string.dialog_sync_desc))
                .setPositiveButton(getString(R.string.dialog_sync_positive), (dialog, which) -> {
                    if (SyncService.isServiceRunning())
                        return;
                    startRepoSyncService();
                })
                .setNegativeButton(getString(R.string.dialog_sync_negative), (dialog, which) -> {
                    dialog.dismiss();
                });
        builder.create();
        builder.show();
    }

    protected void showAddRepoDialog(StaticRepo staticRepo) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.dialog_repo_title))
                .setMessage(StringUtils.joinWith(StringUtils.SPACE,
                        getString(R.string.dialog_repo_desc),
                        staticRepo.getRepoName(),
                        "?"))
                .setPositiveButton(getString(R.string.action_add), (dialog, which) -> {
                    new RepoListManager(this).addToRepoMap(staticRepo);
                })
                .setNegativeButton(getString(R.string.action_cancel), (dialog, which) -> {
                    dialog.dismiss();
                });
        builder.create();
        builder.show();
    }

    private void startRepoSyncService() {
        Intent intent = new Intent(this, SyncService.class);
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
                            Manifest.permission.ACCESS_NETWORK_STATE
                    },
                    1337);
        }
    }
}
