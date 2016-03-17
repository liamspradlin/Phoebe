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

import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import liam.franco.selene.ui.ToolsBarImageView;

public class ColorAnimationUtils {
    /**
     * @param view View/Drawable you want to set a new color on
     * @param from Current View/Drawable color
     * @param to   New color to be set
     *             <p/>
     *             author: Felipe Bari http://stackoverflow.com/a/28239812
     *             <p/>
     *             Original code from Felipe but with some changes to fit my use case
     */
    public static void animateColor(final Object view, int from, int to, long duration) {
        final int initialColor = from;
        final int finalColor = to;

        ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float position = animation.getAnimatedFraction();
                int blended = blendColors(initialColor, finalColor, position);

                if (view instanceof Window) {
                    ((Window) view).setBackgroundDrawable(new ColorDrawable(blended));
                } else if (view instanceof FloatingActionButton) {
                    ((FloatingActionButton) view).setBackgroundTintList(ColorStateList.valueOf(blended));
                } else if (view instanceof CardView) {
                    ((CardView) view).setCardBackgroundColor(blended);
                } else if (view instanceof Drawable) {
                    ((Drawable) view).setTint(blended);
                } else if (view instanceof ImageView) {
                    if (!((ImageView) view).isSelected()) {
                        ((ImageView) view).setImageTintList(ColorStateList.valueOf(blended));
                    }
                } else if (view instanceof EditText) {
                    ((EditText) view).setTextColor(blended);
                    ((EditText) view).setHintTextColor(blended);
                } else if (view instanceof TextView) {
                    ((TextView) view).setTextColor(blended);
                } else if (view instanceof FrameLayout) {
                    ((FrameLayout) view).setBackgroundColor(blended);
                } else if (view instanceof View) {
                    ((View) view).setBackgroundColor(blended);
                }
            }
        });

        anim.setDuration(duration).start();
    }

    public static int blendColors(int from, int to, float ratio) {
        final float inverseRatio = 1f - ratio;

        final float r = Color.red(to) * ratio + Color.red(from) * inverseRatio;
        final float g = Color.green(to) * ratio + Color.green(from) * inverseRatio;
        final float b = Color.blue(to) * ratio + Color.blue(from) * inverseRatio;

        return Color.rgb((int) r, (int) g, (int) b);
    }
}
