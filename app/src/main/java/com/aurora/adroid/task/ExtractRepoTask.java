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

package com.aurora.adroid.task;

import android.content.Context;
import android.content.ContextWrapper;

import com.aurora.adroid.Constants;
import com.aurora.adroid.util.PathUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ExtractRepoTask extends ContextWrapper {

    private File file;
    private String repoDir;

    public ExtractRepoTask(Context context, File file) {
        super(context);
        this.file = file;
        this.repoDir = PathUtil.getRepoDirectory(context);
    }

    public File extract() {
        try {
            final JarFile jarFile = new JarFile(file);
            final String fileName = FilenameUtils.getBaseName(file.getName());
            for (Enumeration<JarEntry> enums = jarFile.entries(); enums.hasMoreElements(); ) {
                JarEntry entry = enums.nextElement();
                if (entry.getName().equals(Constants.DATA_FILE_NAME)) {
                    final File jsonFile = new File(repoDir + fileName + Constants.JSON);
                    FileUtils.copyToFile(jarFile.getInputStream(entry), jsonFile);
                }
            }
        } catch (Exception ignored) {
        }
        return file;
    }
}
