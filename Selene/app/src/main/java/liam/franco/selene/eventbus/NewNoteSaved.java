package liam.franco.selene.eventbus;

import liam.franco.selene.modules.Note;

public class NewNoteSaved {
    public Note note;

    public NewNoteSaved(Note note) {
        this.note = note;
    }
}
