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

package com.aurora.adroid.fragment;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.activity.GenericAppActivity;
import com.aurora.adroid.adapter.CategoriesAdapter;
import com.aurora.adroid.adapter.RepositoriesAdapter;
import com.aurora.adroid.manager.SyncManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.section.NewAppSection;
import com.aurora.adroid.section.UpdatedAppSection;
import com.aurora.adroid.task.CategoriesTask;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.viewmodel.AppsViewModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class HomeFragment extends Fragment {

    @BindView(R.id.recycler_cat)
    RecyclerView recyclerViewCat;
    @BindView(R.id.recycler_repo)
    RecyclerView recyclerViewRepo;
    @BindView(R.id.recycler_latest)
    RecyclerView recyclerViewLatest;
    @BindView(R.id.recycler_new)
    RecyclerView recyclerViewNew;
    @BindView(R.id.btn_more_new)
    ImageButton btnMoreNew;
    @BindView(R.id.btn_more_updated)
    ImageButton btnMoreUpdated;

    private CategoriesAdapter categoriesAdapter;
    private RepositoriesAdapter repositoriesAdapter;

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
        setupCategories();
        setupRepository();
        fetchCategories();

        AppsViewModel appsViewModel = new ViewModelProvider(this).get(AppsViewModel.class);
        appsViewModel.getNewAppsLiveData().observe(getViewLifecycleOwner(), this::setupNewApps);
        appsViewModel.getUpdatedAppsLiveData().observe(getViewLifecycleOwner(), this::setupUpdatedApps);
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

    private void setupCategories() {
        categoriesAdapter = new CategoriesAdapter(requireContext());
        recyclerViewCat.setAdapter(categoriesAdapter);
        recyclerViewCat.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
    }

    private void setupRepository() {
        repositoriesAdapter = new RepositoriesAdapter(requireContext());
        repositoriesAdapter.addData(SyncManager.getSyncedRepos(requireContext()));
        recyclerViewRepo.setAdapter(repositoriesAdapter);
        recyclerViewRepo.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
    }

    private void setupNewApps(List<App> appList) {
        SectionedRecyclerViewAdapter viewAdapter = new SectionedRecyclerViewAdapter();
        NewAppSection newAppSection = new NewAppSection(requireContext(), appList);
        viewAdapter.addSection(newAppSection);
        recyclerViewNew.setAdapter(viewAdapter);
        recyclerViewNew.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
    }

    private void setupUpdatedApps(List<App> appList) {
        SectionedRecyclerViewAdapter viewAdapter = new SectionedRecyclerViewAdapter();
        UpdatedAppSection newAppSection = new UpdatedAppSection(requireContext(), appList);
        viewAdapter.addSection(newAppSection);
        recyclerViewLatest.setAdapter(viewAdapter);
        recyclerViewLatest.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
    }

    private void fetchCategories() {
        Observable.fromCallable(() -> new CategoriesTask(requireContext())
                .getCategories())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(categoryList -> {
                    if (!categoryList.isEmpty()) {
                        categoriesAdapter.addData(categoryList);
                    }
                })
                .doOnError(throwable -> Log.e(throwable.getMessage()))
                .subscribe();
    }
}
