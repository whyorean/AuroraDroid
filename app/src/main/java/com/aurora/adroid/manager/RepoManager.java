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

package com.aurora.adroid.manager;

import android.content.Context;
import android.content.ContextWrapper;

import com.aurora.adroid.R;
import com.aurora.adroid.download.DownloadManager;
import com.aurora.adroid.download.RequestBuilder;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.Events;
import com.aurora.adroid.event.LogEvent;
import com.aurora.adroid.event.RxBus;
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
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.aurora.adroid.Constants.JAR;

public class RepoManager extends ContextWrapper {

    private Context context;
    private RepoListManager repoListManager;
    private CompositeDisposable disposable = new CompositeDisposable();
    private Fetch fetch;
    private AbstractFetchListener abstractFetchListener;
    private int count = 0;
    private int downloadCount = 0;
    private int failedDownloadCount = 0;

    public RepoManager(Context context) {
        super(context);
        this.context = context;
        repoListManager = new RepoListManager(context);
        fetch = DownloadManager.getFetchInstance(context);
    }

    public int getRepoCount() {
        return repoListManager.getRepoCount();
    }

    public void fetchRepo() {
        List<Repo> repoList = RepoListManager.getSelectedRepos(context);
        List<Request> requestList = RequestBuilder.buildRequest(context, repoList);

        abstractFetchListener = getFetchListener();
        fetch.addListener(abstractFetchListener);
        fetch.enqueue(requestList, result -> QuickNotification.show(context, getString(R.string.app_name),
                getString(R.string.download_repo_progress), null));
    }

    private void extractAllRepos() {
        DatabaseUtil.setDatabaseAvailable(context, false);
        RepoListManager.clearSynced(context);
        disposable.add(Observable.fromCallable(() -> new DatabaseTask(context)
                .clearAllTables())
                .subscribeOn(Schedulers.io())
                .subscribe());

        final File repoDirectory = new File(PathUtil.getRepoDirectory(context));
        final File[] files = repoDirectory.listFiles();

        disposable.add(Observable.fromIterable(Arrays.asList(files))
                .filter(file -> FilenameUtils.getExtension(file.getName()).equals(JAR))
                .flatMap(file -> new ExtractRepoTask(this, file).extract())
                .flatMap(file -> new JsonParserTask(this, file).parse())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(repoBundle -> {
                    final Repo repo = repoBundle.getRepo();
                    if (repoBundle.getStatus()) {
                        RxBus.publish(new LogEvent(repo.getRepoName() + " - " + getString(R.string.sync_completed)));
                        RepoListManager.setSynced(context, repo.getRepoId());
                    } else {
                        RxBus.publish(new LogEvent(repo.getRepoName() + " - " + getString(R.string.sync_failed)));
                    }
                    updateProgress();
                    PathUtil.deleteFile(context, repo.getRepoId());
                })
                .doOnComplete(() -> {
                    DatabaseUtil.setDatabaseAvailable(context, true);
                    Log.i("Sync completed");
                })
                .doOnError(throwable -> {
                    RxBus.publish(new Event(Events.SYNC_FAILED));
                    updateProgress();
                    Log.e("Error : %s", throwable.getMessage());
                })
                .subscribe());
    }

    private synchronized void updateProgress() {
        count++;
        RxBus.publish(new Event(Events.SYNC_PROGRESS));
        if (count == getRepoCount() - failedDownloadCount) {
            RxBus.publish(new Event(Events.SYNC_COMPLETED));
            QuickNotification.show(context, getString(R.string.app_name),
                    getString(R.string.download_repo_synced), null);
        }
    }

    private synchronized void updateDownloads() {
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
                final Repo repo = RepoListManager.getRepoById(context, download.getTag());
                Log.i("Downloaded : %s", download.getUrl());
                RxBus.publish(new LogEvent(repo.getRepoName() + " - " + getString(R.string.download_completed)));
                updateDownloads();
            }

            @Override
            public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
                super.onError(download, error, throwable);
                final Repo repo = RepoListManager.getRepoById(context, download.getTag());
                Log.e("Download Failed : %s", download.getUrl());
                RxBus.publish(new LogEvent(repo.getRepoName() + " - " + getString(R.string.download_failed)));
                failedDownloadCount++;
                updateDownloads();
            }
        };
    }
}

