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

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import liam.franco.selene.bus.Future;
import liam.franco.selene.bus.MoveNoteToFrontRow;
import liam.franco.selene.bus.NewNoteSaved;
import liam.franco.selene.bus.NotesCount;
import liam.franco.selene.bus.Present;
import liam.franco.selene.modules.Note;
import liam.franco.selene.ui.NoteItem;
import liam.franco.selene.utils.RecyclerViewUtils;

public class SuperNotesFragment extends Fragment {
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

                startActivity(newNoteActivity);

                return true;
            }
        });

        return layout;
    }

    public void addNote(Note note) {
        if (getAdapter() != null) {
            getAdapter().add(0, new NoteItem(note));
        }
    }

    public void updateNote(int oldPosition) {
        if (getAdapter() != null && getAdapter().getAdapterItemCount() > 0) {
            Collections.swap(getAdapter().getAdapterItems(), oldPosition, 0);
            getAdapter().notifyAdapterItemMoved(oldPosition, 0);
        }
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

    @Subscribe
    public void newNoteCreated(NewNoteSaved newNote) {
        if (newNote.getNote().getArchive()) {
            if (getTabTitle().equals(MainActivity.PAST)) {
                addNote(newNote.getNote());
            }

        } else if (newNote.getNote().getReminder()) {
            if (getTabTitle().equals(MainActivity.FUTURE)) {
                addNote(newNote.getNote());
                App.BUS.post(new Future());
            }

        } else {
            if (getTabTitle().equals(MainActivity.PRESENT)) {
                addNote(newNote.getNote());
                App.BUS.post(new Present());
            }
        }

        setEmptyView();
    }

    @Subscribe
    public void noteUpdated(MoveNoteToFrontRow moveNoteToFrontRow) {
        updateNote(moveNoteToFrontRow.getLayoutPosition());
    }

    @Subscribe
    public void newNotesCount(NotesCount notesCount) {
        boolean hasNotes = notesCount.getSize() > 0;
        int paddingBottom = -1;

        if (parentLayout != null) {
            if (hasNotes) {
                if (parentLayout.getPaddingBottom() > 0) {
                    paddingBottom = 0;
                }
            } else {
                if (parentLayout.getPaddingBottom() <= 0) {
                    paddingBottom = (int) UIUtils.convertDpToPixel(208, App.CONTEXT);
                }
            }

            if (paddingBottom > -1) {
                ValueAnimator animator = ValueAnimator.ofInt(parentLayout.getPaddingBottom(), paddingBottom);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        if (parentLayout != null) {
                            parentLayout.setPadding(parentLayout.getPaddingLeft(),
                                    parentLayout.getPaddingTop(),
                                    parentLayout.getPaddingRight(),
                                    (Integer) valueAnimator.getAnimatedValue());
                        }
                    }
                });
                animator.setDuration(500);
                animator.start();
            }
        }
    }

    @Override
    public void onDestroyView() {
        App.BUS.unregister(this);
        super.onDestroyView();
        unbinder.unbind();
    }
}
