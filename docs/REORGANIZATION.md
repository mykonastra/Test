# 仓库整理与验证记录

整理日期：2026-10-26。

## 整理前后对照

| 项目 | 整理前 | 整理后 |
| --- | --- | --- |
| 工程 | 单个 `app` 混合两个实验 | `experiment02`、`experiment03` 两个独立应用模块 |
| 首页 | 实验二标题，混合十个任务 | 各自标题、五个入口、可滚动菜单 |
| 说明 | 根 README 主要介绍实验二，误把子任务标成多个实验 | 根导航 + 两份实验说明，十项任务均有源码链接、检查步骤 |
| 截图 | 所有截图混放 | 按实验归档到各自 `screenshots/` |
| 参考素材 | 根目录 `pdf_images/` | `docs/reference-images/`，保留图片和原始数据 |
| 安装 | 单个应用 | 两个独立 applicationId，可同时安装 |
| 构建 | `:app:assembleDebug` | `:experiment02:assembleDebug` / `:experiment03:assembleDebug` |

两个实验共享根目录 Wrapper 和版本目录，不互相依赖。少量主题、图标、备份规则及模板测试分别保留在两个模块，避免为阅读一个实验跳转到另一个实验。源码 namespace 保留 `com.example.test`。

原业务代码及资源按归属迁移；调整内容集中在 Gradle 模块配置、Manifest、入口菜单、应用名称、设备测试包名断言和文档。移除了各模块不需要的 RecyclerView/CardView 或 ConstraintLayout 直接依赖。ListView 入口标题按实际代码修正为自定义 BaseAdapter；XML 菜单入口不再声称实现 ContextMenu。

原本机 `app` 的残留构建输出保留在被忽略的 `build/legacy-app-output/`，不参与新模块构建。忽略规则补充所有模块的构建目录、Kotlin 缓存和 IDE 本机状态；仓库原来已跟踪的 `.idea` 文件继续保留，`gradle.xml` 的模块路径已同步更新。

## 本次验证

1. 两个模块离线编译与 APK 打包成功：

   ```powershell
   .\gradlew.bat :experiment02:assembleDebug :experiment03:assembleDebug --offline --no-configuration-cache --console=plain
   ```

   结果：`BUILD SUCCESSFUL`。最后一次构建输出保存在本机 `build/reorganization-build.log`，该目录被 Git 忽略。

2. 使用 Android SDK `aapt dump badging` 核对两个 APK：

   | APK | applicationId | 桌面名称 | 启动 Activity |
   | --- | --- | --- | --- |
   | experiment02-debug.apk | `com.example.test.experiment02` | 实验2 界面布局 | `com.example.test.MainActivity` |
   | experiment03-debug.apk | `com.example.test.experiment03` | 实验3 界面组件 | `com.example.test.MainActivity` |

3. 对迁移文件执行 Git 内容哈希比对，排除上文明确修改的配置、入口、标题与包名断言；核对布局、动物/太空素材、截图、参考图和其余业务代码没有因迁移改变或遗失。核对两份 Manifest 均包含启动页和五个本实验 Activity，检查本地 Markdown 链接、图片路径及 `git diff --check`。

## 未完成的验证与原有实现边界

- 联网构建遇到 `maven.aliyun.com` DNS 解析失败。改用本机已有缓存后，两个 APK 均成功构建；没有因此修改依赖版本或仓库来源。
- 单元测试已尝试，离线模式缺少 `junit:junit:4.13.2` 缓存，未能执行。已有测试是模板算术测试，不能作为十项实验功能的验收证据。
- `adb devices` 未发现连接设备，因此没有进行拆分后安装、启动和交互验证，也没有执行设备测试。截图全部为原有素材。
- RecyclerView 原有 `adapterPosition` 弃用警告仍存在，不阻断本次编译。
- 实验三现有代码采用 BaseAdapter；ContextMenu 未接入；分享仅为 Toast；登录对话框不含认证服务。实验说明已据实披露。本次没有课程任务书，未判定完整评分符合性。
- 本次只整理本地工作区，未创建 Git 提交或推送 GitHub；远程页面需在这些变更提交并推送后才会更新。
