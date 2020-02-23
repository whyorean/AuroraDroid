package com.aurora.adroid.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.aurora.adroid.model.Index;

import java.util.List;

public class IndexRepository {

    private IndexDao indexDao;

    public IndexRepository(Application application) {
        AppDatabase appDatabase = AppDatabase.getDatabase(application);
        indexDao = appDatabase.indexDao();
    }

    public LiveData<List<Index>> getAllIndices() {
        return indexDao.getAllIndexes();
    }
}
