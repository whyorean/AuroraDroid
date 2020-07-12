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
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.aurora.adroid.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HeaderLayout extends RelativeLayout {

    @BindView(R.id.title)
    AppCompatTextView title;
    @BindView(R.id.img_action)
    AppCompatImageView imgAction;

    public HeaderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public HeaderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public AppCompatImageView getImgAction() {
        return imgAction;
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        imgAction.setOnClickListener(onClickListener);
    }

    private void init(Context context, AttributeSet attrs) {
        View view = inflate(context, R.layout.layout_header, this);
        ButterKnife.bind(this, view);

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HeaderLayout);
        String textHeader = typedArray.getString(R.styleable.HeaderLayout_txtHeader);

        int iconHeaderId = typedArray.getResourceId(R.styleable.HeaderLayout_iconHeader, R.drawable.ic_entry);
        int iconActionId = typedArray.getResourceId(R.styleable.HeaderLayout_iconAction, R.drawable.icon_transparent);

        Drawable iconHeader = context.getDrawable(iconHeaderId);
        title.setCompoundDrawablesWithIntrinsicBounds(iconHeader, null, null, null);

        Drawable iconAction = context.getDrawable(iconActionId);
        imgAction.setImageDrawable(iconAction);

        title.setText(textHeader);
        typedArray.recycle();
    }
}
