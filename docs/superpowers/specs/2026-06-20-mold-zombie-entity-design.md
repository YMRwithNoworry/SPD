# 霉染僵尸实体设计

日期：2026-06-20
项目：SPD Minecraft 1.20.1 Architectury 模组

## 目标

将 `霉染僵尸1 (1) (1).bbmodel` 对应的生物加入游戏，支持 Fabric 与 Forge 两个加载器，并使用 GeckoLib 播放静置、奔跑、攻击动画。实体行为整体与原版僵尸一致，但最大生命值为 15 点，移动速度比僵尸快 20%。

## 已确认约束

- 项目版本为 Minecraft `1.20.1`。
- 项目使用 Architectury API，模块为 `common`、`fabric`、`forge`。
- GeckoLib 作为外部依赖，由玩家/运行环境安装，不打包进本模组。
- 实体逻辑尽量放在 `common`，平台代码仅处理加载器差异。
- 霉染僵尸需要自然生成，生成逻辑参考原版僵尸。

## 推荐架构

### Common 模块

`common` 承担核心功能：

- 注册 `EntityType<MoldZombieEntity>`。
- 注册实体属性。
- 注册刷怪蛋物品。
- 定义 `MoldZombieEntity` 行为与 GeckoLib 动画控制器。
- 定义客户端通用 renderer/model 类。
- 放置通用资源文件：模型、动画、贴图、语言文件。

实体类继承原版 `Zombie`，从而复用僵尸 AI、攻击行为、燃烧、亡灵属性、声音、掉落等默认行为。属性通过自定义属性构造覆盖关键数值：

- `MAX_HEALTH = 15.0`
- `MOVEMENT_SPEED = 0.276`，即原版僵尸 `0.23` 的 120%。
- 其他攻击、防御、跟随距离等属性沿用僵尸默认值。

### Fabric 模块

`fabric` 只负责 Fabric 专属接入：

- 在客户端入口注册 GeckoLib renderer。
- 注册实体属性工厂。
- 通过 Fabric Biome Modifications 将霉染僵尸加入怪物自然生成池。

### Forge 模块

`forge` 只负责 Forge 专属接入：

- 在 mod event bus 注册实体属性事件。
- 在客户端事件中注册 GeckoLib renderer。
- 通过 Forge biome loading / spawn placement 相关事件加入自然生成。

## 资源布局

资源文件统一放在 `common/src/main/resources/assets/spd/` 下：

- `geo/mold_zombie.geo.json`
- `animations/mold_zombie.animation.json`
- `textures/entity/mold_zombie.png`
- `lang/en_us.json`
- `lang/zh_cn.json`

`bbmodel` 本身继续保留在 `杂物/` 作为源文件。实现时需要从该文件导出或提取 GeckoLib 运行期资源：

- 几何模型导出为 `geo`。
- 三个动画导出为 `animation`。
- 内嵌贴图导出为 png。

动画名称保持：

- `animation.mold_zombie.idle`
- `animation.mold_zombie.run`
- `animation.mold_zombie.attack`

## 动画控制

`MoldZombieEntity` 实现 GeckoLib `GeoEntity`：

- `idle`：实体未移动且未攻击时循环播放。
- `run`：实体移动时循环播放。
- `attack`：实体攻击时播放一次，并在攻击结束后回到移动或静置动画。

控制器优先级为攻击 > 移动 > 静置，避免攻击动作被移动动画覆盖。

## 自然生成

霉染僵尸作为 `MobCategory.MONSTER` 注册，生成条件参考僵尸：

- 使用怪物通用生成限制。
- 使用与僵尸类似的高度图和亮度限制。
- 加入常规陆地生物群系的怪物生成池。
- 初始生成权重可低于或接近僵尸，建议权重 `80`、组大小 `1-4`，避免过度挤占原版僵尸。

## 数据流

1. 平台入口调用 `Spd.init()`。
2. `Spd.init()` 初始化 common 注册表。
3. Fabric/Forge 平台入口注册属性、渲染器和自然生成接入。
4. 游戏加载实体类型、刷怪蛋、资源文件。
5. 实体生成后由 GeckoLib model/renderer 读取资源并根据状态播放动画。

## 错误处理与兼容性

- GeckoLib 未安装时，由加载器依赖声明阻止进入游戏，并给出缺失依赖提示。
- 资源路径固定使用 `spd` 命名空间，避免跨平台路径差异。
- 平台特有代码不引用对方加载器 API。
- common 中只使用 Architectury、Minecraft 与 GeckoLib 的跨平台可用 API。

## 验证方式

- 运行 `./gradlew build` 验证 common/fabric/forge 均可编译。
- 在 Fabric 开发环境启动客户端，确认实体、刷怪蛋、动画和自然生成可用。
- 在 Forge 开发环境启动客户端，确认实体、刷怪蛋、动画和自然生成可用。
- 使用 `/summon spd:mold_zombie` 验证实体生成。
- 使用刷怪蛋验证物品注册与 renderer。
- 观察静置、移动、攻击三种状态，确认动画切换正确。

## 非目标

- 不改变原版僵尸行为细节，如破门、转化村民、燃烧等。
- 不添加自定义掉落、音效、AI 或特殊攻击。
- 不把 GeckoLib 打包进本模组。
- 不实现额外模型变体或生态群系专属生成规则。
