package com.example.android.notepad;

/** Immutable snapshot: no live Cursor or Activity is held by the UI. */
public final class Note {
    public final long id, created, modified;
    public final String title, body, category;
    public final boolean todo, done;

    public Note(long id, String title, String body, String category, boolean todo,
                boolean done, long created, long modified) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.category = category;
        this.todo = todo;
        this.done = done;
        this.created = created;
        this.modified = modified;
    }
}
