/*
 * Aurora Droid
 * Copyright (C) 2019-20, Rahul Kumar Patel <whyorean@gmail.com>
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
 *
 */

package com.aurora.adroid.manager;

import com.aurora.adroid.model.StaticRepo;

public class RepoBundle {

    private boolean synced;
    private StaticRepo staticRepo;

    public RepoBundle(boolean status, StaticRepo staticRepo) {
        this.synced = status;
        this.staticRepo = staticRepo;
    }

    public boolean isSynced() {
        return synced;
    }

    public StaticRepo getStaticRepo() {
        return staticRepo;
    }
}
