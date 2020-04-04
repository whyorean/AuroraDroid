package com.aurora.adroid.model.items;

import android.content.Context;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.aurora.adroid.R;
import com.aurora.adroid.model.Index;
import com.aurora.adroid.util.Util;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;

public class IndexItem extends AbstractItem<IndexItem.RepoItemHolder> {

    @Getter
    private Index index;

    public IndexItem(Index index) {
        this.index = index;
    }

    @NotNull
    @Override
    public RepoItemHolder getViewHolder(@NotNull View view) {
        return new RepoItemHolder(view);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_index;
    }

    @Override
    public int getType() {
        return R.id.fastadapter_item;
    }

    public static class RepoItemHolder extends FastAdapter.ViewHolder<IndexItem> {
        @BindView(R.id.img)
        AppCompatImageView img;
        @BindView(R.id.line1)
        AppCompatTextView line1;
        @BindView(R.id.line2)
        AppCompatTextView line2;

        private Context context;

        RepoItemHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.context = view.getContext();
        }

        @Override
        public void bindView(@NotNull IndexItem item, @NotNull List<?> list) {
            Index index = item.getIndex();
            line1.setText(index.getName().contains(" ") ? index.getName().split(" ")[0] : index.getName());
            line2.setText(Util.getDateFromMilli(index.getTimestamp()));
            img.setImageDrawable(context.getDrawable(R.drawable.ic_repo_alt));
        }

        @Override
        public void unbindView(@NotNull IndexItem item) {
            line1.setText(null);
            line2.setText(null);
            img.setImageDrawable(null);
        }
    }
}
