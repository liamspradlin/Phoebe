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

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import liam.franco.selene.application.App;
import liam.franco.selene.bus.LightSensor;
import liam.franco.selene.bus.StartAmbientLightSensor;
import liam.franco.selene.bus.StopAmbientLightSensor;

// reads the light values from the Ambient Light sensor and send them to @Subscribe methods
public class AmbientLight implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor ambientLightSensor;

    public AmbientLight() {
        this.sensorManager = (SensorManager) App.CONTEXT.getSystemService(Context.SENSOR_SERVICE);
        this.ambientLightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        App.BUS.post(new LightSensor(event.values[0]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void startSensor() {
        sensorManager.registerListener(this, ambientLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        App.BUS.post(new StartAmbientLightSensor());
    }

    public void stopSensor() {
        sensorManager.unregisterListener(this);
        App.BUS.post(new StopAmbientLightSensor());
    }
}
