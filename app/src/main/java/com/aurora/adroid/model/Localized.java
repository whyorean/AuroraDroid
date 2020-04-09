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

package com.aurora.adroid.model;

import androidx.room.Embedded;

import com.aurora.adroid.model.locales.Af;
import com.aurora.adroid.model.locales.Am;
import com.aurora.adroid.model.locales.Ar;
import com.aurora.adroid.model.locales.Bg;
import com.aurora.adroid.model.locales.Bo;
import com.aurora.adroid.model.locales.Ca;
import com.aurora.adroid.model.locales.Cs;
import com.aurora.adroid.model.locales.Da;
import com.aurora.adroid.model.locales.De;
import com.aurora.adroid.model.locales.El;
import com.aurora.adroid.model.locales.EnUS;
import com.aurora.adroid.model.locales.Eo;
import com.aurora.adroid.model.locales.Es;
import com.aurora.adroid.model.locales.Et;
import com.aurora.adroid.model.locales.Eu;
import com.aurora.adroid.model.locales.Fi;
import com.aurora.adroid.model.locales.Fr;
import com.aurora.adroid.model.locales.Hi;
import com.aurora.adroid.model.locales.Hr;
import com.aurora.adroid.model.locales.Hu;
import com.aurora.adroid.model.locales.Id;
import com.aurora.adroid.model.locales.Is;
import com.aurora.adroid.model.locales.It;
import com.aurora.adroid.model.locales.Ja;
import com.aurora.adroid.model.locales.Ko;
import com.aurora.adroid.model.locales.Lt;
import com.aurora.adroid.model.locales.Lv;
import com.aurora.adroid.model.locales.Nb;
import com.aurora.adroid.model.locales.Nl;
import com.aurora.adroid.model.locales.Pl;
import com.aurora.adroid.model.locales.PtBR;
import com.aurora.adroid.model.locales.PtPT;
import com.aurora.adroid.model.locales.Ro;
import com.aurora.adroid.model.locales.Ru;
import com.aurora.adroid.model.locales.Sk;
import com.aurora.adroid.model.locales.Sl;
import com.aurora.adroid.model.locales.Sv;
import com.aurora.adroid.model.locales.Sw;
import com.aurora.adroid.model.locales.Th;
import com.aurora.adroid.model.locales.Tr;
import com.aurora.adroid.model.locales.Ug;
import com.aurora.adroid.model.locales.Uk;
import com.aurora.adroid.model.locales.Vi;
import com.aurora.adroid.model.locales.ZhCN;
import com.aurora.adroid.model.locales.ZhTW;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class Localized {
    @SerializedName("af")
    @Expose
    @Embedded(prefix = "af-")
    private Af af;
    @SerializedName("am")
    @Expose
    @Embedded(prefix = "am-")
    private Am am;
    @SerializedName("ar")
    @Expose
    @Embedded(prefix = "ar-")
    private Ar ar;
    @SerializedName("bg")
    @Expose
    @Embedded(prefix = "bg-")
    private Bg bg;
    @SerializedName("bo")
    @Expose
    @Embedded(prefix = "bo-")
    private Bo bo;
    @SerializedName("ca")
    @Expose
    @Embedded(prefix = "ca-")
    private Ca ca;
    @SerializedName("cs")
    @Expose
    @Embedded(prefix = "cs-")
    private Cs cs;
    @SerializedName("da")
    @Expose
    @Embedded(prefix = "da-")
    private Da da;
    @SerializedName("de")
    @Expose
    @Embedded(prefix = "de-")
    private De de;
    @SerializedName("el")
    @Expose
    @Embedded(prefix = "el-")
    private El el;
    @SerializedName("eo")
    @Expose
    @Embedded(prefix = "eo-")
    private Eo eo;
    @SerializedName("eu")
    @Expose
    @Embedded(prefix = "eu-")
    private Eu eu;
    @SerializedName("en-US")
    @Expose
    @Embedded(prefix = "en-US-")
    private EnUS enUS;
    @SerializedName("es")
    @Expose
    @Embedded(prefix = "es-")
    private Es es;
    @SerializedName("et")
    @Expose
    @Embedded(prefix = "et-")
    private Et et;
    @SerializedName("fi")
    @Expose
    @Embedded(prefix = "fi-")
    private Fi fi;
    @SerializedName("fr")
    @Expose
    @Embedded(prefix = "fr-")
    private Fr fr;
    @SerializedName("hi")
    @Expose
    @Embedded(prefix = "hi-")
    private Hi hi;
    @SerializedName("hr")
    @Expose
    @Embedded(prefix = "hr-")
    private Hr hr;
    @SerializedName("hu")
    @Expose
    @Embedded(prefix = "hu-")
    private Hu hu;
    @SerializedName("id")
    @Expose
    @Embedded(prefix = "id-")
    private Id id;
    @SerializedName("is")
    @Expose
    @Embedded(prefix = "is-")
    private Is is;
    @SerializedName("it")
    @Expose
    @Embedded(prefix = "it-")
    private It it;
    @SerializedName("ja")
    @Expose
    @Embedded(prefix = "ja-")
    private Ja ja;
    @SerializedName("ko")
    @Expose
    @Embedded(prefix = "ko-")
    private Ko ko;
    @SerializedName("lt")
    @Expose
    @Embedded(prefix = "lt-")
    private Lt lt;
    @SerializedName("lv")
    @Expose
    @Embedded(prefix = "lv-")
    private Lv lv;
    @SerializedName("nb")
    @Expose
    @Embedded(prefix = "nb-")
    private Nb nb;
    @SerializedName("nl")
    @Expose
    @Embedded(prefix = "nl-")
    private Nl nl;
    @SerializedName("pl")
    @Expose
    @Embedded(prefix = "pl-")
    private Pl pl;
    @SerializedName("pt-BR")
    @Expose
    @Embedded(prefix = "pt-BR-")
    private PtBR ptBR;
    @SerializedName("pt-PT")
    @Expose
    @Embedded(prefix = "pt-PT-")
    private PtPT ptPT;
    @SerializedName("ro")
    @Expose
    @Embedded(prefix = "ro-")
    private Ro ro;
    @SerializedName("ru")
    @Expose
    @Embedded(prefix = "ru-")
    private Ru ru;
    @SerializedName("sk")
    @Expose
    @Embedded(prefix = "sk-")
    private Sk sk;
    @SerializedName("sl")
    @Expose
    @Embedded(prefix = "sl-")
    private Sl sl;
    @SerializedName("sv")
    @Expose
    @Embedded(prefix = "sv-")
    private Sv sv;
    @SerializedName("sw")
    @Expose
    @Embedded(prefix = "sw-")
    private Sw sw;
    @SerializedName("th")
    @Expose
    @Embedded(prefix = "th-")
    private Th th;
    @SerializedName("tr")
    @Expose
    @Embedded(prefix = "tr-")
    private Tr tr;
    @SerializedName("ug")
    @Expose
    @Embedded(prefix = "ug-")
    private Ug ug;
    @SerializedName("uk")
    @Expose
    @Embedded(prefix = "uk-")
    private Uk uk;
    @SerializedName("vi")
    @Expose
    @Embedded(prefix = "vi-")
    private Vi vi;
    @SerializedName("zh-CN")
    @Expose
    @Embedded(prefix = "zh-CN-")
    private ZhCN zhCN;
    @SerializedName("zh-TW")
    @Expose
    @Embedded(prefix = "zh-TW-")
    private ZhTW zhTW;
}
