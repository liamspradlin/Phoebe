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

package liam.franco.selene.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IClickable;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import liam.franco.selene.R;
import liam.franco.selene.application.App;
import liam.franco.selene.bus.AboutUrlClicked;
import liam.franco.selene.modules.About;

public class AboutItem extends AbstractItem<AboutItem, AboutItem.ViewHolder> implements IClickable<AboutItem> {
    private final ViewHolderFactory<? extends ViewHolder> FACTORY = new ItemFactory();
    public About about;

    public AboutItem(About about) {
        this.about = about;
    }

    private static class ItemFactory implements ViewHolderFactory<ViewHolder> {
        public ViewHolder create(View v) {
            return new ViewHolder(v);
        }
    }

    public About getAbout() {
        return about;
    }

    @Override
    public ViewHolderFactory<? extends ViewHolder> getFactory() {
        return FACTORY;
    }

    @Override
    public int getType() {
        return R.id.title;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.about_list_item;
    }

    @Override
    public void bindView(ViewHolder viewHolder, List payloads) {
        super.bindView(viewHolder, payloads);

        viewHolder.title.setText(getAbout().getTitle());

        withOnItemClickListener(new FastAdapter.OnClickListener<AboutItem>() {
            @Override
            public boolean onClick(View v, IAdapter<AboutItem> adapter, AboutItem item, int position) {
                App.BUS.post(new AboutUrlClicked(getAbout()));
                return true;
            }
        });
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title)
        protected TextView title;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}