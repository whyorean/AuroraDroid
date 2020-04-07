package com.aurora.adroid.model.items;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.GlideApp;
import com.aurora.adroid.R;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.DatabaseUtil;
import com.aurora.adroid.util.Util;
import com.google.android.material.checkbox.MaterialCheckBox;
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
import lombok.Getter;
import lombok.Setter;

public class UpdatesItem extends AbstractItem<UpdatesItem.ViewHolder> {

    @Getter
    @Setter
    private App app;
    @Getter
    @Setter
    private String packageName;

    private boolean checked;

    public UpdatesItem(App app) {
        this.app = app;
        this.packageName = app.getPackageName();
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_updates;
    }

    @NotNull
    @Override
    public ViewHolder getViewHolder(@NotNull View view) {
        return new ViewHolder(view);
    }

    @Override
    public int getType() {
        return 0;
    }

    public static class ViewHolder extends FastAdapter.ViewHolder<UpdatesItem> {

        @BindView(R.id.img)
        ImageView img;
        @BindView(R.id.line1)
        TextView line1;
        @BindView(R.id.line2)
        TextView line2;
        @BindView(R.id.line3)
        TextView line3;
        @BindView(R.id.txt_changes)
        TextView txtChanges;
        @BindView(R.id.layout_changes)
        RelativeLayout layoutChanges;
        @BindView(R.id.img_expand)
        ImageView imgExpand;
        @BindView(R.id.checkbox)
        MaterialCheckBox checkBox;

        private Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.context = itemView.getContext();
        }

        @Override
        public void bindView(@NotNull UpdatesItem item, @NotNull List<?> list) {
            final App app = item.getApp();

            line1.setText(app.getName());
            line2.setText(StringUtils.joinWith(".", app.getAppPackage().getVersionName(), app.getAppPackage().getVersionCode()));
            line3.setText(StringUtils.joinWith("•", Util.humanReadableByteValue(app.getAppPackage().getSize(), true)));

            if (app.getIcon() == null)
                img.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_placeholder));
            else
                GlideApp
                        .with(context)
                        .asBitmap()
                        .load(DatabaseUtil.getImageUrl(app))
                        .placeholder(R.drawable.ic_placeholder)
                        .into(img);

            imgExpand.setOnClickListener(v -> {
               /* boolean isVisible = layoutChanges.getVisibility() == View.VISIBLE;
                if (isVisible) {
                    ViewUtil.collapse(layoutChanges);
                    ViewUtil.rotateView(imgExpand, true);
                } else {
                    ViewUtil.rotateView(imgExpand, false);
                    ViewUtil.expand(layoutChanges);
                }*/
            });
            imgExpand.setVisibility(View.INVISIBLE);

            checkBox.setChecked(item.checked);
        }

        @Override
        public void unbindView(@NotNull UpdatesItem item) {
            line1.setText(null);
            line2.setText(null);
            line3.setText(null);
            line3.setText(null);
            img.setImageDrawable(null);
        }
    }

    public static final class CheckBoxClickEvent extends ClickEventHook<UpdatesItem> {
        @Nullable
        public View onBind(@NotNull RecyclerView.ViewHolder viewHolder) {
            return viewHolder instanceof UpdatesItem.ViewHolder
                    ? ((UpdatesItem.ViewHolder) viewHolder).checkBox
                    : null;
        }

        @Override
        public void onClick(@NotNull View view, int position, @NotNull FastAdapter<UpdatesItem> fastAdapter, @NotNull UpdatesItem item) {
            SelectExtension<UpdatesItem> selectExtension = fastAdapter.getExtension(SelectExtension.class);
            if (selectExtension != null) {
                selectExtension.toggleSelection(position);
                item.checked = !item.checked;
            }
        }
    }
}
