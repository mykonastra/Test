package com.example.android.notepad;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.*;

/** Small native-view design system, deliberately free of third-party UI dependencies. */
final class Ui {
    final Activity context;
    final int background, surface, ink, muted, accent, border;
    Ui(Activity context, boolean dark) {
        this.context = context;
        background = Color.parseColor(dark ? "#121E1A" : "#F6F4EE");
        surface = Color.parseColor(dark ? "#20332B" : "#FFFFFF");
        ink = Color.parseColor(dark ? "#F2F5EF" : "#203C31");
        muted = Color.parseColor(dark ? "#B6C8BD" : "#66776C");
        accent = Color.parseColor(dark ? "#8ED6B7" : "#176B54");
        border = Color.parseColor(dark ? "#3F5549" : "#DCE3D9");
    }
    int dp(float value) { return Math.round(value * context.getResources().getDisplayMetrics().density); }
    LinearLayout column() { LinearLayout view = new LinearLayout(context); view.setOrientation(LinearLayout.VERTICAL); return view; }
    LinearLayout row() { LinearLayout view = new LinearLayout(context); view.setOrientation(LinearLayout.HORIZONTAL); view.setGravity(android.view.Gravity.CENTER_VERTICAL); return view; }
    TextView text(String value, int size, int color) {
        TextView view = new TextView(context); view.setText(value); view.setTextSize(size); view.setTextColor(color);
        view.setPadding(0, dp(4), 0, dp(4)); return view;
    }
    TextView heading(String value, int size) { TextView view = text(value, size, ink); view.setTypeface(null, Typeface.BOLD); return view; }
    Button button(String value, int id, boolean primary) {
        Button view = new Button(context); view.setId(id); view.setText(value); view.setAllCaps(false);
        view.setTextSize(15); view.setMinHeight(dp(48)); view.setMinimumHeight(dp(48));
        view.setTextColor(primary ? background : accent); view.setBackground(shape(primary ? accent : surface, 12));
        view.setPadding(dp(16), dp(8), dp(16), dp(8)); return view;
    }
    EditText edit(String hint, int id, int size) {
        EditText view = new EditText(context); view.setId(id); view.setHint(hint); view.setTextColor(ink);
        view.setHintTextColor(muted); view.setTextSize(size); view.setPadding(dp(14), dp(10), dp(14), dp(10));
        view.setBackground(shape(surface, 12)); return view;
    }
    Spinner spinner(String[] values, int id, int selected) {
        Spinner view = new Spinner(context); view.setId(id);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, values) {
            @Override public View getView(int pos, View convert, android.view.ViewGroup parent) {
                TextView v = (TextView)super.getView(pos, convert, parent); v.setTextColor(ink); v.setTextSize(14); return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        view.setAdapter(adapter); view.setSelection(selected); view.setMinimumHeight(dp(48)); return view;
    }
    GradientDrawable shape(int color, int radius) {
        GradientDrawable shape = new GradientDrawable(); shape.setColor(color); shape.setCornerRadius(dp(radius));
        shape.setStroke(dp(1), border); return shape;
    }
    void gap(LinearLayout parent, int height) { View view = new View(context); parent.addView(view, new LinearLayout.LayoutParams(1, dp(height))); }
    void pad(View view, int amount) { view.setPadding(dp(amount), dp(amount), dp(amount), dp(amount)); }
    LinearLayout.LayoutParams matchWrap() { return new LinearLayout.LayoutParams(-1, -2); }
}
