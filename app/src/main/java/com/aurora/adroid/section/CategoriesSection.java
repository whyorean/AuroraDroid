/*
 * Aurora Droid
 * Copyright (C) 2019-20, Rahul Kumar Patel <whyorean@gmail.com>
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
 *
 */

package com.aurora.adroid.section;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.ui.generic.activity.GenericAppActivity;
import com.aurora.adroid.util.ImageUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class CategoriesSection extends Section {

    private Context context;
    private String header;
    private List<String> categoryList;

    private Integer[] categoriesImg = {
            R.drawable.ic_connectivity,
            R.drawable.ic_developement,
            R.drawable.ic_games,
            R.drawable.ic_graphics,
            R.drawable.ic_internet,
            R.drawable.ic_money,
            R.drawable.ic_multimedia,
            R.drawable.ic_navigation,
            R.drawable.ic_phone_sms,
            R.drawable.ic_reading,
            R.drawable.ic_education,
            R.drawable.ic_security,
            R.drawable.ic_sports,
            R.drawable.ic_system,
            R.drawable.ic_theme,
            R.drawable.ic_time,
            R.drawable.ic_writings,
    };

    private int[] colorShades;

    public CategoriesSection(Context context, List<String> categoryList, String header) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.item_category_list)
                .headerResourceId(R.layout.item_header)
                .build());
        this.context = context;
        this.categoryList = categoryList;
        this.header = header;
        this.colorShades = context.getResources().getIntArray(R.array.colorShades);
    }

    @Override
    public int getContentItemsTotal() {
        return categoryList.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new ContentHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ContentHolder contentHolder = (ContentHolder) holder;
        @DrawableRes final int imageCat = categoriesImg[position];
        final String categoryName = categoryList.get(position);

        contentHolder.txtCat.setText(categoryName);
        contentHolder.imgCat.setImageDrawable(context.getDrawable(imageCat));
        contentHolder.imgCat.setImageTintList(ColorStateList.valueOf(ImageUtil.getSolidColor(position)));

        contentHolder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, GenericAppActivity.class);
            intent.putExtra("LIST_TYPE", 2);
            intent.putExtra("CATEGORY_NAME", categoryName);
            context.startActivity(intent);
        });
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        final HeaderHolder headerHolder = (HeaderHolder) holder;
        headerHolder.line1.setText(header);
    }

    static class ContentHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.cat_icon)
        AppCompatImageView imgCat;
        @BindView(R.id.cat_txt)
        AppCompatTextView txtCat;

        ContentHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.line1)
        TextView line1;

        HeaderHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
