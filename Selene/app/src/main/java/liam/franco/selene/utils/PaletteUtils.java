/*
 * Copyright 2016 Francisco Franco & Liam Spradlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package liam.franco.selene.utils;

import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;

import java.util.LinkedHashMap;

import liam.franco.selene.R;
import liam.franco.selene.application.App;

public class PaletteUtils {
    public static String DEFAULT;
    public static String SKY;
    public static String SKY_LIGHT;
    public static String INTERIOR;
    public static String INTERIOR_LIGHT;
    public static String WATER;
    public static String WATER_LIGHT;
    public static String WOOD;
    public static String WOOD_LIGHT;

    public LinkedHashMap<String, Palette> palettes;
    private static PaletteUtils ourInstance = new PaletteUtils();

    public static PaletteUtils get() {
        return ourInstance;
    }

    private int getColor(@ColorRes int resId) {
        return ContextCompat.getColor(App.CONTEXT, resId);
    }

    private String getString(@StringRes int resId) {
        return App.CONTEXT.getString(resId);
    }

    private void addPalette(String paletteName,
                            @ColorRes int windowBgColor,
                            @ColorRes int toolsBarBgColor,
                            @ColorRes int toolsBarIconsTintColor,
                            @ColorRes int textColor) {
        palettes.put(paletteName, new Palette(paletteName,
                getColor(windowBgColor),
                getColor(toolsBarBgColor),
                getColor(toolsBarIconsTintColor),
                getColor(textColor))
        );
    }

    /**
     * (window background color, tools bar background color, tools bar icons tint color, text color)
     */
    private PaletteUtils() {
        DEFAULT = getString(R.string.default_palette);
        SKY = getString(R.string.sky_palette);
        SKY_LIGHT = getString(R.string.sky_light_palette);
        INTERIOR = getString(R.string.interior_palette);
        INTERIOR_LIGHT = getString(R.string.interior_light_palette);
        WATER = getString(R.string.water_palette);
        WATER_LIGHT = getString(R.string.water_light_palette);
        WOOD = getString(R.string.wood_palette);
        WOOD_LIGHT = getString(R.string.wood_light_palette);

        palettes = new LinkedHashMap<>();
        addPalette(DEFAULT, R.color.dust_note, R.color.grey_white, R.color.icon_dark, R.color.grey_medium);
        addPalette(SKY, R.color.sky, R.color.sky_light, R.color.icon_light, R.color.white);
        addPalette(SKY_LIGHT, R.color.sky_light, R.color.sky_bright, R.color.icon_light, R.color.white);
        addPalette(INTERIOR, R.color.interior, R.color.interior_light, R.color.icon_light, R.color.white);
        addPalette(INTERIOR_LIGHT, R.color.interior_light, R.color.interior_bright, R.color.icon_dark, R.color.white);
        addPalette(WATER, R.color.water, R.color.water_light, R.color.icon_light, R.color.white);
        addPalette(WATER_LIGHT, R.color.water_light, R.color.water_bright, R.color.icon_dark, R.color.white);
        addPalette(WOOD, R.color.wood, R.color.wood_light, R.color.icon_light, R.color.white);
        addPalette(WOOD_LIGHT, R.color.wood_light, R.color.wood_bright, R.color.icon_light, R.color.white);
    }
}
