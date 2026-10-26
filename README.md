# Android 课程实验仓库

实验二、实验三和期中 NotePad 分别对应独立 Android 应用模块，代码、截图、说明和验证材料分别存放，便于老师逐项查看。

## 实验导航

| 实验 | 内容 | 入口 |
| --- | --- | --- |
| 实验二 | LinearLayout、TableLayout、ConstraintLayout、Compose | [界面布局](experiments/experiment02/) |
| 实验三 | 列表、对话框、菜单、ActionMode、RecyclerView | [界面组件](experiments/experiment03/) |
| 期中实验 | NotePad 记事本 | [实现与运行](experiments/midterm-notepad/) · [APK](experiments/midterm-notepad/apk/NotePad-debug.apk) |

各实验的功能介绍、源码入口和截图见对应目录的 README。NotePad 基于课程指定示例扩展，来源与许可说明保留在模块内。

## 目录结构

```text
.
├── experiments/          # 按实验存放源码、截图和说明
│   ├── experiment02/     # Android 界面布局
│   ├── experiment03/     # Android 界面组件
│   └── midterm-notepad/  # NotePad 记事本
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

- `experiments/experiment02/build/outputs/apk/debug/experiment02-debug.apk`
- `experiments/experiment03/build/outputs/apk/debug/experiment03-debug.apk`
- `experiments/midterm-notepad/build/outputs/apk/debug/midterm-notepad-debug.apk`

实验二、三的应用 ID 分别为 `com.example.test.experiment02`、`com.example.test.experiment03`，源码 namespace 均保留 `com.example.test`。期中应用 ID 为 `com.example.test.notepad`，源码 namespace 为 `com.example.android.notepad`；三个模块不互相依赖。

## 构建环境

仓库配置：Gradle 9.5.0、AGP 9.3.0、Kotlin Compose 插件 2.2.10、Compose BOM 2026.02.01、compileSdk/targetSdk 37、minSdk 24。Gradle Daemon 配置使用 JDK 25，Java 源码兼容级别为 11。

安装 Android SDK 37，通过 Android Studio 或本机 `local.properties` 配置 SDK 路径。首次构建需要联网；已有完整依赖缓存时，可添加 `--offline --no-configuration-cache` 离线构建。

## 实验资料

运行截图随各实验保存。NotePad 的[实现说明](experiments/midterm-notepad/docs/SOURCE_ANALYSIS.md)和[测试说明](experiments/midterm-notepad/docs/VERIFICATION.md)可供参考，测试日志由脚本生成到本地 `build/` 目录。
