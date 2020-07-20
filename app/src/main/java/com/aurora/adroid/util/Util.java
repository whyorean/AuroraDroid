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

package com.aurora.adroid.util;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.service.BulkUpdateService;
import com.aurora.adroid.service.NotificationService;
import com.aurora.adroid.service.SyncService;
import com.aurora.adroid.ui.main.AuroraActivity;
import com.tonyodev.fetch2.Status;
import com.tonyodev.fetch2core.Downloader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Util {

    public static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static long parseLong(String intAsString, long defaultValue) {
        try {
            return Long.parseLong(intAsString);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static int parseInt(String intAsString, int defaultValue) {
        try {
            return Integer.parseInt(intAsString);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static void restartApp(Context context) {
        Intent intent = new Intent(context, AuroraActivity.class);
        int intentId = 1337;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, intentId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
            System.exit(0);
        }
    }

    public static int getColorAttribute(Context context, int styleID) {
        TypedArray arr = context.obtainStyledAttributes(new TypedValue().data, new int[]{styleID});
        int styledColor = arr.getColor(0, -1);
        arr.recycle();
        return styledColor;
    }

    @NonNull
    public static String getETAString(@NonNull final Context context, final long etaInMilliSeconds) {
        if (etaInMilliSeconds < 0) {
            return "";
        }
        int seconds = (int) (etaInMilliSeconds / 1000);
        long hours = seconds / 3600;
        seconds -= hours * 3600;
        long minutes = seconds / 60;
        seconds -= minutes * 60;
        if (hours > 0) {
            return context.getString(R.string.download_eta_hrs, hours, minutes, seconds);
        } else if (minutes > 0) {
            return context.getString(R.string.download_eta_min, minutes, seconds);
        } else {
            return context.getString(R.string.download_eta_sec, seconds);
        }
    }

    @NonNull
    public static String getDownloadSpeedString(@NonNull Context context, long downloadedBytesPerSecond) {
        if (downloadedBytesPerSecond < 0) {
            return "";
        }
        double kb = (double) downloadedBytesPerSecond / (double) 1000;
        double mb = kb / (double) 1000;
        final DecimalFormat decimalFormat = new DecimalFormat(".##");
        if (mb >= 1) {
            return context.getString(R.string.download_speed_mb, decimalFormat.format(mb));
        } else if (kb >= 1) {
            return context.getString(R.string.download_speed_kb, decimalFormat.format(kb));
        } else {
            return context.getString(R.string.download_speed_bytes, downloadedBytesPerSecond);
        }
    }


    public static String humanReadableByteSpeed(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format(Locale.getDefault(), "%.1f %sB/s",
                bytes / Math.pow(unit, exp), pre);
    }

    public static String humanReadableByteValue(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format(Locale.getDefault(), "%.1f %sB",
                bytes / Math.pow(unit, exp), pre);
    }

    public static boolean isEmpty(@Nullable CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static String nullIfEmpty(@Nullable String str) {
        return isEmpty(str) ? null : str;
    }

    public static String emptyIfNull(@Nullable String str) {
        return str == null ? "" : str;
    }

    public static void toggleSoftInput(Context context, boolean show) {
        IBinder windowToken = ((AuroraActivity) context).getWindow().getDecorView().getWindowToken();
        InputMethodManager inputMethodManager = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && windowToken != null)
            if (show)
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            else
                inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
    }

    public static String getDateFromMilli(Long timeInMilli) {
        try {
            final DateFormat dateFormat = new SimpleDateFormat("dd MMM yy", Locale.getDefault());
            final Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeInMilli);
            return dateFormat.format(calendar.getTime());
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    public static long getMilliFromDate(String data, long Default) {
        try {
            return Date.parse(data);
        } catch (Exception ignored) {

        }
        return Default;
    }

    public static boolean isFirstLaunch(Context context) {
        boolean first = getPrefs(context).getBoolean(Constants.PREFERENCE_FIRST_LAUNCH_2, true);
        PrefUtil.putBoolean(context, Constants.PREFERENCE_FIRST_LAUNCH_2, false);
        return first;
    }

    public static boolean shouldAutoInstallApk(Context context) {
        return getPrefs(context).getBoolean(Constants.PREFERENCE_INSTALLATION_AUTO, true);
    }

    public static boolean shouldDeleteApk(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && Util.isRootInstallEnabled(context)) {
            return true;
        } else
            return getPrefs(context).getBoolean(Constants.PREFERENCE_INSTALLATION_DELETE, true);
    }

    public static boolean isNativeInstallerEnforced(Context context) {
        return getPrefs(context).getBoolean(Constants.PREFERENCE_INSTALLATION_TYPE, false);
    }

    public static boolean isExperimentalUpdatesEnabled(Context context) {
        return getPrefs(context).getBoolean(Constants.PREFERENCE_UPDATES_EXPERIMENTAL, false);
    }

    public static boolean isSuggestedUpdatesEnabled(Context context) {
        return getPrefs(context).getBoolean(Constants.PREFERENCE_UPDATES_SUGGESTED, true);
    }

    public static boolean isRootInstallEnabled(Context context) {
        String installMethod = getPrefs(context).getString(Constants.PREFERENCE_INSTALLATION_METHOD, "0");
        return installMethod.equals("1");
    }

    public static ArrayList<String> getMirrorCheckedList(Context context) {
        return PrefUtil.getListString(context, Constants.PREFERENCE_MIRROR_CHECKED);
    }

    public static void putMirrorCheckedList(Context context, ArrayList<String> stringList) {
        PrefUtil.putListString(context, Constants.PREFERENCE_MIRROR_CHECKED, stringList);
    }

    public static boolean isMirrorChecked(Context context, String repoId) {
        ArrayList<String> arrayList = getMirrorCheckedList(context);
        return arrayList.contains(repoId);
    }

    public static boolean isCustomLocaleEnabled(Context context) {
        return getPrefs(context).getBoolean(Constants.PREFERENCE_LOCALE_CUSTOM, false);
    }

    public static List<String> arrayToList(String[] inputArray) {
        List<String> stringList = new ArrayList<>();
        Collections.addAll(stringList, inputArray);
        return stringList;
    }

    public static String getDomainName(String url) {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            if (domain.startsWith("www."))
                domain = domain.substring(4);
            return domain;
        } catch (URISyntaxException e) {
            return "aurora.repo";
        }
    }

    public static boolean verifyUrl(String url) {
        UrlValidator urlValidator = new UrlValidator();
        return urlValidator.isValid(url);
    }

    public static Proxy.Type getProxyType(Context context) {
        String proxyType = getPrefs(context).getString(Constants.PREFERENCE_PROXY_TYPE, "HTTP");
        switch (proxyType) {
            case "HTTP":
                return Proxy.Type.HTTP;
            case "SOCKS":
                return Proxy.Type.SOCKS;
            case "DIRECT":
                return Proxy.Type.DIRECT;
            default:
                return Proxy.Type.HTTP;
        }
    }

    public static boolean isDownloadWifiOnly(Context context) {
        return getPrefs(context).getBoolean(Constants.PREFERENCE_DOWNLOAD_WIFI, false);
    }

    public static int getActiveDownloadCount(Context context) {
        return getPrefs(context).getInt(Constants.PREFERENCE_DOWNLOAD_ACTIVE, 3);
    }

    public static String getInstallationProfile(Context context) {
        if (!Util.isRootInstallEnabled(context))
            return "0";
        else
            return getPrefs(context).getString(Constants.PREFERENCE_INSTALLATION_PROFILE, "0");
    }

    public static boolean isFetchDebugEnabled(Context context) {
        return getPrefs(context).getBoolean(Constants.PREFERENCE_DOWNLOAD_DEBUG, false);
    }

    public static boolean isNetworkProxyEnabled(Context context) {
        return getPrefs(context).getBoolean(Constants.PREFERENCE_ENABLE_PROXY, false);
    }

    public static boolean isPrivilegedInstall(Context context) {
        String prefValue = PrefUtil.getString(context, Constants.PREFERENCE_INSTALLATION_METHOD);
        switch (prefValue) {
            case "1":
            case "2":
                return true;
            default:
                return false;
        }
    }

    public static Proxy getNetworkProxy(Context context) {
        String proxyHost = getPrefs(context).getString(Constants.PREFERENCE_PROXY_HOST, "127.0.0.1");
        String proxyPort = getPrefs(context).getString(Constants.PREFERENCE_PROXY_PORT, "8118");
        int port = Util.parseInt(proxyPort, 8118);
        return new Proxy(getProxyType(context), new InetSocketAddress(proxyHost, port));
    }

    public static Downloader.FileDownloaderType getDownloadStrategy(Context context) {
        String prefValue = getPrefs(context).getString(Constants.PREFERENCE_DOWNLOAD_STRATEGY, "");
        return prefValue.equals("0")
                ? Downloader.FileDownloaderType.SEQUENTIAL
                : Downloader.FileDownloaderType.PARALLEL;
    }

    public static String getStatus(Status status) {
        switch (status) {
            case NONE:
                return "None";
            case ADDED:
                return "Added";
            case FAILED:
                return "Failed";
            case PAUSED:
                return "Paused";
            case QUEUED:
                return "Queued";
            case DELETED:
                return "Deleted";
            case REMOVED:
                return "Removed";
            case CANCELLED:
                return "Cancelled";
            case COMPLETED:
                return "Completed";
            case DOWNLOADING:
                return "Downloading";
            default:
                return "--";
        }
    }

    public static void copyToClipBoard(Context context, String dataToCopy) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Apk Url", dataToCopy);
        clipboard.setPrimaryClip(clip);
    }

    public static void clearOldInstallationSessions(Context context) {
        final PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        for (PackageInstaller.SessionInfo sessionInfo : packageInstaller.getMySessions()) {
            final int sessionId = sessionInfo.getSessionId();
            try {
                packageInstaller.abandonSession(sessionInfo.getSessionId());
                Log.i("Abandoned session id -> %d", sessionId);
            } catch (Exception e) {
                Log.e(e.getMessage());
            }
        }
    }

    public static void startBulkUpdateService(Context context) {
        try {
            if (!BulkUpdateService.isServiceRunning())
                context.startService(new Intent(context, BulkUpdateService.class));
        } catch (IllegalStateException e) {
            Log.e(e.getMessage());
        }
    }

    public static void stopBulkUpdateService(Context context) {
        try {
            if (BulkUpdateService.isServiceRunning())
                context.stopService(new Intent(context, BulkUpdateService.class));
        } catch (IllegalStateException e) {
            Log.e(e.getMessage());
        }
    }

    public static void stopSyncService(Context context) {
        try {
            if (SyncService.isServiceRunning())
                context.stopService(new Intent(context, SyncService.class));
        } catch (IllegalStateException e) {
            Log.e(e.getMessage());
        }
    }

    public static void startNotificationService(Context context) {
        try {
            if (NotificationService.isNotAvailable())
                context.startService(new Intent(context, NotificationService.class));
        } catch (IllegalStateException e) {
            Log.e(e.getMessage());
        }
    }


    public static boolean isMiui(Context context) {
        return StringUtils.isNotEmpty(getSystemProperty("ro.miui.ui.version.name"));
    }

    @SuppressLint("PrivateApi")
    public static boolean isMiuiOptimizationDisabled() {
        if ("0".equals(getSystemProperty("persist.sys.miui_optimization")))
            return true;

        try {
            return (boolean) Class.forName("android.miui.AppOpsUtils")
                    .getDeclaredMethod("isXOptMode")
                    .invoke(null);
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressLint("PrivateApi")
    @Nullable
    public static String getSystemProperty(String key) {
        try {
            return (String) Class.forName("android.os.SystemProperties")
                    .getDeclaredMethod("get", String.class)
                    .invoke(null, key);
        } catch (Exception e) {
            Log.e("Unable to read SystemProperties", e);
            return null;
        }
    }
}
