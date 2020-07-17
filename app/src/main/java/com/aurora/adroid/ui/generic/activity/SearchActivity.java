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

import android.app.Service;
import android.content.Intent;
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

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.Sort;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.items.GenericItem;
import com.aurora.adroid.ui.details.DetailsActivity;
import com.aurora.adroid.ui.sheet.AppMenuSheet;
import com.aurora.adroid.ui.view.ViewFlipper2;
import com.aurora.adroid.util.LocalizationUtil;
import com.aurora.adroid.util.TextUtil;
import com.aurora.adroid.util.ViewUtil;
import com.aurora.adroid.viewmodel.SearchAppsViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.ItemFilterListener;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class SearchActivity extends BaseActivity implements ItemFilterListener<GenericItem> {

    @BindView(R.id.viewFlipper)
    ViewFlipper2 viewFlipper;
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

    private boolean imeVisible = false;
    private SearchAppsViewModel model;
    private FastAdapter<GenericItem> fastAdapter;
    private ItemAdapter<GenericItem> itemAdapter;
    private boolean isDataLoaded = false;

    private InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        Object object = getSystemService(Service.INPUT_METHOD_SERVICE);
        inputMethodManager = (InputMethodManager) object;

        setupRecycler();
        setupSearch();
        setupChip();

        model = new ViewModelProvider(this).get(SearchAppsViewModel.class);
        model.getAppsLiveData().observe(this, this::setupApps);
    }

    @Override
    protected void onResume() {
        super.onResume();
        txtInputSearch.requestFocus();
    }

    @OnClick(R.id.action1)
    public void goBack() {
        onBackPressed();
    }

    @OnClick(R.id.fab_ime)
    public void toggleKeyBoard(View view) {
        view.postDelayed(() -> {
            if (inputMethodManager != null) {
                if (imeVisible) {
                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                } else {
                    inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
                imeVisible = !imeVisible;
            }
        }, 200);
    }

    private void setupSearch() {
        action2.setImageDrawable(getDrawable(R.drawable.ic_cancel));
        action2.setOnClickListener(v -> txtInputSearch.setText(""));

        txtInputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isDataLoaded)
                    itemAdapter.filter(s);
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
        if (itemAdapter != null) {
            sortAdapterItems(sort);
        }
    }

    public void sortAdapterItems(Sort sort) {
        switch (sort) {
            case NAME_AZ:
                Collections.sort(itemAdapter.getAdapterItems(), (App1, App2) ->
                        App1.getApp().getName().compareToIgnoreCase(App2.getApp().getName()));
                break;
            case NAME_ZA:
                Collections.sort(itemAdapter.getAdapterItems(), (App1, App2) ->
                        App2.getApp().getName().compareToIgnoreCase(App1.getApp().getName()));
                break;
            case SIZE_MIN:
                Collections.sort(itemAdapter.getAdapterItems(), (App1, App2) ->
                        App1.getApp().getPkg().getSize().compareTo(App2.getApp().getPkg().getSize()));
                break;
            case SIZE_MAX:
                Collections.sort(itemAdapter.getAdapterItems(), (App1, App2) ->
                        App2.getApp().getPkg().getSize().compareTo(App1.getApp().getPkg().getSize()));
                break;
            case DATE_UPDATED:
                Collections.sort(itemAdapter.getAdapterItems(), (App1, App2) ->
                        App2.getApp().getLastUpdated().compareTo(App1.getApp().getLastUpdated()));
                break;
            case DATE_ADDED:
                Collections.sort(itemAdapter.getAdapterItems(), (App1, App2) ->
                        App2.getApp().getAdded().compareTo(App1.getApp().getAdded()));
                break;
        }
        fastAdapter.notifyAdapterDataSetChanged();
    }

    private void setupApps(List<App> apps) {
        Observable.fromIterable(apps)
                .subscribeOn(Schedulers.io())
                .map(GenericItem::new)
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(genericItems -> {
                    itemAdapter.add(genericItems);
                    isDataLoaded = true;
                    updatePageData();
                })
                .subscribe();
    }

    private void setupRecycler() {
        fastAdapter = new FastAdapter<>();
        itemAdapter = new ItemAdapter<>();

        fastAdapter.addAdapter(0, itemAdapter);

        fastAdapter.setOnClickListener((view, adapter, item, position) -> {
            final App app = item.getApp();
            final Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra(Constants.INTENT_PACKAGE_NAME, app.getPackageName());
            intent.putExtra(Constants.STRING_EXTRA, gson.toJson(app));
            intent.putExtra(Constants.STRING_REPO, app.getRepoName());
            startActivity(intent, ViewUtil.getEmptyActivityBundle(this));
            return false;
        });

        fastAdapter.setOnLongClickListener((view, adapter, item, position) -> {
            final AppMenuSheet menuSheet = new AppMenuSheet();
            final Bundle bundle = new Bundle();
            bundle.putInt(Constants.INT_EXTRA, position);
            bundle.putString(Constants.STRING_EXTRA, gson.toJson(item.getApp()));
            menuSheet.setArguments(bundle);
            menuSheet.show(getSupportFragmentManager(), AppMenuSheet.TAG);
            return true;
        });

        itemAdapter.getItemFilter().setFilterPredicate((genericItem, charSequence) -> {
            final App app = genericItem.getApp();

            String query = charSequence.toString();
            query = TextUtil.cleanUpString(query);

            String appName = app.getName();
            appName = TextUtil.cleanUpString(appName);

            if (appName.contains(query)) {
                return true;
            }

            String summary = LocalizationUtil.getLocalizedSummary(this, app);

            if (!summary.isEmpty()) {
                summary = TextUtil.cleanUpString(summary);
                return summary.contains(query);
            }

            return false;
        });

        itemAdapter.getItemFilter().setItemFilterListener(this);

        recyclerView.setAdapter(fastAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        new FastScrollerBuilder(recyclerView)
                .useMd2Style()
                .build();
    }

    private void updatePageData() {
        if (itemAdapter != null && itemAdapter.getAdapterItems().size() > 0) {
            viewFlipper.switchState(ViewFlipper2.DATA);
        } else {
            viewFlipper.switchState(ViewFlipper2.EMPTY);
        }
    }

    @Override
    public void onReset() {
    }

    @Override
    public void itemsFiltered(@Nullable CharSequence charSequence, @Nullable List<? extends GenericItem> list) {
        updatePageData();
    }
}
