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

package com.aurora.adroid.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ImageUtil {


    private static final List<int[]> gradientInts = new ArrayList<>();
    private static final List<Integer> solidColors = new ArrayList<>();

    static {
        gradientInts.add(new int[]{0xFFEA5455, 0xFFEA5455});
        gradientInts.add(new int[]{0xFF7367F0, 0xFF7367F0});
        gradientInts.add(new int[]{0xFFF38181, 0xFFF38181});
        gradientInts.add(new int[]{0xFF32CCBC, 0xFF32CCBC});
        gradientInts.add(new int[]{0xFF28C76F, 0xFF28C76F});
        gradientInts.add(new int[]{0xFFFF6C00, 0xFFFF6C00});
    }

    static {
        solidColors.add(0xFFEA5455);
        solidColors.add(0xFF7367F0);
        solidColors.add(0xFFF38181);
        solidColors.add(0xFF32CCBC);
        solidColors.add(0xFF28C76F);
        solidColors.add(0xFFFF6C00);
    }

    @ColorInt
    public static int getSolidColor(int colorIndex) {
        return solidColors.get(colorIndex % solidColors.size());
    }

    public static GradientDrawable getDrawable(int position, int shape) {
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TR_BL,
                gradientInts.get(position % gradientInts.size()));
        gradientDrawable.setAlpha(200);
        gradientDrawable.setShape(shape);
        if (shape == GradientDrawable.RECTANGLE)
            gradientDrawable.setCornerRadius(32f);
        return gradientDrawable;
    }

    public static GradientDrawable getDrawable(int position) {
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TR_BL,
                gradientInts.get(position % gradientInts.size()));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setAlpha(200);
        return gradientDrawable;
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

}
