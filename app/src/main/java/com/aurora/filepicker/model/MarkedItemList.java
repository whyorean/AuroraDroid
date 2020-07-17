/*
 * Copyright (C) 2016 Angad Singh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aurora.filepicker.model;

import java.util.HashMap;
import java.util.Set;

public class MarkedItemList {
    private static HashMap<String, FileListItem> ourInstance = new HashMap<>();

    public static void addSelectedItem(FileListItem item) {
        ourInstance.put(item.getLocation(), item);
    }

    public static void removeSelectedItem(String key) {
        ourInstance.remove(key);
    }

    public static boolean hasItem(String key) {
        return ourInstance.containsKey(key);
    }

    public static void clearSelectionList() {
        ourInstance = new HashMap<>();
    }

    public static void addSingleFile(FileListItem item) {
        ourInstance = new HashMap<>();
        ourInstance.put(item.getLocation(), item);
    }

    public static String[] getSelectedPaths() {
        Set<String> paths = ourInstance.keySet();
        String[] filePaths = new String[paths.size()];
        int i = 0;
        for (String path : paths) {
            filePaths[i++] = path;
        }
        return filePaths;
    }

    public static int getFileCount() {
        return ourInstance.size();
    }
}
