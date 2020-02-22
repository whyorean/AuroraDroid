package com.aurora.adroid.section;

import androidx.annotation.NonNull;

public interface FilterableSection {
    void filter(@NonNull final String query);
}
