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

import androidx.annotation.ColorInt;

import java.util.ArrayList;
import java.util.List;

public class ImageUtil {

    private static final List<Integer> solidColors = new ArrayList<>();


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

}
