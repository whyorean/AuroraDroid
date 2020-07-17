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

package com.aurora.filepicker.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import com.aurora.filepicker.model.FileListItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class Utility {

    public static boolean checkStorageAccessPermissions(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
            int result = context.checkCallingOrSelfPermission(permission);
            return (result == PackageManager.PERMISSION_GRANTED);
        } else {
            return true;
        }
    }

    public static ArrayList<FileListItem> prepareFileListEntries(ArrayList<FileListItem> fileListItems,
                                                                 File file,
                                                                 ExtensionFilter extensionFilter) {
        try {
            for (File name : Objects.requireNonNull(file.listFiles(extensionFilter))) {
                if (name.canRead()) {
                    FileListItem item = new FileListItem();
                    item.setFilename(name.getName());
                    item.setDirectory(name.isDirectory());
                    item.setLocation(name.getAbsolutePath());
                    item.setTime(name.lastModified());
                    fileListItems.add(item);
                }
            }
            Collections.sort(fileListItems);
        } catch (NullPointerException e) {
            e.printStackTrace();
            fileListItems = new ArrayList<>();
        }
        return fileListItems;
    }
}
