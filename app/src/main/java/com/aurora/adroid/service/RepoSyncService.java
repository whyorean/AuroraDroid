package com.aurora.adroid.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.download.DownloadManager;
import com.aurora.adroid.download.RequestBuilder;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.EventType;
import com.aurora.adroid.event.LogEvent;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.manager.RepoManager;
import com.aurora.adroid.manager.SyncManager;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.model.RepoRequest;
import com.aurora.adroid.notification.SyncNotification;
import com.aurora.adroid.task.CheckRepoUpdatesTask;
import com.aurora.adroid.task.ExtractRepoTask;
import com.aurora.adroid.task.JsonParserTask;
import com.aurora.adroid.ui.activity.AuroraActivity;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PathUtil;
import com.tonyodev.fetch2.AbstractFetchListener;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RepoSyncService extends Service {

    public static RepoSyncService instance = null;

    private Fetch fetch;
    private SyncNotification syncNotification;
    private AbstractFetchListener abstractFetchListener;
    private CheckRepoUpdatesTask checkRepoUpdatesTask;
    private List<RepoRequest> requestList = new ArrayList<>();

    private int targetCount = 0;
    private int currentCount = 0;
    private int downloadCount = 0;

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
        syncNotification = new SyncNotification(this);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForeground(1, getNotification());
        } else {
            Notification notification = getNotification(new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_GENERAL));
            startForeground(1, notification);
        }
        fetchRepo();
    }

    private boolean isRunning() {
        return true;
    }

    public int getRepoCount() {
        return targetCount;
    }

    public void fetchRepo() {
        final List<Repo> repoList = new RepoManager(this).getRepoList();
        requestList = RequestBuilder.buildRequest(this, repoList);

        if (repoList.isEmpty()) {
            AuroraApplication.rxNotify(new Event(EventType.SYNC_EMPTY));
            destroyService();
        }

        checkRepoUpdatesTask = new CheckRepoUpdatesTask(this);
        fetch = DownloadManager.getFetchInstance(this);

        Observable.fromCallable(() -> checkRepoUpdatesTask.filterList(requestList))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(requests -> {
                    if (requests.isEmpty()) {
                        AuroraApplication.rxNotify(new Event(EventType.SYNC_NO_UPDATES));
                        notifyCompleted();
                    } else {
                        targetCount = requests.size();
                        syncNotification.notifyQueued();
                        abstractFetchListener = getFetchListener();
                        fetch.addListener(abstractFetchListener);
                        fetch.enqueue(requests, result -> {

                        });
                    }
                })
                .doOnError(e -> {
                    if (!StringUtils.isEmpty(e.getMessage())) {
                        AuroraApplication.rxNotify(new LogEvent(e.getMessage()));
                        Log.e(e.getMessage());
                    }
                })
                .subscribe();
    }

    private void extractAllRepos() {
        final SyncManager syncManager = new SyncManager(this);
        final File repoDirectory = new File(PathUtil.getRepoDirectory(this));
        final File[] files = repoDirectory.listFiles();

        Observable.fromIterable(Arrays.asList(files))
                .filter(file -> FilenameUtils.getExtension(file.getName()).equals(Constants.JAR))
                .flatMap(file -> new ExtractRepoTask(this, file).extract())
                .flatMap(file -> new JsonParserTask(this, file).parse())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(repoBundle -> {
                    final Repo repo = repoBundle.getRepo();
                    if (repoBundle.isSynced()) {
                        AuroraApplication.rxNotify(new LogEvent(repo.getRepoName() + " - " + getString(R.string.sync_completed)));
                        syncManager.addToSyncList(repo);
                    } else {
                        AuroraApplication.rxNotify(new LogEvent(repo.getRepoName() + " - " + getString(R.string.sync_failed)));
                    }
                    updateProgress();
                    PathUtil.deleteFile(this, repo.getRepoId());
                })
                .doOnComplete(() -> {
                    notifyCompleted();
                })
                .doOnError(throwable -> {
                    AuroraApplication.rxNotify(new Event(EventType.SYNC_FAILED));
                    updateProgress();
                    Log.e("Error : %s", throwable.getMessage());
                })
                .subscribe();
    }

    private void updateProgress() {
        currentCount++;
        AuroraApplication.rxNotify(new Event(EventType.SYNC_PROGRESS));
        syncNotification.notifySyncProgress(currentCount, getRepoCount());
    }

    private void updateDownloads() {
        downloadCount++;
        if (downloadCount == getRepoCount()) {
            fetch.removeListener(abstractFetchListener);
            fetch.removeGroup(1337);
            extractAllRepos();
        }
    }

    private void notifyCompleted() {
        DatabaseUtil.setDatabaseAvailable(this, true);
        DatabaseUtil.setDatabaseSyncTime(this, Calendar.getInstance().getTimeInMillis());
        AuroraApplication.rxNotify(new Event(EventType.SYNC_COMPLETED));
        syncNotification.notifyCompleted();
        Log.i("Sync completed");
        destroyService();
    }

    private AbstractFetchListener getFetchListener() {
        return new AbstractFetchListener() {
            @Override
            public void onCompleted(@NotNull Download download) {
                super.onCompleted(download);
                final Repo repo = RepoListManager.getRepoById(RepoSyncService.this, download.getTag());
                Log.i("Downloaded : %s", download.getUrl());
                AuroraApplication.rxNotify(new LogEvent(repo.getRepoName() + " - " + getString(R.string.download_completed)));
                updateDownloads();
            }

            @Override
            public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
                super.onError(download, error, throwable);
                final Repo repo = RepoListManager.getRepoById(RepoSyncService.this, download.getTag());
                Log.e("Download Failed : %s", download.getUrl());
                AuroraApplication.rxNotify(new LogEvent(repo.getRepoName() + " - " + getString(R.string.download_failed)));
                updateDownloads();
            }
        };
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private Notification getNotification() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_GENERAL);
        return getNotification(notificationBuilder);
    }

    private Notification getNotification(NotificationCompat.Builder builder) {
        return builder
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.sync_background))
                .setContentIntent(getContentIntent())
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_repo_alt)
                .build();
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
}
