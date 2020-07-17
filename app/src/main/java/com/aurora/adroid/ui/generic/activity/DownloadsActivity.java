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

package com.aurora.adroid.ui.generic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.download.DownloadManager;
import com.aurora.adroid.model.items.DownloadItem;
import com.aurora.adroid.ui.details.DetailsActivity;
import com.aurora.adroid.ui.sheet.DownloadMenuSheet;
import com.aurora.adroid.ui.view.ViewFlipper2;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.Util;
import com.aurora.adroid.util.ViewUtil;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.tonyodev.fetch2.AbstractFetchListener;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.Status;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DownloadsActivity extends BaseActivity {

    @BindView(R.id.viewFlipper)
    ViewFlipper2 viewFlipper;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;

    private FastAdapter<DownloadItem> fastAdapter;
    private ItemAdapter<DownloadItem> itemAdapter;

    private Fetch fetch;
    private CompositeDisposable disposable = new CompositeDisposable();

    private final FetchListener fetchListener = new AbstractFetchListener() {
        @Override
        public void onAdded(@NotNull Download download) {
            updateDownloadsList();
        }

        @Override
        public void onQueued(@NotNull Download download, boolean waitingOnNetwork) {
            updateDownloadsList();
        }

        @Override
        public void onCompleted(@NotNull Download download) {
            updateDownloadsList();
        }

        @Override
        public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
            super.onError(download, error, throwable);
            updateDownloadsList();
        }

        @Override
        public void onProgress(@NotNull Download download, long etaInMilliseconds, long downloadedBytesPerSecond) {
            updateDownloadsList();
        }

        @Override
        public void onPaused(@NotNull Download download) {//downloadsAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
            updateDownloadsList();
        }

        @Override
        public void onResumed(@NotNull Download download) {//downloadsAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
            updateDownloadsList();
        }

        @Override
        public void onCancelled(@NotNull Download download) {//downloadsAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
            updateDownloadsList();
        }

        @Override
        public void onRemoved(@NotNull Download download) {//downloadsAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
            updateDownloadsList();
        }

        @Override
        public void onDeleted(@NotNull Download download) {
            updateDownloadsList();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);
        ButterKnife.bind(this);

        setupActionbar();
        setupRecycler();

        fetch = DownloadManager.getFetchInstance(this);
        updateDownloadsList();
        swipeLayout.setOnRefreshListener(this::updateDownloadsList);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_download_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_pause_all:
                fetch.pauseAll();
                return true;
            case R.id.action_resume_all:
                fetch.resumeAll();
                return true;
            case R.id.action_cancel_all:
                fetch.cancelAll();
                return true;
            case R.id.action_clear_completed:
                fetch.removeAllWithStatus(Status.COMPLETED);
                return true;
            case R.id.action_force_clear_all:
                fetch.deleteAll();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetch.addListener(fetchListener);

        //Check & start notification service
        Util.startNotificationService(this);
    }

    @Override
    protected void onPause() {
        swipeLayout.setRefreshing(false);
        fetch.removeListener(fetchListener);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        fetch.removeListener(fetchListener);
        disposable.dispose();
        super.onDestroy();
    }

    private void setupActionbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(0f);
            actionBar.setTitle(R.string.menu_downloads);
        }
    }

    private void updateDownloadsList() {
        swipeLayout.setRefreshing(false);
        fetch.getDownloads(downloads -> {
            final List<Download> downloadList = new ArrayList<>(downloads);
            Collections.sort(downloadList, (first, second) -> Long.compare(first.getCreated(), second.getCreated()));
            disposable.add(Observable.fromIterable(downloadList)
                    .subscribeOn(Schedulers.io())
                    .map(DownloadItem::new)
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::dispatchAppsToAdapter, throwable -> Log.e(throwable.getMessage())));
        });
    }

    private void dispatchAppsToAdapter(List<DownloadItem> downloadItems) {
        itemAdapter.set(downloadItems);
        if (itemAdapter.getAdapterItems().size() > 0) {
            viewFlipper.switchState(ViewFlipper2.DATA);
        } else {
            viewFlipper.switchState(ViewFlipper2.EMPTY);
        }
    }

    private void setupRecycler() {
        fastAdapter = new FastAdapter<>();
        itemAdapter = new ItemAdapter<>();
        fastAdapter.addAdapter(0, itemAdapter);

        fastAdapter.setOnClickListener((view, downloadItemIAdapter, downloadItem, position) -> {
            final Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra(Constants.INTENT_PACKAGE_NAME, downloadItem.getPackageName());
            startActivity(intent, ViewUtil.getEmptyActivityBundle(this));
            return false;
        });

        fastAdapter.setOnLongClickListener((view, downloadItemIAdapter, downloadItem, position) -> {
            final DownloadMenuSheet menuSheet = new DownloadMenuSheet();
            final Bundle bundle = new Bundle();
            bundle.putInt(DownloadMenuSheet.DOWNLOAD_ID, downloadItem.getDownload().getId());
            bundle.putInt(DownloadMenuSheet.DOWNLOAD_STATUS, downloadItem.getDownload().getStatus().getValue());
            bundle.putString(DownloadMenuSheet.DOWNLOAD_URL, downloadItem.getDownload().getUrl());
            menuSheet.setArguments(bundle);
            menuSheet.show(getSupportFragmentManager(), DownloadMenuSheet.TAG);
            return true;
        });

        recyclerView.setAdapter(fastAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        final DividerItemDecoration itemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        itemDecoration.setDrawable(getResources().getDrawable(R.drawable.downloads_divider));
        recyclerView.addItemDecoration(itemDecoration);
    }
}
