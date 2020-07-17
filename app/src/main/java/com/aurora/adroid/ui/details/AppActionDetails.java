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
import com.aurora.adroid.download.RequestBuilder;
import com.aurora.adroid.installer.AppInstaller;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;
import com.aurora.adroid.util.CertUtil;
import com.aurora.adroid.util.ContextUtil;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PackageUtil;
import com.aurora.adroid.util.PathUtil;
import com.aurora.adroid.util.Util;
import com.aurora.adroid.util.ViewUtil;
import com.google.android.material.button.MaterialButton;
import com.tonyodev.fetch2.AbstractFetchGroupListener;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchGroup;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.DownloadBlock;

import org.jetbrains.annotations.NotNull;

import java.io.File;
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

    public AppActionDetails(DetailsActivity activity, App app) {
        super(activity, app);
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

        fetch.getFetchGroup(hashCode, fetchGroup -> {
            if (fetchGroup.getGroupDownloadProgress() == 100) {
                final File file = new File(PathUtil.getApkPath(context, app.getPackageName(), app.getPkg().getVersionCode()));
                if (!isInstalled && file.exists()) {
                    btnPositive.setOnClickListener(installAppListener(file.getPath()));
                }
            } else if (fetchGroup.getDownloadingDownloads().size() > 0 || fetchGroup.getQueuedDownloads().size() > 0) {
                switchViews(true);
                fetch.addListener(getFetchListener());
            } else if (fetchGroup.getPausedDownloads().size() > 0) {
                isPaused = true;
                btnPositive.setOnClickListener(resumeAppListener());
            }
        });
    }

    private void runOrUpdate() {
        try {
            final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(app.getPackageName(), 0);
            final Package pkg = app.getPkg();
            final String RSA256 = CertUtil.getSHA256(activity, app.getPackageName());

            if (PackageUtil.isUpdatableVersion(context, pkg, packageInfo) && RSA256.equals(pkg.getSigner())) {
                btnPositive.setText(R.string.action_update);
            } else {
                btnPositive.setText(R.string.action_open);
                btnPositive.setOnClickListener(openAppListener());
            }
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

    private View.OnClickListener installAppListener(String filePath) {
        btnPositive.setText(R.string.action_install);
        return v -> {
            btnPositive.setText(R.string.action_installing);
            btnPositive.setEnabled(false);

            AppInstaller.getInstance(context)
                    .getDefaultInstaller()
                    .installApk(app.getPackageName(), filePath);
        };
    }

    private View.OnClickListener uninstallAppListener() {
        return v -> AppInstaller.getInstance(context)
                .getDefaultInstaller()
                .uninstall(app.getPackageName());
    }

    private View.OnClickListener resumeAppListener() {
        btnPositive.setText(R.string.download_resume);
        return v -> {
            switchViews(true);
            fetch.addListener(getFetchListener());
            fetch.resumeGroup(hashCode);
        };
    }

    private View.OnClickListener cancelDownloadListener() {
        return v -> {
            fetch.cancelGroup(hashCode);
            switchViews(false);
        };
    }

    private View.OnClickListener downloadAppListener() {
        boolean supportedPackage = PackageUtil.compatibleApi(app.getPkg().getNativecode());

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
        final Request request = RequestBuilder.buildRequest(context, app);
        final List<Request> requestList = new ArrayList<>();
        requestList.add(request);

        fetch.addListener(getFetchListener());
        fetch.enqueue(requestList, updatedRequestList ->
                Log.i("Downloading Apks : %s", app.getPackageName()));
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
                }
            }

            @Override
            public void onPaused(int groupId, @NotNull Download download, @NotNull FetchGroup fetchGroup) {
                if (groupId == hashCode) {
                    ContextUtil.runOnUiThread(() -> {
                        switchViews(false);
                        progressStatus.setText(R.string.download_paused);
                    });
                }
            }

            @Override
            public void onCompleted(int groupId, @NotNull Download download, @NotNull FetchGroup fetchGroup) {
                if (groupId == hashCode && fetchGroup.getGroupDownloadProgress() == 100) {
                    ContextUtil.runOnUiThread(() -> {
                        switchViews(false);
                        progressStatus.setText(R.string.download_completed);
                        btnPositive.setOnClickListener(installAppListener(download.getFile()));
                    });

                    if (Util.shouldAutoInstallApk(context)) {
                        ContextUtil.runOnUiThread(() -> {
                            btnPositive.setText(R.string.action_installing);
                            btnPositive.setEnabled(false);
                        });

                        AppInstaller.getInstance(context).getDefaultInstaller()
                                .installApk(app.getPackageName(), download.getFile());
                    }
                    fetch.removeListener(this);
                }
            }

            @Override
            public void onCancelled(int groupId, @NotNull Download download, @NotNull FetchGroup fetchGroup) {
                if (groupId == hashCode) {
                    ContextUtil.runOnUiThread(() -> {
                        switchViews(false);
                        progressBar.setIndeterminate(true);
                        progressStatus.setText(R.string.download_canceled);
                    });
                    fetch.removeListener(this);
                }
            }
        };
    }
}
