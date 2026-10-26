package com.example.android.notepad;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Locale;

/** CRUD and parameterized literal substring search over the upstream ContentProvider. */
public final class NotesRepository {
    public static final String[] CATEGORIES = {"未分类", "学习", "工作", "生活", "灵感"};
    public static final String[] SORT_LABELS = {"最近修改", "最早创建", "标题 A–Z"};
    public static final String[] STATUS_LABELS = {"全部笔记", "未完成待办", "已完成待办"};
    private static final String[] SORT_SQL = {"modified DESC, _id DESC", "created ASC, _id ASC",
            "title COLLATE NOCASE ASC, _id ASC"};
    private final ContentResolver resolver;

    public NotesRepository(Context context) { resolver = context.getContentResolver(); }

    public List<Note> list(String keyword, String category, int status, int sort) {
        List<String> clauses = new ArrayList<>(), args = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            // instr treats %, _, quotes and backslashes as literal characters, not SQL syntax.
            clauses.add("(instr(lower(title), lower(?)) > 0 OR instr(lower(note), lower(?)) > 0)");
            args.add(keyword.trim()); args.add(keyword.trim());
        }
        if (category != null && !category.isEmpty()) { clauses.add("category = ?"); args.add(category); }
        if (status == 1) clauses.add("is_todo = 1 AND is_done = 0");
        if (status == 2) clauses.add("is_todo = 1 AND is_done = 1");
        List<Note> notes = new ArrayList<>();
        try (Cursor c = resolver.query(NotePad.Notes.CONTENT_URI, null,
                clauses.isEmpty() ? null : android.text.TextUtils.join(" AND ", clauses),
                args.toArray(new String[0]), SORT_SQL[Math.max(0, Math.min(sort, 2))])) {
            if (c != null) while (c.moveToNext()) notes.add(read(c));
        }
        return notes;
    }

    public Note get(long id) {
        try (Cursor c = resolver.query(uri(id), null, null, null, null)) {
            return c != null && c.moveToFirst() ? read(c) : null;
        }
    }

    public long save(long id, String title, String body, String category, boolean todo, boolean done) {
        title = title.trim(); body = body.trim();
        if (title.isEmpty() && body.isEmpty()) throw new IllegalArgumentException("请填写标题或正文后再保存");
        if (title.length() > 200) throw new IllegalArgumentException("标题最多 200 字");
        if (body.length() > 100000) throw new IllegalArgumentException("正文最多 100000 字");
        if (!Arrays.asList(CATEGORIES).contains(category)) throw new IllegalArgumentException("请选择有效分类");
        if (title.isEmpty()) {
            String firstLine = body.split("\\R", 2)[0];
            title = firstLine.substring(0, Math.min(firstLine.length(), 60));
        }
        ContentValues values = new ContentValues();
        values.put("title", title); values.put("note", body); values.put("category", category);
        values.put("is_todo", todo ? 1 : 0); values.put("is_done", todo && done ? 1 : 0);
        if (id < 0) {
            Uri result = resolver.insert(NotePad.Notes.CONTENT_URI, values);
            if (result == null) throw new IllegalStateException("保存失败，请重试");
            return ContentUris.parseId(result);
        }
        if (resolver.update(uri(id), values, null, null) != 1) throw new IllegalStateException("笔记已不存在");
        return id;
    }

    public void setDone(Note note, boolean done) {
        ContentValues values = new ContentValues(); values.put("is_done", done ? 1 : 0);
        if (!note.todo || resolver.update(uri(note.id), values, null, null) != 1)
            throw new IllegalStateException("待办已不存在");
    }

    public void delete(long id) {
        if (resolver.delete(uri(id), null, null) != 1) throw new IllegalStateException("笔记已不存在");
    }

    public static String formatTime(long time) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(time);
    }

    public static void exportText(Note note, OutputStream stream) throws IOException {
        String text = note.title + "\n\n" + note.body + "\n\n分类：" + note.category
                + "\n创建：" + formatTime(note.created) + "\n修改：" + formatTime(note.modified)
                + (note.todo ? "\n待办：" + (note.done ? "已完成" : "未完成") : "") + "\n";
        stream.write(text.getBytes(StandardCharsets.UTF_8));
        stream.flush();
    }

    private static Uri uri(long id) { return ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, id); }
    private static Note read(Cursor c) {
        return new Note(c.getLong(c.getColumnIndexOrThrow("_id")), text(c, "title"), text(c, "note"),
                text(c, "category"), c.getInt(c.getColumnIndexOrThrow("is_todo")) == 1,
                c.getInt(c.getColumnIndexOrThrow("is_done")) == 1,
                c.getLong(c.getColumnIndexOrThrow("created")), c.getLong(c.getColumnIndexOrThrow("modified")));
    }
    private static String text(Cursor c, String column) {
        String value = c.getString(c.getColumnIndexOrThrow(column)); return value == null ? "" : value;
    }
}
