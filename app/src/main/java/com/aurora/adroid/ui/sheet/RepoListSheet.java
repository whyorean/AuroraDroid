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

package com.aurora.adroid.ui.sheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.manager.RepoSyncManager;
import com.aurora.adroid.model.StaticRepo;
import com.aurora.adroid.model.items.StaticRepoItem;
import com.aurora.adroid.util.ContextUtil;
import com.aurora.adroid.util.ImageUtil;
import com.aurora.adroid.util.Log;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.drag.ItemTouchCallback;
import com.mikepenz.fastadapter.drag.SimpleDragCallback;
import com.mikepenz.fastadapter.select.SelectExtension;
import com.mikepenz.fastadapter.swipe.SimpleSwipeCallback;
import com.mikepenz.fastadapter.swipe_drag.SimpleSwipeDragCallback;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class RepoListSheet extends BaseBottomSheet implements ItemTouchCallback, SimpleSwipeCallback.ItemSwipeCallback {

    public static final String TAG = "REPO_LIST_SHEET";

    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.checkbox_select)
    CheckBox checkBox;

    private FastItemAdapter<StaticRepoItem> fastItemAdapter;
    private SelectExtension<StaticRepoItem> selectExtension;

    private RepoListManager repoListManager;
    private RepoSyncManager repoSyncManager;
    private CompositeDisposable disposable = new CompositeDisposable();

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_repo_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);
        repoListManager = new RepoListManager(requireContext());
        repoSyncManager = new RepoSyncManager(requireContext());
        setupRecycler();

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                selectAll();
            else
                deSelectAll();
        });
    }

    @OnClick(R.id.btn_positive)
    public void saveSelectedRepo() {
        disposable.add(Observable.fromIterable(fastItemAdapter.getAdapterItems())
                .subscribeOn(Schedulers.io())
                .filter(StaticRepoItem::isChecked)
                .map(StaticRepoItem::getStaticRepo)
                .toList()
                .subscribe(repoList -> {
                    repoSyncManager.updateRepoMap(repoList);
                    repoSyncManager.updateSyncMap(repoList);
                    repoSyncManager.updateHeaderMap(repoList);
                    ContextUtil.runOnUiThread(this::dismissAllowingStateLoss);
                }, throwable -> Log.e(throwable.getMessage())));
    }

    @OnClick(R.id.btn_negative)
    public void closeRepoSheet() {
        dismissAllowingStateLoss();
    }

    @OnClick(R.id.btn_reset)
    public void resetToDefault() {
        repoListManager.clear();
        dismissAllowingStateLoss();
    }

    public void selectAll() {
        for (StaticRepoItem staticRepoItem : fastItemAdapter.getAdapterItems()) {
            staticRepoItem.setSelected(true);
            staticRepoItem.setChecked(true);
            fastItemAdapter.notifyAdapterDataSetChanged();
        }
    }

    public void deSelectAll() {
        for (StaticRepoItem staticRepoItem : fastItemAdapter.getAdapterItems()) {
            staticRepoItem.setSelected(false);
            staticRepoItem.setChecked(false);
            fastItemAdapter.notifyAdapterDataSetChanged();
        }
    }

    private List<StaticRepo> fetchData() {
        return repoListManager.getAllRepoList();
    }

    private void setupRecycler() {
        fastItemAdapter = new FastItemAdapter<>();
        selectExtension = new SelectExtension<>(fastItemAdapter);

        selectExtension.setMultiSelect(true);
        selectExtension.setSelectWithItemUpdate(false);

        fastItemAdapter.setOnClickListener((view, repoItemIAdapter, staticRepoItem, integer) -> false);
        fastItemAdapter.setOnPreClickListener((view, repoItemIAdapter, staticRepoItem, integer) -> true);

        fastItemAdapter.addExtension(selectExtension);
        fastItemAdapter.addEventHook(new StaticRepoItem.CheckBoxClickEvent());

        recyclerView.setAdapter(fastItemAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        final SimpleDragCallback callback = new SimpleSwipeDragCallback(this,
                this,
                getResources().getDrawable(R.drawable.ic_delete),
                ItemTouchHelper.LEFT,
                ColorUtils.setAlphaComponent(ImageUtil.getSolidColor(0), 120));

        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        final List<StaticRepo> staticRepoList = fetchData();
        Collections.sort(staticRepoList, (o1, o2) -> o1.getRepoName().compareToIgnoreCase(o2.getRepoName()));
        disposable.add(Observable.fromIterable(staticRepoList)
                .map(repo -> new StaticRepoItem(repo, repoSyncManager.isAdded(repo)))
                .toList()
                .subscribe(repoItems -> fastItemAdapter.add(repoItems), throwable -> Log.e(throwable.getMessage())));
    }

    @Override
    public void onDestroy() {
        disposable.clear();
        super.onDestroy();
    }

    @Override
    public void itemTouchDropped(int oldPosition, int newPosition) {

    }

    @Override
    public boolean itemTouchOnMove(int oldPosition, int newPosition) {
        //Will use when priority for repo is done
        //DragDropUtil.onMove(fastItemAdapter.getItemAdapter(), oldPosition, newPosition);
        return false;
    }

    @Override
    public void itemSwiped(int position, int direction) {
        repoListManager.removeFromRepoMap(fastItemAdapter.getAdapterItem(position).getStaticRepo());
        fastItemAdapter.remove(position);
        fastItemAdapter.notifyAdapterItemChanged(position);
    }
}
