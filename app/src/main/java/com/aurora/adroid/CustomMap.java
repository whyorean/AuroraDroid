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

package com.aurora.adroid;

import java.util.HashMap;
import java.util.Map;

public class CustomMap<K, V> extends HashMap<K, V> {

    private Map<V, K> reverseMap = new HashMap<V, K>();

    @Override
    public V put(K key, V value) {
        // TODO Auto-generated method stub
        reverseMap.put(value, key);
        return super.put(key, value);
    }

    public K getKey(V value) {
        return reverseMap.get(value);
    }
}