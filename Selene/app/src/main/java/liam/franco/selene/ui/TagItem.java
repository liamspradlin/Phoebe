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

import android.content.res.ColorStateList;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import liam.franco.selene.R;
import liam.franco.selene.application.App;
import liam.franco.selene.bus.TagClicked;
import liam.franco.selene.utils.Palette;

public class TagItem extends AbstractItem<TagItem, TagItem.ViewHolder> {
    private final ViewHolderFactory<? extends ViewHolder> FACTORY = new ItemFactory();
    public Palette palette;

    public TagItem(Palette palette) {
        this.palette = palette;
    }

    protected static class ItemFactory implements ViewHolderFactory<ViewHolder> {
        public ViewHolder create(View v) {
            return new ViewHolder(v);
        }
    }

    @Override
    public ViewHolderFactory<? extends ViewHolder> getFactory() {
        return FACTORY;
    }

    @Override
    public int getType() {
        return R.id.tag;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.tag;
    }

    @Override
    public void bindView(ViewHolder viewHolder) {
        super.bindView(viewHolder);

        viewHolder.tag.setImageTintList(ColorStateList.valueOf(palette.getPrimary()));
        withOnItemClickListener(new FastAdapter.OnClickListener<TagItem>() {
            @Override
            public boolean onClick(View v, IAdapter<TagItem> adapter, TagItem item, int position) {
                App.BUS.post(new TagClicked(palette.getName()));
                return true;
            }
        });
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tag)
        protected ImageView tag;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}