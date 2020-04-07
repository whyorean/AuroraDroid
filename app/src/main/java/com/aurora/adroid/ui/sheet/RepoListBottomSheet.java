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
import com.aurora.adroid.manager.RepoSyncManager;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.model.items.RepoItem;
import com.aurora.adroid.util.ContextUtil;
import com.aurora.adroid.util.ImageUtil;
import com.aurora.adroid.util.Log;
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
import io.reactivex.schedulers.Schedulers;

public class RepoListBottomSheet extends BaseBottomSheet implements ItemTouchCallback, SimpleSwipeCallback.ItemSwipeCallback {

    @BindView(R.id.recycler)
    RecyclerView recyclerView;

    private FastItemAdapter<RepoItem> fastItemAdapter;
    private SelectExtension<RepoItem> selectExtension;
    private RepoSyncManager repoSyncManager;
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
        repoSyncManager = new RepoSyncManager(requireContext());
        setupRecycler();
    }

    @OnClick(R.id.btn_positive)
    public void saveSelectedRepo() {
        disposable.add(Observable.fromIterable(fastItemAdapter.getAdapterItems())
                .subscribeOn(Schedulers.io())
                .filter(RepoItem::isChecked)
                .map(RepoItem::getRepo)
                .toList()
                .subscribe(repoList -> {
                    repoSyncManager.updateRepoMap(repoList);
                    repoSyncManager.updateSyncMap(repoList);
                    ContextUtil.runOnUiThread(this::dismissAllowingStateLoss);
                }, throwable -> Log.e(throwable.getMessage())));
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

        disposable.add(Observable.fromIterable(fetchData())
                .map(repo -> new RepoItem(repo, repoSyncManager.isAdded(repo)))
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
        DragDropUtil.onMove(fastItemAdapter.getItemAdapter(), oldPosition, newPosition);
        return true;
    }

    @Override
    public void itemSwiped(int position, int direction) {
        fastItemAdapter.remove(position);
        fastItemAdapter.notifyAdapterItemChanged(position);
    }
}
