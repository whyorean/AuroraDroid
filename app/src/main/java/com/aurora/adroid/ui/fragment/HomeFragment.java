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

package com.aurora.adroid.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.model.items.IndexItem;
import com.aurora.adroid.model.items.cluster.NewClusterItem;
import com.aurora.adroid.model.items.cluster.UpdatesClusterItem;
import com.aurora.adroid.ui.activity.AuroraActivity;
import com.aurora.adroid.ui.activity.DetailsActivity;
import com.aurora.adroid.ui.activity.GenericAppActivity;
import com.aurora.adroid.ui.sheet.RepoDetailsBottomSheet;
import com.aurora.adroid.viewmodel.ClusterAppsViewModel;
import com.aurora.adroid.viewmodel.IndexViewModel;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class HomeFragment extends Fragment {

    @BindView(R.id.recycler_repo)
    RecyclerView recyclerViewIndices;
    @BindView(R.id.recycler_latest)
    RecyclerView recyclerViewUpdates;
    @BindView(R.id.recycler_new)
    RecyclerView recyclerViewNew;
    @BindView(R.id.btn_more_new)
    ImageButton btnMoreNew;
    @BindView(R.id.btn_more_updated)
    ImageButton btnMoreUpdated;

    private FastItemAdapter<NewClusterItem> fastItemAdapterNew;
    private FastItemAdapter<UpdatesClusterItem> fastItemAdapterUpdates;
    private FastItemAdapter<IndexItem> fastItemAdapterIndices;

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

        final ClusterAppsViewModel clusterModel = new ViewModelProvider(this).get(ClusterAppsViewModel.class);

        clusterModel.getNewAppsLiveData().observe(getViewLifecycleOwner(), apps -> {
            Observable.fromIterable(apps)
                    .subscribeOn(Schedulers.io())
                    .map(NewClusterItem::new)
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSuccess(clusterItems -> fastItemAdapterNew.add(clusterItems))
                    .subscribe();
        });

        clusterModel.getUpdatedAppsLiveData().observe(getViewLifecycleOwner(), apps -> {
            Observable.fromIterable(apps)
                    .subscribeOn(Schedulers.io())
                    .map(UpdatesClusterItem::new)
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSuccess(clusterItems -> fastItemAdapterUpdates.add(clusterItems))
                    .subscribe();
        });

        final IndexViewModel indexViewModel = new ViewModelProvider(this).get(IndexViewModel.class);
        indexViewModel.getAllIndicesLive().observe(getViewLifecycleOwner(), indices -> {
            Observable.fromIterable(indices)
                    .map(IndexItem::new)
                    .toList()
                    .doOnSuccess(indexItems -> fastItemAdapterIndices.add(indexItems))
                    .subscribe();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.btn_more_new)
    public void showAllNewApps() {
        Intent intent = new Intent(requireContext(), GenericAppActivity.class);
        intent.putExtra("LIST_TYPE", 0);
        requireActivity().startActivity(intent);
    }

    @OnClick(R.id.btn_more_updated)
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
        fastItemAdapterIndices.setOnLongClickListener((view, adapter, item, position) -> {
            RepoDetailsBottomSheet.index = item.getIndex();
            RepoDetailsBottomSheet repoDetailsBottomSheet = new RepoDetailsBottomSheet();
            repoDetailsBottomSheet.show(requireActivity().getSupportFragmentManager(), "REPO_DETAILS_SHEET");
            return true;
        });
        recyclerViewIndices.setAdapter(fastItemAdapterIndices);
        recyclerViewIndices.setLayoutManager(new LinearLayoutManager(requireContext(),
                RecyclerView.HORIZONTAL, false));
    }

    private void setupNewApps() {
        fastItemAdapterNew = new FastItemAdapter<>();
        fastItemAdapterNew.setOnClickListener((view, adapter, item, position) -> {
            Intent intent = new Intent(requireContext(), DetailsActivity.class);
            intent.putExtra(DetailsActivity.INTENT_PACKAGE_NAME, item.getPackageName());
            startActivity(intent);
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
            intent.putExtra(DetailsActivity.INTENT_PACKAGE_NAME, item.getPackageName());
            startActivity(intent);
            return false;
        });

        recyclerViewUpdates.setAdapter(fastItemAdapterUpdates);
        recyclerViewUpdates.setLayoutManager(new GridLayoutManager(requireContext(), 2,
                RecyclerView.HORIZONTAL, false));
    }
}
