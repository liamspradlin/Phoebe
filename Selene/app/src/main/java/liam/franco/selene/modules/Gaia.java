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

import de.halfbit.tinybus.Subscribe;
import liam.franco.selene.application.App;
import liam.franco.selene.bus.AmbientLightSensorChange;
import liam.franco.selene.utils.ColorAnimationUtils;
import liam.franco.selene.utils.GaiaUtils;

// the God class. It binds views to a Bus event that receives Ambient Light sensor values whenever those change
// and mutate the view color accordingly. This is just to demonstrate a simple mutation based on a real struggle
// The idea here is to easily allow for expansion, you can just add more @Subscribe methods below and send whatever
// you want to the views bound to Gaia, since Gaia shouldn't care about anything else than ruling how to mutate
// the views (or any other object you want to mutate). Gaia is omnipotent, it's a God, it can exist as many times
// as the developer wants, at any point in time
public class Gaia {
    // this field is important because it'll identify the object in the Gaia objects Array inside Application class
    private String name;
    // well the object that'll be mutated... duh!
    private Object mutativeObject;
    // original view color
    @ColorInt
    private int colorToScale;
    // stores the last time, in ms, the object mutated
    private long lastUpdate;

    private Gaia(Builder builder) {
        this.name = builder.getName();
        this.mutativeObject = builder.getMutativeView();
        this.colorToScale = builder.getColorToScale();
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
        private Object mutativeView;
        @ColorInt
        private int defaultBgColor;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setMutativeView(Object mutativeView) {
            this.mutativeView = mutativeView;
            return this;
        }

        public Builder setColorToScale(int colorToScale) {
            this.defaultBgColor = colorToScale;
            return this;
        }

        public Object getMutativeView() {
            return mutativeView;
        }

        public int getColorToScale() {
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
    public void onAmbientLightChange(AmbientLightSensorChange sensor) {
        // calculates the new color based on the sensor values and the original color
        // the algorithm is magical
        final int newColor = Color.HSVToColor(GaiaUtils.computeHSV(sensor, mutativeObject, colorToScale));
        int currentColor = 0;

        // mutate all the things
        if (mutativeObject != null) {
            if (mutativeObject instanceof LinearLayout) {
                currentColor = ((ColorDrawable) ((LinearLayout) mutativeObject).getBackground()).getColor();
            } else if (mutativeObject instanceof TextView) {
                currentColor = ((TextView) mutativeObject).getCurrentTextColor();
            } else if (mutativeObject instanceof FloatingActionButton) {
                currentColor = ((FloatingActionButton) mutativeObject).getBackgroundTintList().getDefaultColor();
            } else if (mutativeObject instanceof FrameLayout) {
                currentColor = ((ColorDrawable) ((FrameLayout) mutativeObject).getBackground()).getColor();
            }
        }

        // we don't want to update the colors every time the sensor outputs new values. Yes, the sensor is very nervous
        if (currentColor != newColor && GaiaUtils.ready(lastUpdate)) {
            final int finalCurrentColor = currentColor;
            lastUpdate = System.currentTimeMillis();

            // run this on the Main Thread, this @Subscribe method runs in a background thread
            App.MAIN_THREAD.post(new Runnable() {
                @Override
                public void run() {
                    // much animation. wow material. such beauty. so rad
                    ColorAnimationUtils.animateColor(mutativeObject, finalCurrentColor, newColor, 250);
                }
            });
        }
    }
}
