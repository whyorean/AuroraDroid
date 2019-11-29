package com.aurora.adroid.activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.SwipeToDeleteRepoCallback;
import com.aurora.adroid.adapter.RepoAdapter;
import com.aurora.adroid.database.AppDatabase;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.manager.SyncManager;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.util.DatabaseUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class RepoListActivity extends AppCompatActivity implements RepoAdapter.ItemClickListener {

    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private ActionBar actionBar;
    private RepoAdapter repoAdapter;
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repository_list);
        ButterKnife.bind(this);
        setupActionbar();
        setupRecycler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_repolist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_clear_all:
                SyncManager.clearAllSynced(this);
                SyncManager.clearRepoHeader(this);
                DatabaseUtil.setDatabaseAvailable(this, false);
                clearRepoSelections();
                clearAllTables();
                updateCount();
                break;
            case R.id.action_reset_defaults:
                break;
            case R.id.action_add:
                Intent intent = new Intent(this, RepoAddActivity.class);
                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(this);
                startActivity(intent, activityOptions.toBundle());
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        repoAdapter.updateRepos(fetchData());
    }

    private void setupActionbar() {
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setElevation(0f);
            actionBar.setTitle(getString(R.string.app_name));
        }
    }

    private void clearRepoSelections() {
        if (repoAdapter != null) {
            repoAdapter.removeSelectionsFromRepoList();
            repoAdapter.notifyDataSetChanged();
        }
    }

    private void updateCount() {
        int count = repoAdapter.getSelectedCount();
        String txtCount = new StringBuilder()
                .append(getResources().getString(R.string.list_repo_select))
                .append(" : ")
                .append(count).toString();
        actionBar.setTitle(count > 0 ? txtCount : getString(R.string.list_repo_none));
    }

    private List<Repo> fetchData() {
        return RepoListManager.getAllRepoList(this);
    }

    private void setupRecycler() {
        repoAdapter = new RepoAdapter(this, this);
        recyclerView.setAdapter(repoAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setMotionEventSplittingEnabled(false);
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeToDeleteRepoCallback(repoAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
        updateCount();
    }

    private void clearAllTables() {
        disposable.add(Observable.fromCallable(() -> AppDatabase.getAppDatabase(this)
                .clearAll())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe()
        );
    }

    @Override
    public void onItemClicked(int position) {
        repoAdapter.toggleSelection(position);
        repoAdapter.addSelectionsToRepoList();
        updateCount();
    }
}
