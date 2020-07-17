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

import java.io.File;

public class DialogProperties {
    public int selectionMode;
    public int selectionType;
    public File root;
    public File fallBackDir;
    public File offset;
    public String[] extensions;

    public DialogProperties() {
        selectionMode = DialogConfigs.SINGLE_MODE;
        selectionType = DialogConfigs.FILE_SELECT;
        root = new File(DialogConfigs.DEFAULT_DIR);
        fallBackDir = new File(DialogConfigs.DEFAULT_DIR);
        offset = new File(DialogConfigs.DEFAULT_DIR);
        extensions = null;
    }
}