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

package com.aurora.adroid.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aurora.adroid.R;

import java.util.ArrayList;
import java.util.List;

public class ViewUtil {

    private static final List<int[]> gradientInts = new ArrayList<>();
    private static final List<Integer> solidColors = new ArrayList<>();
    private static int ANIMATION_DURATION_SHORT = 250;

    static {
        gradientInts.add(new int[]{0xFFFEB692, 0xFFEA5455});
        gradientInts.add(new int[]{0xFFC9CFFC, 0xFF7367F0});
        gradientInts.add(new int[]{0xFFFCE38A, 0xFFF38181});
        gradientInts.add(new int[]{0xFF90F7EC, 0xFF32CCBC});
        gradientInts.add(new int[]{0xFF81FBB8, 0xFF28C76F});
        gradientInts.add(new int[]{0xFFFDEB71, 0xFFFF6C00});
    }

    static {
        solidColors.add(0xFFFFB900);
        solidColors.add(0xFF28C76F);
        solidColors.add(0xFFEE3440);
        solidColors.add(0xFF7367F0);
        solidColors.add(0xFF00AEFF);
        solidColors.add(0xFF32CCBC);
    }

    @ColorInt
    public static int getSolidColors(int colorIndex) {
        return solidColors.get(colorIndex % solidColors.size());
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static void setCustomColors(Context mContext, SwipeRefreshLayout swipeRefreshLayout) {
        swipeRefreshLayout.setColorSchemeColors(mContext
                .getResources()
                .getIntArray(R.array.colorShades));
    }

    @NonNull
    public static Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

    public static int getStyledAttribute(Context context, int styleID) {
        TypedArray arr = context.obtainStyledAttributes(new TypedValue().data, new int[]{styleID});
        int styledColor = arr.getColor(0, -1);
        arr.recycle();
        return styledColor;
    }

    public static void hideBottomNav(View view, boolean withAnimation) {
        ViewCompat.animate(view)
                .translationY(view.getHeight())
                .setInterpolator(new LinearOutSlowInInterpolator())
                .setDuration(withAnimation ? ANIMATION_DURATION_SHORT : 0)
                .start();
    }

    public static void showBottomNav(View view, boolean withAnimation) {
        ViewCompat.animate(view)
                .translationY(0)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .setDuration(withAnimation ? ANIMATION_DURATION_SHORT : 0)
                .start();
    }

    public static void showWithAnimation(View view) {
        final int mShortAnimationDuration = view.getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);

    }

    public static void hideWithAnimation(View view) {
        final int mShortAnimationDuration = view.getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        view.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                });
    }

    public static void rotateView(View view, boolean reverse) {
        final RotateAnimation animation = new RotateAnimation(
                reverse ? 180 : 0,
                reverse ? 0 : 180,
                (float) view.getWidth() / 2,
                (float) view.getHeight() / 2);
        animation.setDuration(ANIMATION_DURATION_SHORT);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }

    public static void expandView(final View v, int targetHeight) {
        int prevHeight = v.getHeight();
        v.setVisibility(View.VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight);
        valueAnimator.addUpdateListener(animation -> {
            v.getLayoutParams().height = (int) animation.getAnimatedValue();
            v.requestLayout();
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(ANIMATION_DURATION_SHORT);
        valueAnimator.start();
    }

    public static void collapseView(final View v, int targetHeight) {
        int prevHeight = v.getHeight();
        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(animation -> {
            v.getLayoutParams().height = (int) animation.getAnimatedValue();
            v.requestLayout();
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(ANIMATION_DURATION_SHORT);
        valueAnimator.start();
    }

    public static void setVisibility(View view, boolean visibility) {
        if (visibility)
            showWithAnimation(view);
        else
            hideWithAnimation(view);
    }

    public static void setVisibility(View view, boolean visibility, boolean noAnim) {
        if (noAnim)
            view.setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
        else
            setVisibility(view, visibility);
    }

    public static GradientDrawable getGradientDrawable(int position, int shape) {
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TR_BL,
                gradientInts.get(position % gradientInts.size()));
        gradientDrawable.setAlpha(200);
        gradientDrawable.setShape(shape);
        if (shape == GradientDrawable.RECTANGLE)
            gradientDrawable.setCornerRadius(32f);
        return gradientDrawable;
    }

    public static GradientDrawable getGradientDrawable(int color) {
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                gradientInts.get(color % gradientInts.size()));
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setAlpha(60);
        return gradientDrawable;
    }

    public static GradientDrawable getGradientDeleteDrawable() {
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xFFFE5858, 0xFFEA5455});
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setAlpha(200);
        return gradientDrawable;
    }
}
