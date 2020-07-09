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

package com.aurora.adroid.model.items;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.model.StaticRepo;
import com.aurora.adroid.task.NetworkTask;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.Util;
import com.aurora.adroid.util.ViewUtil;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.mikepenz.fastadapter.select.SelectExtension;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;
import lombok.Setter;

import static com.aurora.adroid.Constants.SIGNED_FILE_NAME;

@Getter
@Setter
public class StaticRepoItem extends AbstractItem<StaticRepoItem.RepoItemHolder> {

    private StaticRepo staticRepo;
    private boolean checked;

    public StaticRepoItem(StaticRepo staticRepo, boolean checked) {
        this.staticRepo = staticRepo;
        this.checked = checked;
    }

    @NotNull
    @Override
    public RepoItemHolder getViewHolder(@NotNull View view) {
        return new RepoItemHolder(view);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_repo;
    }

    @Override
    public int getType() {
        return R.id.fastadapter_item;
    }

    public static class RepoItemHolder extends FastAdapter.ViewHolder<StaticRepoItem> {
        @BindView(R.id.img)
        ImageView imgRepo;
        @BindView(R.id.line1)
        TextView line1;
        @BindView(R.id.line2)
        TextView line2;
        @BindView(R.id.line3)
        TextView line3;
        @BindView(R.id.checkbox_repo)
        CheckBox checkBox;
        @BindView(R.id.layout_extra)
        LinearLayout layoutExtra;
        @BindView(R.id.img_expand)
        ImageView imgExpand;
        @BindView(R.id.txt_fingerprint)
        TextView txtFingerPrint;
        @BindView(R.id.txt_description)
        TextView txtDescription;
        @BindView(R.id.switch_mirror)
        SwitchCompat mirrorSwitch;
        @BindView(R.id.txt_mirror_url)
        TextView txtMirrorUrl;

        private Context context;
        private ArrayList<String> mirrorCheckedList = new ArrayList<>();
        private CompositeDisposable disposable = new CompositeDisposable();

        RepoItemHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.context = view.getContext();
            this.mirrorCheckedList = Util.getMirrorCheckedList(context);
        }

        @Override
        public void bindView(@NotNull StaticRepoItem item, @NotNull List<?> list) {
            final StaticRepo staticRepo = item.getStaticRepo();
            line1.setText(staticRepo.getRepoName());
            line2.setText(staticRepo.getRepoUrl());

            checkBox.setChecked(item.checked);

            if (checkBox.isChecked()) {
                line3.setText(context.getString(R.string.list_repo_availability));
                line3.setTextColor(ViewUtil.getStyledAttribute(context, android.R.attr.textColorSecondary));
                disposable.add(Observable.fromCallable(() -> new NetworkTask(context)
                        .getStatus(staticRepo.getRepoUrl() + "/" + SIGNED_FILE_NAME))
                        .subscribeOn(Schedulers.io())
                        .map(code -> code == 200)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(success -> {
                            line3.setText(success
                                    ? context.getString(R.string.list_repo_available)
                                    : context.getString(R.string.list_repo_unavailable));
                            line3.setTextColor(success
                                    ? context.getResources().getColor(R.color.colorGreen)
                                    : context.getResources().getColor(R.color.colorRed));
                            line3.setVisibility(View.VISIBLE);
                        }, throwable -> {
                            Log.e(throwable.getMessage());
                        }));
            } else {
                line3.setVisibility(View.INVISIBLE);
            }

            imgExpand.setOnClickListener(v -> {
                boolean isVisible = layoutExtra.getVisibility() == View.VISIBLE;
                if (isVisible) {
                    ViewUtil.collapse(layoutExtra);
                    ViewUtil.rotateView(imgExpand, true);
                } else {
                    ViewUtil.rotateView(imgExpand, false);
                    ViewUtil.expand(layoutExtra);
                }
            });

            setupExtra(staticRepo);
        }

        private void setupExtra(StaticRepo staticRepo) {
            txtFingerPrint.setText(staticRepo.getRepoFingerprint());
            txtDescription.setText(staticRepo.getRepoDescription());

            boolean hasMirror = staticRepo.getRepoMirrors() != null && staticRepo.getRepoMirrors().length >= 1;
            if (hasMirror) {
                txtMirrorUrl.setVisibility(View.VISIBLE);
                txtMirrorUrl.setText(staticRepo.getRepoMirrors()[0]);
                mirrorSwitch.setVisibility(View.VISIBLE);

                if (mirrorCheckedList.contains(staticRepo.getRepoId()))
                    mirrorSwitch.setChecked(true);

                mirrorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (mirrorSwitch.isChecked()) {
                        mirrorCheckedList.add(staticRepo.getRepoId());
                        Util.putMirrorCheckedList(context, mirrorCheckedList);
                    } else {
                        mirrorCheckedList.remove(staticRepo.getRepoId());
                        Util.putMirrorCheckedList(context, mirrorCheckedList);
                    }
                });
            }
        }

        @Override
        public void unbindView(@NotNull StaticRepoItem item) {
            line1.setText(null);
            line2.setText(null);
            line3.setText(null);
            ViewUtil.collapse(layoutExtra);
        }
    }

    public static final class CheckBoxClickEvent extends ClickEventHook<StaticRepoItem> {
        @Nullable
        public View onBind(@NotNull RecyclerView.ViewHolder viewHolder) {
            return viewHolder instanceof RepoItemHolder
                    ? ((RepoItemHolder) viewHolder).checkBox
                    : null;
        }

        @Override
        public void onClick(@NotNull View view, int position, @NotNull FastAdapter<StaticRepoItem> fastAdapter, @NotNull StaticRepoItem item) {
            SelectExtension<StaticRepoItem> selectExtension = fastAdapter.getExtension(SelectExtension.class);
            if (selectExtension != null) {
                selectExtension.toggleSelection(position);
                item.checked = !item.checked;
            }
        }
    }
}
