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

package com.aurora.adroid.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.aurora.adroid.R;
import com.aurora.adroid.activity.DetailsActivity;
import com.aurora.adroid.notification.QuickNotification;
import com.aurora.adroid.util.PackageUtil;
import com.aurora.adroid.util.PathUtil;
import com.aurora.adroid.util.Util;

public class DetailsInstallReceiver extends BroadcastReceiver {

    static public final String ACTION_PACKAGE_REPLACED_NON_SYSTEM = "ACTION_PACKAGE_REPLACED_NON_SYSTEM";
    static public final String ACTION_PACKAGE_INSTALLATION_FAILED = "ACTION_PACKAGE_INSTALLATION_FAILED";
    static public final String ACTION_UNINSTALL_PACKAGE_FAILED = "ACTION_UNINSTALL_PACKAGE_FAILED";

    private String packageName;

    public DetailsInstallReceiver(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getData() == null || !TextUtils.equals(packageName, intent.getData().getSchemeSpecificPart())) {
            return;
        }
        if (context instanceof DetailsActivity)
            ((DetailsActivity) context).redrawButtons();

        String action = intent.getAction();
        assert action != null;
        if (action.equals(Intent.ACTION_PACKAGE_ADDED)
                || action.equals(Intent.ACTION_PACKAGE_REPLACED)
                || action.equals(Intent.ACTION_PACKAGE_INSTALL)) {
            clearNotification(context, packageName);
            QuickNotification.show(
                    context,
                    PackageUtil.getAppDisplayName(context, packageName),
                    context.getString(R.string.notification_installation_complete),
                    getContentIntent(context, packageName));
            if (Util.shouldDeleteApk(context))
                PathUtil.deleteApkFile(context, packageName);
        }

        if (action.equals(Intent.ACTION_PACKAGE_FULLY_REMOVED) || action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
            QuickNotification.show(
                    context,
                    PackageUtil.getAppDisplayName(context, packageName),
                    context.getString(R.string.notification_uninstallation_complete),
                    getContentIntent(context, packageName));
        }

        if (action.equals(Intent.ACTION_INSTALL_FAILURE)) {
            clearNotification(context, packageName);
            QuickNotification.show(
                    context,
                    PackageUtil.getAppDisplayName(context, packageName),
                    context.getString(R.string.notification_installation_failed),
                    getContentIntent(context, packageName));
        }
    }

    public IntentFilter getFilter() {
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

    private PendingIntent getContentIntent(Context context, String packageName) {
        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra("INTENT_PACKAGE_NAME", packageName);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void clearNotification(Context context, String packageName) {
        NotificationManager notificationManager = (NotificationManager)
                context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(packageName.hashCode());
    }
}
