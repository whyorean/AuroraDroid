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

package com.aurora.adroid.ui.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.core.graphics.ColorUtils;

import com.aurora.adroid.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LinkView extends RelativeLayout {

    @BindView(R.id.link_container)
    RelativeLayout layoutLink;
    @BindView(R.id.img_link)
    ImageView imgLink;
    @BindView(R.id.txt_link_title)
    TextView txtLinkTitle;

    private String linkText;
    private int linkImageId;
    private @ColorRes
    int color = R.color.colorAccent;
    private OnClickListener onClickListener;

    public LinkView(Context context) {
        super(context);
        init(context);
    }

    public LinkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LinkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public LinkView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public int getColor() {
        return getContext().getResources().getColor(color);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getLinkText() {
        return linkText;
    }

    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }

    public int getLinkImageId() {
        return linkImageId;
    }

    public void setLinkImageId(int linkImageId) {
        this.linkImageId = linkImageId;
    }

    public OnClickListener getOnClickListener() {
        return onClickListener;
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    private void init(Context context) {
        View view = inflate(context, R.layout.view_links, this);
        ButterKnife.bind(this, view);
    }

    public void build() {
        int color = getColor();
        txtLinkTitle.setText(getLinkText());
        imgLink.setImageDrawable(getContext().getDrawable(getLinkImageId()));
        layoutLink.setOnClickListener(getOnClickListener());
        imgLink.setColorFilter(Color.WHITE);
        layoutLink.setBackgroundTintList(ColorStateList.valueOf(ColorUtils.setAlphaComponent(color,  255)));
    }
}
