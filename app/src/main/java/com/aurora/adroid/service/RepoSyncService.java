package com.aurora.adroid.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.activity.AuroraActivity;
import com.aurora.adroid.download.DownloadManager;
import com.aurora.adroid.download.RequestBuilder;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.Events;
import com.aurora.adroid.event.LogEvent;
import com.aurora.adroid.event.RxBus;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.notification.QuickNotification;
import com.aurora.adroid.task.DatabaseTask;
import com.aurora.adroid.task.ExtractRepoTask;
import com.aurora.adroid.task.JsonParserTask;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PathUtil;
import com.tonyodev.fetch2.AbstractFetchListener;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.Request;

import org.apache.commons.io.FilenameUtils;
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

public class RepoSyncService extends Service {

    public static RepoSyncService instance = null;

    private RepoListManager repoListManager;
    private CompositeDisposable disposable = new CompositeDisposable();
    private Fetch fetch;
    private AbstractFetchListener abstractFetchListener;
    private int count = 0;
    private int downloadCount = 0;
    private int failedDownloadCount = 0;

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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForeground(1, getNotification());
        } else {
            Notification notification = buildNotification(new NotificationCompat.Builder(this));
            startForeground(1, notification);
        }
        fetchRepo();
    }

    private boolean isRunning() {
        return true;
    }

    public int getRepoCount() {
        return repoListManager.getRepoCount();
    }

    public void fetchRepo() {
        List<Repo> repoList = RepoListManager.getSelectedRepos(this);
        List<Request> requestList = RequestBuilder.buildRequest(this, repoList);

        if (repoList.isEmpty()) {
            RxBus.publish(new Event(Events.SYNC_EMPTY));
            destroyService();
        }

        repoListManager = new RepoListManager(this);
        fetch = DownloadManager.getFetchInstance(this);

        abstractFetchListener = getFetchListener();
        fetch.addListener(abstractFetchListener);
        fetch.enqueue(requestList, result -> {
        });
    }

    private void extractAllRepos() {
        DatabaseUtil.setDatabaseAvailable(this, false);
        RepoListManager.clearSynced(this);
        disposable.add(Observable.fromCallable(() -> new DatabaseTask(this)
                .clearAllTables())
                .subscribeOn(Schedulers.io())
                .subscribe());

        final File repoDirectory = new File(PathUtil.getRepoDirectory(this));
        final File[] files = repoDirectory.listFiles();

        disposable.add(Observable.fromIterable(Arrays.asList(files))
                .filter(file -> FilenameUtils.getExtension(file.getName()).equals(Constants.JAR))
                .flatMap(file -> new ExtractRepoTask(this, file).extract())
                .flatMap(file -> new JsonParserTask(this, file).parse())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(repoBundle -> {
                    final Repo repo = repoBundle.getRepo();
                    if (repoBundle.getStatus()) {
                        RxBus.publish(new LogEvent(repo.getRepoName() + " - " + getString(R.string.sync_completed)));
                        RepoListManager.setSynced(this, repo.getRepoId());
                    } else {
                        RxBus.publish(new LogEvent(repo.getRepoName() + " - " + getString(R.string.sync_failed)));
                    }
                    updateProgress();
                    PathUtil.deleteFile(this, repo.getRepoId());
                })
                .doOnComplete(() -> {
                    DatabaseUtil.setDatabaseAvailable(this, true);
                    DatabaseUtil.setDatabaseSyncTime(this, Calendar.getInstance().getTimeInMillis());
                    Log.i("Sync completed");
                })
                .doOnError(throwable -> {
                    RxBus.publish(new Event(Events.SYNC_FAILED));
                    updateProgress();
                    Log.e("Error : %s", throwable.getMessage());
                })
                .subscribe());
    }

    private void updateProgress() {
        count++;
        RxBus.publish(new Event(Events.SYNC_PROGRESS));
        if (count == getRepoCount() - failedDownloadCount) {
            RxBus.publish(new Event(Events.SYNC_COMPLETED));
            QuickNotification.show(this, getString(R.string.app_name),
                    getString(R.string.download_repo_synced), getContentIntent());
            destroyService();
        }
    }

    private void updateDownloads() {
        downloadCount++;
        if (downloadCount == getRepoCount()) {
            fetch.removeListener(abstractFetchListener);
            fetch.removeGroup(1337);
            extractAllRepos();
        }
    }

    private AbstractFetchListener getFetchListener() {
        return new AbstractFetchListener() {
            @Override
            public void onCompleted(@NotNull Download download) {
                super.onCompleted(download);
                final Repo repo = RepoListManager.getRepoById(RepoSyncService.this, download.getTag());
                Log.i("Downloaded : %s", download.getUrl());
                RxBus.publish(new LogEvent(repo.getRepoName() + " - " + getString(R.string.download_completed)));
                updateDownloads();
            }

            @Override
            public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
                super.onError(download, error, throwable);
                final Repo repo = RepoListManager.getRepoById(RepoSyncService.this, download.getTag());
                Log.e("Download Failed : %s", download.getUrl());
                RxBus.publish(new LogEvent(repo.getRepoName() + " - " + getString(R.string.download_failed)));
                failedDownloadCount++;
                updateDownloads();
            }
        };
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private Notification getNotification() {
        String NOTIFICATION_CHANNEL_ID = "com.aurora.adroid";
        String channelName = "Repo Sync Service";

        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        notificationChannel.setLightColor(Color.BLUE);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(RepoSyncService.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(notificationChannel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        return buildNotification(notificationBuilder);
    }

    private Notification buildNotification(NotificationCompat.Builder builder) {
        int versionCode = Build.VERSION.SDK_INT;
        return builder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Syncing repositories")
                .setContentText("Please wait while the repositories are being synced")
                .setPriority(versionCode >= Build.VERSION_CODES.O ? NotificationCompat.PRIORITY_DEFAULT : Notification.PRIORITY_DEFAULT)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(getContentIntent())
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
