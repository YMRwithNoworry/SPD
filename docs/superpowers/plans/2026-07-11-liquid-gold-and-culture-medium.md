# 液金与培养基 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 添加液金、培养基、SPD 生物统一液金掉落、培养基拾取门槛及返还玻璃瓶的专用合成配方。

**Architecture:** 两个物品沿用共享物品注册。通过 `LivingEntity` 死亡 mixin 按实体类型命名空间统一掉落，通过 `ItemEntity#playerTouch` mixin 仅阻止不携带培养基的玩家拾取液金；专用 `CustomRecipe` 精确匹配玻璃、糖和装有水的药水瓶并返还玻璃瓶。

**Tech Stack:** Java 17、Minecraft 1.20.1、Architectury、Mixin、Forge/Fabric、Gradle。

---

### Task 1: 建立红灯断言

**Files:**
- Verify: `common/src/main/java/alku/spd/registry/SpdItems.java`
- Verify: `common/src/main/java/alku/spd/registry/SpdRecipeSerializers.java`
- Verify: `common/src/main/java/alku/spd/recipe/CultureMediumRecipe.java`
- Verify: `common/src/main/java/alku/spd/mixin/LivingEntityMixin.java`
- Verify: `common/src/main/java/alku/spd/mixin/ItemEntityMixin.java`

- [ ] **Step 1: 运行断言并确认因功能缺失而失败**

检查 `LIQUID_GOLD`、`CULTURE_MEDIUM`、概率阈值 `0.7F`/`0.9F`、`spd` 命名空间判定、培养基背包检查、`Potions.WATER` 和 `Items.GLASS_BOTTLE`。

### Task 2: 注册物品与资源

**Files:**
- Modify: `common/src/main/java/alku/spd/registry/SpdItems.java`
- Modify: `common/src/main/java/alku/spd/registry/SpdCreativeTabs.java`
- Modify: `common/src/main/resources/assets/spd/lang/zh_cn.json`
- Modify: `common/src/main/resources/assets/spd/lang/en_us.json`
- Create: `common/src/main/resources/assets/spd/models/item/liquid_gold.json`
- Create: `common/src/main/resources/assets/spd/models/item/culture_medium.json`
- Create: `common/src/main/resources/assets/spd/textures/item/liquid_gold.png`
- Create: `common/src/main/resources/assets/spd/textures/item/culture_medium.png`

- [ ] **Step 1: 注册两个普通物品并加入创造标签页**
- [ ] **Step 2: 添加中英文名称、generated 模型和用户纹理**

### Task 3: 实现掉落与拾取限制

**Files:**
- Modify: `common/src/main/java/alku/spd/mixin/LivingEntityMixin.java`
- Create: `common/src/main/java/alku/spd/mixin/ItemEntityMixin.java`
- Modify: `common/src/main/resources/spd.mixins.json`

- [ ] **Step 1: 在服务端实体死亡时识别 SPD 实体类型**
- [ ] **Step 2: 用单次随机数按 70%/20%/10% 掉落 1/2/4 个液金**
- [ ] **Step 3: 在玩家拾取入口阻止无培养基玩家拾取液金**

### Task 4: 实现培养基专用配方

**Files:**
- Create: `common/src/main/java/alku/spd/recipe/CultureMediumRecipe.java`
- Create: `common/src/main/java/alku/spd/registry/SpdRecipeSerializers.java`
- Modify: `common/src/main/java/alku/spd/Spd.java`
- Create: `common/src/main/resources/data/spd/recipes/culture_medium.json`

- [ ] **Step 1: 精确匹配玻璃、糖和水瓶各一个且无其他材料**
- [ ] **Step 2: 产出培养基并在水瓶槽返还玻璃瓶**
- [ ] **Step 3: 注册专用配方序列化器并添加配方 JSON**

### Task 5: 验证与交付

- [ ] **Step 1: 重跑静态断言并确认全部通过**
- [ ] **Step 2: 强制构建 Forge 与 Fabric**
- [ ] **Step 3: 检查 Forge jar 中的类、配方、模型和纹理，并比对纹理哈希**
- [ ] **Step 4: 部署 Forge jar 并比较 SHA-256**
- [ ] **Step 5: 使用 `-（添加液金与培养基）` 提交并推送**
