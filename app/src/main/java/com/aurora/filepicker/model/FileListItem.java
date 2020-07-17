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

import java.util.Locale;

import lombok.Data;

@Data
public class FileListItem implements Comparable<FileListItem> {

    private String filename;
    private String location;
    private boolean directory;
    private boolean marked;
    private long time;

    @Override
    public int compareTo(FileListItem fileListItem) {
        if (fileListItem.isDirectory() && isDirectory()) {
            return filename.toLowerCase().compareTo(fileListItem.getFilename().toLowerCase(Locale.getDefault()));
        } else if (!fileListItem.isDirectory() && !isDirectory()) {
            return filename.toLowerCase().compareTo(fileListItem.getFilename().toLowerCase(Locale.getDefault()));
        } else if (fileListItem.isDirectory() && !isDirectory()) {
            return 1;
        } else {
            return -1;
        }
    }
}