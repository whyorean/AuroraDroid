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
import com.aurora.adroid.fragment.DetailsFragment;
import com.aurora.adroid.installer.Installer;
import com.aurora.adroid.model.App;
import com.aurora.adroid.notification.GeneralNotification;
import com.aurora.adroid.util.ContextUtil;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PackageUtil;
import com.aurora.adroid.util.PathUtil;
import com.aurora.adroid.util.Util;
import com.aurora.adroid.util.ViewUtil;
import com.google.android.material.button.MaterialButton;
import com.tonyodev.fetch2.AbstractFetchGroupListener;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.EnqueueAction;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchGroup;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.DownloadBlock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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

    private boolean isPaused = false;
    private int hashCode;

    private Fetch fetch;
    private FetchListener fetchListener;
    private GeneralNotification notification;

    public AppActionDetails(DetailsFragment fragment, App app) {
        super(fragment, app);
    }

    @Override
    public void draw() {
        boolean isInstalled = PackageUtil.isInstalled(context, app.getPackageName());
        hashCode = app.getPackageName().hashCode();
        ViewUtil.setVisibility(btnNegative, isInstalled);
        btnNegative.setOnClickListener(uninstallAppListener());
        btnPositive.setOnClickListener(downloadAppListener());
        btnCancel.setOnClickListener(cancelDownloadListener());

        if (isInstalled)
            runOrUpdate();

        fetch = DownloadManager.getFetchInstance(context);
        notification = new GeneralNotification(context, app);

        fetch.getFetchGroup(hashCode, fetchGroup -> {
            if (fetchGroup.getGroupDownloadProgress() == 100) {
                if (!app.isInstalled() && PathUtil.fileExists(context, app.getAppPackage().getApkName()))
                    btnPositive.setOnClickListener(installAppListener());
            } else if (fetchGroup.getDownloadingDownloads().size() > 0) {
                switchViews(true);
                fetchListener = getFetchListener();
                fetch.addListener(fetchListener);
            } else if (fetchGroup.getPausedDownloads().size() > 0) {
                isPaused = true;
                btnPositive.setOnClickListener(resumeAppListener());
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
            fetch.resumeGroup(hashCode);
        };
    }

    private View.OnClickListener cancelDownloadListener() {
        return v -> {
            fetch.cancelGroup(hashCode);
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
                fetch.deleteGroup(hashCode);
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
        final Request request = new Request(DatabaseUtil.getDownloadURl(app), PathUtil.getApkPath(context, apkName));
        request.setEnqueueAction(EnqueueAction.REPLACE_EXISTING);
        request.setGroupId(app.getPackageName().hashCode());
        request.setTag(app.getPackageName());

        List<Request> requestList = new ArrayList<>();
        requestList.add(request);

        fetchListener = getFetchListener();
        fetch.addListener(fetchListener);

        fetch.enqueue(requestList, updatedRequestList ->
                Log.i("Downloading Apks : %s", app.getPackageName()));

        //Add <PackageName,DisplayName> and <PackageName,IconURL> to PseudoMaps
        PackageUtil.addToPseudoPackageMap(context, app.getPackageName(), app.getName());
        PackageUtil.addToPseudoURLMap(context, app.getPackageName(), DatabaseUtil.getImageUrl(app));
    }

    private FetchListener getFetchListener() {
        return new AbstractFetchGroupListener() {

            @Override
            public void onQueued(int groupId, @NotNull Download download, boolean waitingNetwork, @NotNull FetchGroup fetchGroup) {
                if (groupId == hashCode) {
                    ContextUtil.runOnUiThread(() -> {
                        progressBar.setIndeterminate(true);
                        progressStatus.setText(R.string.download_queued);
                    });
                    notification.notifyQueued();
                }
            }

            @Override
            public void onStarted(int groupId, @NotNull Download download, @NotNull List<? extends DownloadBlock> downloadBlocks, int totalBlocks, @NotNull FetchGroup fetchGroup) {
                if (groupId == hashCode) {
                    ContextUtil.runOnUiThread(() -> {
                        progressBar.setIndeterminate(true);
                        progressStatus.setText(R.string.download_queued);
                        switchViews(true);
                    });
                }
            }

            @Override
            public void onResumed(int groupId, @NotNull Download download, @NotNull FetchGroup fetchGroup) {
                if (groupId == hashCode) {
                    int progress = fetchGroup.getGroupDownloadProgress();
                    if (progress < 0)
                        progress = 0;
                    notification.notifyProgress(progress, 0, hashCode);
                    ContextUtil.runOnUiThread(() -> {
                        progressStatus.setText(R.string.download_progress);
                        progressBar.setIndeterminate(false);
                    });
                }
            }

            @Override
            public void onProgress(int groupId, @NotNull Download download, long etaInMilliSeconds, long downloadedBytesPerSecond, @NotNull FetchGroup fetchGroup) {
                if (groupId == hashCode) {
                    final int progress = fetchGroup.getGroupDownloadProgress();
                    ContextUtil.runOnUiThread(() -> {
                        btnCancel.setVisibility(View.VISIBLE);
                        //Set intermediate to false, just in case xD
                        if (progressBar.isIndeterminate())
                            progressBar.setIndeterminate(false);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            progressBar.setProgress(progress, true);
                        } else
                            progressBar.setProgress(progress);
                        progressStatus.setText(R.string.download_progress);
                        progressTxt.setText(new StringBuilder().append(progress).append("%"));
                    });
                    notification.notifyProgress(progress, downloadedBytesPerSecond, hashCode);
                }
            }

            @Override
            public void onPaused(int groupId, @NotNull Download download, @NotNull FetchGroup fetchGroup) {
                if (groupId == hashCode) {
                    notification.notifyResume(hashCode);
                    ContextUtil.runOnUiThread(() -> {
                        switchViews(false);
                        progressStatus.setText(R.string.download_paused);
                    });
                }
            }

            @Override
            public void onError(int groupId, @NotNull Download download, @NotNull Error error, @Nullable Throwable throwable, @NotNull FetchGroup fetchGroup) {
                if (groupId == hashCode) {
                    notification.notifyFailed();
                }
            }

            @Override
            public void onCompleted(int groupId, @NotNull Download download, @NotNull FetchGroup fetchGroup) {
                if (groupId == hashCode && fetchGroup.getGroupDownloadProgress() == 100) {
                    notification.notifyCompleted();
                    ContextUtil.runOnUiThread(() -> {
                        switchViews(false);
                        progressStatus.setText(R.string.download_completed);
                        btnPositive.setOnClickListener(installAppListener());
                    });

                    if (Util.shouldAutoInstallApk(context)) {
                        ContextUtil.runOnUiThread(() -> {
                            btnPositive.setText(R.string.action_installing);
                            btnPositive.setEnabled(false);
                        });
                        //Call the installer
                        new Installer(context).install(app);
                    }
                    if (fetchListener != null) {
                        fetch.removeListener(fetchListener);
                        fetchListener = null;
                    }
                }
            }

            @Override
            public void onCancelled(int groupId, @NotNull Download download, @NotNull FetchGroup fetchGroup) {
                if (groupId == hashCode) {
                    notification.notifyCancelled();
                    ContextUtil.runOnUiThread(() -> {
                        switchViews(false);
                        progressBar.setIndeterminate(true);
                        progressStatus.setText(R.string.download_canceled);
                    });
                }
            }
        };
    }
}
