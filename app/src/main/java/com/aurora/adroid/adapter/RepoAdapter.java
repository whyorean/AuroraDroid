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

package com.aurora.adroid.adapter;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.activity.RepoDetailsActivity;
import com.aurora.adroid.manager.RepoListManager;
import com.aurora.adroid.manager.SyncManager;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.task.CheckRepoUpdatesTask;
import com.aurora.adroid.task.DatabaseTask;
import com.aurora.adroid.util.ContextUtil;
import com.aurora.adroid.util.ImageUtil;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class RepoAdapter extends SelectableAdapter<RepoAdapter.ViewHolder> {

    private Context context;
    private List<Repo> repoList = new ArrayList<>();
    private ItemClickListener itemClickListener;
    private CompositeDisposable disposable = new CompositeDisposable();

    public RepoAdapter(Context context, ItemClickListener itemClickListener) {
        super(context);
        this.context = context;
        this.itemClickListener = itemClickListener;
    }

    public Context getContext() {
        return context;
    }

    public synchronized void deleteRepo(int position) {
        Repo repo = repoList.get(position);
        RepoListManager.removeRepoFromCustomList(context, repo);
        toggleSelection(position);
        repoList.remove(position);
    }

    public void updateRepos(List<Repo> repoList) {
        this.repoList.clear();
        this.repoList = repoList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_repo, parent, false);
        return new ViewHolder(itemView, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final int color = ImageUtil.getSolidColor(position);
        final Repo repo = repoList.get(position);
        holder.imgRepo.setBackgroundTintList(ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, 60)));
        holder.imgRepo.setColorFilter(color);
        holder.txtRepoTitle.setText(repo.getRepoName());
        holder.txtRepoUrl.setText(repo.getRepoUrl());
        holder.checkBoxRepo.setChecked(isSelected(repo.getRepoId()));

        holder.itemView.setOnLongClickListener(v -> {
            RepoDetailsActivity.repo = repo;
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((AppCompatActivity) context);
            context.startActivity(new Intent(context, RepoDetailsActivity.class), options.toBundle());
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return repoList.size();
    }

    @Override
    public void toggleSelection(int position) {
        String repoId = repoList.get(position).getRepoId();
        if (selections.contains(repoId)) {
            selections.remove(repoId);
            repoListManager.remove(repoId);
            if (SyncManager.isSynced(context, repoId))
                showRepoClearDialog(repoId);
        } else {
            selections.add(repoId);
        }
        notifyItemChanged(position);
    }

    public int getSelectedCount() {
        return selections.size();
    }

    private void showRepoClearDialog(String repoId) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.dialog_repo_clear_title))
                .setMessage(context.getString(R.string.dialog_repo_clear_desc))
                .setPositiveButton(context.getString(android.R.string.ok), (dialog, which) -> {
                    CheckRepoUpdatesTask.removeRepoHeader(context, repoId);
                    SyncManager.clearSynced(context, repoId);
                    disposable.add(Observable.fromCallable(() -> new DatabaseTask(context)
                            .clearRepo(repoId))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(success -> ContextUtil.toastLong(context, success
                                    ? "Repo apps cleared"
                                    : "Failed to remove repo apps")));
                })
                .setNegativeButton(context.getString(android.R.string.no), (dialog, which) -> {
                    dialog.dismiss();
                });
        ;
        builder.create();
        builder.show();
    }

    public interface ItemClickListener {
        void onItemClicked(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.img_download)
        ImageView imgRepo;
        @BindView(R.id.txt_apk_version)
        TextView txtRepoTitle;
        @BindView(R.id.txt_apk_repo)
        TextView txtRepoUrl;
        @BindView(R.id.checkbox_repo)
        MaterialCheckBox checkBoxRepo;

        private ItemClickListener listener;

        ViewHolder(View itemLayoutView, ItemClickListener listener) {
            super(itemLayoutView);
            this.listener = listener;
            ButterKnife.bind(this, itemView);
            itemLayoutView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClicked(getAdapterPosition());
            }
        }
    }
}

