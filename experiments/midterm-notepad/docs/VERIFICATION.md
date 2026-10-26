# 期中 NotePad 验证与验收记录

验证日期：2026-10-26。范围：本仓库 `midterm-notepad` 模块及实验二、三构建回归。

## 结论

PDF 的两项基本要求和本次选定的五项扩展均已实现。应用在 Android 14 / API 34 模拟器上完成真实安装和运行，**20 项设备集成检查通过，0 项失败**；Android Lint 报告 **No issues found**。三个应用模块均能构建。本记录只将实际执行到的范围列为验证通过。

相关资料：[运行截图](../screenshots) · [APK 及 SHA-256](../apk/SHA256SUMS.txt) · [测试源码](../src/androidTest/java/com/example/android/notepad/NotePadTestRunner.java)。构建、Lint 和设备测试日志由脚本生成到仓库根目录 `build/notepad-verification/`，不纳入版本管理。

## 环境与构建

| 项目 | 配置 / 实测值 |
| --- | --- |
| Gradle / AGP | 9.5.0 / 9.3.0 |
| Gradle Daemon JDK / Java 源码级别 | 25 / 11 |
| compileSdk / targetSdk / minSdk | 37 / 37 / 24 |
| 设备 | Pixel_7 AVD，Android 14，API 34，1080 × 2400 |
| 设备时区 | GMT；笔记显示设备本地时间 |
| applicationId | `com.example.test.notepad` |
| 启动 Activity | `com.example.android.notepad.NotesList` |
| 数据库 | 应用私有 `note_pad.db`，版本 3 |
| 设备测试运行器 | 自带 Android Instrumentation，自定义 NotePadTestRunner |

执行并通过：

```powershell
.\gradlew.bat :midterm-notepad:assembleDebug :midterm-notepad:assembleDebugAndroidTest :midterm-notepad:lintDebug :experiment02:assembleDebug :experiment03:assembleDebug --offline --no-configuration-cache --console=plain
```

API 24–32 使用旧返回键兼容方法，API 33+ 注册 OnBackInvokedDispatcher。

安装应用与测试 APK 后执行：

```powershell
adb shell am instrument -w com.example.test.notepad.test/com.example.android.notepad.NotePadTestRunner
```

最终结果：`RESULT: 20 passed, 0 failed`。另通过 [verify.ps1](../scripts/verify.ps1) 复现完整构建、Lint、安装和设备测试流程。

## 设备检查明细

| 编号 | 检查 | 实际验证内容 | 结果 |
| --- | --- | --- | --- |
| 01 | 新建、读取、时间戳 | 写入后读回相同标题正文，日期合理，普通笔记不能误成为已完成待办 | PASS |
| 02 | 编辑时间语义 | 创建时间不变，修改时间前进，正文分类状态更新，完成状态可取消 | PASS |
| 03 | 标题和正文查询 | 英文不区分大小写，中文正文可检索，无结果返回空集 | PASS |
| 04 | 特殊字符搜索 | `%`、`_`、单引号作为原文处理，SQL 注入式输入不扩大结果 | PASS |
| 05 | 组合筛选 | 查询、分类、未完成及已完成待办条件组合正确 | PASS |
| 06 | 排序 | 最近修改、最早创建、标题三种顺序正确 | PASS |
| 07 | 输入校验 | 空内容、过长标题、无效分类拒绝，正文可自动生成标题 | PASS |
| 08 | 删除与缺失记录 | 删除后不可读，更新不存在 ID 返回明确失败 | PASS |
| 09 | 磁盘持久化 | 独立 SQLite 连接与新 Repository 读取到已保存内容 | PASS |
| 10 | 数据库版本迁移 | 构造版本 2 数据库，升级后原 ID、正文、创建/修改日期完整保留，新列默认值正确 | PASS |
| 11 | 偏好持久化 | 新偏好对象读到主题、字号、排序设置 | PASS |
| 12 | UTF-8 导出内容 | 输出包含多行中文、分类、时间及待办状态 | PASS |
| 13 | Provider 单条范围 | 额外 OR 条件不会绕过指定 ID 修改另一条笔记 | PASS |
| 14 | 真实 Activity 编辑流程 | 点击新建、输入、保存入库，搜索正文，列表打开后再次修改保存 | PASS |
| 15 | 草稿重建 | 调用 Activity.recreate 触发生命周期重建，标题和正文草稿保留 | PASS |
| 16 | 分类和待办 UI | 卡片勾选实际写库，切换分类实际改变列表结果 | PASS |
| 17 | 设置控件 | 实际选择字号、点击主题开关，重建后主题和控件值正确，能切回浅色 | PASS |
| 18 | 离开与删除确认 | 系统返回键显示放弃提示，未保存草稿不入库；删除对话框出现前数据仍在，确认后才删除 | PASS |
| 19 | 系统文档导出 | 打开 DocumentsUI、设置文件名并点击 SAVE，实际读回 Downloads 中的 UTF-8 文件并核对中文与元信息 | PASS |
| 20 | 运行截图 | 创建演示数据、搜索、编辑、设置、切换深色并截图；结束清理演示笔记 | PASS |

测试源代码：[NotePadTestRunner.java](../src/androidTest/java/com/example/android/notepad/NotePadTestRunner.java)。测试使用真实 SQLite、Activity 和系统文档界面。

## 人工视觉检查

对列表、正文查询、编辑器、设置、深色主题和系统导出共六张截图进行了查看。窄屏筛选栏原先的文字截断已修正为两行，卡片显示双日期；输入、正文区和底部按钮可见，列表可滚动。截图为实际界面记录，测试演示数据不打包进安装包。

## 提交物与范围

- [模块 README](../README.md)：功能对照、运行方法和界面截图。
- [源码分析](SOURCE_ANALYSIS.md)：明确原项目版本、架构、复用位置及修改原因。
- [安装包](../apk/NotePad-debug.apk)：校验文件随附，使用课堂调试签名。
- 原始日志在本机保留；编译缓存、SDK 本机路径和模拟器磁盘不纳入版本管理。

设备实测限 API 34，其他 Android 版本有最低 API 静态检查，未列为实机全覆盖。数据库迁移测试覆盖原项目的版本 2 表结构；不意味着能跨应用 ID 自动读取原应用私有库。批量恢复、云备份、媒体笔记等未选可选项不在完成声明中。
