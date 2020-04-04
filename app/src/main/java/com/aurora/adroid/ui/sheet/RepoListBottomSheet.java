package com.aurora.adroid.ui.sheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.manager.RepoManager;
import com.aurora.adroid.manager.SyncManager;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.model.items.RepoItem;
import com.aurora.adroid.util.ImageUtil;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.drag.ItemTouchCallback;
import com.mikepenz.fastadapter.drag.SimpleDragCallback;
import com.mikepenz.fastadapter.select.SelectExtension;
import com.mikepenz.fastadapter.swipe.SimpleSwipeCallback;
import com.mikepenz.fastadapter.swipe_drag.SimpleSwipeDragCallback;
import com.mikepenz.fastadapter.utils.DragDropUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

public class RepoListBottomSheet extends BaseBottomSheet implements ItemTouchCallback, SimpleSwipeCallback.ItemSwipeCallback {

    @BindView(R.id.recycler)
    RecyclerView recyclerView;

    private FastItemAdapter<RepoItem> fastItemAdapter;
    private SelectExtension<RepoItem> selectExtension;
    private CompositeDisposable disposable = new CompositeDisposable();

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sheet_repository_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);
        setupRecycler();
    }

    @OnClick(R.id.btn_positive)
    public void saveSelectedRepo() {

        final RepoManager repoManager = new RepoManager(requireContext());
        final SyncManager syncManager = new SyncManager(requireContext());
        final List<Repo> syncedRepo = syncManager.getSyncedRepoList();

        //Clear old selections
        repoManager.clear();

        Observable.fromIterable(fastItemAdapter.getAdapterItems())
                .filter(RepoItem::isChecked)
                .map(RepoItem::getRepo)
                .toList()
                .doOnSuccess(repos -> {
                    syncedRepo.removeAll(repos);

                    //Remove repos from synced list
                    for (Repo repo : syncedRepo) {
                        syncManager.removeFromSyncList(repo);
                    }

                    //Add new selections to repo list
                    repoManager.addAllToRepoList(repos);
                    dismissAllowingStateLoss();
                })
                .subscribe();
    }

    @OnClick(R.id.btn_negative)
    public void closeRepoSheet() {
        dismissAllowingStateLoss();
    }

    private List<Repo> fetchData() {
        return RepoListManager.getAllRepoList(requireContext());
    }

    private void setupRecycler() {
        fastItemAdapter = new FastItemAdapter<>();
        selectExtension = new SelectExtension<>(fastItemAdapter);
        selectExtension.setMultiSelect(true);

        fastItemAdapter.setOnClickListener((view, repoItemIAdapter, repoItem, integer) -> false);
        fastItemAdapter.setOnPreClickListener((view, repoItemIAdapter, repoItem, integer) -> true);

        fastItemAdapter.addExtension(selectExtension);
        fastItemAdapter.addEventHook(new RepoItem.CheckBoxClickEvent());

        selectExtension.setMultiSelect(true);

        recyclerView.setAdapter(fastItemAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        final SimpleDragCallback callback = new SimpleSwipeDragCallback(this,
                this,
                getResources().getDrawable(R.drawable.ic_delete),
                ItemTouchHelper.LEFT,
                ColorUtils.setAlphaComponent(ImageUtil.getSolidColor(0), 120));

        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        final RepoManager repoManager = new RepoManager(requireContext());

        Observable.fromIterable(fetchData())
                .map(repo -> new RepoItem(repo, repoManager.isAdded(repo)))
                .toList()
                .doOnSuccess(repoItems -> fastItemAdapter.add(repoItems))
                .subscribe();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void itemTouchDropped(int oldPosition, int newPosition) {

    }

    @Override
    public boolean itemTouchOnMove(int oldPosition, int newPosition) {
        DragDropUtil.onMove(fastItemAdapter.getItemAdapter(), oldPosition, newPosition);
        return true;
    }

    @Override
    public void itemSwiped(int position, int direction) {
        fastItemAdapter.remove(position);
        fastItemAdapter.notifyAdapterItemChanged(position);
    }
}
