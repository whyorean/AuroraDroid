package com.aurora.adroid.section;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.model.Index;
import com.aurora.adroid.ui.activity.AuroraActivity;
import com.aurora.adroid.ui.activity.GenericAppActivity;
import com.aurora.adroid.ui.sheet.RepoDetailsBottomSheet;
import com.aurora.adroid.util.ImageUtil;
import com.aurora.adroid.util.ThemeUtil;
import com.aurora.adroid.util.Util;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class IndexSection extends Section {

    private Context context;
    private List<Index> indexList;
    private boolean isTransparent;

    public IndexSection(Context context, List<Index> indexList) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.item_index)
                .build());
        this.context = context;
        this.indexList = indexList;
        this.isTransparent = ThemeUtil.isTransparentStyle(context);
    }

    @Override
    public int getContentItemsTotal() {
        return indexList.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new ContentHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ContentHolder contentHolder = (ContentHolder) holder;
        final Index index = indexList.get(position);

        @ColorInt final int color = ImageUtil.getSolidColor(position);
        final String repoName = index.getName();

        contentHolder.repoTxt.setText(repoName.contains(" ") ? repoName.split(" ")[0] : repoName);
        contentHolder.repoUpdated.setText(Util.getDateFromMilli(index.getTimestamp()));
        contentHolder.repoIcon.setImageDrawable(context.getDrawable(R.drawable.ic_repo_alt));
        contentHolder.repoIcon.setColorFilter(isTransparent ? color : Color.WHITE);

        contentHolder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, GenericAppActivity.class);
            intent.putExtra("LIST_TYPE", 3);
            intent.putExtra("REPO_ID", index.getRepoId());
            intent.putExtra("REPO_NAME", repoName);
            context.startActivity(intent);
        });

        contentHolder.itemView.setOnLongClickListener(v -> {
            RepoDetailsBottomSheet.index = index;
            RepoDetailsBottomSheet repoDetailsBottomSheet = new RepoDetailsBottomSheet();
            repoDetailsBottomSheet.show(((AuroraActivity) context).getSupportFragmentManager(), "REPO_DETAILS_SHEET");
            return false;
        });
    }

    public static class ContentHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img)
        AppCompatImageView repoIcon;
        @BindView(R.id.line1)
        AppCompatTextView repoTxt;
        @BindView(R.id.line2)
        AppCompatTextView repoUpdated;

        ContentHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
