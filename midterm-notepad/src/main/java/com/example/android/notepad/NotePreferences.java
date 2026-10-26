package com.example.android.notepad;

import android.content.Context;
import android.content.SharedPreferences;

public final class NotePreferences {
    private final SharedPreferences prefs;
    public NotePreferences(Context context) { prefs = context.getSharedPreferences("notepad_preferences", Context.MODE_PRIVATE); }
    public boolean dark() { return prefs.getBoolean("dark", false); }
    public int fontSize() { return prefs.getInt("font", 18); }
    public int sort() { return prefs.getInt("sort", 0); }
    public void setDark(boolean value) { prefs.edit().putBoolean("dark", value).apply(); }
    public void setFontSize(int value) { prefs.edit().putInt("font", value == 16 || value == 22 ? value : 18).apply(); }
    public void setSort(int value) { prefs.edit().putInt("sort", Math.max(0, Math.min(2, value))).apply(); }
}
