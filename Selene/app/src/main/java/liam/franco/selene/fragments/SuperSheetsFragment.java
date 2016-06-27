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

package liam.franco.selene.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.materialize.util.UIUtils;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.halfbit.tinybus.Subscribe;
import liam.franco.selene.R;
import liam.franco.selene.activities.EditNoteActivity;
import liam.franco.selene.activities.MainActivity;
import liam.franco.selene.application.App;
import liam.franco.selene.bus.NewNoteSaved;
import liam.franco.selene.bus.NoteDeleted;
import liam.franco.selene.bus.NoteUpdatingEvent;
import liam.franco.selene.modules.Note;
import liam.franco.selene.ui.NoteItem;
import liam.franco.selene.utils.GaiaUtils;
import liam.franco.selene.utils.RecyclerViewUtils;

public class SuperSheetsFragment extends Fragment {
    @BindView(R.id.parent_layout)
    protected FrameLayout parentLayout;
    @BindView(R.id.recycler_view)
    protected RecyclerView recyclerView;
    @BindView(R.id.empty_view)
    protected LinearLayout emptyView;

    @LayoutRes
    private int resId;

    private FastItemAdapter<NoteItem> adapter;
    private String tabTitle;
    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(resId, container, false);
        unbinder = ButterKnife.bind(this, layout);
        App.BUS.register(this);

        adapter = new FastItemAdapter<>();
        recyclerView.setAdapter(adapter);
        RecyclerView.ItemDecoration itemDecoration
                = new RecyclerViewUtils.SpacesItemDecoration((int) UIUtils.convertDpToPixel(4, App.CONTEXT));
        recyclerView.addItemDecoration(itemDecoration);

        adapter.withOnClickListener(new FastAdapter.OnClickListener<NoteItem>() {
            @Override
            public boolean onClick(View v, IAdapter<NoteItem> adapter, NoteItem item, int position) {
                Intent newNoteActivity = new Intent(getActivity(), EditNoteActivity.class);
                newNoteActivity.putExtra("uid", item.getViewHolder().getNote().getUid());

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                        Pair.create(v.findViewById(R.id.title), "note_title"));

                ActivityCompat.startActivity(getActivity(), newNoteActivity, options.toBundle());

                return true;
            }
        });

        return layout;
    }


    public FastItemAdapter<NoteItem> getAdapter() {
        return adapter;
    }

    public void setResId(@LayoutRes int resId) {
        this.resId = resId;
    }

    public void setEmptyView() {
        emptyView.setVisibility(getAdapter().getAdapterItemCount() > 0 ? View.GONE : View.VISIBLE);
    }

    public String getTabTitle() {
        return tabTitle;
    }

    public void setTabTitle(String tabTitle) {
        this.tabTitle = tabTitle;
    }

    public void addNote(Note note) {
        if (getAdapter() != null) {
            getAdapter().add(0, new NoteItem(note));
        }
    }

    @Subscribe(queue = "note_created")
    public void newNoteCreated(NewNoteSaved newNote) {
        if ((newNote.getNote().getArchive() && getTabTitle().equals(MainActivity.PAST))
                || (newNote.getNote().getReminder() && getTabTitle().equals(MainActivity.FUTURE))
                || getTabTitle().equals(MainActivity.PRESENT) &&
                (!newNote.getNote().getArchive() && !newNote.getNote().getReminder())) {
            addNote(newNote.getNote());
        }

        setEmptyView();
    }

    // why do I get the feeling the code below is fugly?
    @Subscribe(queue = "note_updated")
    public void noteUpdated(NoteUpdatingEvent noteUpdatingEvent) {
        boolean pastAdd = getTabTitle().equals(MainActivity.PAST) && noteUpdatingEvent.getNote().getArchive();
        boolean futureAdd = getTabTitle().equals(MainActivity.FUTURE) && noteUpdatingEvent.getNote().getReminder();
        boolean presentAdd = getTabTitle().equals(MainActivity.PRESENT) &&
                (!noteUpdatingEvent.getNote().getReminder() && !noteUpdatingEvent.getNote().getArchive());

        boolean exists = false;
        for (int i = 0; i < getAdapter().getAdapterItems().size(); i++) {
            exists = noteUpdatingEvent.getNote().getUid() ==
                    getAdapter().getAdapterItem(i).getViewHolder().getNote().getUid();

            if (exists) {
                break;
            }
        }

        if (!exists) {
            if (pastAdd || futureAdd || presentAdd) {
                addNote(noteUpdatingEvent.getNote());
            }
        } else {
            if (!pastAdd && !futureAdd && !presentAdd) {
                App.BUS.unregister(getAdapter().getAdapterItem(noteUpdatingEvent.getLayoutPosition()).getViewHolder());
                getAdapter().remove(noteUpdatingEvent.getLayoutPosition());
            } else {
                Collections.swap(getAdapter().getAdapterItems(), noteUpdatingEvent.getLayoutPosition(), 0);
                getAdapter().notifyAdapterItemMoved(noteUpdatingEvent.getLayoutPosition(), 0);
            }
        }

        setEmptyView();
    }

    @Subscribe(queue = "note_deleted")
    public void noteDeleted(NoteDeleted note) {
        for (int i = 0; i < getAdapter().getAdapterItems().size(); i++) {
            if (note.getDeletedNote().getUid() ==
                    getAdapter().getAdapterItem(i).getViewHolder().getNote().getUid()) {
                getAdapter().remove(i);

                // we gotta clean this note from Gaia otherwise it'll create havoc
                // not super pretty looking code, but gets the job done!
                String noteTitle = note.getDeletedNote().getTitle();
                GaiaUtils.seekAndDestroy(noteTitle + "_" + GaiaUtils.NOTE_PARENT_LAYOUT);
                GaiaUtils.seekAndDestroy(noteTitle + "_" + GaiaUtils.NOTE_TITLE);
                GaiaUtils.seekAndDestroy(noteTitle + "_" + GaiaUtils.NOTE_CONTENT);

                App.REALM.beginTransaction();
                note.getDeletedNote().deleteFromRealm();
                App.REALM.commitTransaction();
                break;
            }
        }

        setEmptyView();
    }

    @Override
    public void onDestroyView() {
        App.BUS.unregister(this);
        super.onDestroyView();
        unbinder.unbind();
    }
}
