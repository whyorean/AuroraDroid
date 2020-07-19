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
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.model.StaticRepo;
import com.aurora.adroid.task.NetworkTask;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.ViewUtil;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.mikepenz.fastadapter.select.SelectExtension;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public StaticRepoItem(StaticRepo staticRepo, boolean checked) {
        this.staticRepo = staticRepo;
        setSelected(checked);
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
        @BindView(R.id.line1)
        TextView line1;
        @BindView(R.id.line2)
        TextView line2;
        @BindView(R.id.line3)
        TextView line3;
        @BindView(R.id.checkbox_repo)
        CheckBox checkBox;

        private Context context;
        private CompositeDisposable disposable = new CompositeDisposable();

        RepoItemHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.context = view.getContext();
        }

        @Override
        public void bindView(@NotNull StaticRepoItem item, @NotNull List<?> list) {
            final StaticRepo staticRepo = item.getStaticRepo();
            line1.setText(staticRepo.getRepoName());

            if (StringUtils.isNotEmpty(staticRepo.getRepoDescription()))
                line2.setText(staticRepo.getRepoDescription());
            else
                line2.setText(R.string.details_no_description);

            checkBox.setChecked(item.isSelected());

            if (item.isSelected()) {
                line3.setText(context.getString(R.string.list_repo_availability));
                line3.setVisibility(View.VISIBLE);
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
                        }, throwable -> {
                            line3.setText(throwable.getMessage());
                            line3.setTextColor(context.getResources().getColor(R.color.colorRed));
                            Log.e(throwable.getMessage());
                        }));
            } else {
                line3.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void unbindView(@NotNull StaticRepoItem item) {
            line1.setText(null);
            line2.setText(null);
            line3.setText(null);
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
            }
        }
    }
}
