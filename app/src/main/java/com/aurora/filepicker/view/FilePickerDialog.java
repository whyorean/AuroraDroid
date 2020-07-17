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

package com.aurora.filepicker.view;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aurora.adroid.R;
import com.aurora.filepicker.controller.DialogSelectionListener;
import com.aurora.filepicker.controller.adapters.FileListAdapter;
import com.aurora.filepicker.model.DialogConfigs;
import com.aurora.filepicker.model.DialogProperties;
import com.aurora.filepicker.model.FileListItem;
import com.aurora.filepicker.model.MarkedItemList;
import com.aurora.filepicker.utils.ExtensionFilter;
import com.aurora.filepicker.utils.Utility;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FilePickerDialog extends Dialog implements AdapterView.OnItemClickListener {
    public static final int EXTERNAL_READ_PERMISSION_GRANT = 112;

    @BindView(R.id.file_list)
    ListView listView;
    @BindView(R.id.dir_name)
    TextView txtDirName;
    @BindView(R.id.dir_path)
    TextView txtDirPath;
    @BindView(R.id.txt_title)
    TextView txtTitle;
    @BindView(R.id.btn_select)
    MaterialButton btnSelect;
    @BindView(R.id.btn_cancel)
    MaterialButton btnCancel;

    private Context context;

    private DialogProperties dialogProperties;
    private DialogSelectionListener callbacks;
    private ArrayList<FileListItem> internalList;
    private ExtensionFilter extensionFilter;
    private FileListAdapter fileListAdapter;

    private String titleStr = null;
    private String positiveBtnNameStr = null;
    private String negativeBtnNameStr = null;

    public FilePickerDialog(Context context) {
        super(context);
        this.context = context;
        dialogProperties = new DialogProperties();
        extensionFilter = new ExtensionFilter(dialogProperties);
        internalList = new ArrayList<>();
    }

    public FilePickerDialog(Context context, DialogProperties dialogProperties) {
        super(context);
        this.context = context;
        this.dialogProperties = dialogProperties;
        extensionFilter = new ExtensionFilter(dialogProperties);
        internalList = new ArrayList<>();
    }

    public FilePickerDialog(Context context, DialogProperties dialogProperties, int themeResId) {
        super(context, themeResId);
        this.context = context;
        this.dialogProperties = dialogProperties;
        extensionFilter = new ExtensionFilter(dialogProperties);
        internalList = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.filepicker_layout);

        ButterKnife.bind(this);

        int size = MarkedItemList.getFileCount();
        if (size == 0) {
            btnSelect.setEnabled(false);
        }

        if (negativeBtnNameStr != null) {
            btnCancel.setText(negativeBtnNameStr);
        }

        btnSelect.setOnClickListener(view -> {
            String[] paths = MarkedItemList.getSelectedPaths();
            if (callbacks != null) {
                callbacks.onSelectedFilePaths(paths);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(view -> cancel());

        fileListAdapter = new FileListAdapter(internalList, context, dialogProperties);
        fileListAdapter.setNotifyItemCheckedListener(() -> {
            positiveBtnNameStr = positiveBtnNameStr == null ?
                    context.getResources().getString(R.string.choose_button_label) : positiveBtnNameStr;
            int size1 = MarkedItemList.getFileCount();
            if (size1 == 0) {
                btnSelect.setEnabled(false);
                btnSelect.setText(positiveBtnNameStr);
            } else {
                btnSelect.setEnabled(true);
                String button_label = positiveBtnNameStr + " (" + size1 + ") ";
                btnSelect.setText(button_label);
            }

            if (dialogProperties.selectionMode == DialogConfigs.SINGLE_MODE) {
                fileListAdapter.notifyDataSetChanged();
            }
        });

        listView.setAdapter(fileListAdapter);
        setTitle();
    }

    private void setTitle() {
        if (txtTitle == null || txtDirName == null) {
            return;
        }
        if (titleStr != null) {
            if (txtTitle.getVisibility() == View.INVISIBLE) {
                txtTitle.setVisibility(View.VISIBLE);
            }
            txtTitle.setText(titleStr);
            if (txtDirName.getVisibility() == View.VISIBLE) {
                txtDirName.setVisibility(View.INVISIBLE);
            }
        } else {
            if (txtTitle.getVisibility() == View.VISIBLE) {
                txtTitle.setVisibility(View.INVISIBLE);
            }
            if (txtDirName.getVisibility() == View.INVISIBLE) {
                txtDirName.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        positiveBtnNameStr = (
                positiveBtnNameStr == null ?
                        context.getResources().getString(R.string.choose_button_label) :
                        positiveBtnNameStr
        );
        btnSelect.setText(positiveBtnNameStr);
        if (Utility.checkStorageAccessPermissions(context)) {
            File currLoc;
            internalList.clear();
            if (dialogProperties.offset.isDirectory() && validateOffsetPath()) {
                currLoc = new File(dialogProperties.offset.getAbsolutePath());
                FileListItem parent = new FileListItem();
                parent.setFilename(context.getString(R.string.label_parent_dir));
                parent.setDirectory(true);
                parent.setLocation(currLoc.getParentFile().getAbsolutePath());
                parent.setTime(currLoc.lastModified());
                internalList.add(parent);
            } else if (dialogProperties.root.exists() && dialogProperties.root.isDirectory()) {
                currLoc = new File(dialogProperties.root.getAbsolutePath());
            } else {
                currLoc = new File(dialogProperties.fallBackDir.getAbsolutePath());
            }
            txtDirName.setText(currLoc.getName());
            txtDirPath.setText(currLoc.getAbsolutePath());
            setTitle();
            internalList = Utility.prepareFileListEntries(internalList, currLoc, extensionFilter);
            fileListAdapter.notifyDataSetChanged();
            listView.setOnItemClickListener(this);
        }
    }

    private boolean validateOffsetPath() {
        String offset_path = dialogProperties.offset.getAbsolutePath();
        String root_path = dialogProperties.root.getAbsolutePath();
        return !offset_path.equals(root_path) && offset_path.contains(root_path);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (internalList.size() > i) {
            FileListItem fitem = internalList.get(i);
            if (fitem.isDirectory()) {
                if (new File(fitem.getLocation()).canRead()) {
                    File currLoc = new File(fitem.getLocation());
                    txtDirName.setText(currLoc.getName());
                    setTitle();
                    txtDirPath.setText(currLoc.getAbsolutePath());
                    internalList.clear();
                    if (!currLoc.getName().equals(dialogProperties.root.getName())) {
                        FileListItem parent = new FileListItem();
                        parent.setFilename(context.getString(R.string.label_parent_dir));
                        parent.setDirectory(true);
                        parent.setLocation(currLoc.getParentFile().getAbsolutePath());
                        parent.setTime(currLoc.lastModified());
                        internalList.add(parent);
                    }
                    internalList = Utility.prepareFileListEntries(internalList, currLoc, extensionFilter);
                    fileListAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(context, R.string.error_dir_access, Toast.LENGTH_SHORT).show();
                }
            } else {
                MaterialCheckBox materialCheckBox = (MaterialCheckBox) view.findViewById(R.id.file_mark);
                materialCheckBox.performClick();
            }
        }
    }

    public DialogProperties getDialogProperties() {
        return dialogProperties;
    }

    public void setDialogProperties(DialogProperties dialogProperties) {
        this.dialogProperties = dialogProperties;
        extensionFilter = new ExtensionFilter(dialogProperties);
    }

    public void setDialogSelectionListener(DialogSelectionListener callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public void setTitle(CharSequence titleStr) {
        if (titleStr != null) {
            this.titleStr = titleStr.toString();
        } else {
            this.titleStr = null;
        }
        setTitle();
    }

    public void markFiles(List<String> paths) {
        if (paths != null && paths.size() > 0) {
            if (dialogProperties.selectionMode == DialogConfigs.SINGLE_MODE) {
                File temp = new File(paths.get(0));
                switch (dialogProperties.selectionType) {
                    case DialogConfigs.DIR_SELECT:
                        if (temp.exists() && temp.isDirectory()) {
                            FileListItem item = new FileListItem();
                            item.setFilename(temp.getName());
                            item.setDirectory(temp.isDirectory());
                            item.setMarked(true);
                            item.setTime(temp.lastModified());
                            item.setLocation(temp.getAbsolutePath());
                            MarkedItemList.addSelectedItem(item);
                        }
                        break;

                    case DialogConfigs.FILE_SELECT:
                        if (temp.exists() && temp.isFile()) {
                            FileListItem item = new FileListItem();
                            item.setFilename(temp.getName());
                            item.setDirectory(temp.isDirectory());
                            item.setMarked(true);
                            item.setTime(temp.lastModified());
                            item.setLocation(temp.getAbsolutePath());
                            MarkedItemList.addSelectedItem(item);
                        }
                        break;

                    case DialogConfigs.FILE_AND_DIR_SELECT:
                        if (temp.exists()) {
                            FileListItem item = new FileListItem();
                            item.setFilename(temp.getName());
                            item.setDirectory(temp.isDirectory());
                            item.setMarked(true);
                            item.setTime(temp.lastModified());
                            item.setLocation(temp.getAbsolutePath());
                            MarkedItemList.addSelectedItem(item);
                        }
                        break;
                }
            } else {
                for (String path : paths) {
                    switch (dialogProperties.selectionType) {
                        case DialogConfigs.DIR_SELECT:
                            File temp = new File(path);
                            if (temp.exists() && temp.isDirectory()) {
                                FileListItem item = new FileListItem();
                                item.setFilename(temp.getName());
                                item.setDirectory(temp.isDirectory());
                                item.setMarked(true);
                                item.setTime(temp.lastModified());
                                item.setLocation(temp.getAbsolutePath());
                                MarkedItemList.addSelectedItem(item);
                            }
                            break;

                        case DialogConfigs.FILE_SELECT:
                            temp = new File(path);
                            if (temp.exists() && temp.isFile()) {
                                FileListItem item = new FileListItem();
                                item.setFilename(temp.getName());
                                item.setDirectory(temp.isDirectory());
                                item.setMarked(true);
                                item.setTime(temp.lastModified());
                                item.setLocation(temp.getAbsolutePath());
                                MarkedItemList.addSelectedItem(item);
                            }
                            break;

                        case DialogConfigs.FILE_AND_DIR_SELECT:
                            temp = new File(path);
                            if (temp.exists() && (temp.isFile() || temp.isDirectory())) {
                                FileListItem item = new FileListItem();
                                item.setFilename(temp.getName());
                                item.setDirectory(temp.isDirectory());
                                item.setMarked(true);
                                item.setTime(temp.lastModified());
                                item.setLocation(temp.getAbsolutePath());
                                MarkedItemList.addSelectedItem(item);
                            }
                            break;
                    }
                }
            }
        }
    }

    @Override
    public void show() {
        if (!Utility.checkStorageAccessPermissions(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ((Activity) context).requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_READ_PERMISSION_GRANT);
            }
        } else {
            super.show();
            positiveBtnNameStr = positiveBtnNameStr == null ?
                    context.getResources().getString(R.string.choose_button_label) : positiveBtnNameStr;
            btnSelect.setText(positiveBtnNameStr);
            int size = MarkedItemList.getFileCount();
            if (size == 0) {
                btnSelect.setText(positiveBtnNameStr);
            } else {
                String button_label = positiveBtnNameStr + " (" + size + ") ";
                btnSelect.setText(button_label);
            }
        }
    }

    @Override
    public void onBackPressed() {
        String currentDirName = txtDirName.getText().toString();
        if (internalList.size() > 0) {
            FileListItem fitem = internalList.get(0);
            File currLoc = new File(fitem.getLocation());
            if (currentDirName.equals(dialogProperties.root.getName()) ||
                    !currLoc.canRead()) {
                super.onBackPressed();
            } else {
                txtDirName.setText(currLoc.getName());
                txtDirPath.setText(currLoc.getAbsolutePath());
                internalList.clear();
                if (!currLoc.getName().equals(dialogProperties.root.getName())) {
                    FileListItem parent = new FileListItem();
                    parent.setFilename(context.getString(R.string.label_parent_dir));
                    parent.setDirectory(true);
                    parent.setLocation(currLoc.getParentFile().getAbsolutePath());
                    parent.setTime(currLoc.lastModified());
                    internalList.add(parent);
                }
                internalList = Utility.prepareFileListEntries(internalList, currLoc, extensionFilter);
                fileListAdapter.notifyDataSetChanged();
            }
            setTitle();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void dismiss() {
        MarkedItemList.clearSelectionList();
        internalList.clear();
        super.dismiss();
    }
}
