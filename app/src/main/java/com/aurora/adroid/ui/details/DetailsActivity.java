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

package com.aurora.adroid.ui.details;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.event.EventType;
import com.aurora.adroid.manager.BlacklistManager;
import com.aurora.adroid.manager.FavouritesManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.ui.generic.activity.BaseActivity;
import com.aurora.adroid.ui.generic.activity.DownloadsActivity;
import com.aurora.adroid.util.ContextUtil;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.ViewUtil;
import com.aurora.adroid.viewmodel.DetailAppViewModel;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DetailsActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.coordinator)
    CoordinatorLayout coordinator;

    private App app;
    private String packageName;
    private String repoName;
    private DetailAppViewModel model;

    private AppActionDetails appActionDetails;
    private AppPackages appPackages;
    private BlacklistManager blacklistManager;
    private FavouritesManager favouritesManager;
    private CompositeDisposable disposable = new CompositeDisposable();

    private BroadcastReceiver globalInstallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getData() == null || !TextUtils.equals(packageName, intent.getData().getSchemeSpecificPart())) {
                return;
            }
            ContextUtil.runOnUiThread(() -> drawButtons());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        setupActionBar();

        favouritesManager = new FavouritesManager(this);
        blacklistManager = new BlacklistManager(this);

        model = new ViewModelProvider(this).get(DetailAppViewModel.class);
        model.getLiveApp().observe(this, app -> {
            draw(app);
        });

        disposable.add(AuroraApplication.getRxBus()
                .getBus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event != null) {
                        final EventType eventType = event.getType();
                        switch (eventType) {
                            case SUB_DOWNLOAD_INITIATED:
                                if (event.getStringExtra().equals(app.getPackageName())) {
                                    drawButtons();
                                    ContextUtil.runOnUiThread(() -> notifyAction(getString(R.string.download_progress)));
                                }
                                break;
                            case DOWNLOAD_INITIATED:
                                ContextUtil.runOnUiThread(() -> notifyAction(getString(R.string.download_progress)));
                                break;
                            case DOWNLOAD_FAILED:
                                ContextUtil.runOnUiThread(() -> notifyAction(getString(R.string.download_failed)));
                                break;
                            case DOWNLOAD_CANCELLED:
                                ContextUtil.runOnUiThread(() -> notifyAction(getString(R.string.download_canceled)));
                                break;
                            case DOWNLOAD_COMPLETED:
                                ContextUtil.runOnUiThread(() -> notifyAction(getString(R.string.download_completed)));
                                break;
                        }

                        switch (event.getType()) {
                            case INSTALLED:
                            case UNINSTALLED:
                                drawButtons();
                                break;
                        }
                    }
                }));

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        packageName = getIntentPackageName(intent);
        stringExtra = intent.getStringExtra(Constants.STRING_EXTRA);
        repoName = intent.getStringExtra(Constants.STRING_REPO);

        if (TextUtils.isEmpty(packageName)) {
            Log.d("No package name provided");
            finishAfterTransition();
        } else {
            stringExtra = intent.getStringExtra(Constants.STRING_EXTRA);
            if (stringExtra != null) {
                app = gson.fromJson(stringExtra, App.class);
            }

            Log.i("Getting info about %s", packageName);
            model.getFullAppByPackageName(packageName, repoName);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(globalInstallReceiver);
            appActionDetails = null;
            appPackages = null;
            disposable.clear();
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_share:
                getShareIntent();
                return true;
            case R.id.action_favourites:
                if (StringUtils.isNotEmpty(packageName)) {
                    if (favouritesManager.isFavourite(packageName)) {
                        favouritesManager.removeFromFavourites(packageName);
                        menuItem.setIcon(R.drawable.ic_fav);
                    } else {
                        favouritesManager.addToFavourites(packageName);
                        menuItem.setIcon(R.drawable.ic_favourite_red);
                    }
                }
                return true;
            case R.id.action_downloads:
                final Intent intent = new Intent(this, DownloadsActivity.class);
                startActivity(intent, ViewUtil.getEmptyActivityBundle(this));
                return true;
            case R.id.action_blacklist:
                if (StringUtils.isNotEmpty(packageName)) {
                    if (blacklistManager.isBlacklisted(packageName)) {
                        blacklistManager.removeFromBlacklist(packageName);
                        menuItem.setTitle(getString(R.string.action_blacklist));
                    } else {
                        blacklistManager.addToBlacklist(packageName);
                        menuItem.setTitle(getString(R.string.action_whitelist));
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.details_main, menu);
        final MenuItem blackListMenu = menu.findItem(R.id.action_blacklist);
        if (StringUtils.isNotEmpty(packageName)) {
            blackListMenu.setTitle(blacklistManager.isBlacklisted(packageName)
                    ? R.string.action_whitelist
                    : R.string.action_blacklist);
        }
        return true;
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void draw(App app) {
        this.app = app;
        drawButtons();
        new AppInfoDetails(this, app).draw();
        new AppSubInfoDetails(this, app).draw();
        new AppLinkDetails(this, app).draw();
        new AppScreenshotsDetails(this, app).draw();
    }

    public void drawButtons() {
        appActionDetails = new AppActionDetails(this, app);
        appPackages = new AppPackages(this, app);
        appActionDetails.draw();
        appPackages.draw();
    }

    private void notifyAction(String message) {
        Snackbar snackbar = Snackbar.make(coordinator, message, Snackbar.LENGTH_LONG);
        snackbar.setDuration(BaseTransientBottomBar.LENGTH_SHORT);
        snackbar.show();
    }

    private String getIntentPackageName(Intent intent) {
        if (intent.hasExtra(Constants.INTENT_PACKAGE_NAME)) {
            return intent.getStringExtra(Constants.INTENT_PACKAGE_NAME);
        } else if (intent.getScheme() != null
                && intent.getScheme().equals("https")
                && intent.getData() != null) {
            Uri data = intent.getData();
            return data.getLastPathSegment();
        } else if (intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            return bundle.getString(Constants.INTENT_PACKAGE_NAME);
        }
        return null;
    }

    private void getShareIntent() {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, app.getName());
        intent.putExtra(Intent.EXTRA_TEXT, Constants.APP_SHARE_URL + app.getPackageName());
        startActivity(Intent.createChooser(intent, getString(R.string.action_share)));
    }
}
