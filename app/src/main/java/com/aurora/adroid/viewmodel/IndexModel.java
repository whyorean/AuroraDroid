package com.aurora.adroid.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.aurora.adroid.database.IndexRepository;
import com.aurora.adroid.model.Index;

import java.util.List;

public class IndexModel extends AndroidViewModel {

    private IndexRepository indexRepository;

    public IndexModel(@NonNull Application application) {
        super(application);
        indexRepository = new IndexRepository(application);
    }

    public LiveData<List<Index>> getAllIndicesLive() {
        return indexRepository.getAllIndices();
    }
}
