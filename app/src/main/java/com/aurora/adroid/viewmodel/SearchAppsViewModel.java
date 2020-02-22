package com.aurora.adroid.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.aurora.adroid.database.AppRepository;
import com.aurora.adroid.model.App;
import com.aurora.adroid.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SearchAppsViewModel extends AndroidViewModel {

    private AppRepository appRepository;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private MutableLiveData<List<App>> liveAppList = new MutableLiveData<>();

    public SearchAppsViewModel(@NonNull Application application) {
        super(application);
        appRepository = new AppRepository(application);
    }

    public LiveData<List<App>> getAppsLiveData() {
        return appRepository.getAllApps();
    }

    public void searchApps(String query) {
        final String rawQuery = "%" + query + "%";
        final String sqlQuery = "SELECT * FROM app WHERE (name like ?) OR (summary like ?) OR (`en-US-summary` like ?);";

        List<String> args = new ArrayList<>();
        args.add(rawQuery);
        args.add(rawQuery);
        args.add(rawQuery);
        SimpleSQLiteQuery sqLiteQuery = new SimpleSQLiteQuery(sqlQuery, args.toArray());

        compositeDisposable.clear();
        Disposable disposable = Observable.fromCallable(() -> appRepository.searchApps(sqLiteQuery))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(appList -> liveAppList.setValue(appList))
                .doOnError(throwable -> Log.e("Failed to search"))
                .subscribe();
        compositeDisposable.add(disposable);
    }
}
