# 霉染僵尸飞扑变种 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为霉染僵尸添加独立的 20% 飞扑变种、专用纹理、玩家飞扑攻击和 5 秒缓慢 I。

**Architecture:** 在 `MoldZombieEntity` 中用同步布尔实体数据保存飞扑变种，与现有行走/奔跑整数变种正交。专用 `Goal` 只在飞扑变种锁定玩家且距离合适时接管移动，命中调用现有近战攻击并附加缓慢；客户端模型根据同步状态切换纹理。

**Tech Stack:** Java 17、Minecraft 1.20.1、Architectury、Forge/Fabric、GeckoLib 4、Gradle。

---

### Task 1: 建立回归信号

**Files:**
- Verify: `common/src/main/java/alku/spd/entity/MoldZombieEntity.java`
- Verify: `common/src/main/java/alku/spd/client/model/MoldZombieModel.java`
- Verify: `common/src/main/resources/assets/spd/textures/entity/mold_zombie_pouncer.png`

- [ ] **Step 1: 运行功能缺失断言并确认失败**

运行 PowerShell 断言，要求源码具有 `POUNCER_VARIANT`、`PounceGoal`、20% 概率和专用纹理路径，并要求纹理文件存在。当前代码应失败，证明回归信号能检测尚未实现的功能。

### Task 2: 添加同步变种状态和生成概率

**Files:**
- Modify: `common/src/main/java/alku/spd/entity/MoldZombieEntity.java`

- [ ] **Step 1: 定义同步布尔状态**

新增 `EntityDataAccessor<Boolean> POUNCER_VARIANT`，默认 `false`，并公开只读查询方法供客户端模型使用。

- [ ] **Step 2: 在生成流程中按 20% 赋值**

在 `finalizeSpawn` 中使用 `random.nextFloat() < 0.2F`，确保自然生成和刷怪蛋生成使用相同比例。

- [ ] **Step 3: 持久化状态**

写入 `MoldZombiePouncerVariant` NBT；旧存档缺少字段时保持默认 `false`。

### Task 3: 实现飞扑攻击

**Files:**
- Modify: `common/src/main/java/alku/spd/entity/MoldZombieEntity.java`

- [ ] **Step 1: 注册飞扑 Goal**

将飞扑 Goal 放在普通近战移动目标之前，仅允许飞扑变种对玩家触发。

- [ ] **Step 2: 实现触发与运动**

目标距离限制为 3 至 6 格，要求地面、可见目标和冷却结束；启动时设置面向目标的水平速度及向上速度。

- [ ] **Step 3: 实现命中与缓慢**

飞扑过程中近距离碰撞时调用 `doHurtTarget`。成功命中玩家后添加 `MobEffects.MOVEMENT_SLOWDOWN`，持续 100 tick、等级 0，并结束飞扑进入冷却。

- [ ] **Step 4: 处理落地和超时**

飞扑落地或超过动作时限后停止，并设置冷却，防止连续触发。

### Task 4: 接入专用纹理

**Files:**
- Modify: `common/src/main/java/alku/spd/client/model/MoldZombieModel.java`
- Create: `common/src/main/resources/assets/spd/textures/entity/mold_zombie_pouncer.png`

- [ ] **Step 1: 导入用户提供的纹理**

将 `C:\Users\ADMINI~1\AppData\Local\Temp\codex-clipboard-7v0eWZ.png` 复制为运行时纹理。

- [ ] **Step 2: 根据同步状态选择纹理**

飞扑变种返回 `mold_zombie_pouncer.png`，普通变种继续返回 `mold_zombie.png`；模型和动画资源不变。

### Task 5: 验证、部署与交付

**Files:**
- Verify: `forge/build/libs/spd-forge-1.0-SNAPSHOT.jar`
- Deploy: `E:\GAME1\MC\pcl2-beta\.minecraft\versions\真斗蛐蛐\mods\spd-forge-1.0-SNAPSHOT.jar`

- [ ] **Step 1: 运行功能断言并确认通过**

重复 Task 1 的断言，确认同步状态、20% 概率、飞扑 Goal、缓慢效果、纹理选择和纹理文件均存在。

- [ ] **Step 2: 构建 Forge 和 Fabric**

运行 `:forge:build :fabric:build --rerun-tasks --console=plain`，预期 `BUILD SUCCESSFUL`。

- [ ] **Step 3: 检查 Forge jar**

确认 jar 包含 `MoldZombieEntity`、其飞扑 Goal 内部类、`MoldZombieModel` 和 `mold_zombie_pouncer.png`。

- [ ] **Step 4: 部署并比较哈希**

覆盖整合包 Forge jar，并确认源文件与目标文件 SHA-256 一致。

- [ ] **Step 5: 提交并推送**

仅提交本功能文件和计划，提交信息使用 `-（添加霉染僵尸飞扑变种）`，推送到 GitHub。
