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

package liam.franco.selene.application;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

import java.util.ArrayList;
import java.util.List;

import de.halfbit.tinybus.TinyBus;
import io.realm.Realm;
import liam.franco.selene.modules.AmbientLight;
import liam.franco.selene.modules.Gaia;

public class App extends Application {
    public static Context CONTEXT;
    public static Resources RESOURCES;
    public static Handler MAIN_THREAD;
    public static TinyBus BUS;
    public static AmbientLight SENSOR_AMBIENT_LIGHT;
    public static List<Gaia> GAIAS;
    public static Realm REALM;

    public static void LOG(Object msg) {
        do {
            Log.d(CONTEXT.getPackageName(), String.valueOf(msg));
        } while (false);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
        Realm.init(this);

        CONTEXT = this;
        RESOURCES = getResources();
        MAIN_THREAD = new Handler(getMainLooper());
        BUS = TinyBus.from(this);
        SENSOR_AMBIENT_LIGHT = new AmbientLight();
        GAIAS = new ArrayList<>();
        REALM = Realm.getDefaultInstance();
    }
}