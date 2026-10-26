package com.example.android.notepad;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/** Dependency-free on-device integration tests. Every case runs against real Android APIs. */
public final class NotePadTestRunner extends Instrumentation {
    private NotesRepository repo;
    private int passed, failed;
    private final List<Long> inserted = new ArrayList<>();
    private final String marker = "TEST_" + System.currentTimeMillis() + "_";
    private NotesList listActivity;
    private StringBuilder details = new StringBuilder();
    private String testFilter = "";
    private interface Check { void run() throws Exception; }

    @Override public void onCreate(Bundle args) {
        super.onCreate(args); testFilter = args == null ? "" : args.getString("testFilter", ""); start();
    }
    @Override public void onStart() {
        android.accessibilityservice.AccessibilityServiceInfo info = getUiAutomation().getServiceInfo();
        info.flags |= android.accessibilityservice.AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        getUiAutomation().setServiceInfo(info);
        repo = new NotesRepository(getTargetContext());
        // Tests must not depend on a previous user's theme. Restore their preferences on exit.
        NotePreferences original = new NotePreferences(getTargetContext());
        boolean originalDark = original.dark(); int originalFont = original.fontSize(), originalSort = original.sort();
        original.setDark(false); original.setFontSize(18); original.setSort(0);
        test("01_create_read_and_timestamps", this::createRead);
        test("02_update_keeps_creation_time", this::update);
        test("03_title_and_content_search", this::search);
        test("04_literal_and_injection_search", this::literalSearch);
        test("05_combined_category_and_todo_filters", this::filters);
        test("06_all_sort_modes", this::sort);
        test("07_empty_and_invalid_input", this::validation);
        test("08_delete_and_missing_note", this::delete);
        test("09_database_reopen_persistence", this::persistence);
        test("10_version2_to3_migration_keeps_data", this::migration);
        test("11_preferences_persist", this::preferences);
        test("12_utf8_text_export", this::export);
        test("13_provider_item_selection_is_scoped", this::itemSelection);
        cleanup();
        test("14_ui_create_save_search_edit", this::uiFlow);
        test("15_ui_rotation_keeps_draft", this::rotation);
        test("16_ui_category_and_todo", this::uiExtras);
        test("17_ui_preferences_controls", this::uiPreferences);
        test("18_ui_discard_and_delete_confirmation", this::uiDelete);
        test("19_system_document_export", this::uiExport);
        cleanup();
        test("20_demo_and_screenshots", this::screenshots);
        cleanup();
        original.setDark(originalDark); original.setFontSize(originalFont); original.setSort(originalSort);
        Bundle result = new Bundle();
        result.putString("stream", "\n" + details + "\nRESULT: " + passed + " passed, " + failed + " failed\n");
        result.putInt("passed", passed); result.putInt("failed", failed);
        finish(failed == 0 ? Activity.RESULT_OK : Activity.RESULT_CANCELED, result);
    }
    private void test(String name, Check check) {
        if (!testFilter.isEmpty() && !name.contains(testFilter)) return;
        try { check.run(); passed++; details.append("PASS ").append(name).append('\n'); }
        catch (Throwable e) {
            failed++; details.append("FAIL ").append(name).append(": ").append(e).append('\n');
            android.util.Log.e("NotePadTests", name, e);
        }
        Bundle status = new Bundle(); status.putString("stream", details.substring(details.lastIndexOf("\n", details.length()-2)+1));
        sendStatus(0, status);
    }
    private long add(String title, String body, String category, boolean todo, boolean done) {
        long id = repo.save(-1, marker + title, body, category, todo, done); inserted.add(id); return id;
    }
    private void createRead() {
        long before = System.currentTimeMillis(); long id = add("第一条", "正文", "学习", false, true);
        Note n = repo.get(id);
        require(n != null && n.title.equals(marker+"第一条") && n.body.equals("正文"), "CRUD round trip");
        require(n.created >= before && n.modified == n.created, "creation and modification timestamps");
        require(!n.done, "plain notes cannot be completed todos");
    }
    private void update() {
        long id = add("update", "v1", "生活", true, false); Note old = repo.get(id);
        SystemClock.sleep(30); repo.save(id, marker+"changed", "v2", "工作", true, true);
        Note n = repo.get(id);
        require(n.created == old.created && n.modified > old.modified, "immutable creation and advancing modification");
        require(n.body.equals("v2") && n.done && n.category.equals("工作"), "update all fields");
        repo.setDone(n, false); require(!repo.get(id).done, "toggle todo persisted");
    }
    private void search() {
        long id = add("TITLEKEY", "只有正文包含稀有词与 BodyKey", "学习", false, false);
        require(contains(repo.list("titlekey", "",0,0),id), "case insensitive title");
        require(contains(repo.list("稀有词", "",0,0),id), "Chinese body search");
        require(contains(repo.list("bodykey", "",0,0),id), "case insensitive body");
        require(repo.list(marker+"nothing_here", "",0,0).isEmpty(), "no results");
    }
    private void literalSearch() {
        long id = add("100%_literal", "quote ' and \\", "灵感",false,false);
        require(contains(repo.list("%_", "",0,0), id), "percent and underscore literal");
        require(contains(repo.list("'", "",0,0), id), "quote literal");
        require(repo.list("' OR 1=1 --", "",0,0).isEmpty(), "injection treated as text");
    }
    private void filters() {
        long a=add("filterA","", "工作", true,false), b=add("filterB","", "工作",true,true);
        long c=add("filterC","", "生活",false,false);
        require(contains(repo.list(marker,"工作",1,0),a) && !contains(repo.list(marker,"工作",1,0),b), "unfinished filter");
        require(contains(repo.list(marker,"工作",2,0),b) && !contains(repo.list(marker,"工作",2,0),a), "finished filter");
        require(!contains(repo.list(marker,"工作",0,0),c), "category combined with search");
    }
    private void sort() {
        long a=add("sortB", "", "未分类",false,false); SystemClock.sleep(10);
        long b=add("sortA", "", "未分类",false,false);
        require(repo.list(marker+"sort", "",0,0).get(0).id==b, "recent modified");
        require(repo.list(marker+"sort", "",0,1).get(0).id==a, "oldest created");
        require(repo.list(marker+"sort", "",0,2).get(0).id==b, "title sort");
    }
    private void validation() {
        boolean empty=false, invalid=false, longTitle=false;
        try { repo.save(-1,"  ","\n", "学习",false,false); } catch(IllegalArgumentException e){empty=true;}
        try { repo.save(-1,"valid","body", "missing",false,false); } catch(IllegalArgumentException e){invalid=true;}
        try { repo.save(-1,new String(new char[201]).replace('\0','x'),"", "学习",false,false); } catch(IllegalArgumentException e){longTitle=true;}
        require(empty && invalid && longTitle,"invalid inputs rejected");
        long id=repo.save(-1,"",marker+"正文自动标题\n第二行","未分类",false,false); inserted.add(id);
        require(repo.get(id).title.equals(marker+"正文自动标题"),"body-only title fallback");
    }
    private void delete() {
        long id=add("delete", "", "生活",false,false); repo.delete(id); inserted.remove(id);
        require(repo.get(id)==null,"deleted row absent");
        boolean missing=false; try {repo.save(id,"missing","","生活",false,false);} catch(IllegalStateException e){missing=true;}
        require(missing,"editing missing row fails safely");
    }
    private void persistence() {
        long id=add("persistent","disk", "灵感",false,false);
        NotePadProvider.DatabaseHelper helper = new NotePadProvider.DatabaseHelper(getTargetContext());
        try (Cursor c=helper.getReadableDatabase().rawQuery("SELECT note FROM notes WHERE _id=?",new String[]{""+id})) {
            require(c.moveToFirst() && c.getString(0).equals("disk"),"separate DB handle sees persisted content");
        } finally {helper.close();}
        require(new NotesRepository(getTargetContext()).get(id)!=null,"new repository sees note");
    }
    private void migration() {
        String name="notepad_migration_test.db";
        getTargetContext().deleteDatabase(name);
        File file=getTargetContext().getDatabasePath(name);
        try {
            try (SQLiteDatabase db=SQLiteDatabase.openOrCreateDatabase(file,null)) {
                db.execSQL("CREATE TABLE notes (_id INTEGER PRIMARY KEY,title TEXT,note TEXT,created INTEGER,modified INTEGER)");
                db.execSQL("INSERT INTO notes VALUES(9,'旧笔记','保留正文',100,200)"); db.setVersion(2);
            }
            NotePadProvider.DatabaseHelper helper=new NotePadProvider.DatabaseHelper(getTargetContext(),name);
            try (Cursor c=helper.getReadableDatabase().rawQuery("SELECT * FROM notes WHERE _id=9",null)) {
                require(c.moveToFirst() && c.getString(c.getColumnIndexOrThrow("note")).equals("保留正文"),"legacy note preserved");
                require(c.getLong(c.getColumnIndexOrThrow("created"))==100 && c.getLong(c.getColumnIndexOrThrow("modified"))==200,"legacy timestamps preserved");
                require(c.getString(c.getColumnIndexOrThrow("category")).equals("未分类") && c.getInt(c.getColumnIndexOrThrow("is_todo"))==0,"new defaults");
            } finally {helper.close();}
        } finally {getTargetContext().deleteDatabase(name);}
    }
    private void preferences() {
        NotePreferences p=new NotePreferences(getTargetContext());
        boolean dark=p.dark(); int size=p.fontSize(), sort=p.sort();
        try {p.setDark(true);p.setFontSize(22);p.setSort(2);
            NotePreferences q=new NotePreferences(getTargetContext()); require(q.dark() && q.fontSize()==22 && q.sort()==2,"preferences reopened");
        } finally {p.setDark(dark);p.setFontSize(size);p.setSort(sort);}
    }
    private void export() throws Exception {
        long id=add("导出", "中文正文\n第二行", "学习", true,false);
        ByteArrayOutputStream out=new ByteArrayOutputStream(); NotesRepository.exportText(repo.get(id),out);
        String text=new String(out.toByteArray(),StandardCharsets.UTF_8);
        require(text.contains("中文正文\n第二行") && text.contains("分类：学习") && text.contains("创建：") && text.contains("待办：未完成"),"UTF-8 text metadata");
    }
    private void itemSelection() {
        long a=add("scopeA","", "学习",false,false), b=add("scopeB","", "学习",false,false);
        ContentValues values=new ContentValues();values.put("note","should_not_leak");
        android.net.Uri uri=android.content.ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI,a);
        int updated=getTargetContext().getContentResolver().update(uri,values,"title=? OR title=?",new String[]{"no_match",marker+"scopeB"});
        require(updated==0 && repo.get(b).body.isEmpty(),"ID scope encloses OR selection");
    }
    private Activity launch(Class<?> type) {
        return startActivitySync(new Intent(getTargetContext(),type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
    private <T> T main(Callable<T> task) throws Exception {
        FutureTask<T> future=new FutureTask<>(task); runOnMainSync(future); return future.get();
    }
    private void click(Activity activity,int id) throws Exception {main(() -> {activity.findViewById(id).performClick();return null;});waitForIdleSync();}
    private void edit(Activity activity,int id,String text) throws Exception {main(() -> {((EditText)activity.findViewById(id)).setText(text);return null;});}
    private void waitUntil(Callable<Boolean> condition,String message) throws Exception {
        long end=SystemClock.uptimeMillis()+10000;
        do {if(condition.call())return;SystemClock.sleep(100);} while(SystemClock.uptimeMillis()<end);
        throw new AssertionError("Timed out: "+message);
    }
    private void uiFlow() throws Exception {
        listActivity=(NotesList)launch(NotesList.class);waitForIdleSync();
        ActivityMonitor monitor=addMonitor(NoteEditor.class.getName(),null,false);
        click(listActivity,R.id.add_note);
        NoteEditor editor=(NoteEditor)waitForMonitorWithTimeout(monitor,5000);removeMonitor(monitor);
        require(editor!=null,"new note editor opens");
        edit(editor,R.id.note_title,marker+"UI笔记");edit(editor,R.id.note_body,"界面保存与正文检索 EvidenceBody");
        click(editor,R.id.save_note);
        waitUntil(() -> repo.list(marker+"UI笔记","",0,0).size()==1,"UI saves real DB");
        long id=repo.list(marker+"UI笔记","",0,0).get(0).id;inserted.add(id);
        waitUntil(() -> main(() -> editor.isFinishing()),"editor closes after save");
        edit(listActivity,R.id.search,"EvidenceBody");
        waitUntil(() -> main(() -> ((ListView)listActivity.findViewById(R.id.notes_list)).getCount()==1),"UI searches body");
        monitor=addMonitor(NoteEditor.class.getName(),null,false);
        main(() -> {ListView list=listActivity.findViewById(R.id.notes_list);list.performItemClick(list.getChildAt(0),0,list.getItemIdAtPosition(0));return null;});
        NoteEditor opened=(NoteEditor)waitForMonitorWithTimeout(monitor,5000);removeMonitor(monitor);
        require(opened!=null,"list opens saved note");
        waitUntil(() -> main(() -> opened.findViewById(R.id.save_note).isEnabled()),"editor loads");
        edit(opened,R.id.note_body,"界面更新后的正文");click(opened,R.id.save_note);
        waitUntil(() -> repo.get(id).body.equals("界面更新后的正文"),"UI updates body");
        edit(listActivity,R.id.search,"");
    }
    private void rotation() throws Exception {
        ActivityMonitor monitor=addMonitor(NoteEditor.class.getName(),null,false);
        click(listActivity,R.id.add_note);NoteEditor editor=(NoteEditor)waitForMonitorWithTimeout(monitor,5000);removeMonitor(monitor);
        require(editor!=null,"draft editor");edit(editor,R.id.note_title,"旋转草稿");edit(editor,R.id.note_body,"正文不应丢失");
        monitor=addMonitor(NoteEditor.class.getName(),null,false);
        main(() -> {editor.recreate();return null;});
        NoteEditor recreated=(NoteEditor)waitForMonitorWithTimeout(monitor,5000);removeMonitor(monitor);
        require(recreated!=null,"Activity recreated");waitForIdleSync();
        require(main(() -> ((EditText)recreated.findViewById(R.id.note_title)).getText().toString().equals("旋转草稿")),"title draft survives");
        require(main(() -> ((EditText)recreated.findViewById(R.id.note_body)).getText().toString().equals("正文不应丢失")),"body draft survives");
        main(() -> {recreated.finish();return null;});
    }
    private void uiExtras() throws Exception {
        long id=add("UITodo","测试待办", "工作",true,false);
        edit(listActivity,R.id.search,marker+"UITodo");
        waitUntil(() -> main(() -> ((ListView)listActivity.findViewById(R.id.notes_list)).getCount()==1),"todo card loaded");
        main(() -> {ListView list=listActivity.findViewById(R.id.notes_list);CheckBox check=findCheck(list.getChildAt(0));require(check!=null,"todo checkbox");check.performClick();return null;});
        waitUntil(() -> repo.get(id).done,"UI completion persists");
        main(() -> {((Spinner)listActivity.findViewById(R.id.category_filter)).setSelection(2);return null;});
        waitUntil(() -> main(() -> ((ListView)listActivity.findViewById(R.id.notes_list)).getCount()==0),"category excludes work");
        main(() -> {((Spinner)listActivity.findViewById(R.id.category_filter)).setSelection(0);return null;});
        edit(listActivity,R.id.search,"");
    }
    private CheckBox findCheck(View view) {
        if(view instanceof CheckBox)return (CheckBox)view;
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){CheckBox c=findCheck(((ViewGroup)view).getChildAt(i));if(c!=null)return c;}
        return null;
    }
    private void uiPreferences() throws Exception {
        SettingsActivity settings=(SettingsActivity)launch(SettingsActivity.class);waitForIdleSync();
        boolean initialDark=new NotePreferences(getTargetContext()).dark();
        boolean checkedBefore=main(() -> ((Switch)settings.findViewById(R.id.theme_choice)).isChecked());
        main(() -> {((Spinner)settings.findViewById(R.id.font_choice)).setSelection(2);return null;});
        waitUntil(() -> new NotePreferences(getTargetContext()).fontSize()==22,"font control persists");
        ActivityMonitor monitor=addMonitor(SettingsActivity.class.getName(),null,false);
        click(settings,R.id.theme_choice);
        SettingsActivity dark=(SettingsActivity)waitForMonitorWithTimeout(monitor,5000);removeMonitor(monitor);
        waitForIdleSync();
        require(dark!=null && new NotePreferences(getTargetContext()).dark(),"theme switch recreates in dark mode; initial="+initialDark+", checkedBefore="+checkedBefore+", returnedSame="+(dark==settings)+", returnedNull="+(dark==null)+", finalPref="+new NotePreferences(getTargetContext()).dark());
        waitForIdleSync();require(main(() -> ((Switch)dark.findViewById(R.id.theme_choice)).isChecked()),"theme control restored");
        monitor=addMonitor(SettingsActivity.class.getName(),null,false);click(dark,R.id.theme_choice);
        SettingsActivity light=(SettingsActivity)waitForMonitorWithTimeout(monitor,5000);removeMonitor(monitor);
        require(light!=null && !new NotePreferences(getTargetContext()).dark(),"light mode restored");
        main(() -> {((Spinner)light.findViewById(R.id.font_choice)).setSelection(1);light.finish();return null;});
        waitForIdleSync();
    }
    private void uiDelete() throws Exception {
        ActivityMonitor monitor=addMonitor(NoteEditor.class.getName(),null,false);click(listActivity,R.id.add_note);
        NoteEditor draft=(NoteEditor)waitForMonitorWithTimeout(monitor,5000);removeMonitor(monitor);
        edit(draft,R.id.note_title,"放弃测试草稿");
        sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK);waitForIdleSync();
        require(repo.list("放弃测试草稿","",0,0).isEmpty(),"unsaved draft not inserted");
        android.view.accessibility.AccessibilityNodeInfo root=getUiAutomation().getRootInActiveWindow();
        List<android.view.accessibility.AccessibilityNodeInfo> discard=root.findAccessibilityNodeInfosByText("放弃修改");
        require(!discard.isEmpty(),"discard confirmation shown");
        discard.get(0).performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK);
        waitUntil(() -> main(() -> draft.isFinishing()),"discard exits");
        long id=add("UI删除", "确认删除测试", "生活",false,false);
        NoteEditor editor=(NoteEditor)startActivitySync(new Intent(getTargetContext(),NoteEditor.class)
                .putExtra("note_id",id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        waitUntil(() -> main(() -> editor.findViewById(R.id.delete_note).isEnabled()),"delete enabled");
        click(editor,R.id.delete_note);waitForIdleSync();
        require(repo.get(id)!=null,"opening delete dialog leaves note intact");
        root=getUiAutomation().getRootInActiveWindow();
        List<android.view.accessibility.AccessibilityNodeInfo> buttons=root.findAccessibilityNodeInfosByViewId("android:id/button1");
        require(!buttons.isEmpty(),"delete confirmation button");
        buttons.get(0).performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK);
        waitUntil(() -> repo.get(id)==null,"confirmed delete persists");inserted.remove(id);
    }
    private void screenshots() throws Exception {
        // Screenshot fixtures are removed after the run; a new install starts empty.
        inserted.add(repo.save(-1,"移动开发 · 期中实验","时间戳、标题与正文查询，把每个想法认真保存。", "学习",false,false));
        inserted.add(repo.save(-1,"本周阅读清单","读完 SQLite 存储章节，整理课堂笔记。", "学习",true,false));
        inserted.add(repo.save(-1,"周末散步的灵感","把生活里微小的发现，写成一段值得回看的文字。", "灵感",false,false));
        inserted.add(repo.save(-1,"完成实验演示准备","检查功能、整理说明、保存运行截图。", "工作",true,true));
        edit(listActivity,R.id.search,"");
        main(() -> {((Spinner)listActivity.findViewById(R.id.category_filter)).setSelection(0);((Spinner)listActivity.findViewById(R.id.status_filter)).setSelection(0);return null;});
        SystemClock.sleep(4500);shot("01-notes-list.png");
        edit(listActivity,R.id.search,"SQLite");SystemClock.sleep(600);shot("02-content-search.png");
        edit(listActivity,R.id.search,"");SystemClock.sleep(400);
        ActivityMonitor monitor=addMonitor(NoteEditor.class.getName(),null,false);click(listActivity,R.id.add_note);
        NoteEditor editor=(NoteEditor)waitForMonitorWithTimeout(monitor,5000);removeMonitor(monitor);
        edit(editor,R.id.note_title,"把计划写下来");edit(editor,R.id.note_body,"今天的三个小目标：\n\n1. 整理课堂笔记\n2. 练习数据库查询\n3. 给自己留一点散步时间");
        main(() -> {((Spinner)editor.findViewById(R.id.note_category)).setSelection(1);((CheckBox)editor.findViewById(R.id.note_todo)).setChecked(true);editor.findViewById(R.id.note_title).clearFocus();return null;});
        SystemClock.sleep(300);shot("03-note-editor.png");main(() -> {editor.finish();return null;});
        SettingsActivity settings=(SettingsActivity)launch(SettingsActivity.class);waitForIdleSync();shot("04-preferences.png");
        main(() -> {settings.finish();return null;});
        NotePreferences p=new NotePreferences(getTargetContext());p.setDark(true);
        monitor=addMonitor(NotesList.class.getName(),null,false);
        main(() -> {listActivity.recreate();return null;});
        NotesList dark=(NotesList)waitForMonitorWithTimeout(monitor,5000);removeMonitor(monitor);
        require(dark!=null,"dark theme recreation");SystemClock.sleep(900);shot("05-dark-theme.png");
        p.setDark(false);main(() -> {dark.finish();return null;});
    }
    private void uiExport() throws Exception {
        long id=add("系统导出", "正文通过系统文件选择器保存，验证中文。", "灵感",true,false);
        NoteEditor editor=(NoteEditor)startActivitySync(new Intent(getTargetContext(),NoteEditor.class)
                .putExtra("note_id",id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        waitUntil(() -> main(() -> editor.findViewById(R.id.export_note).isEnabled()),"export enabled");
        click(editor,R.id.export_note);
        waitUntil(() -> {
            android.view.accessibility.AccessibilityNodeInfo root=getUiAutomation().getRootInActiveWindow();
            return root!=null && root.getPackageName().toString().contains("documentsui");
        },"system document picker");
        String fileName="notepad-export-"+System.currentTimeMillis()+".txt";
        android.view.accessibility.AccessibilityNodeInfo root=getUiAutomation().getRootInActiveWindow();
        List<android.view.accessibility.AccessibilityNodeInfo> fields=root.findAccessibilityNodeInfosByViewId("android:id/title");
        android.view.accessibility.AccessibilityNodeInfo field=null;
        for(android.view.accessibility.AccessibilityNodeInfo node:fields)
            if("android.widget.EditText".contentEquals(node.getClassName()))field=node;
        require(field!=null,"export filename field");
        Bundle args=new Bundle();args.putCharSequence(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,fileName);
        require(field.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT,args),"set export filename");
        SystemClock.sleep(300);shot("06-system-export.png");
        root=getUiAutomation().getRootInActiveWindow();
        List<android.view.accessibility.AccessibilityNodeInfo> buttons=root.findAccessibilityNodeInfosByViewId("android:id/button1");
        require(!buttons.isEmpty(),"system Save button");
        buttons.get(0).performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK);
        waitUntil(() -> {
            String text=shellText("cat /sdcard/Download/"+fileName);
            return text.contains("验证中文") && text.contains("分类：灵感") && text.contains("创建：");
        },"actual exported UTF-8 file contents");
        shellText("rm /sdcard/Download/"+fileName);
        main(() -> {editor.finish();return null;});
    }
    private String shellText(String command) throws Exception {
        try(android.os.ParcelFileDescriptor descriptor=getUiAutomation().executeShellCommand(command);
                java.io.InputStream input=new android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor);
                ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            byte[] buffer=new byte[4096];int count;
            while((count=input.read(buffer))!=-1)out.write(buffer,0,count);
            return new String(out.toByteArray(),StandardCharsets.UTF_8);
        }
    }
    private void shot(String name) throws Exception {
        Bitmap bitmap=getUiAutomation().takeScreenshot();require(bitmap!=null,"screenshot available");
        File dir=getTargetContext().getExternalFilesDir("verification");
        try(FileOutputStream out=new FileOutputStream(new File(dir,name))) {bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}finally{bitmap.recycle();}
    }
    private void cleanup() {
        for(long id:inserted)try {if(repo.get(id)!=null)repo.delete(id);}catch(Exception ignored) { }
        inserted.clear();
    }
    private static boolean contains(List<Note> notes,long id) {for(Note n:notes)if(n.id==id)return true;return false;}
    private static void require(boolean condition,String message) {if(!condition)throw new AssertionError(message);}
}
