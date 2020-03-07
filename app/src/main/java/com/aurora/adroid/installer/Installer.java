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

package com.aurora.adroid.installer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import androidx.core.content.FileProvider;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.ui.activity.DetailsActivity;
import com.aurora.adroid.model.App;
import com.aurora.adroid.notification.QuickNotification;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PathUtil;
import com.aurora.adroid.util.PrefUtil;
import com.aurora.adroid.util.TextUtil;
import com.aurora.adroid.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Installer {

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
            install(app.getAppPackage().getApkName());
        else
            installSplit(app);
    }


    public AppInstallerAbstract getPackageInstaller() {
        return packageInstaller;
    }

    public void install(String apkName) {
        Log.i("Native Installer Called");
        Intent intent;
        File file = new File(PathUtil.getApkPath(context, apkName));
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
        String apkName = app.getAppPackage().getApkName();
        Log.e(apkName);
        List<File> apkFiles = new ArrayList<>();
        File apkDirectory = new File(PathUtil.getRootApkPath(context));
        for (File splitApk : apkDirectory.listFiles()) {
            if (splitApk.getPath().contains(new StringBuilder()
                    .append(apkName))) {
                apkFiles.add(splitApk);
            }
        }

        packageInstaller.addInstallationStatusListener((statusCode, intentPackageName) -> {
            final String status = getStatusString(statusCode);
            final App app1 = appHashMap.get(intentPackageName);
            final String displayName = (app1 != null)
                    ? TextUtil.emptyIfNull(app.getName())
                    : TextUtil.emptyIfNull(intentPackageName);

            Log.i("Package Installer -> %s : %s", displayName, TextUtil.emptyIfNull(status));

            if (app1 != null)
                clearNotification(intentPackageName);

            switch (statusCode) {
                case PackageInstaller.STATUS_FAILURE:
                case PackageInstaller.STATUS_FAILURE_ABORTED:
                case PackageInstaller.STATUS_FAILURE_BLOCKED:
                case PackageInstaller.STATUS_FAILURE_CONFLICT:
                case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                case PackageInstaller.STATUS_FAILURE_INVALID:
                case PackageInstaller.STATUS_FAILURE_STORAGE:
                    QuickNotification.show(
                            context,
                            displayName,
                            status,
                            getContentIntent(intentPackageName));
                    checkAndProcessQueuedApps();
                    break;
                case PackageInstaller.STATUS_SUCCESS:
                    QuickNotification.show(
                            context,
                            displayName,
                            status,
                            getContentIntent(intentPackageName));
                    if (app1 != null) {
                        PathUtil.deleteApkFile(context, intentPackageName);
                        appHashMap.remove(intentPackageName);
                    }
                    checkAndProcessQueuedApps();
                    break;
            }
        });
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

    private AppInstallerAbstract getInstallationMethod(Context context) {
        String prefValue = PrefUtil.getString(context, Constants.PREFERENCE_INSTALLATION_METHOD);
        switch (prefValue) {
            case "0":
                return AppInstaller.getInstance(context);
            case "1":
                return AppInstallerRooted.getInstance(context);
            case "2":
                return AppInstallerPrivileged.getInstance(context);
            default:
                return AppInstaller.getInstance(context);
        }
    }

    private void clearNotification(String packageName) {
        NotificationManager notificationManager = (NotificationManager)
                context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(packageName.hashCode());
    }

    private PendingIntent getContentIntent(String packageName) {
        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra("INTENT_PACKAGE_NAME", packageName);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
}
