package alku.spd.item;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class RumorItem extends Item {
    public RumorItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(style ->
                style.withColor(TextColor.fromRgb(GoldNameWave.colorAt(Util.getMillis()))));
    }
}
