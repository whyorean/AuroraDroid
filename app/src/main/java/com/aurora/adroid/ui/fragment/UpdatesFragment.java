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
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.RecyclerDataObserver;
import com.aurora.adroid.download.DownloadManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.items.UpdatesItem;
import com.aurora.adroid.task.LiveUpdate;
import com.aurora.adroid.ui.activity.DetailsActivity;
import com.aurora.adroid.ui.sheet.AppMenuSheet;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.ViewUtil;
import com.aurora.adroid.util.diff.UpdatesDiffCallback;
import com.aurora.adroid.viewmodel.UpdatesViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil;
import com.mikepenz.fastadapter.select.SelectExtension;
import com.tonyodev.fetch2.Fetch;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class UpdatesFragment extends BaseFragment {

    private static final int UPDATE_GROUP_ID = 1338;

    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.btn_update_all)
    Button btnUpdateAll;
    @BindView(R.id.txt_update_all)
    TextView txtUpdateAll;

    @BindView(R.id.empty_layout)
    RelativeLayout emptyLayout;
    @BindView(R.id.progress_layout)
    RelativeLayout progressLayout;

    private Fetch fetch;
    private Set<UpdatesItem> selectedItems = new HashSet<>();

    private UpdatesViewModel model;
    private RecyclerDataObserver dataObserver;


    private FastAdapter<UpdatesItem> fastAdapter;
    private ItemAdapter<UpdatesItem> itemAdapter;
    private SelectExtension<UpdatesItem> selectExtension;

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetch = DownloadManager.getFetchInstance(requireContext());
        setupRecycler();

        model = new ViewModelProvider(this).get(UpdatesViewModel.class);
        model.getAppsLiveData().observe(getViewLifecycleOwner(), updatesItems -> {
            dispatchAppsToAdapter(updatesItems);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dataObserver != null && !itemAdapter.getAdapterItems().isEmpty()) {
            dataObserver.hideProgress();
        }
    }

    private void dispatchAppsToAdapter(List<UpdatesItem> updatesItems) {
        final FastAdapterDiffUtil fastAdapterDiffUtil = FastAdapterDiffUtil.INSTANCE;
        final UpdatesDiffCallback diffCallback = new UpdatesDiffCallback();
        final DiffUtil.DiffResult diffResult = fastAdapterDiffUtil.calculateDiff(itemAdapter, updatesItems, diffCallback);
        fastAdapterDiffUtil.set(itemAdapter, diffResult);

        if (dataObserver != null)
            dataObserver.checkIfEmpty();
        //updatePageData();
    }

    private void setupRecycler() {
        fastAdapter = new FastAdapter<>();
        itemAdapter = new ItemAdapter<>();
        selectExtension = new SelectExtension<>(fastAdapter);

        fastAdapter.addAdapter(0, itemAdapter);

        fastAdapter.setOnClickListener((view, adapter, item, position) -> {
            final App app = item.getApp();
            final Intent intent = new Intent(requireContext(), DetailsActivity.class);
            intent.putExtra(Constants.INTENT_PACKAGE_NAME, app.getPackageName());
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

        fastAdapter.addExtension(selectExtension);
        fastAdapter.addEventHook(new UpdatesItem.CheckBoxClickEvent());

        dataObserver = new RecyclerDataObserver(recyclerView, emptyLayout, progressLayout);
        fastAdapter.registerAdapterDataObserver(dataObserver);

        selectExtension.setMultiSelect(true);
        selectExtension.setSelectionListener((item, selected) -> {
            if (selected) {
                selectedItems.add(item);
            } else {
                selectedItems.remove(item);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(fastAdapter);
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
