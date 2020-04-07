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
import com.aurora.adroid.model.Repo;
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
public class RepoItem extends AbstractItem<RepoItem.RepoItemHolder> {

    private Repo repo;
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
        public void bindView(@NotNull RepoItem item, @NotNull List<?> list) {
            final Repo repo = item.getRepo();
            line1.setText(repo.getRepoName());
            line2.setText(repo.getRepoUrl());

            checkBox.setChecked(item.checked);

            if (checkBox.isChecked()) {
                line3.setText(context.getString(R.string.list_repo_availability));
                line3.setTextColor(ViewUtil.getStyledAttribute(context, android.R.attr.textColorSecondary));
                disposable.add(Observable.fromCallable(() -> new NetworkTask(context)
                        .getStatus(repo.getRepoUrl() + "/" + SIGNED_FILE_NAME))
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

            setupExtra(repo);
        }

        private void setupExtra(Repo repo) {
            txtFingerPrint.setText(repo.getRepoFingerprint());
            txtDescription.setText(repo.getRepoDescription());

            boolean hasMirror = repo.getRepoMirrors() != null && repo.getRepoMirrors().length >= 1;
            if (hasMirror) {
                txtMirrorUrl.setVisibility(View.VISIBLE);
                txtMirrorUrl.setText(repo.getRepoMirrors()[0]);
                mirrorSwitch.setVisibility(View.VISIBLE);

                if (mirrorCheckedList.contains(repo.getRepoId()))
                    mirrorSwitch.setChecked(true);

                mirrorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (mirrorSwitch.isChecked()) {
                        mirrorCheckedList.add(repo.getRepoId());
                        Util.putMirrorCheckedList(context, mirrorCheckedList);
                    } else {
                        mirrorCheckedList.remove(repo.getRepoId());
                        Util.putMirrorCheckedList(context, mirrorCheckedList);
                    }
                });
            }
        }

        @Override
        public void unbindView(@NotNull RepoItem item) {
            line1.setText(null);
            line2.setText(null);
            line3.setText(null);
            ViewUtil.collapse(layoutExtra);
            disposable.clear();
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
