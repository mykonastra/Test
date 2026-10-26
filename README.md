# Android 课程实验仓库

实验二和实验三分别对应一个独立 Android 应用模块，代码、资源、截图和说明分别存放，便于老师逐项查看。

## 实验导航

| 实验 | 内容 | 说明与验收清单 | 源代码 | 运行模块 |
| --- | --- | --- | --- | --- |
| 实验二 | LinearLayout、TableLayout、ConstraintLayout、Compose | [查看实验二](experiment02/README.md) | [Kotlin](experiment02/src/main/java/com/example/test) · [布局](experiment02/src/main/res/layout) | `experiment02` |
| 实验三 | ListView、AlertDialog、XML 菜单、ActionMode、RecyclerView | [查看实验三](experiment03/README.md) | [Kotlin](experiment03/src/main/java/com/example/test) · [资源](experiment03/src/main/res) | `experiment03` |

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
├── docs/reference-images/ # 原 pdf_images 的参考图及原始数据
├── docs/REORGANIZATION.md # 整理前后对照及验证记录
├── gradle/               # 共用 Wrapper 和版本目录
├── settings.gradle.kts
└── gradlew / gradlew.bat
```

## 打开与运行

1. Android Studio 打开**仓库根目录**并完成 Gradle Sync。两个实验是同一工程的模块，无需分别导入。
2. 选择运行模块 `experiment02` 或 `experiment03`，再选择 API 24 及以上模拟器或真机。
3. 桌面名称分别为“实验2 界面布局”和“实验3 界面组件”，各首页仅包含本实验的五项任务，支持滚动。

PowerShell 命令均在仓库根目录执行：

```powershell
# 分别构建
.\gradlew.bat :experiment02:assembleDebug
.\gradlew.bat :experiment03:assembleDebug

# 分别安装到已连接设备
.\gradlew.bat :experiment02:installDebug
.\gradlew.bat :experiment03:installDebug

# 模板单元测试，不代表界面功能验收
.\gradlew.bat :experiment02:testDebugUnitTest :experiment03:testDebugUnitTest
```

macOS/Linux 使用 `./gradlew`。APK 路径：

- `experiment02/build/outputs/apk/debug/experiment02-debug.apk`
- `experiment03/build/outputs/apk/debug/experiment03-debug.apk`

应用 ID 分别为 `com.example.test.experiment02`、`com.example.test.experiment03`，可同时安装。源码 namespace 均保留 `com.example.test`，方便对照原代码；两个模块不互相依赖。

## 构建环境

仓库配置：Gradle 9.5.0、AGP 9.3.0、Kotlin Compose 插件 2.2.10、Compose BOM 2026.02.01、compileSdk/targetSdk 37、minSdk 24。Gradle Daemon 配置使用 JDK 25，Java 源码兼容级别为 11。

安装 Android SDK 37，通过 Android Studio 或本机 `local.properties` 配置 SDK 路径。首次构建需要联网；已有完整依赖缓存时，可添加 `--offline --no-configuration-cache` 离线构建。

## 证据与范围

截图沿用仓库原有文件，**不是本次拆分后重新截取的运行证据**。本次验证结果见[整理记录](docs/REORGANIZATION.md)。任务归类依据现有代码和说明；仓库没有课程任务书，不能据此认定满足所有评分要求。
