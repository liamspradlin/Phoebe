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

import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.halfbit.tinybus.Subscribe;
import liam.franco.selene.R;
import liam.franco.selene.application.App;
import liam.franco.selene.bus.NoteUpdated;
import liam.franco.selene.bus.NoteUpdatingEvent;
import liam.franco.selene.modules.Gaia;
import liam.franco.selene.modules.Note;
import liam.franco.selene.utils.GaiaUtils;
import liam.franco.selene.utils.Palette;
import liam.franco.selene.utils.PaletteUtils;

public class NoteItem extends AbstractItem<NoteItem, NoteItem.ViewHolder> {
    private final ViewHolderFactory<? extends ViewHolder> FACTORY = new ItemFactory();
    private Note note;
    private ViewHolder viewHolder;

    public NoteItem(Note note) {
        this.note = note;
    }

    private static class ItemFactory implements ViewHolderFactory<ViewHolder> {
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
        return R.id.parent_layout;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.note;
    }

    public ViewHolder getViewHolder() {
        return viewHolder;
    }

    private void setViewHolder(ViewHolder viewHolder) {
        this.viewHolder = viewHolder;
    }

    private Note getNote() {
        return note;
    }

    @Override
    public void bindView(ViewHolder viewHolder, List payloads) {
        super.bindView(viewHolder, payloads);

        viewHolder.setNote(getNote());
        setViewHolder(viewHolder);
        setUi(viewHolder);

        if (!App.BUS.hasRegistered(viewHolder)) {
            App.BUS.register(viewHolder);
        }
    }

    private void setUi(ViewHolder holder) {
        if (((ColorDrawable) holder.parentLayout.getBackground()).getColor() != holder.getPalette().getPrimary()) {
            holder.parentLayout.setBackgroundColor(holder.getPalette().getPrimary());
        }
        if (!holder.title.getText().toString().equals(holder.getNote().getTitle())) {
            holder.title.setText(holder.getNote().getTitle());
        }
        if (holder.title.getCurrentTextColor() != holder.getPalette().getTextColor()) {
            holder.title.setTextColor(holder.getPalette().getTextColor());
        }
        if (!holder.content.getText().toString().equals(holder.getNote().getContent())) {
            holder.content.setText(holder.getNote().getContent());
        }
        if (holder.content.getCurrentTextColor() != holder.getPalette().getTextColor()) {
            holder.content.setTextColor(holder.getPalette().getTextColor());
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.parent_layout)
        protected ForegroundLinearLayout parentLayout;
        @BindView(R.id.title)
        protected TextView title;
        @BindView(R.id.content)
        protected TextView content;

        protected Note note;
        protected Palette palette;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public Note getNote() {
            return note;
        }

        public Palette getPalette() {
            return palette;
        }

        public void setNote(Note note) {
            this.note = note;
            this.palette = PaletteUtils.get().palettes.get(note.getPalette());

            new Gaia.Builder()
                    .setName(getNote().getTitle() + "_" + GaiaUtils.NOTE_PARENT_LAYOUT)
                    .setMutativeView(new WeakReference<Object>(parentLayout))
                    .setColorToScale(palette.getPrimary())
                    .build();

            new Gaia.Builder()
                    .setName(getNote().getTitle() + "_" + GaiaUtils.NOTE_TITLE)
                    .setMutativeView(new WeakReference<Object>(title))
                    .setColorToScale(palette.getTextColor())
                    .build();

            new Gaia.Builder()
                    .setName(getNote().getTitle() + "_" + GaiaUtils.NOTE_CONTENT)
                    .setMutativeView(new WeakReference<Object>(content))
                    .setColorToScale(palette.getTextColor())
                    .build();
        }

        @Subscribe
        public void onNoteUpdated(NoteUpdated updatedNote) {
            if (note != null && note.isValid() && note.getUid() == updatedNote.getEditedNote().getUid()) {
                setNote(updatedNote.getEditedNote());
                parentLayout.setBackgroundColor(getPalette().getPrimary());
                title.setText(getNote().getTitle());
                title.setTextColor(getPalette().getTextColor());
                content.setText(getNote().getContent());
                content.setTextColor(getPalette().getTextColor());

                App.BUS.post(new NoteUpdatingEvent(updatedNote.getEditedNote(), getLayoutPosition()));
            }
        }
    }
}