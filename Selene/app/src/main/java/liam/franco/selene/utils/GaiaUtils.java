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

import android.graphics.Color;
import android.hardware.SensorManager;
import android.widget.TextView;

import java.util.Locale;

import liam.franco.selene.application.App;
import liam.franco.selene.bus.LightSensor;
import liam.franco.selene.modules.Gaia;

public class GaiaUtils {
    private final static int MIN_UPDATE_INTERVAL_MS = 300;
    public final static String FAB = "fab";
    public final static String NOTE_PARENT_LAYOUT = "note_parent_layout";
    public final static String NOTE_TITLE = "note_title";
    public final static String NOTE_CONTENT = "note_content";

    // makes sure it doesn't go too bright
    public final static float HIGH_THRESHOLD = 0.8f;
    // makes sure it doesn't go too dark
    public final static float TOLERABLE_THRESHOLD = 0.2f;
    public final static float LOW_THRESHOLD = 0.05f;
    public final static float MIN = 0.0f;

    public static boolean isReady(long lastUpdate) {
        return System.currentTimeMillis() > (lastUpdate + MIN_UPDATE_INTERVAL_MS);
    }

    public static void seekAndDestroy(String name) {
        for (int i = 0; i < App.GAIAS.size(); i++) {
            Gaia gaia = App.GAIAS.get(i);

            if (gaia.getName().equals(name)) {
                if (App.BUS.hasRegistered(gaia)) {
                    App.BUS.unregister(gaia);
                    App.GAIAS.remove(i);
                    break;
                }
            }
        }
    }

    /**
     *
     * @param sensor object coming from our bus containing the Ambient Light sensor value
     * @param objectToMutate the object that'll be mutated
     * @param originalColor the original color of the object that'll be mutated
     *
     * @return a new HSV value to be applied into the object
     */
    public static float[] computeHSV(LightSensor sensor, Object objectToMutate, int originalColor) {
        // we divide the color into red green and blue
        int red = Color.red(originalColor);
        int green = Color.green(originalColor);
        int blue = Color.blue(originalColor);

        final float hsv[] = new float[3];

        Color.RGBToHSV(red, green, blue, hsv);

        // 'magic' algorithm
        float div = Float.valueOf(String.format(Locale.US, "%.2f",
                sensor.getLight() / ((int) SensorManager.LIGHT_OVERCAST >> 1)));

        if (div > HIGH_THRESHOLD) {
            div = HIGH_THRESHOLD;
        } else if (div < LOW_THRESHOLD) {
            div = MIN;
        }

        // Text is, by rule, in a contrasted color to the background, so we have to apply the formula backwards to the
        // rest of the views
        if (objectToMutate instanceof TextView) {
            hsv[2] += div;
        } else {
            hsv[2] -= div;
        }

        // making sure we don't have a weird negative value
        hsv[2] = Math.max(hsv[2], TOLERABLE_THRESHOLD);

        return hsv;
    }
}
