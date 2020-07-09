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

package com.aurora.adroid.installer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.EventType;
import com.aurora.adroid.model.App;
import com.aurora.adroid.notification.QuickNotification;
import com.aurora.adroid.ui.activity.DetailsActivity;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PathUtil;
import com.aurora.adroid.util.PrefUtil;
import com.aurora.adroid.util.TextUtil;
import com.aurora.adroid.util.Util;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Installer implements AppInstallerAbstract.InstallationStatusListener {

    private Context context;
    private Map<String, App> appHashMap = new HashMap<>();
    private AppInstallerAbstract packageInstaller;
    private List<App> installationQueue = new ArrayList<>();

    private boolean isInstalling = false;
    private boolean isWaiting = false;

    public Installer(Context context) {
        this.context = context;
        packageInstaller = getInstallationMethod(context.getApplicationContext());
    }

    public AppInstallerAbstract getPackageInstaller() {
        return packageInstaller;
    }

    public void install(App app) {
        appHashMap.put(app.getPackageName(), app);
        installationQueue.add(app);

        if (isInstalling)
            isWaiting = true;
        else
            processApp(app);
    }

    private void processApp(App app) {
        isInstalling = true;
        installationQueue.remove(app);

        if (Util.isNativeInstallerEnforced(context))
            install(app.getPackageName(), app.getPkg().getVersionCode());
        else
            installSplit(app);
    }

    public void install(String packageName, long versionCode) {
        Log.i("Native Installer Called");
        Intent intent;
        final File file = new File(PathUtil.getApkPath(context, packageName, versionCode));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(FileProvider.getUriForFile(context, "com.aurora.adroid.fileProvider", file));
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void installSplit(App app) {
        Log.i("Split Installer Called");
        final String packageName = app.getPackageName();
        final List<File> apkFiles = new ArrayList<>();
        final File apkDirectory = new File(PathUtil.getRootApkPath(context));

        for (File splitApk : apkDirectory.listFiles()) {
            if (splitApk.getPath().contains(packageName)) {
                apkFiles.add(splitApk);
            }
        }

        packageInstaller.addInstallationStatusListener(this);
        AsyncTask.execute(() -> packageInstaller.installApkFiles(app.getPackageName(), apkFiles));
    }

    private void checkAndProcessQueuedApps() {
        if (installationQueue.isEmpty()) {
            isWaiting = false;
            isInstalling = false;
        }

        if (isWaiting)
            processApp(installationQueue.get(0));
    }

    private void clearNotification(String packageName) {
        final Object object = context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationManager notificationManager = (NotificationManager) object;
        if (notificationManager != null)
            notificationManager.cancel(packageName, packageName.hashCode());
    }

    private void sendStatusBroadcast(String packageName, int status) {
        AuroraApplication.rxNotify(new Event(EventType.INSTALLED, packageName, status));
    }

    private PendingIntent getContentIntent(String packageName) {
        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra(Constants.INTENT_PACKAGE_NAME, packageName);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private AppInstallerAbstract getInstallationMethod(Context context) {
        String prefValue = PrefUtil.getString(context, Constants.PREFERENCE_INSTALLATION_METHOD);
        switch (prefValue) {
            case "1":
                return AppInstallerRooted.getInstance(context);
            case "2":
                return AppInstallerPrivileged.getInstance(context);
            default:
                return AppInstaller.getInstance(context);
        }
    }

    private String getStatusString(int status) {
        switch (status) {
            case PackageInstaller.STATUS_FAILURE:
                return context.getString(R.string.installer_status_failure);
            case PackageInstaller.STATUS_FAILURE_ABORTED:
                return context.getString(R.string.installer_status_failure_aborted);
            case PackageInstaller.STATUS_FAILURE_BLOCKED:
                return context.getString(R.string.installer_status_failure_blocked);
            case PackageInstaller.STATUS_FAILURE_CONFLICT:
                return context.getString(R.string.installer_status_failure_conflict);
            case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                return context.getString(R.string.installer_status_failure_incompatible);
            case PackageInstaller.STATUS_FAILURE_INVALID:
                return context.getString(R.string.installer_status_failure_invalid);
            case PackageInstaller.STATUS_FAILURE_STORAGE:
                return context.getString(R.string.installer_status_failure_storage);
            case PackageInstaller.STATUS_PENDING_USER_ACTION:
                return context.getString(R.string.installer_status_user_action);
            case PackageInstaller.STATUS_SUCCESS:
                return context.getString(R.string.installer_status_success);
            default:
                return context.getString(R.string.installer_status_unknown);
        }
    }

    @Override
    public void onStatusChanged(int status, @Nullable String packageName) {
        final String statusMessage = getStatusString(status);
        final App app = appHashMap.get(packageName);

        String displayName = (app != null)
                ? TextUtil.emptyIfNull(app.getName())
                : TextUtil.emptyIfNull(packageName);

        if (StringUtils.isEmpty(displayName))
            displayName = context.getString(R.string.app_name);

        Log.i("Package Installer -> %s : %s", displayName, TextUtil.emptyIfNull(statusMessage));

        if (packageName != null)
            clearNotification(packageName);

        if (status == PackageInstaller.STATUS_SUCCESS) {
            sendStatusBroadcast(packageName, 1);
            if (app != null && Util.shouldDeleteApk(context)) {
                PathUtil.deleteApkFile(context, packageName);
            }
        } else {
            sendStatusBroadcast(packageName, 0);
        }

        QuickNotification.show(
                context,
                displayName,
                statusMessage,
                getContentIntent(packageName));

        appHashMap.remove(packageName);
        checkAndProcessQueuedApps();
    }
}
