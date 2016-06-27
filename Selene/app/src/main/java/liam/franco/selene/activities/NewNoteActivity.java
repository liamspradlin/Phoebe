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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.ButterKnife;
import icepick.State;
import liam.franco.selene.R;
import liam.franco.selene.application.App;
import liam.franco.selene.bus.Future;
import liam.franco.selene.bus.NewNoteSaved;
import liam.franco.selene.bus.Past;
import liam.franco.selene.bus.Present;
import liam.franco.selene.modules.Note;
import liam.franco.selene.utils.PaletteUtils;
import liam.franco.selene.utils.RandomUtils;

public class NewNoteActivity extends SuperNoteActivity {
    @State
    protected int fabX;
    @State
    protected int fabY;

    private Animator startRevealAnim;

    private void initUIStuff(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (parentLayout.getViewTreeObserver().isAlive()) {
                parentLayout.getViewTreeObserver()
                        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                parentLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                circularRevealActivity();
                            }
                        });
            }
        } else {
            parentLayout.setVisibility(View.VISIBLE);
            setUiElementsColor(currentPalette);
        }

        noteTitle.setOnFocusChangeListener(onEditTextFocusChangeListener);
        noteContent.setOnFocusChangeListener(onEditTextFocusChangeListener);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        currentPalette = PaletteUtils.get().palettes.get(PaletteUtils.DEFAULT);

        super.onCreate(savedInstanceState);

        fabX = getIntent().getIntExtra("fabX", 0);
        fabY = getIntent().getIntExtra("fabY", 0);

        initUIStuff(savedInstanceState);
    }

    private View.OnFocusChangeListener onEditTextFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (view instanceof EditText) {
                String hasText = ((EditText) view).getText().toString();
                String hintText = view == noteTitle
                        ? App.CONTEXT.getString(R.string.untitled)
                        : App.CONTEXT.getString(R.string.new_note_hint);

                ((EditText) view).setHint(hasFocus ? "" : (TextUtils.isEmpty(hasText) ? hintText : ""));
            }
        }
    };

    private void circularRevealActivity() {
        float finalRadius = Math.max(parentLayout.getWidth(), parentLayout.getHeight());

        // create the animator for this view (the start radius is zero)
        startRevealAnim = ViewAnimationUtils.createCircularReveal(parentLayout, fabX, fabY, 0, finalRadius);
        startRevealAnim.setDuration(400);
        startRevealAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                App.MAIN_THREAD.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        parentLayout.setBackgroundColor(Color.TRANSPARENT);
                        setUiElementsColor(currentPalette);
                        App.MAIN_THREAD.removeCallbacks(this);
                    }
                }, 100);
            }
        });

        // make the view visible and start the animation
        parentLayout.setVisibility(View.VISIBLE);
        startRevealAnim.start();
    }

    // making sure the Note title is valid
    private boolean assertNewNote(String title) {
        if (TextUtils.isEmpty(title)) {
            return false;
        } else if (App.REALM.where(Note.class).equalTo("title", title).findAll().size() > 0) {
            Toast.makeText(App.CONTEXT, R.string.note_already_exists, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (startRevealAnim != null && !startRevealAnim.isRunning()) {
            tagsList.setVisibility(View.GONE);
            tagsList.post(new Runnable() {
                @Override
                public void run() {
                    int startRadius = Math.max(parentLayout.getWidth(), parentLayout.getHeight());
                    int endRadius = 0;

                    Animator animate = ViewAnimationUtils.createCircularReveal(parentLayout, fabX, fabY, startRadius, endRadius);
                    animate.setDuration(400);
                    animate.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            parentLayout.setVisibility(View.INVISIBLE);
                            finish();
                        }
                    });
                    parentLayout.setBackgroundColor(((ColorDrawable) getWindow().getDecorView().getBackground()).getColor());
                    getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    animate.start();
                }
            });
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        if (assertNewNote(noteTitle.getText().toString())) {
            String title = noteTitle.getText().toString();
            String content = noteContent.getText().toString();
            String palette = currentPalette.getName();
            boolean isArchive = archive.isSelected();
            boolean isReminder = reminder.isSelected();

            App.REALM.beginTransaction();
            Note note = App.REALM.createObject(Note.class);
            note.setDate(System.currentTimeMillis());
            note.setUid(RandomUtils.getNewLong());
            note.setTitle(title);
            note.setContent(content);
            note.setPalette(palette);
            note.setArchive(isArchive);
            note.setReminder(isReminder);
            App.REALM.commitTransaction();

            App.BUS.post(new NewNoteSaved(note));

            if (isArchive) {
                App.BUS.post(new Past());
            } else if (isReminder) {
                App.BUS.post(new Future());
            } else {
                App.BUS.post(new Present());
            }
        }

        App.BUS.unregister(this);
        ButterKnife.unbind(this);
        super.onDestroy();
    }
}
