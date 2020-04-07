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

package com.aurora.adroid.ui.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.aurora.adroid.manager.FavouritesManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.ui.fragment.details.AppActionDetails;
import com.aurora.adroid.ui.fragment.details.AppInfoDetails;
import com.aurora.adroid.ui.fragment.details.AppLinkDetails;
import com.aurora.adroid.ui.fragment.details.AppPackages;
import com.aurora.adroid.ui.fragment.details.AppScreenshotsDetails;
import com.aurora.adroid.ui.fragment.details.AppSubInfoDetails;
import com.aurora.adroid.util.ContextUtil;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PackageUtil;
import com.aurora.adroid.viewmodel.DetailAppViewModel;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

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

    private String packageName;
    private String repoName;
    private DetailAppViewModel model;

    private App app;
    private AppActionDetails appActionDetails;
    private FavouritesManager favouritesManager;
    private CompositeDisposable disposable = new CompositeDisposable();

    private BroadcastReceiver localInstallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String packageName = intent.getStringExtra("PACKAGE_NAME");
            int statusCode = intent.getIntExtra("STATUS_CODE", -1);
            if (packageName != null && packageName.equals(app.getPackageName())) {
                ContextUtil.runOnUiThread(() -> drawButtons());
                clearNotification(context, packageName);
            }
            if (statusCode == 0)
                ContextUtil.toastLong(context, getString(R.string.installer_status_failure));
        }
    };

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
                        EventType eventEnum = event.getType();
                        switch (eventEnum) {
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
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_share:
                getShareIntent();
                return true;

            case R.id.action_favourites:
                if (favouritesManager.isFavourite(packageName)) {
                    favouritesManager.removeFromFavourites(packageName);
                    menuItem.setIcon(R.drawable.ic_fav);
                } else {
                    favouritesManager.addToFavourites(packageName);
                    menuItem.setIcon(R.drawable.ic_favourite_red);
                }
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.details_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(localInstallReceiver, new IntentFilter("ACTION_INSTALL"));
        registerReceiver(globalInstallReceiver, PackageUtil.getFilter());
    }

    @Override
    protected void onPause() {
        try {
            unregisterReceiver(localInstallReceiver);
            unregisterReceiver(globalInstallReceiver);
            appActionDetails = null;
            disposable.clear();
            disposable.dispose();
        } catch (Exception ignored) {
        }
        super.onPause();
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void draw(App mApp) {
        app = mApp;
        drawButtons();
        new AppInfoDetails(this, app).draw();
        new AppSubInfoDetails(this, app).draw();
        new AppLinkDetails(this, app).draw();
        new AppScreenshotsDetails(this, app).draw();
        new AppPackages(this, app).draw();
    }

    public void drawButtons() {
        appActionDetails = new AppActionDetails(this, app);
        appActionDetails.draw();
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
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, app.getName());
        i.putExtra(Intent.EXTRA_TEXT, Constants.APP_SHARE_URL + app.getPackageName());
        startActivity(Intent.createChooser(i, getString(R.string.action_share)));
    }

    private void clearNotification(Context context, String packageName) {
        final NotificationManager manager = (NotificationManager)
                context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null)
            manager.cancel(packageName, packageName.hashCode());
    }
}
