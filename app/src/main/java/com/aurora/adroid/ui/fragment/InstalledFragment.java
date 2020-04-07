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
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.RecyclerDataObserver;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.items.InstalledItem;
import com.aurora.adroid.ui.activity.DetailsActivity;
import com.aurora.adroid.ui.sheet.AppMenuSheet;
import com.aurora.adroid.util.PrefUtil;
import com.aurora.adroid.util.ViewUtil;
import com.aurora.adroid.util.diff.InstalledDiffCallback;
import com.aurora.adroid.viewmodel.InstalledAppsViewModel;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InstalledFragment extends BaseFragment {

    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.switch_system)
    SwitchMaterial switchSystem;

    @BindView(R.id.empty_layout)
    RelativeLayout emptyLayout;
    @BindView(R.id.progress_layout)
    RelativeLayout progressLayout;

    private InstalledAppsViewModel model;
    private RecyclerDataObserver dataObserver;
    private FastAdapter<InstalledItem> fastAdapter;
    private ItemAdapter<InstalledItem> itemAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_installed, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecycler();

        switchSystem.setChecked(PrefUtil.getBoolean(requireContext(), Constants.PREFERENCE_INCLUDE_SYSTEM));
        switchSystem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PrefUtil.putBoolean(requireContext(), Constants.PREFERENCE_INCLUDE_SYSTEM, isChecked);
        });

        model = new ViewModelProvider(this).get(InstalledAppsViewModel.class);
        model.getData().observe(getViewLifecycleOwner(), installedItems -> {
            dispatchAppsToAdapter(installedItems);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dataObserver != null && !itemAdapter.getAdapterItems().isEmpty()) {
            dataObserver.hideProgress();
        }
    }

    private void dispatchAppsToAdapter(List<InstalledItem> installedItems) {
        final FastAdapterDiffUtil fastAdapterDiffUtil = FastAdapterDiffUtil.INSTANCE;
        final InstalledDiffCallback diffCallback = new InstalledDiffCallback();
        final DiffUtil.DiffResult diffResult = fastAdapterDiffUtil.calculateDiff(itemAdapter, installedItems, diffCallback);
        fastAdapterDiffUtil.set(itemAdapter, diffResult);

        if (dataObserver != null)
            dataObserver.checkIfEmpty();
    }

    private void setupRecycler() {

        fastAdapter = new FastAdapter<>();
        itemAdapter = new ItemAdapter<>();

        fastAdapter.addAdapter(0, itemAdapter);

        fastAdapter.setOnClickListener((view, adapter, item, position) -> {
            final App app = item.getApp();
            final Intent intent = new Intent(requireContext(), DetailsActivity.class);
            intent.putExtra(Constants.INTENT_PACKAGE_NAME, app.getPackageName());
            intent.putExtra(Constants.STRING_REPO, app.getRepoName());
            intent.putExtra(Constants.STRING_EXTRA, gson.toJson(app));
            startActivity(intent, ViewUtil.getEmptyActivityBundle((AppCompatActivity) requireActivity()));
            return false;
        });

        fastAdapter.setOnLongClickListener((view, adapter, item, position) -> {
            final AppMenuSheet menuSheet = new AppMenuSheet();
            final Bundle bundle = new Bundle();
            bundle.putInt(Constants.INT_EXTRA, position);
            bundle.putString(Constants.STRING_EXTRA, gson.toJson(item.getApp()));
            menuSheet.setArguments(bundle);
            menuSheet.show(getChildFragmentManager(), AppMenuSheet.TAG);
            return true;
        });

        dataObserver = new RecyclerDataObserver(recyclerView, emptyLayout, progressLayout);
        fastAdapter.registerAdapterDataObserver(dataObserver);

        recyclerView.setAdapter(fastAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
    }
}