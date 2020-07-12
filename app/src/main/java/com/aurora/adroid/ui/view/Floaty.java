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
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Floaty extends FloatingActionButton implements View.OnTouchListener {

    private final static float CLICK_DRAG_TOLERANCE = 10;

    private int viewWidth;
    private int viewHeight;
    private int parentWidth;
    private int parentHeight;
    private float newX;
    private float newY;

    private float downRawX, downRawY;
    private float dX, dY;

    private ClickListener clickListener;

    public Floaty(Context context) {
        super(context);
        init();
    }

    public Floaty(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Floaty(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

        int action = motionEvent.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            downRawX = motionEvent.getRawX();
            downRawY = motionEvent.getRawY();
            dX = view.getX() - downRawX;
            dY = view.getY() - downRawY;
            return false;
        } else if (action == MotionEvent.ACTION_MOVE) {

            viewWidth = view.getWidth();
            viewHeight = view.getHeight();

            View viewParent = (View) view.getParent();
            parentWidth = viewParent.getWidth();
            parentHeight = viewParent.getHeight();

            newX = motionEvent.getRawX() + dX;
            newX = Math.max(layoutParams.leftMargin, newX);
            newX = Math.min(parentWidth - viewWidth - layoutParams.rightMargin, newX);

            newY = motionEvent.getRawY() + dY;
            newY = Math.max(layoutParams.topMargin, newY);
            newY = Math.min(parentHeight - viewHeight - layoutParams.bottomMargin, newY);

            view.animate()
                    .x(newX)
                    .y(newY)
                    .setDuration(0)
                    .start();
            return true;
        } else if (action == MotionEvent.ACTION_UP) {
            float upRawX = motionEvent.getRawX();
            float upRawY = motionEvent.getRawY();
            float upDX = upRawX - downRawX;
            float upDY = upRawY - downRawY;

            if (newX > ((parentWidth - viewWidth - layoutParams.rightMargin) / 2.0f)) {
                newX = parentWidth - viewWidth - layoutParams.rightMargin;
            } else {
                newX = layoutParams.leftMargin;
            }

            view.animate()
                    .x(newX)
                    .y(newY)
                    .setInterpolator(new OvershootInterpolator())
                    .setDuration(300)
                    .start();

            if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE) { // A click
                if (clickListener != null) {
                    clickListener.onClick(view);
                }
            }
            return false;
        } else {
            return super.onTouchEvent(motionEvent);
        }
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onClick(View view);
    }
}
