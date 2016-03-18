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

package liam.franco.selene.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import butterknife.ButterKnife;
import icepick.State;
import liam.franco.selene.R;
import liam.franco.selene.application.App;
import liam.franco.selene.bus.NoteDeleted;
import liam.franco.selene.bus.NoteUpdated;
import liam.franco.selene.modules.Note;
import liam.franco.selene.utils.PaletteUtils;

public class EditNoteActivity extends SuperNoteActivity {
    @State
    protected long uid;

    protected Note noteToEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        uid = getIntent().getLongExtra("uid", 0);

        noteToEdit = App.REALM.where(Note.class).equalTo("uid", uid).findFirst();
        if (noteToEdit == null) {
            // shit happens
            supportFinishAfterTransition();
        }

        currentPalette = PaletteUtils.get().palettes.get(noteToEdit.getPalette());

        super.onCreate(savedInstanceState);
        parentLayout.setVisibility(View.VISIBLE);
        noteTitle.setText(noteToEdit.getTitle());
        noteContent.setText(noteToEdit.getContent());
        setUiElementsColor(currentPalette, 0);
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();

        archive.post(new Runnable() {
            @Override
            public void run() {
                if (noteToEdit.getArchive()) {
                    archive.performClick();
                }
            }
        });

        reminder.post(new Runnable() {
            @Override
            public void run() {
                if (noteToEdit.getReminder()) {
                    reminder.performClick();
                }
            }
        });
    }

    // we gotta make sure the edited note makes sense before updating the view back in the main sheets
    private boolean assertNoteEdit(String title, String content, String palette, boolean isArchive, boolean isReminder) {
        return !noteToEdit.getTitle().equals(title) ||
                !noteToEdit.getContent().equals(content) ||
                !noteToEdit.getPalette().equals(palette) ||
                noteToEdit.getArchive() != isArchive ||
                noteToEdit.getReminder() != isReminder;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.existing_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.delete:
                App.BUS.post(new NoteDeleted(noteToEdit));
                supportFinishAfterTransition();
                break;
            case R.id.share:
                String textToShare = noteTitle.getText().toString();
                textToShare += "\n\n";
                textToShare += noteContent.getText().toString();

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, App.CONTEXT.getString(R.string.intent_chooser_title)));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        // we check if the Realm object is valid before trying to mess with it
        if (noteToEdit.isValid()) {
            String title = noteTitle.getText().toString();
            String content = noteContent.getText().toString();
            String palette = currentPalette.getName();
            boolean isArchive = archive.isSelected();
            boolean isReminder = reminder.isSelected();

            if (assertNoteEdit(title, content, palette, isArchive, isReminder)) {
                App.REALM.beginTransaction();
                noteToEdit.setDate(System.currentTimeMillis());
                noteToEdit.setTitle(title);
                noteToEdit.setContent(content);
                noteToEdit.setPalette(palette);
                noteToEdit.setArchive(isArchive);
                noteToEdit.setReminder(isReminder);
                App.REALM.commitTransaction();

                App.BUS.post(new NoteUpdated(noteToEdit));
            }
        }

        App.BUS.unregister(this);
        ButterKnife.unbind(this);
        super.onDestroy();
    }
}
