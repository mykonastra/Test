# Android 课程实验仓库

实验二、实验三和期中 NotePad 分别对应独立 Android 应用模块，代码、截图、说明和验证材料分别存放，便于老师逐项查看。

## 实验导航

| 实验 | 内容 | 说明与验收清单 | 源代码 | 运行模块 |
| --- | --- | --- | --- | --- |
| 实验二 | LinearLayout、TableLayout、ConstraintLayout、Compose | [查看实验二](experiment02/README.md) | [Kotlin](experiment02/src/main/java/com/example/test) · [布局](experiment02/src/main/res/layout) | `experiment02` |
| 实验三 | ListView、AlertDialog、XML 菜单、ActionMode、RecyclerView | [查看实验三](experiment03/README.md) | [Kotlin](experiment03/src/main/java/com/example/test) · [资源](experiment03/src/main/res) | `experiment03` |
| **期中实验** | **NotePad：时间戳、标题/正文查询、分类、待办、主题/字号、排序、文本导出** | **[查看期中实验](midterm-notepad/README.md)** · [测试记录](midterm-notepad/docs/VERIFICATION.md) · [下载 APK](midterm-notepad/download/NotePad-debug.apk) | [Java](midterm-notepad/src/main/java/com/example/android/notepad) | `midterm-notepad` |

建议查看顺序：进入相应实验说明 → 对照任务表阅读源码和截图 → 运行对应模块 → 按检查步骤逐项操作。

## 目录结构

```text
.
├── experiment02/          # 实验二独立应用
│   ├── README.md          # 任务、源码、截图、检查步骤
│   ├── screenshots/       # 本实验已有截图
│   ├── build.gradle.kts
│   └── src/              # 本实验代码、资源、测试
├── experiment03/          # 实验三独立应用，结构同上
├── midterm-notepad/       # 期中记事本应用、源码分析、20项设备测试、截图和APK
├── docs/reference-images/ # 原 pdf_images 的参考图及原始数据
├── docs/REORGANIZATION.md # 整理前后对照及验证记录
├── gradle/               # 共用 Wrapper 和版本目录
├── settings.gradle.kts
└── gradlew / gradlew.bat
```

## 打开与运行

1. Android Studio 打开**仓库根目录**并完成 Gradle Sync，所有实验均是同一工程的模块，无需分别导入。
2. 选择 `experiment02`、`experiment03` 或 `midterm-notepad`，再选择 API 24 及以上模拟器或真机。
3. 桌面名称分别为“实验2 界面布局”“实验3 界面组件”“拾记 NotePad”，三个应用可同时安装。

PowerShell 命令均在仓库根目录执行：

```powershell
# 分别构建
.\gradlew.bat :experiment02:assembleDebug
.\gradlew.bat :experiment03:assembleDebug
.\gradlew.bat :midterm-notepad:assembleDebug

# 分别安装到已连接设备
.\gradlew.bat :experiment02:installDebug
.\gradlew.bat :experiment03:installDebug
.\gradlew.bat :midterm-notepad:installDebug

# 模板单元测试，不代表界面功能验收
.\gradlew.bat :experiment02:testDebugUnitTest :experiment03:testDebugUnitTest
```

macOS/Linux 使用 `./gradlew`。APK 路径：

- `experiment02/build/outputs/apk/debug/experiment02-debug.apk`
- `experiment03/build/outputs/apk/debug/experiment03-debug.apk`
- `midterm-notepad/build/outputs/apk/debug/midterm-notepad-debug.apk`

实验二、三的应用 ID 分别为 `com.example.test.experiment02`、`com.example.test.experiment03`，源码 namespace 均保留 `com.example.test`。期中应用 ID 为 `com.example.test.notepad`，源码 namespace 为 `com.example.android.notepad`；三个模块不互相依赖。

## 构建环境

仓库配置：Gradle 9.5.0、AGP 9.3.0、Kotlin Compose 插件 2.2.10、Compose BOM 2026.02.01、compileSdk/targetSdk 37、minSdk 24。Gradle Daemon 配置使用 JDK 25，Java 源码兼容级别为 11。

安装 Android SDK 37，通过 Android Studio 或本机 `local.properties` 配置 SDK 路径。首次构建需要联网；已有完整依赖缓存时，可添加 `--offline --no-configuration-cache` 离线构建。

## 证据与范围

实验二、三的截图沿用原有文件，其整理范围见[整理记录](docs/REORGANIZATION.md)。期中实验依据所提供的 PDF 实现，其[截图](midterm-notepad/screenshots)、[设备测试日志](midterm-notepad/docs/device-tests.txt)及[功能对照和验收说明](midterm-notepad/README.md)为本次实际执行结果。
