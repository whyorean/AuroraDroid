package com.aurora.adroid.model.items;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.R;
import com.aurora.adroid.model.Repo;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.mikepenz.fastadapter.select.SelectExtension;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;

public class RepoItem extends AbstractItem<RepoItem.RepoItemHolder> {

    @Getter
    private Repo repo;
    @Getter
    private boolean checked;

    public RepoItem(Repo repo, boolean checked) {
        this.repo = repo;
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

    public static class RepoItemHolder extends FastAdapter.ViewHolder<RepoItem> {
        @BindView(R.id.img)
        ImageView imgRepo;
        @BindView(R.id.line1)
        TextView txtRepoTitle;
        @BindView(R.id.line3)
        TextView txtRepoUrl;
        @BindView(R.id.checkbox_repo)
        CheckBox checkBox;

        RepoItemHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(@NotNull RepoItem item, @NotNull List<?> list) {
            txtRepoTitle.setText(item.repo.getRepoName());
            txtRepoUrl.setText(item.repo.getRepoUrl());
            checkBox.setChecked(item.checked);
        }

        @Override
        public void unbindView(@NotNull RepoItem item) {
            txtRepoTitle.setText(null);
            txtRepoUrl.setText(null);
        }
    }

    public static final class CheckBoxClickEvent extends ClickEventHook<RepoItem> {
        @Nullable
        public View onBind(@NotNull RecyclerView.ViewHolder viewHolder) {
            return viewHolder instanceof RepoItemHolder
                    ? ((RepoItemHolder) viewHolder).checkBox
                    : null;
        }

        @Override
        public void onClick(@NotNull View view, int position, @NotNull FastAdapter<RepoItem> fastAdapter, @NotNull RepoItem item) {
            SelectExtension<RepoItem> selectExtension = fastAdapter.getExtension(SelectExtension.class);
            if (selectExtension != null) {
                selectExtension.toggleSelection(position);
                item.checked = !item.checked;
            }
        }
    }
}
