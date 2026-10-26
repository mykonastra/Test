package com.example.android.notepad;

import android.os.Bundle;
import android.widget.*;
import android.view.View;

public final class SettingsActivity extends NoteActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = ui.column(), page = ui.column(); ui.pad(page, 24);
        ScrollView scroll = new ScrollView(this); scroll.addView(page); root.addView(scroll);
        Button back = ui.button("‹  返回笔记", View.generateViewId(), false); back.setOnClickListener(v -> finish());
        page.addView(back); ui.gap(page, 24); page.addView(ui.heading("让记录更合心意", 26));
        page.addView(ui.text("设置会保存在本机，下次打开仍然生效。", 14, ui.muted));
        ui.gap(page, 28); page.addView(ui.heading("外观主题", 18));
        Switch dark = new Switch(this); dark.setId(R.id.theme_choice); dark.setText("深色模式");
        // Preferences are the source of truth. Restoring an older View state must not toggle them back.
        dark.setSaveEnabled(false);
        dark.setTextColor(ui.ink); dark.setTextSize(17); dark.setMinHeight(ui.dp(56)); dark.setChecked(preferences.dark());
        dark.setOnCheckedChangeListener((view, checked) -> { preferences.setDark(checked); recreate(); });
        page.addView(dark); ui.gap(page, 20); page.addView(ui.heading("编辑器字号", 18));
        int font = preferences.fontSize();
        Spinner fonts = ui.spinner(new String[]{"小 · 16sp", "标准 · 18sp", "大 · 22sp"}, R.id.font_choice,
                font == 16 ? 0 : font == 22 ? 2 : 1);
        fonts.setSaveEnabled(false);
        fonts.setContentDescription("编辑器字号");
        fonts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { preferences.setFontSize(new int[]{16,18,22}[pos]); }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        page.addView(fonts); ui.gap(page, 28); page.addView(ui.heading("关于拾记", 18));
        page.addView(ui.text("NotePad 课程期中实验\n\n笔记保存在本机 SQLite 数据库，无需登录或联网。\n\n在编辑页可导出 UTF-8 文本。卸载应用会清除本机笔记，请提前导出重要内容。", 15, ui.muted));
        content(root);
    }
}
