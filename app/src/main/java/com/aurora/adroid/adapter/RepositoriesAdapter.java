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

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.activity.AuroraActivity;
import com.aurora.adroid.fragment.RepositoryAppsFragment;
import com.aurora.adroid.model.Repo;
import com.aurora.adroid.util.ThemeUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RepositoriesAdapter extends RecyclerView.Adapter<RepositoriesAdapter.ViewHolder> {

    private List<Repo> repoList = new ArrayList<>();
    private Context context;
    private boolean isTransparent;

    private int[] colorShades;

    public RepositoriesAdapter(Context context) {
        this.context = context;
        this.colorShades = context.getResources().getIntArray(R.array.colorShades);
        this.isTransparent = ThemeUtil.isTransparentStyle(context);
    }

    public void clearData() {
        repoList.clear();
    }

    public void addData(List<Repo> repoList) {
        this.repoList.clear();
        this.repoList = repoList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_repository, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        final Repo repo = repoList.get(position);
        @ColorInt final int color = colorShades[(position + 3) % colorShades.length];

        viewHolder.itemView.setBackgroundTintList(ColorStateList.valueOf(
                ColorUtils.setAlphaComponent(color, isTransparent ? 60 : 255)));
        viewHolder.txtCat.setText(repo.getRepoName());
        viewHolder.txtCat.setTextColor(isTransparent ? color : Color.WHITE);
        viewHolder.imgCat.setImageDrawable(context.getDrawable(R.drawable.ic_repo_alt));
        viewHolder.imgCat.setColorFilter(isTransparent ? color : Color.WHITE);

        viewHolder.itemView.setOnClickListener(v -> {
            RepositoryAppsFragment fragment = new RepositoryAppsFragment();
            Bundle arguments = new Bundle();
            arguments.putString("REPO_ID", repo.getRepoId());
            fragment.setArguments(arguments);
            ((AuroraActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        });
    }

    @Override
    public int getItemCount() {
        return repoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.repo_icon)
        ImageView imgCat;
        @BindView(R.id.repo_txt)
        TextView txtCat;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
