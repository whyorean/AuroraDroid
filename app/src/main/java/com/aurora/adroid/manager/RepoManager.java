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

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.download.DownloadManager;
import com.aurora.adroid.download.RequestBuilder;
import com.aurora.adroid.event.Event;
import com.aurora.adroid.event.Events;
import com.aurora.adroid.event.LogEvent;
import com.aurora.adroid.event.RxBus;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.notification.QuickNotification;
import com.aurora.adroid.task.ExtractRepoTask;
import com.aurora.adroid.task.JsonParserTask;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PathUtil;
import com.aurora.adroid.util.PrefUtil;
import com.tonyodev.fetch2.AbstractFetchListener;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.Request;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class RepoManager extends ContextWrapper {

    private Context context;
    private RepoListManager repoListManager;
    private CompositeDisposable disposable = new CompositeDisposable();
    private Fetch fetch;
    private int count = 0;

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
        QuickNotification.show(context, getString(R.string.app_name),
                getString(R.string.download_repo_progress), null);
        final List<Request> requestList = RequestBuilder.buildRequest(context, repoList);

        for (Request request : requestList) {
            fetch.enqueue(request, updatedRequest -> {
                Log.i("Downloading : %s", request.getUrl());
            }, error -> {
                Log.e("Failed to download : %s", request.getUrl());
            });

            fetch.addListener(new AbstractFetchListener() {
                @Override
                public void onCompleted(@NotNull Download download) {
                    super.onCompleted(download);
                    if (request.getId() == download.getId()) {
                        Log.i("Downloaded : %s", download.getUrl());
                        final Repo repo = RepoListManager.getRepoById(context, download.getTag());
                        RxBus.publish(new LogEvent(repo.getRepoName() + " - " + getString(R.string.download_completed)));
                        extractRepo(download, repo);
                    }
                }

                @Override
                public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
                    super.onError(download, error, throwable);
                    if (request.getId() == download.getId()) {
                        Log.i("Download Failed : %s", download.getUrl());
                        final Repo repo = RepoListManager.getRepoById(context, download.getTag());
                        RxBus.publish(new LogEvent(repo.getRepoName() + " - " + getString(R.string.download_failed)));
                        updateCount();
                    }
                }
            });
        }
    }

    private synchronized void extractRepo(Download download, Repo repo) {
        final String jarFile = download.getFile();
        final String repoId = download.getTag();

        disposable.add(Observable.fromCallable(() -> new ExtractRepoTask(this, jarFile, repoId)
                .extract())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    RxBus.publish(new LogEvent(repo.getRepoName() + " - " + getString(R.string.extract_completed)));
                    final File tempJson = new File(PathUtil.getRepoDirectory(this) + repoId + ".json");
                    parseJson(FileUtils.openInputStream(tempJson), repo);
                    fetch.delete(download.getId());
                }, err -> {
                    Log.e(err.getMessage());
                    RxBus.publish(new LogEvent(repo.getRepoName() + " - " + getString(R.string.extract_failed)));
                    updateCount();
                    fetch.delete(download.getId());
                }));
    }

    private synchronized void parseJson(InputStream inputStream, Repo repo) {
        disposable.add(Observable.fromCallable(() -> new JsonParserTask(this)
                .parse(inputStream, repo.getRepoId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((success) -> {
                    if (success) {
                        PrefUtil.putBoolean(getApplicationContext(), Constants.DATABASE_AVAILABLE, true);
                    }
                    QuickNotification.show(this,
                            repo.getRepoName(),
                            success ? getString(R.string.sync_completed) : getString(R.string.sync_failed),
                            null);
                    RxBus.publish(new LogEvent(repo.getRepoName() + " - " + getString(R.string.sync_completed)));
                    repoListManager.synced(repo.getRepoId());
                    updateCount();
                    PathUtil.deleteFile(context, repo.getRepoId());
                }, err -> {
                    Log.e(err.getMessage());
                    updateCount();
                    PathUtil.deleteFile(context, repo.getRepoId());
                }));
    }

    private synchronized void updateCount() {
        count++;
        if (count == getRepoCount())
            RxBus.publish(new Event(Events.SYNC_COMPLETED));
    }
}

