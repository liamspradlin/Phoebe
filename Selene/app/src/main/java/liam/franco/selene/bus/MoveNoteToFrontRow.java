package liam.franco.selene.bus;

import liam.franco.selene.modules.Note;

public class MoveNoteToFrontRow {
    private Note note;
    private int layoutPosition;

    public MoveNoteToFrontRow(Note note, int layoutPosition) {
        this.note = note;
        this.layoutPosition = layoutPosition;
    }

    public Note getNote() {
        return note;
    }

    public int getLayoutPosition() {
        return layoutPosition;
    }
}
