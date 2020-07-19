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

package com.aurora.adroid.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.download.DownloadManager;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.EventType;
import com.aurora.adroid.event.LogEvent;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.manager.RepoSyncManager;
import com.aurora.adroid.model.StaticRepo;
import com.aurora.adroid.task.CheckRepoUpdatesTask;
import com.aurora.adroid.task.ExtractRepoTask;
import com.aurora.adroid.task.JsonParserTask;
import com.aurora.adroid.ui.main.AuroraActivity;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PathUtil;
import com.tonyodev.fetch2.AbstractFetchGroupListener;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchGroup;
import com.tonyodev.fetch2.Request;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SyncService extends Service {

    public static SyncService instance = null;

    private Fetch fetch;
    private AbstractFetchGroupListener fetchListener;
    private RepoListManager repoListManager;
    private CompositeDisposable disposable = new CompositeDisposable();

    public static boolean isServiceRunning() {
        try {
            return instance != null && instance.isRunning();
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        repoListManager = new RepoListManager(this);
        startForeground(1337, getNotificationBuilder().build());
        fetchRepo();
    }

    private void sendNotification(NotificationType type) {
        final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder builder = getNotificationBuilder();
        final NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();

        switch (type) {
            case INIT:
                builder.setOngoing(true);
                builder.setProgress(0, 0, true);
                builder.setCategory(Notification.CATEGORY_PROGRESS);
                bigTextStyle.bigText(getString(R.string.sync_init));
                break;
            case DOWNLOAD:
                builder.setOngoing(true);
                builder.setProgress(0, 0, true);
                builder.setCategory(Notification.CATEGORY_PROGRESS);
                bigTextStyle.bigText(getString(R.string.sync_downloading));
                break;
            case SYNCING:
                builder.setOngoing(true);
                builder.setProgress(0, 0, true);
                builder.setCategory(Notification.CATEGORY_PROGRESS);
                bigTextStyle.bigText(getString(R.string.sync_progress));
                break;
            case SUCCESS:
                builder.setOngoing(false);
                bigTextStyle.bigText(getString(R.string.sync_completed_all));
                builder.setCategory(Notification.CATEGORY_STATUS);
                break;
            case FAILED:
                builder.setOngoing(false);
                bigTextStyle.bigText(getString(R.string.sync_failed));
                builder.setCategory(Notification.CATEGORY_STATUS);
                break;
        }

        builder.setStyle(bigTextStyle);

        if (manager != null)
            manager.notify(1337, builder.build());
    }

    private boolean isRunning() {
        return true;
    }

    public void fetchRepo() {
        sendNotification(NotificationType.INIT);
        fetch = DownloadManager.getFetchInstance(this);
        disposable.add(Observable.fromCallable(() -> new CheckRepoUpdatesTask(this)
                .getRepoRequestList())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::enqueueDownloads, throwable -> {
                    if (!StringUtils.isEmpty(throwable.getMessage())) {
                        AuroraApplication.rxNotify(new LogEvent(throwable.getMessage()));
                    }
                }));
    }

    private void enqueueDownloads(List<Request> requestList) {
        if (requestList.isEmpty()) {
            sendNotification(NotificationType.SUCCESS);
            AuroraApplication.rxNotify(new Event(EventType.SYNC_NO_UPDATES));
            notifyCompleted();
        } else {
            fetchListener = getFetchListener();
            fetch.addListener(fetchListener);
            fetch.enqueue(requestList, result -> {
                sendNotification(NotificationType.DOWNLOAD);
                Log.d("Repo requests enqueued : %d", requestList.size());
            });
        }
    }

    private void extractAllRepos() {
        sendNotification(NotificationType.SYNCING);

        final RepoSyncManager repoSyncManager = new RepoSyncManager(this);
        final File repoDirectory = new File(PathUtil.getRepoDirectory(this));
        final File[] files = repoDirectory.listFiles();

        if (files == null) {
            sendNotification(NotificationType.FAILED);
            Log.e("Error : Repo files not found");
            destroyService();
        } else {
            Observable.fromIterable(Arrays.asList(files))
                    .subscribeOn(Schedulers.io())
                    .filter(file -> FilenameUtils.getExtension(file.getName()).equals(Constants.JAR))//Filter JAR files
                    .map(file -> new ExtractRepoTask(this, file).extract())//Extract the JAR
                    .map(file -> new JsonParserTask(this, file).parse())//Add RawJSON to database
                    .map(repoBundle -> {
                        final StaticRepo staticRepo = repoBundle.getStaticRepo();
                        if (repoBundle.isSynced()) {
                            AuroraApplication.rxNotify(new LogEvent(staticRepo.getRepoName() + " - " + getString(R.string.sync_completed)));
                            repoSyncManager.addToSyncMap(staticRepo);
                        } else {
                            AuroraApplication.rxNotify(new LogEvent(staticRepo.getRepoName() + " - " + getString(R.string.sync_failed)));
                        }
                        PathUtil.deleteRepoFiles(this, staticRepo.getRepoId());
                        return repoBundle.isSynced();
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete(this::notifyCompleted)
                    .doOnError(throwable -> {
                        AuroraApplication.rxNotify(new Event(EventType.SYNC_FAILED));
                        Log.e("Error : %s", throwable.getMessage());
                    })
                    .onErrorResumeNext(Observable.empty())
                    .subscribe();
        }
    }

    private void notifyCompleted() {
        sendNotification(NotificationType.SUCCESS);
        DatabaseUtil.setDatabaseAvailable(this, true);
        DatabaseUtil.setDatabaseSyncTime(this, Calendar.getInstance().getTimeInMillis());
        AuroraApplication.rxNotify(new Event(EventType.SYNC_COMPLETED));
        destroyService();
    }

    private AbstractFetchGroupListener getFetchListener() {
        return new AbstractFetchGroupListener() {
            @Override
            public void onCompleted(@NotNull Download download) {
                super.onCompleted(download);
                final StaticRepo staticRepo = repoListManager.getRepoById(download.getTag());
                Log.i("Downloaded : %s", download.getUrl());
                AuroraApplication.rxNotify(new LogEvent(staticRepo.getRepoName() + " - " + getString(R.string.download_completed)));
            }

            @Override
            public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
                super.onError(download, error, throwable);
                final StaticRepo staticRepo = repoListManager.getRepoById(download.getTag());
                Log.e("Download Failed : %s", download.getUrl());
                AuroraApplication.rxNotify(new LogEvent(staticRepo.getRepoName() + " - " + getString(R.string.download_failed)));
            }

            @Override
            public void onCompleted(int groupId, @NotNull Download download, @NotNull FetchGroup fetchGroup) {
                super.onCompleted(groupId, download, fetchGroup);
                if (groupId == 1337 && fetchGroup.getGroupDownloadProgress() == 100) {
                    extractAllRepos();
                    fetch.removeGroup(1337);
                    fetch.removeListener(this);
                }
            }

            @Override
            public void onError(int groupId, @NotNull Download download, @NotNull Error error, @Nullable Throwable throwable, @NotNull FetchGroup fetchGroup) {
                super.onError(groupId, download, error, throwable, fetchGroup);
                if (groupId == 1337) {
                    fetch.removeGroup(1337);
                    fetch.removeListener(this);
                }
            }
        };
    }

    private NotificationCompat.Builder getNotificationBuilder() {
        return new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_GENERAL)
                .setContentTitle(getString(R.string.sync_service))
                .setContentIntent(getContentIntent())
                .setOngoing(true)
                .setColor(getResources().getColor(R.color.colorAccent))
                .setSmallIcon(android.R.drawable.stat_sys_download);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }

    private void destroyService() {
        stopForeground(true);
        stopSelf();
    }

    private PendingIntent getContentIntent() {
        Intent intent = new Intent(this, AuroraActivity.class);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private enum NotificationType {
        INIT,
        DOWNLOAD,
        SYNCING,
        SUCCESS,
        FAILED
    }
}
