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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.Sort;
import com.aurora.adroid.model.App;
import com.aurora.adroid.section.GenericAppSection;
import com.aurora.adroid.viewmodel.SearchAppsViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class SearchFragment extends Fragment {
    @BindView(R.id.container)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.txt_input_search)
    TextInputEditText txtInputSearch;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.sort_name_az)
    Chip chipNameAZ;
    @BindView(R.id.sort_name_za)
    Chip chipNameZA;
    @BindView(R.id.sort_size_min)
    Chip chipSizeMin;
    @BindView(R.id.sort_size_max)
    Chip chipSizeMax;
    @BindView(R.id.sort_date_updated)
    Chip chipDateUpdated;
    @BindView(R.id.sort_date_added)
    Chip chipDateAdded;

    private SectionedRecyclerViewAdapter adapter;
    private GenericAppSection section;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SearchAppsViewModel viewModel = new ViewModelProvider(this).get(SearchAppsViewModel.class);
        viewModel.getAppsLiveData().observe(getViewLifecycleOwner(), appList -> {
            setupRecycler(appList);
        });
        setupSearch();
        setupChip();
    }

    private void setupSearch() {
        txtInputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterApps(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupChip() {
        chipNameAZ.setOnClickListener(v -> sortAppsBy(Sort.NAME_AZ));
        chipNameZA.setOnClickListener(v -> sortAppsBy(Sort.NAME_ZA));
        chipSizeMin.setOnClickListener(v -> sortAppsBy(Sort.SIZE_MIN));
        chipSizeMax.setOnClickListener(v -> sortAppsBy(Sort.SIZE_MAX));
        chipSizeMax.setVisibility(View.GONE);
        chipSizeMin.setVisibility(View.GONE);
        chipDateUpdated.setOnClickListener(v -> sortAppsBy(Sort.DATE_UPDATED));
        chipDateAdded.setOnClickListener(v -> sortAppsBy(Sort.DATE_ADDED));
    }

    private void sortAppsBy(Sort sort) {
        if (section != null) {
            section.sortBy(sort);
            adapter.getAdapterForSection(section).notifyAllItemsChanged();
        }
    }

    private void setupRecycler(List<App> appList) {
        adapter = new SectionedRecyclerViewAdapter();
        section = new GenericAppSection(requireContext(), appList);
        adapter.addSection(section);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
    }

    private void filterApps(String query) {
        section.filter(query);
        adapter.notifyDataSetChanged();
    }
}
