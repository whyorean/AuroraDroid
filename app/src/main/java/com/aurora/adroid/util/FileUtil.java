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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.aurora.adroid.Constants.DATA_FILE_NAME;
import static com.aurora.adroid.Constants.JSON;

public class FileUtil {

    public static synchronized boolean unzipJar(File source, String destination) {
        try {
            final JarFile jarFile = new JarFile(source);
            final String fileName = FilenameUtils.getBaseName(source.getName());
            for (Enumeration<JarEntry> enums = jarFile.entries(); enums.hasMoreElements(); ) {
                JarEntry entry = enums.nextElement();
                if (entry.getName().equals(DATA_FILE_NAME)) {
                    final File jsonFile = new File(destination + fileName + JSON);
                    FileUtils.copyToFile(jarFile.getInputStream(entry), jsonFile);
                }
            }
            return true;
        } catch (IOException e) {
            Log.e("Failed to extract %s", source.getName());
            return false;
        }
    }
}
