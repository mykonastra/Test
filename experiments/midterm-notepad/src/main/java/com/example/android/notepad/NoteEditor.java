package com.example.android.notepad;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/** Explicit save with discard confirmation; no empty row is inserted merely by opening. */
public final class NoteEditor extends NoteActivity {
    private EditText title, body;
    private Spinner category;
    private CheckBox todo, done;
    private TextView timestamp, words;
    private Button save, delete, export;
    private long noteId = -1;
    private boolean loaded, busy;
    private String baseline = "";
    private Note exportSnapshot;
    private SaveSession session;
    private static final int EXPORT = 20;
    private static final class SaveSession { CompletableFuture<Long> future; }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::leave);
        }
        Object retained = getLastNonConfigurationInstance();
        session = retained instanceof SaveSession ? (SaveSession)retained : new SaveSession();
        noteId = state == null ? getIntent().getLongExtra("note_id", -1) : state.getLong("note_id", -1);
        LinearLayout root = ui.column(), page = ui.column(); ui.pad(page, 20);
        LinearLayout header = ui.row();
        Button back = ui.button("‹  返回", View.generateViewId(), false); back.setOnClickListener(v -> leave());
        header.addView(back); header.addView(ui.heading(noteId < 0 ? "  新的记录" : "  编辑笔记", 22));
        page.addView(header); ui.gap(page, 16);
        title = ui.edit("给这份记录起个名字", R.id.note_title, 24); title.setSingleLine(true);
        title.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        title.setContentDescription("笔记标题"); page.addView(title, ui.matchWrap()); ui.gap(page, 10);
        timestamp = ui.text("保存后显示创建与修改时间", 12, ui.muted); page.addView(timestamp);
        page.addView(ui.text("分类", 13, ui.muted));
        category = ui.spinner(NotesRepository.CATEGORIES, R.id.note_category, 0);
        category.setContentDescription("笔记分类"); page.addView(category);
        LinearLayout checks = ui.row();
        todo = new CheckBox(this); todo.setId(R.id.note_todo); todo.setText("设为待办"); todo.setTextColor(ui.ink);
        done = new CheckBox(this); done.setId(R.id.note_done); done.setText("已完成"); done.setTextColor(ui.ink);
        checks.addView(todo); checks.addView(done); page.addView(checks);
        todo.setOnCheckedChangeListener((v, checked) -> { done.setEnabled(checked && !busy); if (!checked) done.setChecked(false); });
        ui.gap(page, 8);
        body = ui.edit("从一句话开始，记录想法、计划或今天的收获…", R.id.note_body, preferences.fontSize());
        body.setGravity(android.view.Gravity.TOP); body.setMinLines(9);
        body.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        body.setContentDescription("笔记正文"); page.addView(body, ui.matchWrap());
        words = ui.text("0 字", 12, ui.muted); page.addView(words);
        body.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int c, int d) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { words.setText(getString(R.string.word_count, s.length())); }
            @Override public void afterTextChanged(Editable e) { }
        });
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.addView(page);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout actions = ui.row(); ui.pad(actions, 16);
        delete = ui.button("删除", R.id.delete_note, false); delete.setOnClickListener(v -> confirmDelete());
        export = ui.button("导出", R.id.export_note, false); export.setOnClickListener(v -> requestExport());
        save = ui.button("保存", R.id.save_note, true); save.setOnClickListener(v -> save());
        actions.addView(delete, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout.LayoutParams gap = new LinearLayout.LayoutParams(0, -2, 1); gap.setMargins(ui.dp(8),0,ui.dp(8),0);
        actions.addView(export, gap); actions.addView(save, new LinearLayout.LayoutParams(0, -2, 1.3f));
        root.addView(actions); content(root);
        if (state != null && state.getBoolean("loaded")) {
            title.setText(state.getString("title")); body.setText(state.getString("body"));
            category.setSelection(state.getInt("category")); todo.setChecked(state.getBoolean("todo"));
            done.setChecked(state.getBoolean("done")); baseline = state.getString("baseline", "");
            timestamp.setText(state.getString("timestamp")); exportSnapshot = restoreSnapshot(state);
            loaded = true; enabled(true);
        } else if (noteId < 0) { loaded = true; baseline = snapshot(); enabled(true); }
        else {
            enabled(false);
            work(() -> repository.get(noteId), note -> {
                if (note == null) { toast("笔记已不存在"); finish(); return; }
                title.setText(note.title); body.setText(note.body);
                category.setSelection(Math.max(0, Arrays.asList(NotesRepository.CATEGORIES).indexOf(note.category)));
                todo.setChecked(note.todo); done.setChecked(note.done);
                timestamp.setText(getString(R.string.note_dates, NotesRepository.formatTime(note.created), NotesRepository.formatTime(note.modified)));
                loaded = true; baseline = snapshot(); enabled(true);
            });
        }
        if (session.future != null) attachSave();
    }

    private String snapshot() {
        // JSON avoids collisions if a user enters delimiter-like characters.
        return new org.json.JSONArray().put(title.getText().toString()).put(body.getText().toString())
                .put(category.getSelectedItemPosition()).put(todo.isChecked()).put(done.isChecked()).toString();
    }
    private void enabled(boolean value) {
        busy = !value; title.setEnabled(value); body.setEnabled(value); category.setEnabled(value);
        todo.setEnabled(value); done.setEnabled(value && todo.isChecked()); save.setEnabled(value);
        delete.setEnabled(value && noteId >= 0); export.setEnabled(value && noteId >= 0);
    }
    private void save() {
        if (!loaded || busy) return;
        String t = title.getText().toString(), b = body.getText().toString(), c = category.getSelectedItem().toString();
        boolean td = todo.isChecked(), dn = done.isChecked(); long id = noteId;
        NotesRepository store = new NotesRepository(getApplicationContext());
        enabled(false);
        session.future = CompletableFuture.supplyAsync(() -> store.save(id, t, b, c, td, dn), IO);
        attachSave();
    }
    private void attachSave() {
        enabled(false);
        session.future.whenComplete((id, error) -> runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            if (error != null) {
                session.future = null; enabled(true);
                Throwable cause = error.getCause() == null ? error : error.getCause();
                failure(cause instanceof Exception ? (Exception)cause : new RuntimeException(cause));
            } else { noteId = id; toast("已保存"); finish(); }
        }));
    }
    private void leave() {
        if (!loaded) { finish(); return; }
        if (busy) { toast("正在读取或保存，请稍候"); return; }
        if (baseline.equals(snapshot())) { finish(); return; }
        new AlertDialog.Builder(this).setTitle("有尚未保存的修改")
                .setMessage("保存后再离开，或放弃本次修改。")
                .setPositiveButton("保存", (d,w) -> save())
                .setNegativeButton("放弃修改", (d,w) -> finish()).setNeutralButton("继续编辑", null).show();
    }
    // API 24-32 compatibility. API 33+ uses the OnBackInvokedDispatcher registered above.
    @android.annotation.SuppressLint("GestureBackNavigation")
    @Override public void onBackPressed() { leave(); }
    private void confirmDelete() {
        new AlertDialog.Builder(this).setTitle("删除这条笔记？").setMessage("删除后无法恢复。可先导出文本留存。")
                .setNegativeButton("取消", null).setPositiveButton("删除", (d,w) -> {
                    enabled(false); work(() -> { repository.delete(noteId); return true; }, result -> { toast("已删除"); finish(); });
                }).show();
    }
    private void requestExport() {
        if (!baseline.equals(snapshot())) { toast("请先保存修改，再打开笔记导出"); return; }
        enabled(false);
        work(() -> repository.get(noteId), note -> {
            enabled(true);
            if (note == null) { toast("笔记已不存在"); return; }
            exportSnapshot = note;
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("text/plain").putExtra(Intent.EXTRA_TITLE, note.title.replaceAll("[\\\\/:*?\"<>|]", "_") + ".txt");
            try { startActivityForResult(intent, EXPORT); }
            catch (android.content.ActivityNotFoundException e) { toast("设备未提供文件保存界面"); }
        });
    }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != EXPORT || result != RESULT_OK || data == null || data.getData() == null) return;
        Uri destination = data.getData(); Note note = exportSnapshot;
        if (note == null) { toast("导出中断，请重新选择导出"); return; }
        enabled(false);
        work(() -> {
            try (OutputStream stream = getContentResolver().openOutputStream(destination, "wt")) {
                if (stream == null) throw new java.io.IOException("Unable to open output");
                NotesRepository.exportText(note, stream);
            }
            return true;
        }, resultValue -> { enabled(true); toast("文本已导出"); });
    }
    @Override protected void onResume() { super.onResume(); if (body != null) body.setTextSize(preferences.fontSize()); }
    @Override protected void failure(Exception error) { super.failure(error); if (loaded) enabled(true); }
    @Override public Object onRetainNonConfigurationInstance() { return session; }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putLong("note_id", noteId); state.putBoolean("loaded", loaded);
        state.putString("title", title.getText().toString()); state.putString("body", body.getText().toString());
        state.putInt("category", category.getSelectedItemPosition()); state.putBoolean("todo", todo.isChecked());
        state.putBoolean("done", done.isChecked()); state.putString("baseline", baseline);
        state.putString("timestamp", timestamp.getText().toString());
        if (exportSnapshot != null) {
            state.putLong("export_id", exportSnapshot.id); state.putString("export_title", exportSnapshot.title);
            state.putString("export_body", exportSnapshot.body); state.putString("export_category", exportSnapshot.category);
            state.putLong("export_created", exportSnapshot.created); state.putLong("export_modified", exportSnapshot.modified);
            state.putBoolean("export_todo", exportSnapshot.todo); state.putBoolean("export_done", exportSnapshot.done);
        }
        super.onSaveInstanceState(state);
    }
    private Note restoreSnapshot(Bundle state) {
        if (!state.containsKey("export_id")) return null;
        return new Note(state.getLong("export_id"), state.getString("export_title"), state.getString("export_body"),
                state.getString("export_category"), state.getBoolean("export_todo"), state.getBoolean("export_done"),
                state.getLong("export_created"), state.getLong("export_modified"));
    }
}
