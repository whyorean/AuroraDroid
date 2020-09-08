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

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.preference.Preference;

import com.aurora.adroid.R;
import com.aurora.filepicker.controller.DialogSelectionListener;
import com.aurora.filepicker.model.DialogConfigs;
import com.aurora.filepicker.model.DialogProperties;

import java.io.File;

public class FilePickerPreference extends Preference implements DialogSelectionListener, Preference.OnPreferenceClickListener {

    public static final String SEPARATOR = ":";

    private FilePickerDialog filePickerDialog;
    private DialogProperties properties;

    private String titleText = null;

    public FilePickerPreference(Context context) {
        super(context);
        properties = new DialogProperties();
        setOnPreferenceClickListener(this);
    }

    public FilePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        properties = new DialogProperties();
        initProperties(attrs);
        setOnPreferenceClickListener(this);
    }

    public FilePickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        properties = new DialogProperties();
        initProperties(attrs);
        setOnPreferenceClickListener(this);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return super.onGetDefaultValue(a, index);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (filePickerDialog == null || !filePickerDialog.isShowing()) {
            return superState;
        }

        final SavedState savedState = new SavedState(superState);
        savedState.bundle = filePickerDialog.onSaveInstanceState();
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        showDialog(savedState.bundle);
    }

    private void showDialog(Bundle state) {
        filePickerDialog = new FilePickerDialog(getContext());
        setProperties(properties);
        filePickerDialog.setDialogSelectionListener(this);
        if (state != null) {
            filePickerDialog.onRestoreInstanceState(state);
        }
        filePickerDialog.setTitle(titleText);
        filePickerDialog.show();
    }

    @Override
    public void onSelectedFilePaths(String[] files) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String path : files) {
            stringBuilder.append(path).append(FilePickerPreference.SEPARATOR);
        }

        String dirFiles = stringBuilder.toString();
        if (isPersistent()) {
            persistString(dirFiles);
        }

        try {
            getOnPreferenceChangeListener().onPreferenceChange(this, dirFiles);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        showDialog(null);
        return false;
    }

    public void setProperties(DialogProperties properties) {
        filePickerDialog.setDialogProperties(properties);
    }

    private void initProperties(AttributeSet attrs) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.FilePickerPreference, 0, 0);
        final int indexCount = typedArray.getIndexCount();

        for (int i = 0; i < indexCount; ++i) {
            int attr = typedArray.getIndex(i);
            if (attr == R.styleable.FilePickerPreference_selectionMode) {
                properties.selectionMode = typedArray.getInteger(R.styleable.FilePickerPreference_selectionMode, DialogConfigs.SINGLE_MODE);
            } else if (attr == R.styleable.FilePickerPreference_selectionType) {
                properties.selectionType = typedArray.getInteger(R.styleable.FilePickerPreference_selectionType, DialogConfigs.FILE_SELECT);
            } else if (attr == R.styleable.FilePickerPreference_rootDir) {
                String root_dir = typedArray.getString(R.styleable.FilePickerPreference_rootDir);
                if (root_dir != null && !root_dir.equals("")) {
                    properties.root = new File(root_dir);
                }
            } else if (attr == R.styleable.FilePickerPreference_fallbackDir) {
                String error_dir = typedArray.getString(R.styleable.FilePickerPreference_fallbackDir);
                if (error_dir != null && !error_dir.equals("")) {
                    properties.fallBackDir = new File(error_dir);
                }
            } else if (attr == R.styleable.FilePickerPreference_offsetDir) {
                String offset_dir = typedArray.getString(R.styleable.FilePickerPreference_offsetDir);
                if (offset_dir != null && !offset_dir.equals("")) {
                    properties.offset = new File(offset_dir);
                }
            } else if (attr == R.styleable.FilePickerPreference_extensions) {
                String extensions = typedArray.getString(R.styleable.FilePickerPreference_extensions);
                if (extensions != null && !extensions.equals("")) {
                    properties.extensions = extensions.split(":");
                }
            } else if (attr == R.styleable.FilePickerPreference_txtTitle) {
                titleText = typedArray.getString(R.styleable.FilePickerPreference_txtTitle);
            }
        }
        typedArray.recycle();
    }

    private static class SavedState extends BaseSavedState {

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private Bundle bundle;

        public SavedState(Parcel parcel) {
            super(parcel);
            bundle = parcel.readBundle(getClass().getClassLoader());
        }

        public SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            super.writeToParcel(parcel, flags);
            parcel.writeBundle(bundle);
        }
    }
}
