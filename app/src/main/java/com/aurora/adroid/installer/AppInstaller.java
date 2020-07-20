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

package com.aurora.adroid.installer;

import android.content.Context;

import com.aurora.adroid.Constants;
import com.aurora.adroid.util.PrefUtil;

public abstract class AppInstaller {

    private static volatile AppInstaller INSTANCE;

    public static AppInstaller getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppInstaller.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppInstaller() {
                        @Override
                        public InstallerBase getDefaultInstaller() {
                            String prefValue = PrefUtil.getString(context, Constants.PREFERENCE_INSTALLATION_METHOD);
                            switch (prefValue) {
                                case "0":
                                    return new NativeInstaller(context);
                                case "1":
                                    return new RootInstaller(context);
                                case "2":
                                    return new ServiceInstaller(context);
                                default:
                                    return new SessionInstaller(context);
                            }

                        }
                    };
                }
            }
        }
        return INSTANCE;
    }

    public abstract InstallerBase getDefaultInstaller();
}
