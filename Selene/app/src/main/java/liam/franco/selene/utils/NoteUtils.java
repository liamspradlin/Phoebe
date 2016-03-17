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

import liam.franco.selene.application.App;
import liam.franco.selene.modules.Note;

public class NoteUtils {
    // it's ok to call this on the Main Thread, Realm is ridiculously fast it doesn't block Main Thread
    public static boolean hasNotes() {
        return App.REALM.where(Note.class).findAll().size() > 0;
    }
}
