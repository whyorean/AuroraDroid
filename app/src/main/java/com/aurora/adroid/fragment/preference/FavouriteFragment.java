/*
 * Aurora Droid
 * Copyright (C) 2019, Rahul Kumar Patel <whyorean@gmail.com>
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
 */

package com.aurora.adroid.fragment.preference;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.FavouriteItemTouchHelper;
import com.aurora.adroid.R;
import com.aurora.adroid.adapter.FavouriteAppsAdapter;
import com.aurora.adroid.adapter.FavouriteViewHolder;
import com.aurora.adroid.download.DownloadManager;
import com.aurora.adroid.manager.FavouriteListManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.PathUtil;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.Request;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

public class FavouriteFragment extends Fragment implements FavouriteViewHolder.ItemClickListener,
        FavouriteItemTouchHelper.RecyclerItemTouchHelperListener {

    private static final int BULK_GROUP_ID = 1996;

    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.export_list)
    Button buttonExport;
    @BindView(R.id.install_list)
    Button buttonInstall;
    @BindView(R.id.count_selection)
    TextView txtCount;

    private Context context;
    private FavouriteListManager manager;
    private List<App> favouriteApps;
    private List<App> selectedApps;
    private List<Request> requestList = new ArrayList<>();
    private ArrayList<String> favouriteList;
    private FavouriteAppsAdapter favouriteAppsAdapter;
    private CompositeDisposable disposable = new CompositeDisposable();
    private Fetch fetch;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = new FavouriteListManager(context);
        fetch = DownloadManager.getFetchInstance(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_favourites, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        buttonInstall.setOnClickListener(bulkInstallListener());
        buttonExport.setOnClickListener(v -> {
            exportList();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchData();
    }

    @Override
    public void onDestroy() {
        disposable.dispose();
        super.onDestroy();
    }

    private View.OnClickListener bulkInstallListener() {
        /*return v -> disposable.add(Observable.fromCallable(() -> new FetchAppsTask(context)
                .getAppsByPackageName(favouriteList))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((appList) -> {
                    for (App app : appList) {
                        Request request = new Request(DatabaseUtil.getDownloadURl(app),
                                PathUtil.getApkPath(context, app.getAppPackage().getApkName()));
                        request.setEnqueueAction(EnqueueAction.REPLACE_EXISTING);
                        request.setTag(app.getPackageName());
                        requestList.add(request);
                        PackageUtil.addToPseudoPackageMap(context, app.getPackageName(), app.getName());
                        PackageUtil.addToPseudoURLMap(context, app.getPackageName(), DatabaseUtil.getImageUrl(app));
                    }
                    fetch.enqueue(requestList, updatedRequestList -> {
                        String bulkInstallText = new StringBuilder()
                                .append(selectedApps.size())
                                .append(StringUtils.SPACE)
                                .append(context.getString(R.string.list_bulk_install)).toString();
                        QuickNotification.show(
                                context,
                                context.getString(R.string.app_name),
                                bulkInstallText,
                                null);
                    });
                }, err -> Log.e(err.getMessage())));*/
        return null;
    }

    private void exportList() {
        try {
            ArrayList<String> packageList = manager.get();
            File file = verifyAndGetFile();
            if (file != null) {
                OutputStream fileOutputStream = new FileOutputStream(file, false);
                for (String packageName : packageList)
                    fileOutputStream.write((packageName + System.lineSeparator()).getBytes());
                fileOutputStream.close();
                Toast.makeText(context, "List exported to" + PathUtil.getRootApkPath(context),
                        Toast.LENGTH_SHORT).show();
            }
        } catch (NullPointerException e) {
            Toast.makeText(context, "Could not create directory", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void importList() {
        ArrayList<String> packageList = new ArrayList<>();
        File file = verifyAndGetFile();
        if (file != null) {
            try {
                InputStream in = new FileInputStream(file);
                Scanner sc = new Scanner(in);
                while (sc.hasNext()) {
                    packageList.add(sc.nextLine());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "Favourite AppList not found", Toast.LENGTH_SHORT).show();
        }
        manager.addAll(packageList);
    }

    private boolean verifyDirectory() {
        PathUtil.checkBaseFavDirectory();
        File directory = new File(PathUtil.getBaseFavDirectory());
        if (!directory.exists())
            directory.mkdir();
        return (directory.exists());
    }

    private File verifyAndGetFile() {
        String fileExt = "fav_list.txt";
        boolean success = verifyDirectory();
        File file = new File(PathUtil.getBaseFavDirectory() + fileExt);
        try {
            success = file.exists() || file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (success)
            return file;
        else
            return null;
    }

    private void fetchData() {
        favouriteList = manager.get();
        /*disposable.add(Observable.fromCallable(() -> new FetchAppsTask(context)
                .getAppsByPackageName(favouriteList))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(appList -> {

                })
                .doOnComplete(() -> {
                })
                .subscribe());*/
    }

    private void setupFavourites(List<App> appsToAdd) {
        favouriteAppsAdapter = new FavouriteAppsAdapter(context, this, appsToAdd);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(favouriteAppsAdapter);
        new ItemTouchHelper(
                new FavouriteItemTouchHelper(0, ItemTouchHelper.LEFT, this))
                .attachToRecyclerView(recyclerView);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof FavouriteViewHolder) {
            favouriteAppsAdapter.remove(position);
        }
    }

    @Override
    public void onItemClicked(int position) {
        favouriteAppsAdapter.toggleSelection(position);
        selectedApps = favouriteAppsAdapter.getSelectedList();
        if (selectedApps.isEmpty()) {
            buttonInstall.setEnabled(false);
            txtCount.setText("");
        } else {
            buttonInstall.setEnabled(true);
            txtCount.setText(new StringBuilder()
                    .append(getString(R.string.list_selected))
                    .append(StringUtils.SPACE)
                    .append(selectedApps.size())
            );
        }
    }
}
