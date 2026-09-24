# cs2-like-console

[English](README_EN.md) | 中文

一个给 Minecraft 客户端用的 CS2 风格控制台：按 `` ` `` 呼出全屏面板，输入并执行原版或服务器指令。

## 特性

- 全屏深色控制台面板，替代原版聊天栏输入
- 原版补全提示：输入 `give ` 时按 `Tab` 可补全物品 ID 等
- `↑` / `↓` 翻阅历史，鼠标滚轮、`PageUp` / `PageDown` 翻看日志
- 日志环形缓冲，最多保留最近 300 行
- 内置客户端指令：`disconnect`、`exit`、`quit`

## 环境要求

| 项目 | 版本 |
| --- | --- |
| Minecraft | 26.2 |
| Fabric Loader | 0.19.5+ |
| Fabric API | 0.161.0+26.2 |
| Java | 25 |

仅客户端模组，服务器无需安装。

## 安装

1. 安装 Fabric Loader 与 Fabric API。
2. 把 `build/libs/cs2-like-console-<version>.jar` 放入 `.minecraft/mods/`。

## 使用

进入世界后按 `` ` `` 打开控制台（也可按 `Esc` 关闭），输入指令回车执行，前导 `/` 可省略。

| 指令 | 作用 |
| --- | --- |
| `disconnect` | 断开当前世界，回到主菜单 |
| `exit` / `quit` | 退出整个游戏 |

其余输入按原版指令发送，例如 `give @s stone`、`time set day`。

按键可在"选项 → 控制 → 按键绑定"的 `CS2 风格控制台` 分类中修改。

## 从源码构建

需要 JDK 25。版本号集中在 `gradle.properties`。

```bash
./gradlew build        # 构建 jar，输出到 build/libs/
./gradlew runClient    # 启动带模组的开发环境客户端
./gradlew genSources   # 反编译 Minecraft 源码
```

## 目录结构

- `src/main/java/com/lunarlake/cs2LikeConsole/` — 通用代码与模组入口
- `src/client/java/com/lunarlake/cs2LikeConsole/client/` — 控制台界面、指令与按键注册
- `src/main/resources/` — `fabric.mod.json` 与 mixin 配置
- `src/client/resources/` — 语言文件、图标与客户端 mixin 配置

## 许可证

All Rights Reserved，详见 `LICENSE.txt`。
