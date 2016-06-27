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
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mikepenz.materialize.util.UIUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.halfbit.tinybus.Subscribe;
import liam.franco.selene.R;
import liam.franco.selene.application.App;
import liam.franco.selene.bus.Future;
import liam.franco.selene.bus.Past;
import liam.franco.selene.bus.Present;
import liam.franco.selene.fragments.FutureFragment;
import liam.franco.selene.fragments.PastFragment;
import liam.franco.selene.fragments.PresentFragment;
import liam.franco.selene.modules.Gaia;
import liam.franco.selene.utils.GaiaUtils;
import liam.franco.selene.utils.NoteUtils;
import liam.franco.selene.utils.ViewUtils;

public class MainActivity extends SuperAppCompatActivity {
    public static final String PAST = App.CONTEXT.getString(R.string.past);
    public static final String PRESENT = App.CONTEXT.getString(R.string.present);
    public static final String FUTURE = App.CONTEXT.getString(R.string.future);

    @Bind(R.id.parent_layout)
    protected FrameLayout parentLayout;
    @Bind(R.id.appbar)
    protected AppBarLayout appBarLayout;
    @Bind(R.id.toolbar)
    protected Toolbar toolbar;
    @Bind(R.id.tablayout)
    protected TabLayout tabLayout;
    @Bind(R.id.viewpager)
    protected ViewPager viewPager;
    @Bind(R.id.try_writing_a_note)
    protected TextView tryWritingAnote;
    @Bind(R.id.fab)
    protected FloatingActionButton fab;
    @Bind(R.id.bottom_fab_bar)
    protected FrameLayout bottomFabBar;

    private static String[] tabs;

    static {
        tabs = new String[]{
                PAST,
                PRESENT,
                FUTURE
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        App.BUS.register(this);

        setSupportActionBar(toolbar);

        ViewCompat.setElevation(appBarLayout, 0f);

        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(fragmentPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        // we want custom fonts on the tabs textViews
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setCustomView(R.layout.tab_layout_item);
        }

        viewPager.setCurrentItem(1);

        // mutate the FAB's colour!
        new Gaia.Builder()
                .setName(GaiaUtils.FAB)
                .setMutativeView(fab)
                .setColorToScale(fab.getBackgroundTintList().getDefaultColor())
                .build();
    }

    private void animateOpenBottomFabSheet() {
        if (bottomFabBar != null && bottomFabBar.getVisibility() == View.INVISIBLE) {
            final int[] xy = ViewUtils.viewCoordinates(fab);

            Animator animator = ViewAnimationUtils.createCircularReveal(
                    bottomFabBar,
                    (xy[0] + (fab.getMeasuredWidth() >> 1)),
                    -bottomFabBar.getHeight(),
                    UIUtils.convertDpToPixel(56f, App.CONTEXT),
                    Math.max(bottomFabBar.getWidth(), bottomFabBar.getHeight()));
            animator.setDuration(500);
            animator.setStartDelay(7000);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (fab != null) {
                        fab.setVisibility(View.GONE);
                    }

                    if (bottomFabBar != null) {
                        bottomFabBar.setVisibility(View.VISIBLE);
                    }
                }
            });
            animator.start();
        }
    }

    private void animateCloseBottomFabSheet() {
        if (bottomFabBar != null && bottomFabBar.getVisibility() == View.VISIBLE) {
            final int[] xy = ViewUtils.viewCoordinates(fab);

            Animator animator = ViewAnimationUtils.createCircularReveal(
                    bottomFabBar,
                    (xy[0] + (fab.getMeasuredWidth() >> 1)),
                    -bottomFabBar.getHeight(),
                    Math.max(bottomFabBar.getWidth(), bottomFabBar.getHeight()),
                    0);
            animator.setDuration(500);
            animator.setStartDelay(500);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (fab != null) {
                        fab.setVisibility(View.VISIBLE);
                    }
                    if (bottomFabBar != null) {
                        bottomFabBar.setVisibility(View.INVISIBLE);
                    }
                }
            });
            animator.start();
        }
    }

    private FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new PastFragment();
                case 1:
                    return new PresentFragment();
                case 2:
                    return new FutureFragment();
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }

        @Override
        public int getCount() {
            return tabs.length;
        }
    };

    @Subscribe
    public void onPast(Past past) {
        viewPager.setCurrentItem(0);
    }

    @Subscribe
    public void onPresent(Present present) {
        viewPager.setCurrentItem(1);
    }

    @Subscribe
    public void onFuture(Future future) {
        viewPager.setCurrentItem(2);
    }

    @OnClick({R.id.fab, R.id.bottom_fab_bar})
    protected void onFabClick(final View newNoteView) {
        Intent newNote = new Intent(this, NewNoteActivity.class);

        // we want the right coordinates to start the circular reveal
        int[] xy = ViewUtils.viewCoordinates(fab);
        newNote.putExtra("fabX", (xy[0] + (fab.getMeasuredWidth() >> 1)));
        newNote.putExtra("fabY", (xy[1] + (fab.getMeasuredHeight() >> 1)));

        startActivity(newNote);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        // Liam would kill me if the (info) button was not coloured the same as the tabs titles
        menu.findItem(R.id.about).getIcon().setTint(ContextCompat.getColor(this,
                android.support.v7.appcompat.R.color.secondary_text_default_material_light));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class),
                        ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (fab.getVisibility() == View.VISIBLE) {
            fab.startAnimation(AnimationUtils.loadAnimation(App.CONTEXT, R.anim.scale_fab_in));
        }
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
        App.MAIN_THREAD.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (NoteUtils.hasNotes()) {
                    animateCloseBottomFabSheet();
                } else {
                    animateOpenBottomFabSheet();
                }

                App.MAIN_THREAD.removeCallbacks(this);
            }
        }, 250);
    }

    @Override
    protected void onDestroy() {
        App.BUS.unregister(this);
        ButterKnife.unbind(this);
        super.onDestroy();
    }
}
