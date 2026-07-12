package alku.spd.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CultureMediumRecipeTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void acceptsGlassSugarAndWaterBottle() {
        CultureMediumRecipe recipe = recipe();
        TestCraftingContainer container = new TestCraftingContainer(
                new ItemStack(Items.GLASS),
                new ItemStack(Items.SUGAR),
                PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));

        assertTrue(recipe.matches(container, null));
        assertTrue(recipe.getRemainingItems(container).get(2).is(Items.GLASS_BOTTLE));
    }

    @Test
    void appearsInTheRecipeBook() {
        assertFalse(recipe().isSpecial());
    }

    private static CultureMediumRecipe recipe() {
        return new CultureMediumRecipe(
                new ResourceLocation("spd", "culture_medium"),
                CraftingBookCategory.MISC);
    }

    private static final class TestCraftingContainer extends SimpleContainer implements CraftingContainer {
        private TestCraftingContainer(ItemStack... items) {
            super(items);
        }

        @Override
        public int getWidth() {
            return 3;
        }

        @Override
        public int getHeight() {
            return 1;
        }

        @Override
        public List<ItemStack> getItems() {
            return this.items;
        }

        @Override
        public void fillStackedContents(net.minecraft.world.entity.player.StackedContents contents) {
            for (ItemStack stack : this.items) {
                contents.accountSimpleStack(stack);
            }
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
