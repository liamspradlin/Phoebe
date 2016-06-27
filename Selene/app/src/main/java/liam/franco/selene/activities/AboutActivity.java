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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.LinearLayout;

import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.materialize.util.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.halfbit.tinybus.Subscribe;
import liam.franco.selene.R;
import liam.franco.selene.application.App;
import liam.franco.selene.bus.AboutUrlClicked;
import liam.franco.selene.modules.About;
import liam.franco.selene.ui.AboutItem;
import liam.franco.selene.ui.SimpleDividerItemDecoration;
import liam.franco.selene.utils.PackageUtils;
import liam.franco.selene.utils.custom_tabs.CustomTabActivityHelper;

public class AboutActivity extends AppCompatActivity {
    private final static String GOOGLE_PLUS_PACKAGE = "com.google.android.apps.plus";
    private final static String TWITTER_PACKAGE = "com.twitter.android";
    private final static String PLAY_STORE_PACKAGE = "com.android.vending";

    @BindView(R.id.parent_layout)
    protected LinearLayout parentLayout;
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.scrollable_layout)
    protected LinearLayout scrollableLayout;
    @BindView(R.id.liam_list)
    protected RecyclerView liamList;
    @BindView(R.id.francisco_list)
    protected RecyclerView franciscoList;
    @BindView(R.id.libs_list)
    protected RecyclerView libsList;
    @BindView(R.id.mutate_more_list)
    protected RecyclerView mutateMoreList;

    private FastItemAdapter<AboutItem> liamAdapter;
    private FastItemAdapter<AboutItem> franciscoAdapter;
    private FastItemAdapter<AboutItem> libsAdapter;
    private FastItemAdapter<AboutItem> mutateMoreAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        App.BUS.register(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        UIUtils.setTranslucentNavigationFlag(this, true);
        UIUtils.setTranslucentStatusFlag(this, true);

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

                // making sure it's all inside bounds because of our transparent status and nav bars
                scrollableLayout.setPadding(scrollableLayout.getPaddingLeft(),
                        scrollableLayout.getPaddingTop(),
                        scrollableLayout.getPaddingRight() +
                                (orientation == 1 ? insets.getSystemWindowInsetRight() : navBarHeight),
                        scrollableLayout.getPaddingBottom() +
                                (orientation == 1 ? navBarHeight : insets.getSystemWindowInsetBottom()));

                parentLayout.setOnApplyWindowInsetsListener(null);
                return insets.consumeSystemWindowInsets();
            }
        });

        liamList.addItemDecoration(new SimpleDividerItemDecoration(this));
        liamAdapter = new FastItemAdapter<>();
        liamList.setLayoutManager(new UnscrollableLinearLayoutManager(this));
        liamList.setAdapter(liamAdapter);

        franciscoList.addItemDecoration(new SimpleDividerItemDecoration(this));
        franciscoAdapter = new FastItemAdapter<>();
        franciscoList.setLayoutManager(new UnscrollableLinearLayoutManager(this));
        franciscoList.setAdapter(franciscoAdapter);

        libsList.addItemDecoration(new SimpleDividerItemDecoration(this));
        libsAdapter = new FastItemAdapter<>();
        libsList.setLayoutManager(new UnscrollableLinearLayoutManager(this));
        libsList.setAdapter(libsAdapter);

        mutateMoreList.addItemDecoration(new SimpleDividerItemDecoration(this));
        mutateMoreAdapter = new FastItemAdapter<>();
        mutateMoreList.setLayoutManager(new UnscrollableLinearLayoutManager(this));
        mutateMoreList.setAdapter(mutateMoreAdapter);

        initAboutEntries();
    }

    // with RecyclerViews using wrap_content height if the parent is a NestedScrollView (this doesn't even work with a
    // normal ScrollView) fling doesn't work when you're touching in the RecyclerView area. This hack goes around
    // the problem. Bug tracker: https://goo.gl/kpLjnI
    private static class UnscrollableLinearLayoutManager extends LinearLayoutManager {
        public UnscrollableLinearLayoutManager(Context context) {
            super(context, LinearLayoutManager.VERTICAL, false);
        }

        @Override
        public boolean canScrollHorizontally() {
            return false;
        }

        @Override
        public boolean canScrollVertically() {
            return false;
        }
    }

    // might look a bit cumbersome laying it out like this but it's just personal preference instead of just starting
    // About objects through a cycle. u mad?
    private void initAboutEntries() {
        About liamGooglePlus = new About.Builder()
                .setTitle(App.CONTEXT.getString(R.string.google_plus))
                .setUrl(App.CONTEXT.getString(R.string.url_liam_google_plus))
                .build();

        About liamTwitter = new About.Builder()
                .setTitle(App.CONTEXT.getString(R.string.twitter))
                .setUrl(App.CONTEXT.getString(R.string.url_liam_twitter))
                .build();

        liamAdapter.add(new AboutItem(liamGooglePlus));
        liamAdapter.add(new AboutItem(liamTwitter));

        About franciscoGooglePlus = new About.Builder()
                .setTitle(App.CONTEXT.getString(R.string.google_plus))
                .setUrl(App.CONTEXT.getString(R.string.url_francisco_google_plus))
                .build();

        About franciscoTwitter = new About.Builder()
                .setTitle(App.CONTEXT.getString(R.string.twitter))
                .setUrl(App.CONTEXT.getString(R.string.url_francisco_twitter))
                .build();

        About franciscoPlayStore = new About.Builder()
                .setTitle(App.CONTEXT.getString(R.string.play_store_apps))
                .setUrl(App.CONTEXT.getString(R.string.url_francisco_play_store))
                .build();

        franciscoAdapter.add(new AboutItem(franciscoGooglePlus));
        franciscoAdapter.add(new AboutItem(franciscoTwitter));
        franciscoAdapter.add(new AboutItem(franciscoPlayStore));

        About appCompat = new About.Builder()
                .setTitle(App.CONTEXT.getString(R.string.appcompat))
                .setUrl(App.CONTEXT.getString(R.string.url_appcompat))
                .build();

        About cardView = new About.Builder()
                .setTitle(App.CONTEXT.getString(R.string.cardview))
                .setUrl(App.CONTEXT.getString(R.string.url_cardview))
                .build();

        About design = new About.Builder()
                .setTitle(App.CONTEXT.getString(R.string.design))
                .setUrl(App.CONTEXT.getString(R.string.url_design))
                .build();

        About customTabs = new About.Builder()
                .setTitle(App.CONTEXT.getString(R.string.customtabs))
                .setUrl(App.CONTEXT.getString(R.string.url_customtabs))
                .build();

        About butterknife = new About.Builder()
                .setTitle(App.CONTEXT.getString(R.string.butterknife))
                .setUrl(App.CONTEXT.getString(R.string.url_butterknife))
                .build();

        About realm = new About.Builder()
                .setTitle(App.CONTEXT.getString(R.string.realm))
                .setUrl(App.CONTEXT.getString(R.string.url_realm))
                .build();

        About tinyBus = new About.Builder()
                .setTitle(App.CONTEXT.getString(R.string.tinybus))
                .setUrl(App.CONTEXT.getString(R.string.url_tinybus))
                .build();

        About icepick = new About.Builder()
                .setTitle(App.CONTEXT.getString(R.string.icepick))
                .setUrl(App.CONTEXT.getString(R.string.url_icepick))
                .build();

        About materialize = new About.Builder()
                .setTitle(App.CONTEXT.getString(R.string.materialize))
                .setUrl(App.CONTEXT.getString(R.string.url_materialize))
                .build();

        About fastAdapter = new About.Builder()
                .setTitle(App.CONTEXT.getString(R.string.fastadapter))
                .setUrl(App.CONTEXT.getString(R.string.url_fastadapter))
                .build();

        About plaid = new About.Builder()
                .setTitle(App.CONTEXT.getString(R.string.plaid))
                .setUrl(App.CONTEXT.getString(R.string.url_plaid))
                .build();

        libsAdapter.add(new AboutItem(appCompat));
        libsAdapter.add(new AboutItem(cardView));
        libsAdapter.add(new AboutItem(design));
        libsAdapter.add(new AboutItem(customTabs));
        libsAdapter.add(new AboutItem(butterknife));
        libsAdapter.add(new AboutItem(realm));
        libsAdapter.add(new AboutItem(tinyBus));
        libsAdapter.add(new AboutItem(icepick));
        libsAdapter.add(new AboutItem(materialize));
        libsAdapter.add(new AboutItem(fastAdapter));
        libsAdapter.add(new AboutItem(plaid));

        About sourceCode = new About.Builder()
                .setTitle(App.CONTEXT.getString(R.string.selene_source_code))
                .setUrl(App.CONTEXT.getString(R.string.url_selene_source_code))
                .build();

        About projectPhoebe = new About.Builder()
                .setTitle(App.CONTEXT.getString(R.string.project_phoebe))
                .setUrl(App.CONTEXT.getString(R.string.url_project_phoebe))
                .build();

        mutateMoreAdapter.add(new AboutItem(sourceCode));
        mutateMoreAdapter.add(new AboutItem(projectPhoebe));
    }

    @Subscribe
    public void onAboutUrlClicked(AboutUrlClicked aboutUrlClicked) {
        // we want G+, Twitter and Play Store to open their native apps if they exist on the device
        if ((PackageUtils.isPackageInstalled(GOOGLE_PLUS_PACKAGE) &&
                aboutUrlClicked.getAboutItem().getTitle().equals(App.CONTEXT.getString(R.string.google_plus))) ||
                PackageUtils.isPackageInstalled(TWITTER_PACKAGE) &&
                        aboutUrlClicked.getAboutItem().getTitle().equals(App.CONTEXT.getString(R.string.twitter)) ||
                PackageUtils.isPackageInstalled(PLAY_STORE_PACKAGE) &&
                        aboutUrlClicked.getAboutItem().getTitle().equals(App.CONTEXT.getString(R.string.play_store_apps))) {

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(aboutUrlClicked.getAboutItem().getUrl()));
            startActivity(i);
        } else {
            // because Custom Tabs are the way to go amirite?
            CustomTabActivityHelper.openCustomTab(
                    this,
                    new CustomTabsIntent.Builder()
                            .setToolbarColor(ContextCompat.getColor(App.CONTEXT, R.color.accent))
                            .addDefaultShareMenuItem()
                            .enableUrlBarHiding()
                            .build(),
                    Uri.parse(aboutUrlClicked.getAboutItem().getUrl()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        App.BUS.unregister(this);
        super.onDestroy();
    }
}
