# 怨甲海龟设计

## 目标

新增一个名为“怨甲海龟”的 SPD 生物，继承原版海龟的水陆移动、上岸、回游和路径行为，并加入液态金属感染后的水中冲撞、陆地缩壳反击、污染轨迹、伪装产卵和专属掉落。实体使用用户提供的 GeckoLib 模型、纹理和动画资源。

正式实体 ID 为 `spd:spite_armored_turtle`。中文正式名称固定为“怨甲海龟”，英文名称为 “Spite-Armored Turtle”。

## 已确认决策

- 行为基类：继承原版 `Turtle`，在其目标和导航之上增加 SPD 行为。
- 资源格式：GeckoLib 4，使用提供的 `idle`、`walk`、`attack` 动画。
- 掉落物：新增 `heavy_spite_scute`（沉重怨鳞）和 `residual_malice`（液态怨金残留）两个独立物品。
- 液态怨金残留首版作为物品实现，不注册新流体或桶交互。
- 结核污染首版作为服务端方块计时逻辑实现，不创建大范围实体或持续流体实体。

## 实体与属性

新增 `SpiteArmoredTurtleEntity extends Turtle implements GeoEntity`，注册到 shared/common 的 `SpdEntities`。实体类型归入 `MobCategory.CREATURE`，尺寸按海龟碰撞体设置，客户端追踪距离与现有 SPD 生物一致。

未由需求给出具体数值的基础属性采用以下首版值：

| 属性 | 数值 |
| --- | ---: |
| 生命值 | 32 |
| 攻击伤害 | 8 |
| 护甲 | 8 |
| 击退抗性 | 0.65 |
| 水中移动速度 | 0.45 |
| 陆地移动速度 | 0.08 |
| 跟随范围 | 24 |
| 普通攻击间隔 | 20 tick |

Forge 和 Fabric 分别注册相同的属性表。自然生成限制在海洋和海滩表面，生成组为 1-2 只；刷怪蛋可以在任意合法位置直接生成。

## 阵营与目标

目标筛选复用 `SpdEntityTargeting`。怨甲海龟可以攻击玩家及非 SPD 生物，水中优先选择玩家和鱼类，不会主动攻击 SPD 生物。普通目标被攻击后仍可通过现有 SPD 的反击逻辑建立仇恨。

## 战斗行为

### 水中鱼雷模式

水中发现目标且距离合适时，专用目标会暂停普通寻路，短暂蓄势后朝目标施加水平冲刺。命中造成高额物理伤害、击退，并施加现有 `RendingEffect` 作为流血效果。冲撞有独立冷却，未命中时恢复普通寻路，避免每 tick 重复创建攻击。

### 水中污染轨迹

实体在水中移动时按固定 tick 间隔在身后生成短时污染位置。污染只在服务端扫描有限范围内的非 SPD 生物，并施加短时中毒/伤害效果；客户端只负责粒子表现。实现不创建持续追踪的流体实体，避免大规模实体和每 tick 分配。

### 陆地重装堡垒

受到直接近战伤害时进入短时 `shell_defensive` 状态。状态期间减少大部分伤害、暂停普通攻击，并向周围非 SPD 生物发射一次重金属碎片反击。缩壳状态和反击冷却使用同步数据，并写入实体 NBT。

## GeckoLib 资源与动画

资源复制到 common 运行时资源目录：

- `assets/spd/geo/spite_armored_turtle.geo.json`
- `assets/spd/animations/spite_armored_turtle.animation.json`
- `assets/spd/textures/entity/spite_armored_turtle.png`

新增 `SpiteArmoredTurtleModel` 与 `SpiteArmoredTurtleRenderer`。模型绑定以上三个资源。动画控制器按以下优先级选择：死亡状态、攻击动作、移动状态、静止状态。移动状态同时检查 GeckoLib `state.isMoving()` 和实体水平位移，保证服务端同步速度变化时仍能播放 `walk`。

Forge 和 Fabric 客户端都注册同一个实体 renderer。服务端类不引用 client 包。

## 伪装产卵与污染方块

怨甲海龟保留原版回游到出生地/海滩的行为，但不生成普通海龟蛋。沿用海龟产卵触发时机，在合适的沙地放置 2-4 个 `spite_nodule`（怨金结核）。

`spite_nodule` 是带 BlockEntity 的方块，默认寿命为 60 秒，倒计时写入 NBT。带精准采集的工具破坏时掉落 1-2 个 `residual_malice`；普通破坏不掉落完整结核。计时结束后，以结核为中心在有限半径内扫描普通沙、红沙和血沙，将可替换方块转成 `rust_sand`（铁锈沙），并移除结核。铁锈沙沿用血沙的不可正常种植约束和下落/碰撞风格。

首版结核和铁锈沙复用模组已有菌核、血沙的材质风格；方块 ID、模型和纹理路径独立，后续替换 PNG 不影响存档。

## 掉落与注册

怨甲海龟的额外掉落由实体服务端死亡逻辑处理：

- `heavy_spite_scute`：1-2 个。
- `residual_malice`：按实体随机概率掉落 1-2 个。

现有 `LivingEntityMixin` 为所有 SPD 生物提供的液金掉落继续生效，怨甲海龟不重复实现该逻辑；`ItemEntityMixin` 的培养基拾取限制自动适用于液金。两个新物品加入 SPD 创造标签和基础 item model，并添加中英文语言键。

新增注册项包括：

- `SpdEntities.SPITE_ARMORED_TURTLE`
- `SpdItems.SPITE_ARMORED_TURTLE_SPAWN_EGG`
- `SpdItems.HEAVY_SPITE_SCUTE`
- `SpdItems.RESIDUAL_MALICE`
- `SpdBlocks.SPITE_NODULE`
- `SpdBlocks.RUST_SAND`
- `SpdBlockEntities.SPITE_NODULE`

## 验证

实现后执行完整 Gradle 构建：

```powershell
& 'C:\tmp\gradle-8121\gradle-8.12.1\bin\gradle.bat' build --console=plain
```

增加资源/注册静态验证，检查实体、刷怪蛋、物品、方块、模型、纹理和动画路径，以及 `idle`、`walk`、`attack` 动画名称。构建后检查最终 Forge/Fabric JAR 内容。

手动验证重点：刷怪蛋生成、海洋/海滩自然生成、模型纹理、移动动画、攻击动画、水中冲撞、污染轨迹、陆地缩壳反伤、结核倒计时转化、精准采集掉落、额外掉落和液金培养基拾取限制。

## 非目标

- 本次不新增液态怨金流体、桶、燃料或武器淬火系统。
- 本次不改动普通海龟的原版注册、纹理和行为。
- 本次不提交视觉预览生成的 `.superpowers/` 文件和用户未跟踪的 `CLAUDE.md`。
