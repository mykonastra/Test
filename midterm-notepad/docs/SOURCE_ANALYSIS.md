# NotePad 原项目分析与扩展设计

## 来源与版本

课程指定源码：[fjnu-cse/NotePad](https://github.com/fjnu-cse/NotePad)。本次通过 Git 实际获取并阅读的提交为 `9279eab943f50012dd299df3417259e3478c8364`，不是仅根据示意图重做一个无关应用。原始代码可通过[固定提交](https://github.com/fjnu-cse/NotePad/tree/9279eab943f50012dd299df3417259e3478c8364/app/src/main)查看。

参考文档中的解析博客未能通过网页工具读取；以下分析直接以该提交的源码为依据。SQLite 线程与参数化查询做法同时对照了 [Android 官方 SQLite 文档](https://developer.android.com/training/data-storage/sqlite)。

## 原程序的数据流

```text
NotesList / NoteEditor / TitleEditor
                 │ ContentResolver + content URI
                 ▼
          NotePadProvider
                 │ SQLiteOpenHelper / SQLiteQueryBuilder
                 ▼
      note_pad.db → notes 表
```

1. `NotePad.java` 是契约类，定义 `notes` 表、列名、MIME 类型及 URI。原表已有 `_id`、`title`、`note`、`created`、`modified`；两种日期均为毫秒整数。因此时间戳需求的关键是查询投影、格式化展示和更新语义，不是重新发明日期字段。
2. `NotePadProvider` 使用 UriMatcher 区分集合与单条笔记，SQLiteOpenHelper 管理数据库，SQLiteQueryBuilder 组装查询，ContentResolver 负责增删改查并发送变更通知。原插入逻辑会填充默认日期；原升级示例通过删表重建，会丢数据。
3. `NotesList` 使用 `managedQuery` 和 SimpleCursorAdapter，仅投影 ID 与标题，因此看不到现有时间列；查询没有关键词条件。其注释明确说明 UI 线程进行数据库操作仅适用于示例。
4. `NoteEditor` 区分插入与编辑，以 URI 定位记录，原界面使用带行线的 EditText，TitleEditor 另行编辑标题；原程序另含 NotesLiveFolder 等旧系统集成。
5. 原 Manifest 使用公开的 `com.google.provider.NotePad` authority、旧主题和 SDK 配置，直接与本仓库共用会造成配置与权限边界问题。

## 复用与改动对照

| 原文件 / 设计 | 本次处理 | 理由 |
| --- | --- | --- |
| NotePad.java | 保留原契约结构和版权头，新增 `category/is_todo/is_done`，修改 authority | 基于原架构扩展，避免安装冲突 |
| NotePadProvider.java | 保留 URI 路由、查询构建、CRUD、通知与数据库辅助类；版本 3 增量迁移 | 满足基于原源码扩展，并保留数据 |
| Provider.update | 创建时间不可改，修改时间由存储层刷新；单条操作附加条件加括号 | 避免忘记更新时间，防止 OR 条件绕开 ID 限定 |
| 原 NotesList | 重写为卡片列表、双时间、摘要、搜索、筛选、排序 | 两项基本要求及附加交互 |
| 原 NoteEditor / TitleEditor | 统一为标题、正文、分类、待办编辑页 | 减少往返，同时提供保存、放弃、删除确认和导出 |
| NotesLiveFolder 等 | 不迁移、不注册 | 不属于本次扩展目标；不声称支持已废弃集成 |
| 新 NotesRepository | 封装参数化查询、校验和模型映射，关闭 Cursor | 将 SQL 与界面逻辑分开，便于测试 |
| 新 NoteActivity / Ui | 单线程 IO 队列、生命周期检查、系统栏/键盘 Insets、复用样式 | 避免主线程数据库与文件操作、统一界面 |
| 新 SettingsActivity / NotePreferences | 深浅主题、编辑字号与排序持久化 | 完成偏好设置扩展 |
| 原 Provider 公开导出 | 本模块 Provider 与编辑/设置页均不导出 | 本实验不需要对第三方应用公开笔记库 |

原始数据契约和 Provider 的 AOSP 版权头均保留，模块附带 Apache 2.0 许可及 NOTICE。本模块没有复制上游完整 Git 元数据或旧 Gradle Wrapper，以仓库统一构建环境集成。

## 查询与状态设计

标题/正文采用 `instr(lower(column), lower(?)) > 0`，所有关键词通过参数绑定。它是字面子串查询，`%`、`_`、引号和反斜杠不会被当成通配符或 SQL。SQLite `lower` 提供 ASCII 大小写转换；中文原文可直接匹配，不承诺所有 Unicode 语言的大小写折叠。

筛选条件以 AND 组合，标题与正文查询用括号内的 OR：

```sql
(instr(lower(title), lower(?)) > 0 OR instr(lower(note), lower(?)) > 0)
AND category = ?
AND is_todo = 1 AND is_done = 0
```

排序 SQL 由三个固定选项选择，不接受用户拼接。相同日期再按 `_id` 排序，避免顺序抖动。搜索约 180ms 防抖，返回结果附请求序号，旧结果不会覆盖更新的查询。数据变化通过 ContentObserver 触发列表刷新。

保存校验：标题最多 200 字、正文最多 100000 字；两者都为空时拒绝；只有正文时取首行生成标题。只有待办可以有完成状态。编辑器显式保存，打开新页面不插入空行；视图状态保存草稿，保留保存中的异步操作以处理 Activity 重建。偏好控件由 SharedPreferences 恢复，不让旧视图状态反向覆盖刚更改的主题。

导出使用 `ACTION_CREATE_DOCUMENT` 和 UTF-8 输出流，成功关闭文件后提示完成。没有申请广泛存储权限，也没有将本地导出描述为云备份。
