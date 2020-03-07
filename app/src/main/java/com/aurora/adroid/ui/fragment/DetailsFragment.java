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

package com.aurora.adroid.ui.fragment;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.R;
import com.aurora.adroid.event.EventType;
import com.aurora.adroid.ui.fragment.details.AppActionDetails;
import com.aurora.adroid.ui.fragment.details.AppInfoDetails;
import com.aurora.adroid.ui.fragment.details.AppLinkDetails;
import com.aurora.adroid.ui.fragment.details.AppPackages;
import com.aurora.adroid.ui.fragment.details.AppScreenshotsDetails;
import com.aurora.adroid.ui.fragment.details.AppSubInfoDetails;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.ContextUtil;
import com.aurora.adroid.viewmodel.DetailAppViewModel;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DetailsFragment extends Fragment {

    private static final String ACTION_PACKAGE_REPLACED_NON_SYSTEM = "ACTION_PACKAGE_REPLACED_NON_SYSTEM";
    private static final String ACTION_PACKAGE_INSTALLATION_FAILED = "ACTION_PACKAGE_INSTALLATION_FAILED";
    private static final String ACTION_UNINSTALL_PACKAGE_FAILED = "ACTION_UNINSTALL_PACKAGE_FAILED";

    public static App app;

    @BindView(R.id.coordinator)
    CoordinatorLayout coordinatorLayout;

    private Context context;
    private String packageName;
    private String repoName;
    private CompositeDisposable disposable = new CompositeDisposable();
    private AppActionDetails appActionDetails;

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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        ButterKnife.bind(this, view);
        Bundle arguments = getArguments();
        if (arguments != null) {
            packageName = arguments.getString("PackageName");
            repoName = arguments.getString("RepoName");
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DetailAppViewModel viewModel = new ViewModelProvider(this).get(DetailAppViewModel.class);
        viewModel.getLiveApp().observe(getViewLifecycleOwner(), liveApps -> {
            draw(liveApps);
        });
        viewModel.getFullAppByPackageName(packageName, repoName);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context.registerReceiver(localInstallReceiver, new IntentFilter("ACTION_INSTALL"));
        context.registerReceiver(globalInstallReceiver, getFilter());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            context.unregisterReceiver(localInstallReceiver);
            context.unregisterReceiver(globalInstallReceiver);
            appActionDetails = null;
            disposable.clear();
            disposable.dispose();
        } catch (Exception ignored) {
        }
    }

    private void draw(App mApp) {
        app = mApp;
        drawButtons();
        new AppInfoDetails(this, app).draw();
        new AppSubInfoDetails(this, app).draw();
        new AppLinkDetails(this, app).draw();
        new AppScreenshotsDetails(this, app).draw();
    }

    public void drawButtons() {
        appActionDetails = new AppActionDetails(this, app);
        AppPackages appPackages = new AppPackages(this, app);
        appActionDetails.draw();
        appPackages.draw();
    }

    private void notifyAction(String message) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.setDuration(BaseTransientBottomBar.LENGTH_SHORT);
        snackbar.show();
    }

    private IntentFilter getFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("package");
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_INSTALL);
        filter.addAction(Intent.ACTION_UNINSTALL_PACKAGE);
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addAction(ACTION_PACKAGE_REPLACED_NON_SYSTEM);
        filter.addAction(ACTION_PACKAGE_INSTALLATION_FAILED);
        filter.addAction(ACTION_UNINSTALL_PACKAGE_FAILED);
        return filter;
    }

    private void clearNotification(Context context, String packageName) {
        NotificationManager notificationManager = (NotificationManager)
                context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancel(packageName.hashCode());
    }
}
