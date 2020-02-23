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

package com.aurora.adroid.activity;

import android.app.Service;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
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
import butterknife.OnClick;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class SearchActivity extends BaseActivity {

    @BindView(R.id.coordinator)
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
    @BindView(R.id.action2)
    ImageView action2;

    private SectionedRecyclerViewAdapter adapter;
    private GenericAppSection section;
    private InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        Object object = getSystemService(Service.INPUT_METHOD_SERVICE);
        inputMethodManager = (InputMethodManager) object;

        SearchAppsViewModel viewModel = new ViewModelProvider(this).get(SearchAppsViewModel.class);
        viewModel.getAppsLiveData().observe(this, appList -> {
            setupRecycler(appList);
        });
        setupSearch();
        setupChip();
    }

    private void setupSearch() {
        action2.setImageDrawable(getDrawable(R.drawable.ic_cancel));
        action2.setOnClickListener(v -> {
            txtInputSearch.setText("");
        });
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

    @OnClick(R.id.action1)
    public void goBack() {
        onBackPressed();
    }

    @OnClick(R.id.fab_ime)
    public void toggleKeyBoard() {
        if (inputMethodManager != null)
            inputMethodManager.showSoftInput(txtInputSearch, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        txtInputSearch.requestFocus();
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
        section = new GenericAppSection(this, appList);
        adapter.addSection(section);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }

    private void filterApps(String query) {
        if (section != null) {
            section.filter(query);
            adapter.notifyDataSetChanged();
        }
    }
}
