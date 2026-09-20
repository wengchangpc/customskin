# Custom Skin (Self) — 自定义皮肤（仅自己可见）

适用于 **Minecraft Java 1.20.1 Forge** 的纯客户端模组：
在**任何世界/服务器**中，把**你自己**的皮肤显示为你导入的贴图。其他玩家看不到，服务器也感知不到。

## 使用方法

1. 从 [GitHub Actions](../../actions) 下载构建好的 jar（Artifacts 区），放进 `.minecraft/mods/`。
2. 把你的皮肤 PNG（64x64 或 64x32）放到：
   ```
   .minecraft/config/CustomSkin/skin.png
   ```
3. （可选）如果皮肤是 **Alex 细手臂模型**，编辑 `config/CustomSkin/model.txt`，把内容改为：
   ```
   slim
   ```
4. 首次启动会自动释放一张默认皮肤。换好贴图后，游戏内输入：
   ```
   /customskin reload
   ```
   即可热更新，无需重启。

## 原理

Mixin 注入 `AbstractClientPlayer#getSkinTextureLocation`（SRG: `m_108560_`）与
`#getModelName`（SRG: `m_108564_`），仅在渲染**本地玩家**时替换贴图与模型，客户端本地生效。

## GitHub 自动构建

推送代码后 Actions 会自动构建；打 `v*` 标签（如 `v1.0.0`）会自动创建 Release 并附上 jar。

本地构建：需要 JDK 17 与 Gradle 8.1.1，运行 `gradle build`。

## 常见问题

- **皮肤没生效？** 确认贴图路径与文件名（`config/CustomSkin/skin.png`），并执行 `/customskin reload`。
- **手臂粗细不对？** 修改 `model.txt` 为 `slim` 或 `classic` 后 reload。
- **披风也想自定义？** 配套模组 [customcape](https://github.com/wengchangpc/customcape)。

## License

MIT

## 更新日志
- 1.1.0：新增琉璃镀层——gloss 流光(全亮度脉动光泽层) + vivid 增艳(贴图加载时提饱和+35%/亮度+10%)；/customskin gloss|vivid 开关，settings.txt 可调 glossAlpha。
