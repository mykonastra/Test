package com.example.android.notepad;

import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.util.ArrayList;
import java.util.List;

/** Modernized upstream NotesList: timestamp cards, live search and combined filters. */
public final class NotesList extends NoteActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<Note> notes = new ArrayList<>();
    private EditText search;
    private Spinner category, status, sort;
    private TextView count, empty;
    private BaseAdapter adapter;
    private int generation;
    private boolean ready;
    private final Runnable refresh = this::reload;
    private final ContentObserver observer = new ContentObserver(handler) {
        @Override public void onChange(boolean selfChange) { schedule(); }
    };

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = ui.column(), body = ui.column();
        ui.pad(body, 20);
        root.addView(body, new LinearLayout.LayoutParams(-1, -1));
        body.addView(ui.text("把值得记住的，留在这里", 13, ui.muted));
        LinearLayout header = ui.row();
        header.addView(ui.heading("拾记 NotePad", 28), new LinearLayout.LayoutParams(0, -2, 1));
        Button settings = ui.button("设置", R.id.settings, false);
        settings.setContentDescription("设置主题和字号");
        settings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        header.addView(settings); body.addView(header); ui.gap(body, 16);
        search = ui.edit("搜索标题或正文…", R.id.search, 16);
        search.setSingleLine(true); search.setContentDescription("搜索标题或正文");
        search.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        if (state != null) search.setText(state.getString("query", ""));
        body.addView(search, ui.matchWrap()); ui.gap(body, 8);
        LinearLayout filters = ui.row();
        String[] categories = {"全部分类", "未分类", "学习", "工作", "生活", "灵感"};
        category = ui.spinner(categories, R.id.category_filter, state == null ? 0 : state.getInt("category"));
        status = ui.spinner(NotesRepository.STATUS_LABELS, R.id.status_filter, state == null ? 0 : state.getInt("status"));
        sort = ui.spinner(NotesRepository.SORT_LABELS, R.id.sort_order, preferences.sort());
        category.setContentDescription("按分类筛选"); status.setContentDescription("按待办状态筛选");
        sort.setContentDescription("笔记排序");
        filters.addView(category, new LinearLayout.LayoutParams(0, -2, 1));
        filters.addView(status, new LinearLayout.LayoutParams(0, -2, 1));
        body.addView(filters);
        count = ui.text("正在读取笔记…", 13, ui.muted); count.setId(R.id.result_count);
        count.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        LinearLayout summary = ui.row();
        summary.addView(count, new LinearLayout.LayoutParams(0, -2, 1));
        summary.addView(sort, new LinearLayout.LayoutParams(ui.dp(150), -2));
        body.addView(summary); ui.gap(body, 4);
        FrameLayout listFrame = new FrameLayout(this);
        ListView list = new ListView(this); list.setId(R.id.notes_list);
        list.setDivider(null); list.setClipToPadding(false);
        list.setPadding(0, 0, 0, ui.dp(8));
        empty = ui.text("正在读取笔记…", 18, ui.muted);
        empty.setGravity(android.view.Gravity.CENTER); ui.pad(empty, 20);
        listFrame.addView(empty, new FrameLayout.LayoutParams(-1, -1));
        listFrame.addView(list, new FrameLayout.LayoutParams(-1, -1));
        list.setEmptyView(empty);
        adapter = new BaseAdapter() {
            @Override public int getCount() { return notes.size(); }
            @Override public Note getItem(int pos) { return notes.get(pos); }
            @Override public long getItemId(int pos) { return notes.get(pos).id; }
            @Override public View getView(int pos, View recycled, ViewGroup parent) { return card(notes.get(pos)); }
        };
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, pos, id) -> open(id));
        body.addView(listFrame, new LinearLayout.LayoutParams(-1, 0, 1));
        Button add = ui.button("＋  新建笔记", R.id.add_note, true);
        add.setOnClickListener(v -> open(-1)); body.addView(add, ui.matchWrap());
        content(root);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { schedule(); }
            @Override public void afterTextChanged(Editable e) { }
        });
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (parent == sort) preferences.setSort(pos);
                schedule();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        };
        category.setOnItemSelectedListener(listener); status.setOnItemSelectedListener(listener);
        sort.setOnItemSelectedListener(listener); ready = true;
    }

    private View card(Note note) {
        FrameLayout outer = new FrameLayout(this);
        LinearLayout card = ui.column(); ui.pad(card, 16);
        card.setBackground(ui.shape(ui.surface, 16));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -2); lp.bottomMargin = ui.dp(10);
        outer.addView(card, lp);
        LinearLayout top = ui.row();
        top.addView(ui.text(note.category + (note.todo ? (note.done ? "  ·  已完成" : "  ·  待办") : ""), 12, ui.accent),
                new LinearLayout.LayoutParams(0, -2, 1));
        if (note.todo) {
            CheckBox done = new CheckBox(this); done.setChecked(note.done);
            done.setMinHeight(ui.dp(48)); done.setMinWidth(ui.dp(48));
            done.setContentDescription((note.done ? "标记未完成：" : "完成待办：") + note.title);
            done.setOnCheckedChangeListener((button, checked) -> {
                button.setEnabled(false);
                work(() -> { repository.setDone(note, checked); return true; }, result -> reload());
            });
            done.setFocusable(false); top.addView(done);
        }
        card.addView(top);
        TextView title = ui.heading(note.title, 20); title.setMaxLines(2);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        if (note.todo && note.done) title.setPaintFlags(title.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        card.addView(title);
        if (!note.body.isEmpty()) {
            TextView preview = ui.text(note.body.replace('\n', ' '), 14, ui.muted);
            preview.setMaxLines(2); preview.setEllipsize(android.text.TextUtils.TruncateAt.END); card.addView(preview);
        }
        ui.gap(card, 6);
        card.addView(ui.text("修改于 " + NotesRepository.formatTime(note.modified), 12, ui.muted));
        card.addView(ui.text("创建于 " + NotesRepository.formatTime(note.created), 11, ui.muted));
        return outer;
    }

    private void open(long id) { startActivity(new Intent(this, NoteEditor.class).putExtra("note_id", id)); }
    private void schedule() { if (ready) { handler.removeCallbacks(refresh); handler.postDelayed(refresh, 180); } }
    private void reload() {
        if (!ready || isDestroyed()) return;
        int request = ++generation;
        String keyword = search.getText().toString();
        String filter = category.getSelectedItemPosition() == 0 ? "" : category.getSelectedItem().toString();
        int state = status.getSelectedItemPosition(), order = sort.getSelectedItemPosition();
        work(() -> repository.list(keyword, filter, state, order), result -> {
            if (request != generation) return;
            notes.clear(); notes.addAll(result); adapter.notifyDataSetChanged();
            count.setText(getString(R.string.note_count, result.size()));
            empty.setText(keyword.trim().isEmpty() && filter.isEmpty() && state == 0
                    ? "还没有笔记\n\n点击下方按钮，记录第一份灵感" : "没有找到匹配的笔记\n\n试试其他关键词，或切换筛选条件");
        });
    }
    @Override protected void onResume() {
        super.onResume();
        getContentResolver().registerContentObserver(NotePad.Notes.CONTENT_URI, true, observer);
        if (ready) reload();
    }
    @Override protected void onPause() { getContentResolver().unregisterContentObserver(observer); super.onPause(); }
    @Override protected void onDestroy() { ++generation; handler.removeCallbacksAndMessages(null); super.onDestroy(); }
    @Override protected void failure(Exception error) {
        super.failure(error); count.setText("读取或更新失败，请重试");
        // Restore checkboxes after a failed write rather than showing an unsaved completion state.
        adapter.notifyDataSetChanged();
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putString("query", search.getText().toString()); state.putInt("category", category.getSelectedItemPosition());
        state.putInt("status", status.getSelectedItemPosition()); super.onSaveInstanceState(state);
    }
}
