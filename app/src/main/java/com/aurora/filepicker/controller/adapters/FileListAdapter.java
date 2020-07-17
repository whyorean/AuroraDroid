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

package com.aurora.filepicker.controller.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aurora.adroid.R;
import com.aurora.filepicker.controller.NotifyItemChecked;
import com.aurora.filepicker.model.DialogConfigs;
import com.aurora.filepicker.model.DialogProperties;
import com.aurora.filepicker.model.FileListItem;
import com.aurora.filepicker.model.MarkedItemList;
import com.aurora.filepicker.widget.MaterialCheckbox;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileListAdapter extends BaseAdapter {

    private ArrayList<FileListItem> fileListItems;
    private Context context;
    private DialogProperties dialogProperties;
    private NotifyItemChecked notifyItemChecked;

    public FileListAdapter(ArrayList<FileListItem> fileListItems, Context context, DialogProperties dialogProperties) {
        this.fileListItems = fileListItems;
        this.context = context;
        this.dialogProperties = dialogProperties;
    }

    @Override
    public int getCount() {
        return fileListItems.size();
    }

    @Override
    public FileListItem getItem(int i) {
        return fileListItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {

        final ViewHolder viewHolder;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.filepicker_item, viewGroup, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final FileListItem fileListItem = fileListItems.get(position);
        if (MarkedItemList.hasItem(fileListItem.getLocation())) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.marked_item_animation);
            view.setAnimation(animation);
        } else {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.unmarked_item_animation);
            view.setAnimation(animation);
        }

        if (fileListItem.isDirectory()) {
            viewHolder.imgType.setImageResource(R.drawable.ic_type_folder);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                viewHolder.imgType.setColorFilter(context.getResources().getColor(R.color.colorPrimary, context.getTheme()));
            } else {
                viewHolder.imgType.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
            }
            if (dialogProperties.selectionType == DialogConfigs.FILE_SELECT) {
                viewHolder.materialCheckbox.setVisibility(View.INVISIBLE);
            } else {
                viewHolder.materialCheckbox.setVisibility(View.VISIBLE);
            }
        } else {
            viewHolder.imgType.setImageResource(R.drawable.ic_type_file);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                viewHolder.imgType.setColorFilter(context.getResources().getColor(R.color.colorAccent, context.getTheme()));
            } else {
                viewHolder.imgType.setColorFilter(context.getResources().getColor(R.color.colorAccent));
            }
            if (dialogProperties.selectionType == DialogConfigs.DIR_SELECT) {
                viewHolder.materialCheckbox.setVisibility(View.INVISIBLE);
            } else {
                viewHolder.materialCheckbox.setVisibility(View.VISIBLE);
            }
        }

        viewHolder.imgType.setContentDescription(fileListItem.getFilename());
        viewHolder.txtFileName.setText(fileListItem.getFilename());

        SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
        Date date = new Date(fileListItem.getTime());

        if (position == 0 && fileListItem.getFilename().startsWith(context.getString(R.string.label_parent_dir))) {
            viewHolder.txtFileType.setText(R.string.label_parent_directory);
        } else {
            viewHolder.txtFileType.setText(context.getString(R.string.last_edit) + formatDate.format(date) + ", " + formatTime.format(date));
        }
        if (viewHolder.materialCheckbox.getVisibility() == View.VISIBLE) {
            if (position == 0 && fileListItem.getFilename().startsWith(context.getString(R.string.label_parent_dir))) {
                viewHolder.materialCheckbox.setVisibility(View.INVISIBLE);
            }
            if (MarkedItemList.hasItem(fileListItem.getLocation())) {
                viewHolder.materialCheckbox.setChecked(true);
            } else {
                viewHolder.materialCheckbox.setChecked(false);
            }
        }

        viewHolder.materialCheckbox.setOnCheckedChangedListener((checkbox, isChecked) -> {
            fileListItem.setMarked(isChecked);
            if (fileListItem.isMarked()) {
                if (dialogProperties.selectionMode == DialogConfigs.MULTI_MODE) {
                    MarkedItemList.addSelectedItem(fileListItem);
                } else {
                    MarkedItemList.addSingleFile(fileListItem);
                }
            } else {
                MarkedItemList.removeSelectedItem(fileListItem.getLocation());
            }
            notifyItemChecked.notifyCheckBoxIsClicked();
        });
        return view;
    }

    public void setNotifyItemCheckedListener(NotifyItemChecked notifyItemChecked) {
        this.notifyItemChecked = notifyItemChecked;
    }

    public static class ViewHolder {
        @BindView(R.id.image_type)
        ImageView imgType;
        @BindView(R.id.file_name)
        TextView txtFileName;
        @BindView(R.id.file_type)
        TextView txtFileType;
        @BindView(R.id.file_mark)
        MaterialCheckbox materialCheckbox;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
