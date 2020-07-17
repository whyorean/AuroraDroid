package com.aurora.adroid.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.EventType;
import com.aurora.adroid.util.PathUtil;
import com.aurora.adroid.util.Util;

import java.io.File;

public class PackageManagerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && intent.getData() != null) {
                String packageName = intent.getData().getEncodedSchemeSpecificPart();
                switch (action) {
                    case Intent.ACTION_PACKAGE_ADDED:
                        AuroraApplication.rxNotify(new Event(EventType.INSTALLED, packageName));
                        if (Util.shouldDeleteApk(context))
                            delete(context, packageName);
                        break;
                    case Intent.ACTION_PACKAGE_REMOVED:
                        AuroraApplication.rxNotify(new Event(EventType.UNINSTALLED, packageName));
                        break;
                }
                //Clear notification
                clearNotification(context, packageName);
            }
        }
    }

    private void clearNotification(Context context, String packageName) {
        final Object object = context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationManager notificationManager = (NotificationManager) object;
        if (notificationManager != null)
            notificationManager.cancel(packageName, packageName.hashCode());
    }

    private void delete(Context context, String packageName) {
        File[] files = new File(PathUtil.getRootApkPath(context)).listFiles();
        if (files == null)
            return;
        for (File file : files)
            if (file.getName().startsWith(packageName))
                file.delete();
    }
}
