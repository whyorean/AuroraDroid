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

package com.aurora.adroid.event;

public enum EventType {
    /*Sync Events*/
    SYNC_EMPTY,
    SYNC_COMPLETED,
    SYNC_NO_UPDATES,
    SYNC_FAILED,
    SYNC_PROGRESS,
    /*Download Events*/
    DOWNLOAD_INITIATED,
    SUB_DOWNLOAD_INITIATED,
    DOWNLOAD_COMPLETED,
    DOWNLOAD_CANCELLED,
    DOWNLOAD_FAILED,
    /*List Events*/
    BLACKLIST,
    WHITELIST,
    /*Package Manger Events*/
    INSTALLED,
    UNINSTALLED,
    SESSION,
    /*Misc*/
    LOG,
    BULK_UPDATE_NOTIFY,
    NO_ROOT
}
