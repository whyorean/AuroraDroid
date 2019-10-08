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
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.GlideApp;
import com.aurora.adroid.R;
import com.aurora.adroid.activity.DetailsActivity;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.TextUtil;
import com.aurora.adroid.util.Util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LatestUpdatedAdapter extends RecyclerView.Adapter<LatestUpdatedAdapter.ViewHolder> {

    public List<App> appList = new ArrayList<>();
    private Context context;

    public LatestUpdatedAdapter(Context context) {
        this.context = context;
    }

    public void clearData() {
        appList.clear();
        notifyDataSetChanged();
    }

    public void addData(List<App> appList) {
        this.appList.clear();
        this.appList = appList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_latest_updated, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final App app = appList.get(position);
        holder.txtTitle.setText(app.getName());
        holder.txtVersion.setText(Util.getDateFromMilli(app.getLastUpdated()));
        String summary = null;
        if (app.getLocalized() != null
                && app.getLocalized().getEnUS() != null
                && app.getLocalized().getEnUS().getSummary() != null) {
            summary = StringUtils.capitalize(TextUtil.emptyIfNull(app.getLocalized().getEnUS().getSummary()));
        } else
            summary = StringUtils.capitalize(TextUtil.emptyIfNull(app.getSummary()));
        holder.txtExtra.setText(summary);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("INTENT_APK_FILE_NAME", app.getPackageName());
            context.startActivity(intent);
        });

        if (app.getIcon() != null)
            GlideApp
                    .with(context)
                    .asBitmap()
                    .load(DatabaseUtil.getImageUrl(app))
                    .placeholder(R.drawable.ic_placeholder)
                    .into(holder.imgIcon);
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_icon)
        ImageView imgIcon;
        @BindView(R.id.txt_title)
        TextView txtTitle;
        @BindView(R.id.txt_version)
        TextView txtVersion;
        @BindView(R.id.txt_extra)
        TextView txtExtra;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
