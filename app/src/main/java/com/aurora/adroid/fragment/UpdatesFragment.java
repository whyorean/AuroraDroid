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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.download.DownloadManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.section.UpdatableAppSection;
import com.aurora.adroid.task.LiveUpdate;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.ViewUtil;
import com.aurora.adroid.viewmodel.UpdatableAppsViewModel;
import com.tonyodev.fetch2.Fetch;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class UpdatesFragment extends Fragment {

    private static final int UPDATE_GROUP_ID = 1338;

    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.btn_update_all)
    Button btnUpdateAll;
    @BindView(R.id.txt_update_all)
    TextView txtUpdateAll;

    private Fetch fetch;
    private boolean onGoingUpdate = false;
    private List<App> updatableAppList = new ArrayList<>();
    private CompositeDisposable disposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_updates, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetch = DownloadManager.getFetchInstance(requireContext());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        UpdatableAppsViewModel model = new ViewModelProvider(this).get(UpdatableAppsViewModel.class);
        model.getAppsLiveData().observe(getViewLifecycleOwner(), apps -> {
            setupRecycler(apps);
        });
        model.fetchUpdatableApps();
    }

    private void setupRecycler(List<App> appList) {
        SectionedRecyclerViewAdapter viewAdapter = new SectionedRecyclerViewAdapter();
        UpdatableAppSection section = new UpdatableAppSection(requireContext(), appList);
        viewAdapter.addSection(section);
        recyclerView.setAdapter(viewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
    }

    private void setupUpdateAll() {
        if (updatableAppList.isEmpty()) {
            ViewUtil.hideWithAnimation(btnUpdateAll);
            txtUpdateAll.setText(requireContext().getString(R.string.list_empty_updates));
        } else if (onGoingUpdate) {
            btnUpdateAll.setOnClickListener(cancelAllListener());
        } else {
            btnUpdateAll.setOnClickListener(updateAllListener());
        }
    }

    private void updateCounter() {
        txtUpdateAll.setText(new StringBuilder()
                .append(updatableAppList.size())
                .append(StringUtils.SPACE)
                .append(requireContext().getString(R.string.list_update_all_txt)));
    }

    private View.OnClickListener updateAllListener() {
        btnUpdateAll.setText(getString(R.string.list_update_all));
        btnUpdateAll.setEnabled(true);
        return v -> {
            updateAllApps();
            onGoingUpdate = true;
            btnUpdateAll.setText(getString(R.string.list_updating));
            btnUpdateAll.setEnabled(false);
        };
    }

    private View.OnClickListener cancelAllListener() {
        btnUpdateAll.setText(getString(R.string.action_cancel));
        btnUpdateAll.setEnabled(true);
        return v -> {
            fetch.deleteGroup(UPDATE_GROUP_ID);
            setupUpdateAll();
        };
    }

    private void updateAllApps() {
        disposable.add(Observable.fromIterable(updatableAppList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(app -> new LiveUpdate(requireContext(), app).enqueueUpdate())
                .doOnError(throwable -> {
                    Log.e(throwable.getMessage());
                })
                .subscribe());
    }
}
