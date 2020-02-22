package com.aurora.adroid.section;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.GlideApp;
import com.aurora.adroid.R;
import com.aurora.adroid.activity.DetailsActivity;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.Util;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class NewAppSection extends Section {
    private Context context;
    private List<App> appList;

    public NewAppSection(Context context, List<App> appList) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.item_cluster)
                .build());
        this.context = context;
        this.appList = appList;
    }

    @Override
    public int getContentItemsTotal() {
        return appList.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new ContentHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ContentHolder contentHolder = (ContentHolder) holder;
        final App app = appList.get(position);

        contentHolder.txtApp.setText(app.getName());
        contentHolder.txtExtra.setText(Util.getDateFromMilli(app.getLastUpdated()));
        contentHolder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra(DetailsActivity.INTENT_PACKAGE_NAME, app.getPackageName());
            context.startActivity(intent);
        });

        if (app.getIcon() == null)
            contentHolder.imgIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_placeholder));
        else
            GlideApp
                    .with(context)
                    .asBitmap()
                    .load(DatabaseUtil.getImageUrl(app))
                    .placeholder(R.drawable.ic_placeholder)
                    .into(contentHolder.imgIcon);
    }

    public static class ContentHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_icon)
        ImageView imgIcon;
        @BindView(R.id.txt_app)
        AppCompatTextView txtApp;
        @BindView(R.id.txt_extra)
        AppCompatTextView txtExtra;

        ContentHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
