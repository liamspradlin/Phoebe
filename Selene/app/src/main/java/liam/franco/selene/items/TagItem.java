package liam.franco.selene.items;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

        viewHolder.tag.getBackground().setTint(palette.getPrimary());
        viewHolder.tag.setTag(palette.getName());
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tag)
        protected ImageView tag;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.tag)
        public void onTagClick(ImageView tag) {
            App.BUS.post(new TagClicked((String) tag.getTag()));
        }
    }
}