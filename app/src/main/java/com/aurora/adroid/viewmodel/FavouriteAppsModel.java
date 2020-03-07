package com.aurora.adroid.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aurora.adroid.database.AppRepository;
import com.aurora.adroid.manager.FavouritesManager;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.items.FavouriteItem;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FavouriteAppsModel extends BaseViewModel {

    private AppRepository appRepository;
    private List<String> packageList;

    private MutableLiveData<List<FavouriteItem>> data = new MutableLiveData<>();

    public FavouriteAppsModel(@NonNull Application application) {
        super(application);
        appRepository = new AppRepository(application);
        fetchFavouriteApps();
    }

    public LiveData<List<FavouriteItem>> getFavouriteApps() {
        return data;
    }

    public void fetchFavouriteApps() {
        final FavouritesManager favouritesManager = new FavouritesManager(getApplication());
        packageList = favouritesManager.getFavouritePackages();
        if (packageList.size() > 0) {
            Observable.fromCallable(() -> new FavouritesManager(getApplication())
                    .getFavouritePackages())
                    .subscribeOn(Schedulers.io())
                    .map(packageList -> {
                        List<App> appList = new ArrayList<>();
                        for (String packageName : packageList) {

                            final App app = appRepository.getAppByPackageName(packageName);

                            if (app == null) //Filter non-existing apps in current synced repos
                                continue;

                            appList.add(app);
                        }
                        return appList;
                    })
                    .map(apps -> sortList(apps))
                    .flatMap(apps -> Observable.fromIterable(apps).map(FavouriteItem::new))
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSuccess(favouriteItems -> data.setValue(favouriteItems))
                    .doOnError(throwable -> throwable.printStackTrace())
                    .subscribe();
        } else {
            data.setValue(new ArrayList<>());
        }
    }
}

