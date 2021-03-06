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

package liam.franco.selene.modules;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.ColorInt;
import android.support.design.widget.FloatingActionButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import de.halfbit.tinybus.Subscribe;
import liam.franco.selene.application.App;
import liam.franco.selene.bus.LightSensor;
import liam.franco.selene.utils.ColorAnimationUtils;
import liam.franco.selene.utils.GaiaUtils;

// the God class. It binds views to a Bus event that receives Ambient Light sensor values whenever those change
// and mutate the view color accordingly. This is just to demonstrate a simple mutation based on a real struggle
// The idea here is to easily allow for expansion, you can just add more @Subscribe methods below and send whatever
// you want to the views bound to Gaia, since Gaia shouldn't care about anything else than ruling how to mutate
// the views (or any other object you want to mutate). Gaia is omnipotent, it's a God, it can exist as many times
// as the developer wants, at any point in time
public class Gaia {
    private static final int COLOR_TRANSFORM_DURATION = 250;

    // this field is important because it'll identify the object in the Gaia objects Array inside Application class
    private String name;
    // well the object that'll be mutated... duh!
    private WeakReference<Object> objectToMutate;
    // original view color
    @ColorInt
    private int originalColor;
    // stores the last time, in ms, the object mutated
    private long lastUpdate;

    private Gaia(Builder builder) {
        this.name = builder.getName();
        this.objectToMutate = builder.getMutativeView();
        this.originalColor = builder.getColorToScale();
        // tango down!
        GaiaUtils.seekAndDestroy(getName());
        // good morning!!
        App.GAIAS.add(this);
        // One Bus to rule them all, One Bus to find them,
        // One Bus to bring them all and in the darkness bind them
        App.BUS.register(this);
    }

    public String getName() {
        return name;
    }

    public static class Builder {
        private String name;
        private WeakReference<Object> mutativeView;
        @ColorInt
        private int defaultBgColor;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setMutativeView(WeakReference<Object> mutativeView) {
            this.mutativeView = mutativeView;
            return this;
        }

        public Builder setColorToScale(int colorToScale) {
            this.defaultBgColor = colorToScale;
            return this;
        }

        WeakReference<Object> getMutativeView() {
            return mutativeView;
        }

        int getColorToScale() {
            return defaultBgColor;
        }

        public Gaia build() {
            return new Gaia(this);
        }

        public String getName() {
            return name;
        }
    }

    @Subscribe(mode = Subscribe.Mode.Background)
    public void onUpdatedAmbientLight(LightSensor sensor) {
        // calculates the new color based on the sensor values and the original color
        // the algorithm is magical
        final int newColor = Color.HSVToColor(GaiaUtils.computeHSV(sensor, objectToMutate, originalColor));
        int currentColor = 0;

        // mutate all the things
        Object view = objectToMutate.get();
        if (view != null) {
            if (view instanceof LinearLayout) {
                currentColor = ((ColorDrawable) ((LinearLayout) view).getBackground()).getColor();
            } else if (view instanceof TextView) {
                currentColor = ((TextView) view).getCurrentTextColor();
            } else if (view instanceof FloatingActionButton) {
                currentColor = ((FloatingActionButton) view).getBackgroundTintList().getDefaultColor();
            } else if (view instanceof FrameLayout) {
                currentColor = ((ColorDrawable) ((FrameLayout) view).getBackground()).getColor();
            }
        }

        boolean hasNewColor = currentColor != newColor;
        boolean isReady = GaiaUtils.isReady(lastUpdate);

        // we don't want to update the colors every time the sensor outputs new values. Yes, the sensor is very nervous
        if (hasNewColor && isReady) {
            final int fromColor = currentColor;
            lastUpdate = System.currentTimeMillis();

            // run this on the Main Thread, this @Subscribe method runs in a background thread
            App.MAIN_THREAD.post(new Runnable() {
                @Override
                public void run() {
                    // much animation. wow material. such beauty. so rad
                    // pass it the object (in this case it's a view) that's going to be mutated,
                    // the starting color value, and the color we want to animate to
                    // lastly the duration of the animation, in this case the default should be 250ms
                    ColorAnimationUtils.animateColor(objectToMutate, fromColor, newColor, COLOR_TRANSFORM_DURATION);
                }
            });
        }
    }
}
