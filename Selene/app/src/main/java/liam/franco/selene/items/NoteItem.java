package liam.franco.selene.items;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import butterknife.Bind;
import butterknife.ButterKnife;
import liam.franco.selene.R;
import liam.franco.selene.modules.Gaia;
import liam.franco.selene.modules.Note;
import liam.franco.selene.utils.Palette;
import liam.franco.selene.utils.PaletteUtils;

public class NoteItem extends AbstractItem<NoteItem, NoteItem.ViewHolder> {
    private final ViewHolderFactory<? extends ViewHolder> FACTORY = new ItemFactory();
    private Note note;

    public NoteItem(Note note) {
        this.note = note;
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
        return R.id.parent_layout;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.note;
    }

    @Override
    public void bindView(final ViewHolder viewHolder) {
        super.bindView(viewHolder);
        viewHolder.setNote(getNote());
        setUi(viewHolder, getNote());
    }

    private void setUi(ViewHolder holder, Note note) {
        Palette palette = PaletteUtils.get().palettes.get(note.getPalette());

        if (((ColorDrawable) holder.parentLayout.getBackground()).getColor() != palette.getPrimary()) {
            holder.parentLayout.setBackgroundColor(palette.getPrimary());
        }
        if (!holder.title.getText().toString().equals(note.getTitle())) {
            holder.title.setText(note.getTitle());
        }
        if (holder.title.getCurrentTextColor() != palette.getTextColor()) {
            holder.title.setTextColor(palette.getTextColor());
        }
        if (!holder.content.getText().toString().equals(note.getContent())) {
            holder.content.setText(note.getContent());
        }
        if (holder.content.getCurrentTextColor() != palette.getTextColor()) {
            holder.content.setTextColor(palette.getTextColor());
        }
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.parent_layout)
        protected LinearLayout parentLayout;
        @Bind(R.id.title)
        protected TextView title;
        @Bind(R.id.content)
        protected TextView content;

        protected Note note;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void setNote(Note note) {
            this.note = note;
            new Gaia.Builder()
                    .setMutativeView(parentLayout)
                    .setColorToScale(PaletteUtils.get().palettes.get(note.getPalette()).getPrimary())
                    .build();
            new Gaia.Builder()
                    .setMutativeView(title)
                    .setColorToScale(PaletteUtils.get().palettes.get(note.getPalette()).getTextColor())
                    .build();
            new Gaia.Builder()
                    .setMutativeView(content)
                    .setColorToScale(PaletteUtils.get().palettes.get(note.getPalette()).getTextColor())
                    .build();
        }
    }

    public Note getNote() {
        return note;
    }
}