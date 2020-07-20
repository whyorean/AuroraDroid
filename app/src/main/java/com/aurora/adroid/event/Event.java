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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Event {

    private String stringExtra;
    private int intExtra;
    private EventType type;

    public Event(EventType type) {
        this.type = type;
    }

    public Event(EventType type, String stringExtra) {
        this.type = type;
        this.stringExtra = stringExtra;
    }

    public Event(EventType type, String stringExtra, int intExtra) {
        this.type = type;
        this.stringExtra = stringExtra;
        this.intExtra = intExtra;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }
}
