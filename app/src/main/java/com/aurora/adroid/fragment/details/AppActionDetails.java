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

package com.aurora.adroid.fragment.details;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.aurora.adroid.R;
import com.aurora.adroid.download.DownloadManager;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.Events;
import com.aurora.adroid.event.RxBus;
import com.aurora.adroid.fragment.DetailsFragment;
import com.aurora.adroid.installer.Installer;
import com.aurora.adroid.model.App;
import com.aurora.adroid.notification.GeneralNotification;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PackageUtil;
import com.aurora.adroid.util.PathUtil;
import com.aurora.adroid.util.Util;
import com.aurora.adroid.util.ViewUtil;
import com.google.android.material.button.MaterialButton;
import com.tonyodev.fetch2.AbstractFetchListener;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.EnqueueAction;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.DownloadBlock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import butterknife.BindView;

public class AppActionDetails extends AbstractDetails {

    @BindView(R.id.btn_positive)
    MaterialButton btnPositive;
    @BindView(R.id.btn_negative)
    MaterialButton btnNegative;
    @BindView(R.id.viewSwitcher)
    ViewSwitcher mViewSwitcher;
    @BindView(R.id.view1)
    LinearLayout actions_layout;
    @BindView(R.id.view2)
    LinearLayout progress_layout;
    @BindView(R.id.progress_download)
    ProgressBar progressBar;
    @BindView(R.id.txt_progress)
    TextView progressTxt;
    @BindView(R.id.txt_status)
    TextView progressStatus;
    @BindView(R.id.btn_cancel)
    ImageButton btnCancel;

    private Fetch fetch;
    private FetchListener fetchListener;
    private Request request;
    private GeneralNotification notification;
    private int requestId;
    private boolean isPaused;

    public AppActionDetails(DetailsFragment fragment, App app) {
        super(fragment, app);
    }

    @Override
    public void draw() {
        boolean installed = PackageUtil.isInstalled(context, app.getPackageName());
        ViewUtil.setVisibility(btnNegative, installed);
        btnNegative.setOnClickListener(uninstallAppListener());
        btnPositive.setOnClickListener(downloadAppListener());
        btnCancel.setOnClickListener(cancelDownloadListener());
        app.setInstalled(PackageUtil.isInstalled(context, app.getPackageName()));

        if (installed)
            runOrUpdate();

        fetch = DownloadManager.getFetchInstance(context);
        notification = new GeneralNotification(context, app);

        fetch.getDownloads(downloadList -> {
            for (Download download : downloadList) {
                if (download.getTag() != null && download.getTag().equals(app.getPackageName())) {
                    request = download.getRequest();
                    requestId = download.getId();
                    switch (download.getStatus()) {
                        case COMPLETED:
                            if (!installed && PathUtil.fileExists(context, app.getAppPackage().getApkName()))
                                btnPositive.setOnClickListener(installAppListener());
                            break;
                        case DOWNLOADING:
                        case QUEUED: {
                            switchViews(true);
                            fetch.addListener(fetchListener = getFetchListener());
                        }
                        break;
                        case PAUSED: {
                            isPaused = true;
                            btnPositive.setOnClickListener(resumeAppListener());
                        }
                        break;
                    }
                }
            }
        });
    }

    private void runOrUpdate() {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(app.getPackageName(), 0);
            if (info.versionName.compareTo(app.getAppPackage().getVersionName()) >= 0
                    && info.versionCode >= app.getAppPackage().getVersionCode()) {
                btnPositive.setText(R.string.action_open);
                btnPositive.setOnClickListener(openAppListener());
                return;
            } else if (PathUtil.fileExists(context, app.getAppPackage().getApkName())) {
                btnPositive.setOnClickListener(installAppListener());
            }
            btnPositive.setText(R.string.action_update);
        } catch (PackageManager.NameNotFoundException ignored) {
        }
    }

    private View.OnClickListener openAppListener() {
        btnPositive.setText(R.string.action_open);
        return v -> {
            Intent i = getLaunchIntent();
            if (null != i) {
                try {
                    context.startActivity(i);
                } catch (ActivityNotFoundException e) {
                    Log.e(e.getMessage());
                }
            }
        };
    }

    private View.OnClickListener installAppListener() {
        btnPositive.setText(R.string.action_install);
        return v -> {
            btnPositive.setText(R.string.action_installing);
            btnPositive.setEnabled(false);
            new Installer(context).install(app);
        };
    }

    private View.OnClickListener uninstallAppListener() {
        return v -> new Installer(context).uninstall(app);
    }

    private View.OnClickListener resumeAppListener() {
        btnPositive.setText(R.string.download_resume);
        return v -> {
            switchViews(true);
            fetchListener = getFetchListener();
            fetch.addListener(fetchListener);
            fetch.resume(requestId);
        };
    }

    private View.OnClickListener cancelDownloadListener() {
        return v -> {
            fetch.cancel(request.getId());
            if (notification != null)
                notification.notifyCancelled();
            switchViews(false);
        };
    }

    private View.OnClickListener downloadAppListener() {
        boolean supportedPackage = PackageUtil.isSupportedPackage(app.getAppPackage());
        if (!supportedPackage) {
            btnPositive.setText(R.string.action_unsupported);
            btnPositive.setEnabled(false);
            btnPositive.setStrokeColor(ColorStateList.valueOf(Color.TRANSPARENT));
            return null;
        }
        btnPositive.setText(R.string.action_download);
        btnPositive.setEnabled(true);
        return v -> {
            switchViews(true);
            //Remove any previous requests
            if (!isPaused)
                fetch.delete(requestId);
            initDownload();
        };
    }

    private void switchViews(boolean showDownloads) {
        if (mViewSwitcher.getCurrentView() == actions_layout && showDownloads)
            mViewSwitcher.showNext();
        else if (mViewSwitcher.getCurrentView() == progress_layout && !showDownloads)
            mViewSwitcher.showPrevious();
    }

    private Intent getLaunchIntent() {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(app.getPackageName());
        boolean isTv = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isTv();
        if (isTv) {
            Intent l = context.getPackageManager()
                    .getLeanbackLaunchIntentForPackage(app.getPackageName());
            if (null != l) {
                intent = l;
            }
        }
        if (intent == null) {
            return null;
        }
        intent.addCategory(isTv ? Intent.CATEGORY_LEANBACK_LAUNCHER : Intent.CATEGORY_LAUNCHER);
        return intent;
    }

    private boolean isTv() {
        int uiMode = context.getResources().getConfiguration().uiMode;
        return (uiMode & Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    private void initDownload() {
        final String apkName = app.getAppPackage().getApkName();
        request = new Request(DatabaseUtil.getDownloadURl(app), PathUtil.getApkPath(context, apkName));
        request.setEnqueueAction(EnqueueAction.REPLACE_EXISTING);
        request.setTag(app.getPackageName());
        fetch.addListener(getFetchListener());
        if (isPaused)
            fetch.remove(requestId);
        else
            fetch.enqueue(request, updatedRequest -> Log.i("Downloading : %s",
                    app.getPackageName()), error -> Log.e("Unknown error occurred"));

        PackageUtil.addToPseudoPackageMap(context, app.getPackageName(), app.getName());
        PackageUtil.addToPseudoURLMap(context, app.getPackageName(), DatabaseUtil.getImageUrl(app));
    }

    private FetchListener getFetchListener() {
        return new AbstractFetchListener() {

            @Override
            public void onStarted(@NotNull Download download,
                                  @NotNull List<? extends DownloadBlock> list, int i) {
                if (download.getId() == request.getId()) {
                    progressBar.setIndeterminate(false);
                    switchViews(true);
                    progressStatus.setText(R.string.download_queued);
                }
            }

            @Override
            public void onResumed(@NotNull Download download) {
                if (download.getId() == request.getId()) {
                    int progress = download.getProgress();
                    if (progress < 0)
                        progress = 0;
                    notification.notifyProgress(progress, 0,
                            request.getId());
                    progressBar.setIndeterminate(false);
                }
            }

            @Override
            public void onQueued(@NotNull Download download, boolean waitingOnNetwork) {
                if (waitingOnNetwork)
                    Log.d("Waiting on network");
                if (download.getId() == request.getId()) {
                    notification.notifyQueued();
                    progressStatus.setText(R.string.download_queued);
                }
            }

            @Override
            public void onProgress(@NotNull Download download, long etaInMilliSeconds,
                                   long downloadedBytesPerSecond) {
                if (download.getId() == request.getId()) {
                    int progress = download.getProgress();
                    if (progress < 0)
                        progress = 0;
                    btnCancel.setVisibility(View.VISIBLE);
                    notification.notifyProgress(progress, downloadedBytesPerSecond,
                            request.getId());
                    //Set intermediate to false, just in case xD
                    if (progressBar.isIndeterminate())
                        progressBar.setIndeterminate(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressBar.setProgress(progress, true);
                    } else
                        progressBar.setProgress(progress);
                    progressStatus.setText(R.string.download_progress);
                    progressTxt.setText(new StringBuilder().append(progress).append("%"));
                }
            }

            @Override
            public void onPaused(@NotNull Download download) {
                if (download.getId() == request.getId()) {
                    notification.notifyResume(request.getId());
                    progressStatus.setText(R.string.download_paused);
                }
            }

            @Override
            public void onCompleted(@NotNull Download download) {
                if (download.getId() == request.getId()) {
                    notification.notifyCompleted();
                    progressStatus.setText(R.string.download_completed);
                    switchViews(false);
                    btnPositive.setOnClickListener(installAppListener());

                    // Check for AutoInstall & Disable InstallButton
                    if (Util.shouldAutoInstallApk(context)) {
                        btnPositive.setText(R.string.action_installing);
                        btnPositive.setEnabled(false);
                        new Installer(context).install(app.getAppPackage().getApkName());
                    }
                    RxBus.publish(new Event(Events.DOWNLOAD_COMPLETED));
                }
            }

            @Override
            public void onCancelled(@NotNull Download download) {
                if (download.getId() == request.getId()) {
                    notification.notifyCancelled();
                    progressBar.setIndeterminate(true);
                    progressStatus.setText(R.string.download_canceled);
                    switchViews(false);
                    RxBus.publish(new Event(Events.DOWNLOAD_CANCELLED));
                }
            }

            @Override
            public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
                super.onError(download, error, throwable);
                if (download.getId() == request.getId()) {
                    RxBus.publish(new Event(Events.DOWNLOAD_FAILED));
                }
            }
        };
    }
}
