# Abyssal Blazing Rune Stele Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `spd:abyssal_blazing_rune_stele`, a two-block-tall, four-direction decorative block rendered with the supplied GeckoLib model and texture, including a fully rendered block item and survival recipe.

**Architecture:** Extend vanilla `DoublePlantBlock` so placement, paired-half removal, creative breaking, and one-item drops retain proven vanilla semantics. Add horizontal facing and model-sized collision, create a GeckoLib block entity only for the lower half, and render the full 32-pixel model from that lower half. A dedicated GeckoLib `BlockItem` reuses the same resources; Fabric and Forge client Mixins provide loader-specific item renderers without a split platform package.

**Tech Stack:** Java 17, Minecraft 1.20.1, Architectury 9.2.14, GeckoLib 4.4.9, Fabric, Forge 47.4.20, JUnit 5, Gradle 8.12.1.

---

## File Map

New common Java files:

- `common/src/main/java/alku/spd/block/AbyssalBlazingRuneSteleBlock.java`: paired halves, facing, placement, collision, render shape, and lower-half block entity creation.
- `common/src/main/java/alku/spd/block/entity/AbyssalBlazingRuneSteleBlockEntity.java`: GeckoLib block animatable.
- `common/src/main/java/alku/spd/item/AbyssalBlazingRuneSteleItem.java`: GeckoLib block item with a client-neutral fallback provider.
- `common/src/main/java/alku/spd/client/model/AbyssalBlazingRuneSteleModel.java`: world model resource locations.
- `common/src/main/java/alku/spd/client/model/AbyssalBlazingRuneSteleItemModel.java`: item model resource locations.
- `common/src/main/java/alku/spd/client/renderer/AbyssalBlazingRuneSteleRenderer.java`: world renderer and facing rotation.
- `common/src/main/java/alku/spd/client/renderer/AbyssalBlazingRuneSteleItemRenderer.java`: inventory and hand renderer.
- `common/src/test/java/alku/spd/block/AbyssalBlazingRuneSteleBlockTest.java`: pure paired-position and collision tests.
- `common/src/test/java/alku/spd/block/AbyssalBlazingRuneSteleResourceTest.java`: recipe and packaged-resource tests.

New loader Java files:

- `fabric/src/main/java/alku/spd/fabric/mixin/AbyssalBlazingRuneSteleItemFabricMixin.java`: Fabric GeckoLib `RenderProvider` bridge.
- `forge/src/main/java/alku/spd/forge/mixin/AbyssalBlazingRuneSteleItemForgeMixin.java`: Forge `IClientItemExtensions` bridge.

Modified registration files:

- `common/src/main/java/alku/spd/registry/SpdBlocks.java`
- `common/src/main/java/alku/spd/registry/SpdBlockEntities.java`
- `common/src/main/java/alku/spd/registry/SpdItems.java`
- `common/src/main/java/alku/spd/registry/SpdCreativeTabs.java`
- `fabric/src/main/java/alku/spd/fabric/client/SpdFabricClient.java`
- `forge/src/main/java/alku/spd/forge/client/SpdForgeClient.java`
- `fabric/src/main/resources/spd.fabric.mixins.json`
- `forge/src/main/resources/spd.forge.mixins.json`

New resources:

- `common/src/main/resources/assets/spd/geo/abyssal_blazing_rune_stele.geo.json`
- `common/src/main/resources/assets/spd/textures/block/abyssal_blazing_rune_stele.png`
- `common/src/main/resources/assets/spd/blockstates/abyssal_blazing_rune_stele.json`
- `common/src/main/resources/assets/spd/models/block/abyssal_blazing_rune_stele.json`
- `common/src/main/resources/assets/spd/models/item/abyssal_blazing_rune_stele.json`
- `common/src/main/resources/data/spd/recipes/abyssal_blazing_rune_stele.json`
- `common/src/main/resources/data/spd/loot_tables/blocks/abyssal_blazing_rune_stele.json`
- `common/src/main/resources/data/minecraft/tags/blocks/needs_diamond_tool.json`

Modified resources:

- `common/src/main/resources/assets/spd/lang/zh_cn.json`
- `common/src/main/resources/assets/spd/lang/en_us.json`
- `common/src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json`

### Task 1: Implement and test the paired two-block structure

**Files:**
- Create: `common/src/test/java/alku/spd/block/AbyssalBlazingRuneSteleBlockTest.java`
- Create: `common/src/main/java/alku/spd/block/AbyssalBlazingRuneSteleBlock.java`
- Modify: `common/src/main/java/alku/spd/registry/SpdBlocks.java`

- [ ] **Step 1: Write failing tests for counterpart positions and collision rotation**

```java
package alku.spd.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class AbyssalBlazingRuneSteleBlockTest {
    @Test
    void lowerHalfPairsWithTheBlockAbove() {
        assertEquals(new BlockPos(4, 11, 7),
                AbyssalBlazingRuneSteleBlock.otherHalfPos(new BlockPos(4, 10, 7), DoubleBlockHalf.LOWER));
    }

    @Test
    void upperHalfPairsWithTheBlockBelow() {
        assertEquals(new BlockPos(4, 9, 7),
                AbyssalBlazingRuneSteleBlock.otherHalfPos(new BlockPos(4, 10, 7), DoubleBlockHalf.UPPER));
    }

    @Test
    void collisionRotatesWithTheStele() {
        AABB north = AbyssalBlazingRuneSteleBlock.shapeFor(Direction.NORTH).bounds();
        AABB east = AbyssalBlazingRuneSteleBlock.shapeFor(Direction.EAST).bounds();

        assertEquals(15.0D / 16.0D, north.getXsize(), 1.0E-9D);
        assertEquals(10.0D / 16.0D, north.getZsize(), 1.0E-9D);
        assertEquals(10.0D / 16.0D, east.getXsize(), 1.0E-9D);
        assertEquals(15.0D / 16.0D, east.getZsize(), 1.0E-9D);
    }
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run:

```powershell
& 'C:\tmp\gradle-8121\gradle-8.12.1\bin\gradle.bat' :common:test --tests alku.spd.block.AbyssalBlazingRuneSteleBlockTest --console=plain
```

Expected: test compilation fails because `AbyssalBlazingRuneSteleBlock` does not exist.

- [ ] **Step 3: Add the minimal paired block implementation**

Create `AbyssalBlazingRuneSteleBlock` with these members and overrides:

```java
package alku.spd.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AbyssalBlazingRuneSteleBlock extends DoublePlantBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape NORTH_SOUTH_SHAPE = Block.box(0.0D, 0.0D, 3.0D, 15.0D, 16.0D, 13.0D);
    private static final VoxelShape EAST_WEST_SHAPE = Block.box(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 15.0D);

    public AbyssalBlazingRuneSteleBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(FACING, Direction.NORTH));
    }

    static BlockPos otherHalfPos(BlockPos pos, DoubleBlockHalf half) {
        return half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
    }

    static VoxelShape shapeFor(Direction facing) {
        return facing.getAxis() == Direction.Axis.Z ? NORTH_SOUTH_SHAPE : EAST_WEST_SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return true;
        }
        BlockState lower = level.getBlockState(pos.below());
        return lower.is(this)
                && lower.getValue(HALF) == DoubleBlockHalf.LOWER
                && lower.getValue(FACING) == state.getValue(FACING);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction counterpartDirection = state.getValue(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN;
        if (direction == counterpartDirection && neighborState.is(this)
                && neighborState.getValue(HALF) != state.getValue(HALF)
                && neighborState.getValue(FACING) != state.getValue(FACING)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(FACING));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF, FACING);
    }
}
```

Register it in `SpdBlocks`:

```java
public static final RegistrySupplier<Block> ABYSSAL_BLAZING_RUNE_STELE = BLOCKS.register("abyssal_blazing_rune_stele", () ->
        new AbyssalBlazingRuneSteleBlock(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)
                .noOcclusion()));
```

- [ ] **Step 4: Run the focused test and common compilation to verify GREEN**

```powershell
& 'C:\tmp\gradle-8121\gradle-8.12.1\bin\gradle.bat' :common:test --tests alku.spd.block.AbyssalBlazingRuneSteleBlockTest :common:compileJava --console=plain
```

Expected: all three tests pass and common compiles.

- [ ] **Step 5: Commit and push the structural slice**

```powershell
git add common/src/main/java/alku/spd/block/AbyssalBlazingRuneSteleBlock.java common/src/main/java/alku/spd/registry/SpdBlocks.java common/src/test/java/alku/spd/block/AbyssalBlazingRuneSteleBlockTest.java
git commit -m '-（添加铭符碑双格结构）'
git push origin master
```

### Task 2: Add GeckoLib world rendering

**Files:**
- Create: `common/src/main/java/alku/spd/block/entity/AbyssalBlazingRuneSteleBlockEntity.java`
- Create: `common/src/main/java/alku/spd/client/model/AbyssalBlazingRuneSteleModel.java`
- Create: `common/src/main/java/alku/spd/client/renderer/AbyssalBlazingRuneSteleRenderer.java`
- Modify: `common/src/main/java/alku/spd/block/AbyssalBlazingRuneSteleBlock.java`
- Modify: `common/src/main/java/alku/spd/registry/SpdBlockEntities.java`
- Modify: `fabric/src/main/java/alku/spd/fabric/client/SpdFabricClient.java`
- Modify: `forge/src/main/java/alku/spd/forge/client/SpdForgeClient.java`
- Create: `common/src/main/resources/assets/spd/geo/abyssal_blazing_rune_stele.geo.json`
- Create: `common/src/main/resources/assets/spd/textures/block/abyssal_blazing_rune_stele.png`

- [ ] **Step 1: Add the GeckoLib block entity**

```java
package alku.spd.block.entity;

import alku.spd.registry.SpdBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AbyssalBlazingRuneSteleBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AbyssalBlazingRuneSteleBlockEntity(BlockPos pos, BlockState state) {
        super(SpdBlockEntities.ABYSSAL_BLAZING_RUNE_STELE.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
```

Register the type in `SpdBlockEntities`:

```java
public static final RegistrySupplier<BlockEntityType<AbyssalBlazingRuneSteleBlockEntity>> ABYSSAL_BLAZING_RUNE_STELE =
        BLOCK_ENTITIES.register("abyssal_blazing_rune_stele", () ->
                BlockEntityType.Builder.of(AbyssalBlazingRuneSteleBlockEntity::new,
                        SpdBlocks.ABYSSAL_BLAZING_RUNE_STELE.get()).build(null));
```

- [ ] **Step 2: Make only the lower half own and render the block entity**

Change `AbyssalBlazingRuneSteleBlock` to implement `EntityBlock`, then add:

```java
@Override
public RenderShape getRenderShape(BlockState state) {
    return state.getValue(HALF) == DoubleBlockHalf.LOWER
            ? RenderShape.ENTITYBLOCK_ANIMATED
            : RenderShape.INVISIBLE;
}

@Nullable
@Override
public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return state.getValue(HALF) == DoubleBlockHalf.LOWER
            ? new AbyssalBlazingRuneSteleBlockEntity(pos, state)
            : null;
}
```

- [ ] **Step 3: Add the world model and renderer**

```java
package alku.spd.client.model;

import alku.spd.Spd;
import alku.spd.block.entity.AbyssalBlazingRuneSteleBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class AbyssalBlazingRuneSteleModel extends GeoModel<AbyssalBlazingRuneSteleBlockEntity> {
    @Override
    public ResourceLocation getModelResource(AbyssalBlazingRuneSteleBlockEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "geo/abyssal_blazing_rune_stele.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AbyssalBlazingRuneSteleBlockEntity animatable) {
        return new ResourceLocation(Spd.MOD_ID, "textures/block/abyssal_blazing_rune_stele.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AbyssalBlazingRuneSteleBlockEntity animatable) {
        return null;
    }
}
```

```java
package alku.spd.client.renderer;

import alku.spd.block.AbyssalBlazingRuneSteleBlock;
import alku.spd.block.entity.AbyssalBlazingRuneSteleBlockEntity;
import alku.spd.client.model.AbyssalBlazingRuneSteleModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public final class AbyssalBlazingRuneSteleRenderer extends GeoBlockRenderer<AbyssalBlazingRuneSteleBlockEntity> {
    public AbyssalBlazingRuneSteleRenderer(BlockEntityRendererProvider.Context context) {
        super(new AbyssalBlazingRuneSteleModel());
    }

    @Override
    public void preRender(PoseStack poseStack, AbyssalBlazingRuneSteleBlockEntity animatable,
                          BakedGeoModel model, MultiBufferSource bufferSource,
                          com.mojang.blaze3d.vertex.VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {
        Direction facing = animatable.getBlockState().getValue(AbyssalBlazingRuneSteleBlock.FACING);
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
```

- [ ] **Step 4: Copy the validated model and texture into runtime resources**

```powershell
Copy-Item -LiteralPath 'E:\代码\MC模型\转换后\柱子.json' -Destination 'common\src\main\resources\assets\spd\geo\abyssal_blazing_rune_stele.geo.json'
Copy-Item -LiteralPath 'E:\代码\MC模型\转换后\texture.png' -Destination 'common\src\main\resources\assets\spd\textures\block\abyssal_blazing_rune_stele.png'
```

Preserve `format_version: 1.12.0`, the 128×128 dimensions, `bb_main`, all five cubes, pivots, rotations, and UV coordinates.

- [ ] **Step 5: Register the world renderer on both clients**

Add to both `SpdFabricClient.initializeClient()` and `SpdForgeClient.initializeClient()` beside the mascot registration:

```java
BlockEntityRendererRegistry.register(SpdBlockEntities.ABYSSAL_BLAZING_RUNE_STELE.get(),
        AbyssalBlazingRuneSteleRenderer::new);
```

- [ ] **Step 6: Compile both loaders**

```powershell
& 'C:\tmp\gradle-8121\gradle-8.12.1\bin\gradle.bat' :fabric:compileJava :forge:compileJava --console=plain
```

Expected: both loaders compile without client classes leaking into common server initialization.

- [ ] **Step 7: Commit and push world rendering**

```powershell
git add common/src/main/java/alku/spd/block common/src/main/java/alku/spd/client common/src/main/java/alku/spd/registry/SpdBlockEntities.java common/src/main/resources/assets/spd/geo/abyssal_blazing_rune_stele.geo.json common/src/main/resources/assets/spd/textures/block/abyssal_blazing_rune_stele.png fabric/src/main/java/alku/spd/fabric/client/SpdFabricClient.java forge/src/main/java/alku/spd/forge/client/SpdForgeClient.java
git commit -m '-（添加铭符碑世界渲染）'
git push origin master
```

### Task 3: Add the GeckoLib block item renderer

**Files:**
- Create: `common/src/main/java/alku/spd/item/AbyssalBlazingRuneSteleItem.java`
- Create: `common/src/main/java/alku/spd/client/model/AbyssalBlazingRuneSteleItemModel.java`
- Create: `common/src/main/java/alku/spd/client/renderer/AbyssalBlazingRuneSteleItemRenderer.java`
- Create: `fabric/src/main/java/alku/spd/fabric/mixin/AbyssalBlazingRuneSteleItemFabricMixin.java`
- Create: `forge/src/main/java/alku/spd/forge/mixin/AbyssalBlazingRuneSteleItemForgeMixin.java`
- Modify: `common/src/main/java/alku/spd/registry/SpdItems.java`
- Modify: `common/src/main/java/alku/spd/registry/SpdCreativeTabs.java`
- Modify: `fabric/src/main/resources/spd.fabric.mixins.json`
- Modify: `forge/src/main/resources/spd.forge.mixins.json`

- [ ] **Step 1: Add the client-neutral GeckoLib block item**

```java
package alku.spd.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class AbyssalBlazingRuneSteleItem extends BlockItem implements GeoItem {
    private static final Supplier<Object> EMPTY_RENDER_PROVIDER = () -> null;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AbyssalBlazingRuneSteleItem(Block block, Properties properties) {
        super(block, properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return EMPTY_RENDER_PROVIDER;
    }
}
```

Register it in `SpdItems`:

```java
public static final RegistrySupplier<Item> ABYSSAL_BLAZING_RUNE_STELE = ITEMS.register("abyssal_blazing_rune_stele", () ->
        new AbyssalBlazingRuneSteleItem(SpdBlocks.ABYSSAL_BLAZING_RUNE_STELE.get(), new Item.Properties()));
```

Add `SpdItems.ABYSSAL_BLAZING_RUNE_STELE.get()` to `SpdCreativeTabs` beside other blocks.

- [ ] **Step 2: Add the shared item model and renderer**

`AbyssalBlazingRuneSteleItemModel` must return the same geo and texture paths as the block model and return `null` for animation. Add:

```java
public final class AbyssalBlazingRuneSteleItemRenderer extends GeoItemRenderer<AbyssalBlazingRuneSteleItem> {
    public AbyssalBlazingRuneSteleItemRenderer() {
        super(new AbyssalBlazingRuneSteleItemModel());
    }
}
```

- [ ] **Step 3: Add the Fabric client Mixin**

```java
package alku.spd.fabric.mixin;

import alku.spd.client.renderer.AbyssalBlazingRuneSteleItemRenderer;
import alku.spd.item.AbyssalBlazingRuneSteleItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(AbyssalBlazingRuneSteleItem.class)
public abstract class AbyssalBlazingRuneSteleItemFabricMixin {
    @Inject(method = "createRenderer", at = @At("HEAD"), cancellable = true)
    private void spd$createRenderer(Consumer<Object> consumer, CallbackInfo ci) {
        consumer.accept(new RenderProvider() {
            private AbyssalBlazingRuneSteleItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new AbyssalBlazingRuneSteleItemRenderer();
                }
                return renderer;
            }
        });
        ci.cancel();
    }

    @Inject(method = "getRenderProvider", at = @At("HEAD"), cancellable = true)
    private void spd$getRenderProvider(CallbackInfoReturnable<Supplier<Object>> cir) {
        cir.setReturnValue(GeoItem.makeRenderer((AbyssalBlazingRuneSteleItem) (Object) this));
    }
}
```

Append `AbyssalBlazingRuneSteleItemFabricMixin` to the `client` array in `spd.fabric.mixins.json`.

- [ ] **Step 4: Add the Forge client Mixin**

```java
package alku.spd.forge.mixin;

import alku.spd.client.renderer.AbyssalBlazingRuneSteleItemRenderer;
import alku.spd.item.AbyssalBlazingRuneSteleItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(AbyssalBlazingRuneSteleItem.class)
public abstract class AbyssalBlazingRuneSteleItemForgeMixin {
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private AbyssalBlazingRuneSteleItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new AbyssalBlazingRuneSteleItemRenderer();
                }
                return renderer;
            }
        });
    }
}
```

Append `AbyssalBlazingRuneSteleItemForgeMixin` to the `client` array in `spd.forge.mixins.json`.

- [ ] **Step 5: Compile both loader item bridges**

```powershell
& 'C:\tmp\gradle-8121\gradle-8.12.1\bin\gradle.bat' :fabric:compileJava :forge:compileJava --console=plain
```

Expected: both compile and no `alku.spd.platform` source package is introduced.

- [ ] **Step 6: Commit and push item rendering**

```powershell
git add common/src/main/java/alku/spd/item/AbyssalBlazingRuneSteleItem.java common/src/main/java/alku/spd/client/model/AbyssalBlazingRuneSteleItemModel.java common/src/main/java/alku/spd/client/renderer/AbyssalBlazingRuneSteleItemRenderer.java common/src/main/java/alku/spd/registry/SpdItems.java common/src/main/java/alku/spd/registry/SpdCreativeTabs.java fabric/src/main/java/alku/spd/fabric/mixin/AbyssalBlazingRuneSteleItemFabricMixin.java fabric/src/main/resources/spd.fabric.mixins.json forge/src/main/java/alku/spd/forge/mixin/AbyssalBlazingRuneSteleItemForgeMixin.java forge/src/main/resources/spd.forge.mixins.json
git commit -m '-（添加铭符碑物品渲染）'
git push origin master
```

### Task 4: Add recipes, loot, models, tags, and languages with resource tests

**Files:**
- Create: `common/src/test/java/alku/spd/block/AbyssalBlazingRuneSteleResourceTest.java`
- Create: `common/src/main/resources/assets/spd/blockstates/abyssal_blazing_rune_stele.json`
- Create: `common/src/main/resources/assets/spd/models/block/abyssal_blazing_rune_stele.json`
- Create: `common/src/main/resources/assets/spd/models/item/abyssal_blazing_rune_stele.json`
- Create: `common/src/main/resources/data/spd/recipes/abyssal_blazing_rune_stele.json`
- Create: `common/src/main/resources/data/spd/loot_tables/blocks/abyssal_blazing_rune_stele.json`
- Create: `common/src/main/resources/data/minecraft/tags/blocks/needs_diamond_tool.json`
- Modify: `common/src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json`
- Modify: `common/src/main/resources/assets/spd/lang/zh_cn.json`
- Modify: `common/src/main/resources/assets/spd/lang/en_us.json`

- [ ] **Step 1: Write a failing resource contract test**

```java
package alku.spd.block;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class AbyssalBlazingRuneSteleResourceTest {
    @Test
    void recipeUsesFourObsidianAroundOneAmethystShard() throws Exception {
        JsonObject recipe = resourceJson("/data/spd/recipes/abyssal_blazing_rune_stele.json");
        JsonArray pattern = recipe.getAsJsonArray("pattern");

        assertEquals(" O ", pattern.get(0).getAsString());
        assertEquals("OAO", pattern.get(1).getAsString());
        assertEquals(" O ", pattern.get(2).getAsString());
        assertEquals("minecraft:obsidian", recipe.getAsJsonObject("key").getAsJsonObject("O").get("item").getAsString());
        assertEquals("minecraft:amethyst_shard", recipe.getAsJsonObject("key").getAsJsonObject("A").get("item").getAsString());
        assertEquals("spd:abyssal_blazing_rune_stele", recipe.getAsJsonObject("result").get("item").getAsString());
        assertEquals(1, recipe.getAsJsonObject("result").get("count").getAsInt());
    }

    @Test
    void clientAndLootResourcesArePackaged() {
        assertNotNull(getClass().getResource("/assets/spd/geo/abyssal_blazing_rune_stele.geo.json"));
        assertNotNull(getClass().getResource("/assets/spd/textures/block/abyssal_blazing_rune_stele.png"));
        assertNotNull(getClass().getResource("/assets/spd/blockstates/abyssal_blazing_rune_stele.json"));
        assertNotNull(getClass().getResource("/assets/spd/models/item/abyssal_blazing_rune_stele.json"));
        assertNotNull(getClass().getResource("/data/spd/loot_tables/blocks/abyssal_blazing_rune_stele.json"));
    }

    private JsonObject resourceJson(String path) throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            assertNotNull(stream, path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
```

- [ ] **Step 2: Run the focused resource test and verify RED**

```powershell
& 'C:\tmp\gradle-8121\gradle-8.12.1\bin\gradle.bat' :common:test --tests alku.spd.block.AbyssalBlazingRuneSteleResourceTest --console=plain
```

Expected: failure because recipe, blockstate, item model, and loot table do not exist.

- [ ] **Step 3: Add blockstate and model resources**

`blockstates/abyssal_blazing_rune_stele.json`:

```json
{
  "multipart": [
    {
      "apply": {
        "model": "spd:block/abyssal_blazing_rune_stele"
      }
    }
  ]
}
```

`models/block/abyssal_blazing_rune_stele.json`:

```json
{
  "textures": {
    "particle": "spd:block/abyssal_blazing_rune_stele"
  }
}
```

`models/item/abyssal_blazing_rune_stele.json`:

```json
{
  "parent": "builtin/entity",
  "textures": {
    "particle": "spd:block/abyssal_blazing_rune_stele"
  },
  "display": {
    "gui": { "rotation": [30, 225, 0], "translation": [0, -2, 0], "scale": [0.42, 0.42, 0.42] },
    "ground": { "translation": [0, 2, 0], "scale": [0.25, 0.25, 0.25] },
    "fixed": { "rotation": [0, 180, 0], "scale": [0.4, 0.4, 0.4] },
    "thirdperson_righthand": { "rotation": [75, 45, 0], "translation": [0, 1.5, 0], "scale": [0.32, 0.32, 0.32] },
    "thirdperson_lefthand": { "rotation": [75, -45, 0], "translation": [0, 1.5, 0], "scale": [0.32, 0.32, 0.32] },
    "firstperson_righthand": { "rotation": [0, 45, 0], "translation": [1.5, 1.5, 0], "scale": [0.32, 0.32, 0.32] },
    "firstperson_lefthand": { "rotation": [0, 225, 0], "translation": [1.5, 1.5, 0], "scale": [0.32, 0.32, 0.32] }
  }
}
```

- [ ] **Step 4: Add recipe and loot table**

`recipes/abyssal_blazing_rune_stele.json`:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "building",
  "pattern": [" O ", "OAO", " O "],
  "key": {
    "O": { "item": "minecraft:obsidian" },
    "A": { "item": "minecraft:amethyst_shard" }
  },
  "result": {
    "item": "spd:abyssal_blazing_rune_stele",
    "count": 1
  }
}
```

`loot_tables/blocks/abyssal_blazing_rune_stele.json`:

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1.0,
      "entries": [
        { "type": "minecraft:item", "name": "spd:abyssal_blazing_rune_stele" }
      ],
      "conditions": [
        { "condition": "minecraft:survives_explosion" }
      ]
    }
  ]
}
```

Because `DoublePlantBlock.playerWillDestroy` manually drops resources once and passes air to `playerDestroy`, the loot table intentionally has no half condition.

- [ ] **Step 5: Add mining tags and translations**

Append `spd:abyssal_blazing_rune_stele` to `mineable/pickaxe.json`. Create `needs_diamond_tool.json`:

```json
{
  "replace": false,
  "values": [
    "spd:abyssal_blazing_rune_stele"
  ]
}
```

Add language keys:

```json
"block.spd.abyssal_blazing_rune_stele": "渊炽铭符碑"
```

```json
"block.spd.abyssal_blazing_rune_stele": "Abyssal Blazing Rune Stele"
```

- [ ] **Step 6: Run the focused resource test and verify GREEN**

```powershell
& 'C:\tmp\gradle-8121\gradle-8.12.1\bin\gradle.bat' :common:test --tests alku.spd.block.AbyssalBlazingRuneSteleResourceTest --console=plain
```

Expected: both resource tests pass.

- [ ] **Step 7: Commit and push resources**

```powershell
git add common/src/main/resources common/src/test/java/alku/spd/block/AbyssalBlazingRuneSteleResourceTest.java
git commit -m '-（添加铭符碑配方与资源）'
git push origin master
```

### Task 5: Full verification and runtime artifact audit

**Files:**
- Verify only; do not stage `.architectury-transformer`, `.superpowers`, `CLAUDE.md`, `common/logs`, build outputs, or run logs.

- [ ] **Step 1: Run focused and full tests**

```powershell
& 'C:\tmp\gradle-8121\gradle-8.12.1\bin\gradle.bat' :common:test --tests 'alku.spd.block.AbyssalBlazingRuneStele*' --console=plain
& 'C:\tmp\gradle-8121\gradle-8.12.1\bin\gradle.bat' build --console=plain
git diff --check
```

Expected: focused tests and all 32 Gradle tasks succeed; `git diff --check` reports no errors.

- [ ] **Step 2: Audit both remapped JARs**

```powershell
& jar tf 'forge/build/libs/spd-forge-1.0-SNAPSHOT.jar' | Select-String 'abyssal_blazing_rune_stele|AbyssalBlazingRuneStele'
& jar tf 'fabric/build/libs/spd-fabric-1.0-SNAPSHOT.jar' | Select-String 'abyssal_blazing_rune_stele|AbyssalBlazingRuneStele'
```

Expected: both contain the block, block entity, models, renderers, texture, blockstate, item model, recipe, loot table, and their loader Mixin; neither contains `alku/spd/platform`.

- [ ] **Step 3: Attempt Forge startup without widening scope**

Run `:forge:runClient`, then inspect `forge/run/logs/latest.log`. The previous accepted baseline is that Forge advances past SPD's module layer but may stop in LowDragLib's `ldlib2.mixins.json:accessor.SlotAccessor`. Confirm there is no new error naming the stele, its Mixin configs, resource locations, or renderer classes. Do not modify LowDragLib as part of this task.

- [ ] **Step 4: Confirm remote and untouched user files**

```powershell
git status --short
git ls-remote origin refs/heads/master
```

Expected: only the pre-existing `.architectury-transformer/`, `.superpowers/`, `CLAUDE.md`, and `common/logs/` remain untracked, and remote `master` equals local `HEAD`.

## Self-Review

- Spec coverage: two-block occupancy, four-way facing, paired removal, one drop, exact collision, pure decoration, GeckoLib world and item rendering, supplied model and texture, cross recipe, creative tab, language, mining tags, loot, both loaders, and artifact verification are all assigned.
- Type consistency: the registry key, class names, resource paths, Mixin names, and language key consistently use `abyssal_blazing_rune_stele` / `AbyssalBlazingRuneStele`.
- Platform boundary: common code imports no Forge API and no Fabric client rendering API; client-only item renderer integration remains in loader-specific Mixin packages.
- Scope: the known LowDragLib development-runtime failure is recorded as a verification limitation and is not folded into this feature.
