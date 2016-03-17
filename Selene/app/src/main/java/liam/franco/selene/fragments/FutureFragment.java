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

package liam.franco.selene.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;

import io.realm.RealmResults;
import liam.franco.selene.R;
import liam.franco.selene.activities.MainActivity;
import liam.franco.selene.application.App;
import liam.franco.selene.modules.Note;

public class FutureFragment extends SuperSheetsFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResId(R.layout.fragment_future);
        setTabTitle(MainActivity.FUTURE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RealmResults<Note> results = App.REALM.where(Note.class)
                .equalTo("archive", false)
                .equalTo("reminder", true)
                .findAllSorted("date");

        for (int i = 0; i < results.size(); i++) {
            addNote(results.get(i));
        }

        setEmptyView();
    }
}
