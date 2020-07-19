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

package com.aurora.adroid.ui.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.manager.RepoSyncManager;
import com.aurora.adroid.model.items.RepoItem;
import com.aurora.adroid.model.items.cluster.GenericClusterItem;
import com.aurora.adroid.model.items.cluster.NewClusterItem;
import com.aurora.adroid.service.SyncService;
import com.aurora.adroid.ui.details.DetailsActivity;
import com.aurora.adroid.ui.generic.activity.GenericAppActivity;
import com.aurora.adroid.util.ContextUtil;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.ViewUtil;
import com.aurora.adroid.viewmodel.ClusterAppsViewModel;
import com.aurora.adroid.viewmodel.IndexModel;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class HomeFragment extends Fragment {

    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeLayout;
    @BindView(R.id.recycler_repo)
    RecyclerView recyclerViewIndices;
    @BindView(R.id.recycler_latest)
    RecyclerView recyclerViewUpdates;
    @BindView(R.id.recycler_new)
    RecyclerView recyclerViewNew;

    private FastItemAdapter<NewClusterItem> fastItemAdapterNew;
    private FastItemAdapter<GenericClusterItem> fastItemAdapterUpdates;
    private FastItemAdapter<RepoItem> fastItemAdapterIndices;
    private CompositeDisposable disposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupNewApps();
        setupUpdatedApps();
        setupRepository();

        ClusterAppsViewModel clusterModel = new ViewModelProvider(requireActivity()).get(ClusterAppsViewModel.class);
        clusterModel.getNewAppsLiveData().observe(getViewLifecycleOwner(), apps -> {
            disposable.add(Observable.fromIterable(apps)
                    .subscribeOn(Schedulers.io())
                    .map(NewClusterItem::new)
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(clusterItems -> {
                        fastItemAdapterNew.set(clusterItems);
                    }, throwable -> Log.e(throwable.getMessage())));
        });

        clusterModel.getUpdatedAppsLiveData().observe(getViewLifecycleOwner(), apps -> {
            disposable.add(Observable.fromIterable(apps)
                    .subscribeOn(Schedulers.io())
                    .map(GenericClusterItem::new)
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(clusterItems -> {
                        fastItemAdapterUpdates.set(clusterItems);
                    }, throwable -> Log.e(throwable.getMessage())));
        });

        IndexModel indexModel = new ViewModelProvider(requireActivity()).get(IndexModel.class);
        indexModel.getAllIndicesLive().observe(getViewLifecycleOwner(), indices -> {
            final RepoSyncManager repoSyncManager = new RepoSyncManager(requireContext());
            disposable.add(Observable.fromIterable(indices)
                    .filter(index -> repoSyncManager.isSynced(index.getRepoId()))
                    .map(RepoItem::new)
                    .toList()
                    .subscribe(indexItems -> {
                        fastItemAdapterIndices.clear();
                        fastItemAdapterIndices.add(indexItems);
                    }, throwable -> Log.e(throwable.getMessage())));
        });

        AuroraApplication.getRxBus().getBus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(event -> {
                    switch (event.getType()) {
                        case SYNC_EMPTY:
                            swipeLayout.setRefreshing(false);
                            ContextUtil.toastLong(requireContext(), getString(R.string.toast_repo_sync_empty));
                            break;
                        case SYNC_COMPLETED:
                            ContextUtil.toastLong(requireContext(), getString(R.string.toast_repo_sync_completed));
                            swipeLayout.setRefreshing(false);
                            break;
                        case SYNC_NO_UPDATES:
                            ContextUtil.toastLong(requireContext(), getString(R.string.toast_repo_sync_no_updates));
                            swipeLayout.setRefreshing(false);
                            break;
                    }
                })
                .subscribe();
        swipeLayout.setOnRefreshListener(this::startRepoSyncService);
    }

    private void startRepoSyncService() {
        if (SyncService.isServiceRunning())
            return;

        final Intent intent = new Intent(requireActivity(), SyncService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().startForegroundService(intent);
        } else {
            requireActivity().startService(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        swipeLayout.setRefreshing(false);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.header_new_apps)
    public void showAllNewApps() {
        Intent intent = new Intent(requireContext(), GenericAppActivity.class);
        intent.putExtra("LIST_TYPE", 0);
        requireActivity().startActivity(intent);
    }

    @OnClick(R.id.header_updated_apps)
    public void showAllUpdatedApps() {
        Intent intent = new Intent(requireContext(), GenericAppActivity.class);
        intent.putExtra("LIST_TYPE", 1);
        requireActivity().startActivity(intent);
    }

    private void setupRepository() {
        fastItemAdapterIndices = new FastItemAdapter<>();
        fastItemAdapterIndices.setOnClickListener((view, adapter, item, position) -> {
            Intent intent = new Intent(requireContext(), GenericAppActivity.class);
            intent.putExtra("LIST_TYPE", 3);
            intent.putExtra("REPO_ID", item.getIndex().getRepoId());
            intent.putExtra("REPO_NAME", item.getIndex().getName());
            startActivity(intent);
            return false;
        });
        recyclerViewIndices.setAdapter(fastItemAdapterIndices);
        recyclerViewIndices.setLayoutManager(new LinearLayoutManager(requireContext(),
                RecyclerView.HORIZONTAL, false));
    }

    private void setupNewApps() {
        fastItemAdapterNew = new FastItemAdapter<>();
        fastItemAdapterNew.setOnClickListener((view, adapter, item, position) -> {
            Intent intent = new Intent(requireContext(), DetailsActivity.class);
            intent.putExtra(Constants.INTENT_PACKAGE_NAME, item.getPackageName());
            intent.putExtra(Constants.STRING_REPO, item.getApp().getRepoName());
            startActivity(intent, ViewUtil.getEmptyActivityBundle((AppCompatActivity) requireActivity()));
            return false;
        });

        recyclerViewNew.setAdapter(fastItemAdapterNew);
        recyclerViewNew.setLayoutManager(new GridLayoutManager(requireContext(), 2,
                RecyclerView.HORIZONTAL, false));
    }

    private void setupUpdatedApps() {
        fastItemAdapterUpdates = new FastItemAdapter<>();
        fastItemAdapterUpdates.setOnClickListener((view, adapter, item, position) -> {
            Intent intent = new Intent(requireContext(), DetailsActivity.class);
            intent.putExtra(Constants.INTENT_PACKAGE_NAME, item.getPackageName());
            intent.putExtra(Constants.STRING_REPO, item.getApp().getRepoName());
            startActivity(intent, ViewUtil.getEmptyActivityBundle((AppCompatActivity) requireActivity()));
            return false;
        });

        recyclerViewUpdates.setAdapter(fastItemAdapterUpdates);
        recyclerViewUpdates.setLayoutManager(new GridLayoutManager(requireContext(), 2,
                RecyclerView.HORIZONTAL, false));
    }
}
