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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.manager.BlacklistManager;
import com.aurora.adroid.model.items.BlacklistItem;
import com.aurora.adroid.ui.view.ViewFlipper2;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PathUtil;
import com.aurora.adroid.viewmodel.BlackListedAppsModel;
import com.google.gson.reflect.TypeToken;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.select.SelectExtension;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;


public class BlacklistFragment extends BaseFragment {

    @BindView(R.id.viewFlipper)
    ViewFlipper2 viewFlipper;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.txt_blacklist)
    TextView txtBlacklist;

    private BlacklistManager blacklistManager;
    private BlackListedAppsModel model;

    private FastItemAdapter<BlacklistItem> fastItemAdapter;
    private SelectExtension<BlacklistItem> selectExtension;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_generic_list, menu);
        menu.findItem(R.id.action_remove_all).setVisible(false);
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
        }
        return false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_blacklist, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecycler();

        model = new ViewModelProvider(this).get(BlackListedAppsModel.class);
        model.getBlacklistedItems().observe(getViewLifecycleOwner(), this::dispatchToAdapter);
        model.fetchBlackListedApps();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void dispatchToAdapter(List<BlacklistItem> blacklistItems) {
        fastItemAdapter.set(sortBlackListedApps(blacklistItems));
        updateCount();
        updatePageData();
    }

    /*
     * Sorts the Blacklisted Items in order Blacklisted -> Whitelisted (Alphabetically)
     */
    private List<BlacklistItem> sortBlackListedApps(List<BlacklistItem> blacklistItems) {
        final List<BlacklistItem> blackListedItems = new ArrayList<>();
        final List<BlacklistItem> whiteListedItems = new ArrayList<>();
        final List<BlacklistItem> sortedList = new ArrayList<>();

        //Sort Apps by Names
        Collections.sort(blacklistItems, (blacklistItem1, blacklistItem2) ->
                blacklistItem1.getApp().getName()
                        .compareToIgnoreCase(blacklistItem2.getApp().getName()));

        //Sort Apps by blacklist status
        for (BlacklistItem blacklistItem : blacklistItems) {
            if (blacklistManager.isBlacklisted(blacklistItem.getApp().getPackageName()))
                blackListedItems.add(blacklistItem);
            else
                whiteListedItems.add(blacklistItem);
        }

        sortedList.addAll(blackListedItems);
        sortedList.addAll(whiteListedItems);
        return sortedList;
    }

    private void updatePageData() {
        if (fastItemAdapter != null && fastItemAdapter.getAdapterItems().size() > 0) {
            viewFlipper.switchState(ViewFlipper2.DATA);
        } else {
            viewFlipper.switchState(ViewFlipper2.EMPTY);
        }
    }

    private void setupRecycler() {
        blacklistManager = new BlacklistManager(requireContext());
        fastItemAdapter = new FastItemAdapter<>();
        selectExtension = new SelectExtension<>(fastItemAdapter);

        fastItemAdapter.setOnClickListener((view, blacklistItemIAdapter, blacklistItem, position) -> false);
        fastItemAdapter.setOnPreClickListener((view, blacklistItemIAdapter, blacklistItem, position) -> true);

        fastItemAdapter.addExtension(selectExtension);
        fastItemAdapter.addEventHook(new BlacklistItem.CheckBoxClickEvent());

        selectExtension.setMultiSelect(true);
        selectExtension.setSelectionListener((item, selected) -> {
            String packageName = item.getApp().getPackageName();
            if (selected) {
                blacklistManager.addToBlacklist(packageName);
                Log.d("Blacklisted : %s", packageName);
            } else {
                blacklistManager.removeFromBlacklist(packageName);
                Log.d("Whitelisted : %s", packageName);
            }

            updateCount();
        });

        recyclerView.setAdapter(fastItemAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        new FastScrollerBuilder(recyclerView)
                .useMd2Style()
                .build();
    }

    private void updateCount() {
        final int count = blacklistManager.getBlacklistedPackages().size();
        final String txtCount = StringUtils.joinWith(" : ", getString(R.string.list_blacklist), count);
        txtBlacklist.setText(count > 0 ? txtCount : getString(R.string.list_blacklist_none));
    }

    private void exportList() {
        try {
            final Set<String> packageList = blacklistManager.getBlacklistedPackages();
            final File baseDir = new File(PathUtil.getBaseFilesDirectory());

            /*Create base directory if it doesn't exist*/
            if (!baseDir.exists())
                baseDir.mkdirs();

            final File file = new File(baseDir.getPath() + Constants.FILE_BLACKLIST);
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
        final File file = new File(PathUtil.getBaseFilesDirectory() + Constants.FILE_BLACKLIST);
        try {
            final InputStream inputStream = new FileInputStream(file);
            final byte[] bytes = IOUtils.toByteArray(inputStream);
            final String rawFavourites = new String(bytes);

            if (StringUtils.isNotEmpty(rawFavourites)) {
                Type type = new TypeToken<Set<String>>() {
                }.getType();
                Set<String> packageList = gson.fromJson(rawFavourites, type);
                if (packageList != null && !packageList.isEmpty()) {
                    new BlacklistManager(requireContext()).addToBlacklist(packageList);
                    model.fetchBlackListedApps(packageList);
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
