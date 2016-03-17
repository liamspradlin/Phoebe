package liam.franco.selene.eventbus;

public class MoveNoteToFrontRow {
    private int layoutPosition;

    public MoveNoteToFrontRow(int layoutPosition) {
        this.layoutPosition = layoutPosition;
    }

    public int getLayoutPosition() {
        return layoutPosition;
    }
}
