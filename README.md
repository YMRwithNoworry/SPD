# SPD 模组资源说明

本仓库包含 Minecraft 模组 `SPD` 的基础工程，以及一个用于怪物资源制作的 Blockbench `bbmodel` 文件。

## 当前模型

- `杂物/霉染僵尸1 (1) (1).bbmodel`
- 模型格式：`Geckolib Entity`
- 已内置动画：
  - `animation.mold_zombie.idle`
  - `animation.mold_zombie.run`
  - `animation.mold_zombie.attack`

## 使用方法

1. 用 Blockbench 打开 `.bbmodel` 文件。
2. 在 GeckoLib / Entity 动画列表里直接查看或继续微调动画。
3. 导出时保持 `Geckolib` 兼容格式，模型动画名称可直接在代码中引用。

## 动画说明

- `idle`：静置呼吸、头部轻微扫视、四肢微摆。
- `run`：奔跑摆臂摆腿，带有前冲感和身体起伏。
- `attack`：攻击前压、挥砸、回弹三段式动作。

## 备注

- 仓库中的临时脚本已用于批量写入动画，后续如需继续调整，建议直接在 Blockbench 中微调。
- 如果你希望，我也可以继续把这套动画同步整理成 GeckoLib 动画源码/导出文件命名方案。
