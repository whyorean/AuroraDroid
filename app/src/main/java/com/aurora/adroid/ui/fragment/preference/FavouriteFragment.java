/*
 * Aurora Store
 * Copyright (C) 2019, Rahul Kumar Patel <whyorean@gmail.com>
 *
 * Aurora Store is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Aurora Store is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Aurora Store.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.aurora.adroid.ui.fragment.preference;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.RecyclerDataObserver;
import com.aurora.adroid.manager.FavouritesManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.items.FavouriteItem;
import com.aurora.adroid.task.LiveUpdate;
import com.aurora.adroid.ui.activity.DetailsActivity;
import com.aurora.adroid.ui.fragment.BaseFragment;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PackageUtil;
import com.aurora.adroid.util.PathUtil;
import com.aurora.adroid.util.ViewUtil;
import com.aurora.adroid.viewmodel.FavouriteAppsModel;
import com.google.android.material.button.MaterialButton;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.select.SelectExtension;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class FavouriteFragment extends BaseFragment {

    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.export_list)
    MaterialButton btnAction;
    @BindView(R.id.install_list)
    MaterialButton btnInstall;
    @BindView(R.id.count_selection)
    TextView txtCount;

    @BindView(R.id.empty_layout)
    RelativeLayout emptyLayout;
    @BindView(R.id.progress_layout)
    RelativeLayout progressLayout;

    private Set<App> selectedAppSet = new HashSet<>();
    private FavouriteAppsModel model;
    private RecyclerDataObserver dataObserver;
    private FavouritesManager favouritesManager;

    private FastItemAdapter<FavouriteItem> fastItemAdapter;
    private SelectExtension<FavouriteItem> selectExtension;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        favouritesManager = new FavouritesManager(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_favourites, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupRecycler();

        model = new ViewModelProvider(this).get(FavouriteAppsModel.class);
        model.getFavouriteApps().observe(getViewLifecycleOwner(), favouriteItems -> {
            fastItemAdapter.add(favouriteItems);
            updatePageData();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dataObserver != null && !fastItemAdapter.getAdapterItems().isEmpty()) {
            dataObserver.hideProgress();
        }
    }

    private View.OnClickListener bulkInstallListener() {
        return v -> {
            btnInstall.setText(getString(R.string.action_installing));
            btnInstall.setEnabled(false);
            Observable
                    .fromIterable(selectedAppSet)
                    .subscribeOn(Schedulers.io())
                    .doOnNext(app -> new LiveUpdate(requireContext(), app).enqueueUpdate())
                    .doOnError(throwable -> {
                        throwable.printStackTrace();
                    })
                    .subscribe();
        };
    }

    private void exportList() {
        try {
            final List<String> packageList = favouritesManager.getFavouritePackages();
            final File file = new File(PathUtil.getBaseApkDirectory(requireContext()) + "/Favourite.txt");
            final FileWriter fileWriter = new FileWriter(file);
            for (String packageName : packageList)
                fileWriter.write((packageName + System.lineSeparator()));
            fileWriter.close();
            Toast.makeText(requireContext(), "List exported to" + file.getPath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Failed to export list", Toast.LENGTH_LONG).show();
            Log.e(e.getMessage());
        }
    }

    private void importList() {
        final ArrayList<String> packageList = new ArrayList<>();
        final File file = new File(PathUtil.getBaseApkDirectory(requireContext()) + "/Favourite.txt");
        try {
            final InputStream inputStream = new FileInputStream(file);
            final Scanner scanner = new Scanner(inputStream);
            while (scanner.hasNext()) {
                packageList.add(scanner.nextLine());
            }
            if (packageList.isEmpty()) {
                Toast.makeText(requireContext(), "Failed to import list", Toast.LENGTH_LONG).show();
            } else {
                favouritesManager.addToFavourites(packageList);
                model.fetchFavouriteApps();
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(requireContext(), "Failed to import list", Toast.LENGTH_LONG).show();
            Log.e(e.getMessage());
        }
    }

    private void updatePageData() {
        updateText();
        updateButtons();
        updateActions();

        if (dataObserver != null)
            dataObserver.checkIfEmpty();
    }

    private void updateText() {
        final int size = selectExtension.getSelectedItems().size();
        final StringBuilder countString = new StringBuilder()
                .append(requireContext().getResources().getString(R.string.list_selected))
                .append(" : ")
                .append(size);
        txtCount.setText(size > 0 ? countString : StringUtils.EMPTY);
    }

    private void updateButtons() {
        int size = selectExtension.getSelectedItems().size();
        btnInstall.setEnabled(size > 0);
    }

    private void updateActions() {
        btnInstall.setOnClickListener(bulkInstallListener());
        btnAction.setOnClickListener(v -> {
            if (fastItemAdapter.getAdapterItems().size() == 0) {
                importList();
            } else {
                exportList();
            }
        });
    }

    private void setupRecycler() {
        fastItemAdapter = new FastItemAdapter<>();
        selectExtension = new SelectExtension<>(fastItemAdapter);

        fastItemAdapter.setOnPreClickListener((view, favouriteItemIAdapter, favouriteItem, position) -> true);
        fastItemAdapter.setOnClickListener((view, favouriteItemIAdapter, favouriteItem, position) -> {
            final App app = favouriteItem.getApp();
            final Intent intent = new Intent(requireContext(), DetailsActivity.class);
            intent.putExtra(Constants.INTENT_PACKAGE_NAME, app.getPackageName());
            intent.putExtra(Constants.STRING_EXTRA, gson.toJson(app));
            startActivity(intent, ViewUtil.getEmptyActivityBundle((AppCompatActivity) requireActivity()));
            return false;
        });

        fastItemAdapter.addExtension(selectExtension);
        fastItemAdapter.addEventHook(new FavouriteItem.CheckBoxClickEvent());

        dataObserver = new RecyclerDataObserver(recyclerView, emptyLayout, progressLayout);
        fastItemAdapter.registerAdapterDataObserver(dataObserver);

        selectExtension.setMultiSelect(true);
        selectExtension.setSelectionListener((item, selected) -> {
            if (selected) {
                if (!PackageUtil.isInstalled(requireContext(), item.getPackageName()))
                    selectedAppSet.add(item.getApp());
            } else
                selectedAppSet.remove(item.getApp());
            updatePageData();
        });

        recyclerView.setAdapter(fastItemAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }
}
