package com.example.android.notepad;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Toast;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/** One serial IO queue keeps writes ordered and survives Activity recreation. */
public abstract class NoteActivity extends Activity {
    static final ExecutorService IO = Executors.newSingleThreadExecutor();
    protected NotePreferences preferences;
    protected NotesRepository repository;
    protected Ui ui;
    private boolean dark;
    private static Toast currentToast;
    @Override protected void onCreate(Bundle state) {
        preferences = new NotePreferences(this);
        dark = preferences.dark();
        setTheme(dark ? R.style.Theme_NotePad_Dark : R.style.Theme_NotePad);
        super.onCreate(state);
        repository = new NotesRepository(this);
        ui = new Ui(this, dark);
        getWindow().setStatusBarColor(ui.background);
        getWindow().setNavigationBarColor(ui.background);
    }
    @Override protected void onResume() {
        super.onResume();
        if (dark != preferences.dark()) recreate();
    }
    protected void content(View root) {
        root.setBackgroundColor(ui.background);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.ime());
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            } else view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        setContentView(root);
        root.requestApplyInsets();
    }
    protected <T> void work(Callable<T> operation, Consumer<T> success) {
        IO.execute(() -> {
            try {
                T result = operation.call();
                runOnUiThread(() -> { if (!isFinishing() && !isDestroyed()) success.accept(result); });
            } catch (Exception error) {
                runOnUiThread(() -> { if (!isFinishing() && !isDestroyed()) failure(error); });
            }
        });
    }
    protected void failure(Exception error) {
        android.util.Log.e("NotePad", "Operation failed", error);
        toast(error instanceof IllegalArgumentException || error instanceof IllegalStateException
                ? error.getMessage() : "操作失败，请检查存储空间或文件权限后重试");
    }
    protected void toast(String message) {
        if (currentToast != null) currentToast.cancel();
        currentToast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        currentToast.show();
    }
}
