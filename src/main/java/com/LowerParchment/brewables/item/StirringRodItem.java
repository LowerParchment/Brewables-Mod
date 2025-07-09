// Import user defined dependencies
package com.LowerParchment.brewables.item;
import com.LowerParchment.brewables.item.StirringRodItem;

// Import Minecraft dependencies
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;

// The Stirring Rod is used to interact with a cauldron to finalize a brew.
// It does not consume durability or resources, but triggers an animation and brewing logic.
public class StirringRodItem extends Item
{
    // Constructor for Stirring Rod.
    // The item is configured via the passed-in Properties during registration.
    public StirringRodItem(Properties properties)
    {
        super(properties);
    }

    // Override to define the use animation of a block
    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.BLOCK;
    }

    // Override to define how long the use animation lasts
    @Override
    public int getUseDuration(ItemStack stack)
    {
        return 32; // ~1.6 seconds
    }

    // Called when the player right-clicks to begin using the rod
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack item = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(item);       // Singles ongoing use (not instant)
    }
}