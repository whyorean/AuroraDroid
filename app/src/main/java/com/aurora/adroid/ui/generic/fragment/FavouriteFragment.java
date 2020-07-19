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

package com.aurora.adroid.ui.generic.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.manager.FavouritesManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.items.FavouriteItem;
import com.aurora.adroid.task.LiveUpdate;
import com.aurora.adroid.ui.details.DetailsActivity;
import com.aurora.adroid.ui.view.ViewFlipper2;
import com.aurora.adroid.util.ImageUtil;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PackageUtil;
import com.aurora.adroid.util.PathUtil;
import com.aurora.adroid.util.ViewUtil;
import com.aurora.adroid.viewmodel.FavouriteAppsModel;
import com.google.android.material.button.MaterialButton;
import com.google.gson.reflect.TypeToken;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.select.SelectExtension;
import com.mikepenz.fastadapter.swipe.SimpleSwipeCallback;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class FavouriteFragment extends BaseFragment implements SimpleSwipeCallback.ItemSwipeCallback {

    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.install_list)
    MaterialButton btnInstall;
    @BindView(R.id.count_selection)
    TextView txtCount;

    @BindView(R.id.empty_layout)
    RelativeLayout emptyLayout;
    @BindView(R.id.progress_layout)
    RelativeLayout progressLayout;
    @BindView(R.id.viewFlipper)
    ViewFlipper2 viewFlipper;

    private Set<App> selectedAppSet = new HashSet<>();
    private FavouriteAppsModel model;
    private FavouritesManager favouritesManager;

    private FastItemAdapter<FavouriteItem> fastItemAdapter;
    private SelectExtension<FavouriteItem> selectExtension;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        favouritesManager = new FavouritesManager(requireContext());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_generic_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_import:
                importList();
                break;
            case R.id.action_export:
                exportList();
                break;
            case R.id.action_select_all:
                if (fastItemAdapter != null && selectExtension != null) {
                    for (int i = 0; i < fastItemAdapter.getAdapterItemCount(); i++) {
                        selectExtension.select(i);
                    }
                }
                break;
            case R.id.action_clear_all:
                if (fastItemAdapter != null && selectExtension != null) {
                    for (int i = 0; i < fastItemAdapter.getAdapterItemCount(); i++) {
                        selectExtension.deselect(i);
                    }
                }
                break;
            case R.id.action_remove_all:
                if (favouritesManager != null && fastItemAdapter != null) {
                    favouritesManager.clear();
                    fastItemAdapter.clear();
                    updatePageData();
                }
                break;
        }
        return false;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_favourites, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecycler();
        model = new ViewModelProvider(this).get(FavouriteAppsModel.class);
        model.getFavouriteApps().observe(getViewLifecycleOwner(), this::dispatchToAdapter);
        model.fetchFavoriteApps();
    }

    private View.OnClickListener bulkInstallListener() {
        return v -> {
            btnInstall.setText(getString(R.string.action_installing));
            btnInstall.setEnabled(false);
            initDownload();
        };
    }

    private void initDownload() {
        for (App app : selectedAppSet) {
            try {
                new LiveUpdate(requireContext(), app).enqueueUpdate();
            } catch (Exception e) {
                Log.e("Failed to download : %s", app.getName());
            }
        }
    }

    private void dispatchToAdapter(List<FavouriteItem> favouriteItems) {
        fastItemAdapter.set(favouriteItems);
        updatePageData();
    }

    private void updatePageData() {
        updateSelectionText();
        updateButtons();

        if (fastItemAdapter != null && fastItemAdapter.getAdapterItems().size() > 0) {
            viewFlipper.switchState(ViewFlipper2.DATA);
        } else {
            viewFlipper.switchState(ViewFlipper2.EMPTY);
        }
    }

    private void updateSelectionText() {
        final int size = selectExtension.getSelectedItems().size();
        final StringBuilder countString = new StringBuilder()
                .append(requireContext().getResources().getString(R.string.list_selected))
                .append(" : ")
                .append(size);
        txtCount.setText(size > 0 ? countString : StringUtils.EMPTY);
    }

    private void updateButtons() {
        int selectionSize = selectExtension.getSelectedItems().size();
        btnInstall.setEnabled(selectionSize > 0);
        btnInstall.setOnClickListener(bulkInstallListener());
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
            intent.putExtra(Constants.STRING_REPO, app.getRepoName());
            startActivity(intent, ViewUtil.getEmptyActivityBundle((AppCompatActivity) requireActivity()));
            return false;
        });

        fastItemAdapter.addExtension(selectExtension);
        fastItemAdapter.addEventHook(new FavouriteItem.CheckBoxClickEvent());

        selectExtension.setMultiSelect(true);
        selectExtension.setSelectionListener((item, selected) -> {
            if (selected) {
                if (!PackageUtil.isInstalled(requireContext(), item.getPackageName()))
                    selectedAppSet.add(item.getApp());
            } else
                selectedAppSet.remove(item.getApp());
            updatePageData();
        });

        final SimpleSwipeCallback callback = new SimpleSwipeCallback(
                this,
                getResources().getDrawable(R.drawable.ic_delete),
                ItemTouchHelper.LEFT,
                ColorUtils.setAlphaComponent(ImageUtil.getSolidColor(0), 120));

        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(fastItemAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        new FastScrollerBuilder(recyclerView)
                .useMd2Style()
                .build();
    }

    @Override
    public void itemSwiped(int position, int direction) {
        final FavouriteItem item = fastItemAdapter.getAdapterItem(position);
        new FavouritesManager(requireContext()).removeFromFavourites(item.getApp());
        fastItemAdapter.remove(position);
        fastItemAdapter.notifyAdapterItemChanged(position);
        updatePageData();
    }

    private void exportList() {
        try {
            final List<App> packageList = favouritesManager.getFavouriteApps();
            final File baseDir = new File(PathUtil.getBaseFilesDirectory());

            /*Create base directory if it doesn't exist*/
            if (!baseDir.exists())
                baseDir.mkdirs();

            final File file = new File(baseDir.getPath() + Constants.FILE_FAVOURITES);
            final FileWriter fileWriter = new FileWriter(file);

            fileWriter.write(gson.toJson(packageList));
            fileWriter.close();
            Toast.makeText(requireContext(), StringUtils.joinWith(StringUtils.SPACE,
                    getString(R.string.string_export_to),
                    file.getPath()), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(requireContext(), R.string.string_export_failed, Toast.LENGTH_LONG).show();
            Log.e(e.getMessage());
        }
    }

    private void importList() {
        final File file = new File(PathUtil.getBaseFilesDirectory() + Constants.FILE_FAVOURITES);
        try {
            final InputStream inputStream = new FileInputStream(file);
            final byte[] bytes = IOUtils.toByteArray(inputStream);
            final String rawFavourites = new String(bytes);

            if (StringUtils.isNotEmpty(rawFavourites)) {
                Type type = new TypeToken<List<App>>() {
                }.getType();
                List<App> appList = gson.fromJson(rawFavourites, type);
                if (appList != null && !appList.isEmpty()) {
                    new FavouritesManager(requireContext()).addToFavourites(appList);
                    model.fetchFavouriteApps(appList);
                } else {
                    Toast.makeText(requireContext(), R.string.string_import_failed, Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), R.string.string_import_failed, Toast.LENGTH_LONG).show();
            Log.e(e.getMessage());
        }
    }
}
