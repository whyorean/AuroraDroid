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

package com.aurora.adroid.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.SwipeToDeleteRepoCallback;
import com.aurora.adroid.adapter.RepoAdapter;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.sheet.RepoAddSheet;
import com.aurora.adroid.util.ViewUtil;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

public class RepoListFragment extends Fragment implements RepoAdapter.ItemClickListener {

    private static final int RESULT_CODE = 6;
    private static final String RESULT_KEY = "RESULT_KEY";
    private static final String SHEET_TAG = "ADD_REPO_SHEET";

    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.fab_repo_add)
    ExtendedFloatingActionButton fabAdd;
    @BindView(R.id.txt_selection)
    TextView txtSelection;
    @BindView(R.id.btn_clear_all)
    Button btnClearAll;

    private Context context;
    private RepoAdapter repoAdapter;
    private CompositeDisposable disposable = new CompositeDisposable();

    public static Intent newIntent(boolean added) {
        Intent intent = new Intent();
        intent.putExtra(RESULT_KEY, added);
        return intent;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_repository_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupFab();
        setupClearAll();
        setupRecycler(fetchData());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (repoAdapter != null)
            repoAdapter.updateRepos(fetchData());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }

    private void clearBlackListedApps() {
        if (repoAdapter != null) {
            repoAdapter.removeSelectionsFromRepoList();
            repoAdapter.notifyDataSetChanged();
            txtSelection.setText(getString(R.string.list_blacklist_none));
        }
    }

    private void setupFab() {
        fabAdd.setOnClickListener(v -> {
            RepoAddSheet repoAddSheet = new RepoAddSheet();
            repoAddSheet.setTargetFragment(RepoListFragment.this, RESULT_CODE);
            repoAddSheet.show(getFragmentManager(), SHEET_TAG);
        });
    }

    private void setupClearAll() {
        btnClearAll.setOnClickListener(v -> {
            clearBlackListedApps();
        });
    }

    private void updateCount() {
        int count = repoAdapter.getSelectedCount();
        String txtCount = new StringBuilder()
                .append(getResources().getString(R.string.list_repo_select))
                .append(" : ")
                .append(count).toString();
        txtSelection.setText(count > 0 ? txtCount : getString(R.string.list_repo_none));
        ViewUtil.setVisibility(btnClearAll, count > 0, true);
    }

    private List<Repo> fetchData() {
        return RepoListManager.getAllRepoList(context);
    }

    private void setupRecycler(List<Repo> repoList) {
        repoAdapter = new RepoAdapter(context, repoList, this);
        recyclerView.setAdapter(repoAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(context, R.anim.anim_falldown));
        recyclerView.setMotionEventSplittingEnabled(false);
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeToDeleteRepoCallback(repoAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                if (velocityY < 0) {
                    fabAdd.show(true);
                } else if (velocityY > 0) {
                    fabAdd.hide(true);
                }
                return false;
            }
        });
        updateCount();
    }

    @Override
    public void onItemClicked(int position) {
        repoAdapter.toggleSelection(position);
        repoAdapter.addSelectionsToRepoList();
        updateCount();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == RESULT_CODE && data != null) {
            boolean added = data.getBooleanExtra(RESULT_KEY, false);
            if (added)
                repoAdapter.updateRepos(fetchData());
        }
    }
}
