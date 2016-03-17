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

package liam.franco.selene.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ImageView;

import liam.franco.selene.R;
import liam.franco.selene.application.App;

// not currently being used, just a remnant of the past. A Relic
// this class alone will be worth 10 trillion dollars sometime in the next 1 million years, at best
public class ToolsBarImageView extends ImageView {
    @ColorInt
    private int defaultTint;

    public ToolsBarImageView(Context context) {
        super(context);
    }

    public ToolsBarImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ToolsBarImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ToolsBarImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setDefaultTint(@ColorInt int tint) {
        this.defaultTint = tint;
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        setImageTintList(selected ?
                ColorStateList.valueOf(ContextCompat.getColor(App.CONTEXT, R.color.accent))
                : ColorStateList.valueOf(defaultTint));
    }
}
