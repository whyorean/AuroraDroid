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

package com.aurora.adroid.task;

import android.content.Context;
import android.content.ContextWrapper;

import com.aurora.adroid.util.FileUtil;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PathUtil;

import java.io.IOException;

public class ExtractRepoTask extends ContextWrapper {

    private Context context;
    private String jarFile;
    private String jsonFile;

    public ExtractRepoTask(Context context, String fileName, String jsonFileName) {
        super(context);
        this.context = context;
        this.jarFile = fileName;
        this.jsonFile = jsonFileName;
    }

    public boolean extract() {
        try {
            FileUtil.unzipJar(jarFile, PathUtil.getRepoDirectory(context), jsonFile);
            Log.i("Jar extracted successfully");
            return true;
        } catch (IOException e) {
            Log.e("Jar extraction failed");
            return false;
        }
    }
}
