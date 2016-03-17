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

import android.support.annotation.ColorInt;

public class Palette {
    private String name;

    @ColorInt
    private int primary;
    @ColorInt
    private int primaryLight;
    @ColorInt
    private int toolsBarIconColor;
    @ColorInt
    private int textColor;

    public Palette(String name, @ColorInt int primary, @ColorInt int primaryLight, @ColorInt int toolsBarIconColor,
                   @ColorInt int textColor) {
        this.name = name;
        this.primary = primary;
        this.primaryLight = primaryLight;
        this.toolsBarIconColor = toolsBarIconColor;
        this.textColor = textColor;
    }

    public String getName() {
        return name;
    }

    public int getPrimary() {
        return primary;
    }

    public int getPrimaryLight() {
        return primaryLight;
    }

    public int getToolsBarIconColor() {
        return toolsBarIconColor;
    }

    public int getTextColor() {
        return textColor;
    }
}