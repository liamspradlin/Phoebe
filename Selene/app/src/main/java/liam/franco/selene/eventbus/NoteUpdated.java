package liam.franco.selene.eventbus;

import liam.franco.selene.modules.Note;

public class NoteUpdated {
    private Note editedNote;

    public NoteUpdated(Note editedNote) {
        this.editedNote = editedNote;
    }

    public Note getEditedNote() {
        return editedNote;
    }
}
