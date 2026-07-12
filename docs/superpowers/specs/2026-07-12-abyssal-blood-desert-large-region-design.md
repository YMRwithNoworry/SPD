# 血漠大区域生成设计

## 目标

将 `spd:abyssal_blood_desert` 变成主世界中独立、连续且面积较大的血沙沙漠。普通 `minecraft:desert` 保持原有生成和方块，不再通过替换普通沙子来表现血漠。

## 现状与问题

当前 TerraBlender 区域通过 `addBiomeSimilar(..., Biomes.DESERT, ...)` 复用原版沙漠的少量气候参数点。这样血漠只覆盖原版沙漠气候分布中的部分位置，区域面积和连续性受原版参数点限制；表面规则即使正确，也无法让血漠本身形成一大片独立沙漠。

## 方案

### TerraBlender 区域

保留现有血漠群系 ID 和 `OVERWORLD` 区域注册，但把区域映射改为显式的宽气候参数范围：

- 高温、低湿度作为核心条件。
- 大范围覆盖内陆度，允许生成连续的沙漠腹地。
- 侵蚀、奇异度和深度使用覆盖面较宽的参数，减少零碎斑块。
- 提高区域权重，使血漠区域在符合条件的气候位置更容易胜出。

该映射只指向 `spd:abyssal_blood_desert`，不会把普通沙漠改名或替换成血漠。

### 血沙表面

继续使用 TerraBlender 的世界表面规则，并将规则限制在血漠群系：地表、浅层地下和深层地下表层统一使用 `spd:abyssal_blood_sand`。规则不作用于普通沙漠或其他群系。

### 群系特征

保留血漠现有的黄绿色水色、血漠生物生成和较少仙人掌特征。仙人掌的稀疏概率不通过增加普通沙漠特征实现，仍由血漠专属 placed feature 控制。

## 数据流

新世界生成时，TerraBlender 将热、干、内陆气候点映射到血漠群系；群系解析到 `spd:abyssal_blood_desert` 后，血漠专属生物群系数据和特征生效；地形表面阶段再根据群系条件把地表层写成血沙。

## 兼容与边界

- 不移除或重命名现有血漠资源 ID，避免已有命令、标签和实体逻辑失效。
- 不修改 `minecraft:desert` 的群系 JSON、地形表面或结构标签行为。
- 只影响新生成区块；已有区块不会自动重生成。
- Forge 和 Fabric 共用同一套 TerraBlender 注册逻辑。

## 验证

- 编译 common、Forge 和 Fabric 模块。
- 检查 Forge/Fabric jar 中包含更新后的 TerraBlender 类和血漠资源。
- 解析所有新增或修改的 JSON，并运行 `git diff --check`。
- 在新世界中使用 `/locate biome spd:abyssal_blood_desert`，确认血漠位置可找到且地表为血沙；使用 `/locate biome minecraft:desert`，确认普通沙漠仍使用原版沙子。
- 检查血漠中的水色和仙人掌生成资源仍然存在。
