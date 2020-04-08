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

    private FastItemAdapter<RepoItem> fastItemAdapter;
    private SelectExtension<RepoItem> selectExtension;

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
                .filter(RepoItem::isChecked)
                .map(RepoItem::getRepo)
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
        for (RepoItem repoItem : fastItemAdapter.getAdapterItems()) {
            repoItem.setSelected(true);
            repoItem.setChecked(true);
            fastItemAdapter.notifyAdapterDataSetChanged();
        }
    }

    public void deSelectAll() {
        for (RepoItem repoItem : fastItemAdapter.getAdapterItems()) {
            repoItem.setSelected(false);
            repoItem.setChecked(false);
            fastItemAdapter.notifyAdapterDataSetChanged();
        }
    }

    private List<Repo> fetchData() {
        return repoListManager.getAllRepoList();
    }

    private void setupRecycler() {
        fastItemAdapter = new FastItemAdapter<>();
        selectExtension = new SelectExtension<>(fastItemAdapter);

        selectExtension.setMultiSelect(true);
        selectExtension.setSelectWithItemUpdate(false);

        fastItemAdapter.setOnClickListener((view, repoItemIAdapter, repoItem, integer) -> false);
        fastItemAdapter.setOnPreClickListener((view, repoItemIAdapter, repoItem, integer) -> true);

        fastItemAdapter.addExtension(selectExtension);
        fastItemAdapter.addEventHook(new RepoItem.CheckBoxClickEvent());

        recyclerView.setAdapter(fastItemAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        final SimpleDragCallback callback = new SimpleSwipeDragCallback(this,
                this,
                getResources().getDrawable(R.drawable.ic_delete),
                ItemTouchHelper.LEFT,
                ColorUtils.setAlphaComponent(ImageUtil.getSolidColor(0), 120));

        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        final List<Repo> repoList = fetchData();
        Collections.sort(repoList, (o1, o2) -> o1.getRepoName().compareToIgnoreCase(o2.getRepoName()));
        disposable.add(Observable.fromIterable(repoList)
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
        //Will use when priority for repo is done
        //DragDropUtil.onMove(fastItemAdapter.getItemAdapter(), oldPosition, newPosition);
        return false;
    }

    @Override
    public void itemSwiped(int position, int direction) {
        repoListManager.removeFromRepoMap(fastItemAdapter.getAdapterItem(position).getRepo());
        fastItemAdapter.remove(position);
        fastItemAdapter.notifyAdapterItemChanged(position);
    }
}
