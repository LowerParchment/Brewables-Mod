package com.LowerParchment.brewables.item;
import com.LowerParchment.brewables.BrewablesMod;
import com.LowerParchment.brewables.item.StirringRodItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
//import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.entity.player.Player;

public class StirringRodItem extends Item {
    // public static final RegistryObject<Item> STIRRING_ROD = com.LowerParchment.brewables.BrewablesMod.ITEMS.register(
    //     "stirring_rod", () -> new StirringRodItem(new Item.Properties()));

    public StirringRodItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK; // or UseAnim.BLOCK, UseAnim.BOW, etc.
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32; // duration in ticks (32 = ~1.5 seconds)
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(item);
    }
}