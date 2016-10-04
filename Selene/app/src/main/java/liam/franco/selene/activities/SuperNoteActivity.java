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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.materialize.util.UIUtils;

import java.lang.ref.WeakReference;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import de.halfbit.tinybus.Subscribe;
import icepick.Icepick;
import liam.franco.selene.R;
import liam.franco.selene.application.App;
import liam.franco.selene.bus.TagClicked;
import liam.franco.selene.modules.Gaia;
import liam.franco.selene.ui.TagItem;
import liam.franco.selene.utils.ColorAnimationUtils;
import liam.franco.selene.utils.GaiaUtils;
import liam.franco.selene.utils.Palette;
import liam.franco.selene.utils.PaletteUtils;

public class SuperNoteActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.parent_layout)
    protected FrameLayout parentLayout;
    @BindView(R.id.note_title)
    protected EditText noteTitle;
    @BindView(R.id.note_content)
    protected EditText noteContent;
    @BindView(R.id.tools_bar_parent)
    protected FrameLayout toolsBarParent;
    @BindView(R.id.tools_bar)
    protected CardView toolsBar;
    @BindView(R.id.archive)
    protected ImageView archive;
    @BindView(R.id.category)
    protected ImageView category;
    @BindView(R.id.reminder)
    protected ImageView reminder;
    @BindView(R.id.archive_indicator)
    protected View archiveIndicator;
    @BindView(R.id.category_indicator)
    protected View categoryIndicator;
    @BindView(R.id.reminder_indicator)
    protected View reminderIndicator;
    @BindView(R.id.tags_list)
    protected RecyclerView tagsList;

    protected Palette currentPalette;
    protected FastItemAdapter<TagItem> adapter;

    protected boolean archiveSelection;
    protected boolean reminderSelection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_edit_note);
        Icepick.restoreInstanceState(this, savedInstanceState);
        ButterKnife.bind(this);
        App.BUS.register(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        parentLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        parentLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                int navBarHeight = UIUtils.getNavigationBarHeight(App.CONTEXT);

                // 1 = portrait, 2 = landscape
                int orientation = App.RESOURCES.getConfiguration().orientation;

                ViewGroup.MarginLayoutParams bar = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
                bar.topMargin += insets.getSystemWindowInsetTop();
                bar.rightMargin += insets.getSystemWindowInsetRight();
                toolbar.setLayoutParams(bar);

                toolsBarParent.setPadding(toolsBarParent.getPaddingLeft(),
                        toolsBarParent.getPaddingTop(),
                        toolsBarParent.getPaddingRight() +
                                (orientation == 1 ? insets.getSystemWindowInsetRight() : navBarHeight),
                        toolsBarParent.getPaddingBottom() +
                                (orientation == 1 ? navBarHeight : insets.getSystemWindowInsetBottom()));

                noteContent.setPadding(noteContent.getPaddingLeft(),
                        noteContent.getPaddingTop(),
                        noteContent.getPaddingRight() +
                                (orientation == 1 ? insets.getSystemWindowInsetRight() : navBarHeight),
                        noteContent.getPaddingBottom() +
                                (orientation == 1 ? navBarHeight : insets.getSystemWindowInsetBottom()));

                parentLayout.setOnApplyWindowInsetsListener(null);
                return insets.consumeSystemWindowInsets();
            }
        });

        if (savedInstanceState != null) {
            archiveSelection = savedInstanceState.getBoolean("archive", false);
            reminderSelection = savedInstanceState.getBoolean("reminder", false);

            if (archiveSelection) {
                archive.performClick();
            }

            if (reminderSelection) {
                reminder.performClick();
            }
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // set up the adapter for the circular coloured tags
        adapter = new FastItemAdapter<>();
        tagsList.setAdapter(adapter);
        for (Map.Entry<String, Palette> entry : PaletteUtils.get().palettes.entrySet()) {
            adapter.add(new TagItem(entry.getValue()));
        }
    }

    // here we set up our views colours, and animating the colour changes progressively
    protected void setUiElementsColor(final Palette palette, long duration) {
        // making really damn sure these views are not being mutated anymore before we change it's colours
        GaiaUtils.seekAndDestroy(GaiaUtils.NOTE_PARENT_LAYOUT);
        GaiaUtils.seekAndDestroy(GaiaUtils.NOTE_TITLE);
        GaiaUtils.seekAndDestroy(GaiaUtils.NOTE_CONTENT);

        ColorAnimationUtils.animateColor(getWindow(), currentPalette.getPrimary(), palette.getPrimary(), duration);
        ColorAnimationUtils.animateColor(parentLayout, currentPalette.getPrimary(), palette.getPrimary(), duration);

        if (toolbar.getNavigationIcon() != null) {
            ColorAnimationUtils.animateColor(toolbar.getNavigationIcon(),
                    currentPalette.getTextColor(), palette.getTextColor(), duration);
        }
        if (toolbar.getOverflowIcon() != null) {
            ColorAnimationUtils.animateColor(toolbar.getOverflowIcon(),
                    currentPalette.getTextColor(), palette.getTextColor(), duration);
        }

        ColorAnimationUtils.animateColor(noteTitle, currentPalette.getTextColor(), palette.getTextColor(), duration);
        ColorAnimationUtils.animateColor(noteContent, currentPalette.getTextColor(), palette.getTextColor(), duration);
        ColorAnimationUtils.animateColor(toolsBar, currentPalette.getPrimaryLight(),
                palette.getPrimaryLight(), duration);
        ColorAnimationUtils.animateColor(archive, currentPalette.getToolsBarIconColor(),
                palette.getToolsBarIconColor(), duration);
        ColorAnimationUtils.animateColor(category, currentPalette.getToolsBarIconColor(),
                palette.getToolsBarIconColor(), duration);
        ColorAnimationUtils.animateColor(reminder, currentPalette.getToolsBarIconColor(),
                palette.getToolsBarIconColor(), duration);

        currentPalette = palette;

        App.MAIN_THREAD.postDelayed(new Runnable() {
            @Override
            public void run() {
                // we re-add these views by binding them with Gaia
                // they'll mutate now based on the heuristics we have in place
                // this is inside a delayed runnable because the animateColor section just above runs for
                // 500ms and we don't wanna have issues
                new Gaia.Builder()
                        .setName(GaiaUtils.NOTE_PARENT_LAYOUT)
                        .setMutativeView(new WeakReference<Object>(parentLayout))
                        .setColorToScale(currentPalette.getPrimary())
                        .build();
                new Gaia.Builder()
                        .setName(GaiaUtils.NOTE_TITLE)
                        .setMutativeView(new WeakReference<Object>(noteTitle))
                        .setColorToScale(currentPalette.getTextColor())
                        .build();
                new Gaia.Builder()
                        .setName(GaiaUtils.NOTE_CONTENT)
                        .setMutativeView(new WeakReference<Object>(noteContent))
                        .setColorToScale(currentPalette.getTextColor())
                        .build();

                App.MAIN_THREAD.removeCallbacks(this);
            }
        }, duration);
    }

    protected void setUiElementsColor(final Palette palette) {
        setUiElementsColor(palette, 500);
    }

    @Subscribe
    public void tagClicked(TagClicked tag) {
        setUiElementsColor(PaletteUtils.get().palettes.get(tag.paletteName));
    }

    private void toolsbarIconsVisibility(View view) {
        view.setVisibility(view.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
    }

    // sets the note to be in the Archive sheet
    @OnClick(R.id.archive)
    public void onArchiveClick(ImageView archive) {
        toolsbarIconsVisibility(archiveIndicator);
        archive.setSelected(!archive.isSelected());
        if (archive.isSelected() && reminder.isSelected()) {
            toolsbarIconsVisibility(reminderIndicator);
            reminder.setSelected(false);
        }
    }

    @OnLongClick(R.id.archive)
    public boolean onArchiveLongPress(ImageView archive) {
        Toast.makeText(App.CONTEXT,
                archive.isSelected() ? R.string.unarchive : R.string.archive,
                Toast.LENGTH_SHORT).show();

        return true;
    }

    // opens the circular tags view
    @OnClick(R.id.category)
    public void onCategoryClick(final ImageView category) {
        tagsList.setVisibility(tagsList.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        category.post(new Runnable() {
            @Override
            public void run() {
                toolsbarIconsVisibility(categoryIndicator);
                category.setSelected(!category.isSelected());
            }
        });
    }

    // sets the note to be in the Reminder sheet
    // in the future we'll have real time-based reminders, but this is a demo so YOLO!
    @OnClick(R.id.reminder)
    public void onReminderClick(ImageView reminder) {
        toolsbarIconsVisibility(reminderIndicator);
        reminder.setSelected(!reminder.isSelected());
        if (archive.isSelected() && reminder.isSelected()) {
            toolsbarIconsVisibility(archiveIndicator);
            archive.setSelected(false);
        }
    }

    @OnLongClick(R.id.reminder)
    public boolean onReminderLongPress(ImageView reminder) {
        Toast.makeText(App.CONTEXT,
                reminder.isSelected() ? R.string.unreminder : R.string.reminder,
                Toast.LENGTH_SHORT).show();

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("archive", archive.isSelected());
        outState.putBoolean("reminder", reminder.isSelected());

        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.SENSOR_AMBIENT_LIGHT.stopSensor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.SENSOR_AMBIENT_LIGHT.startSensor();
    }

    // again make sure these views are unbounded from Gaia after this activity is assassinated
    @Override
    protected void onDestroy() {
        GaiaUtils.seekAndDestroy(GaiaUtils.NOTE_PARENT_LAYOUT);
        GaiaUtils.seekAndDestroy(GaiaUtils.NOTE_TITLE);
        GaiaUtils.seekAndDestroy(GaiaUtils.NOTE_CONTENT);

        super.onDestroy();
    }
}
