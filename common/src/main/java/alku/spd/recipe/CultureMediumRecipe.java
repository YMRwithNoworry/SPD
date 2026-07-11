package alku.spd.recipe;

import alku.spd.registry.SpdItems;
import alku.spd.registry.SpdRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class CultureMediumRecipe extends CustomRecipe {
    public CultureMediumRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        boolean glass = false;
        boolean sugar = false;
        boolean waterBottle = false;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(Items.GLASS) && !glass) {
                glass = true;
            } else if (stack.is(Items.SUGAR) && !sugar) {
                sugar = true;
            } else if (stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER && !waterBottle) {
                waterBottle = true;
            } else {
                return false;
            }
        }
        return glass && sugar && waterBottle;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, net.minecraft.core.RegistryAccess registryAccess) {
        return SpdItems.CULTURE_MEDIUM.get().getDefaultInstance();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER) {
                remaining.set(slot, Items.GLASS_BOTTLE.getDefaultInstance());
            }
        }
        return remaining;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SpdRecipeSerializers.CULTURE_MEDIUM.get();
    }
}
