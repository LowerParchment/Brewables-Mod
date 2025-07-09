// Import user defined dependencies
package com.LowerParchment.brewables.item;
import com.LowerParchment.brewables.entity.ThrownLingeringWitchsWartEntity;

// Import Minecraft, Forge, and Java dependencies
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import java.util.List;


public class LingeringWitchsWartItem extends Item
{
    // Constructor for the Lingering Witch's Wart item
    public LingeringWitchsWartItem()
    {
        super(new Item.Properties().stacksTo(16));
    }

    // Get the name of the Lingering Witch's Wart item for display in the inventory
    @Override
    public Component getName(ItemStack stack)
    {
        return Component.translatable("item.brewables.lingering_witchs_wart");
    }

    // Adds tooltip text for the Lingering Witch's Wart item when hovered in inventory
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag)
    {
        tooltip.add(Component.translatable("item.brewables.witchs_wart.tooltip")
            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }

    // Called when the player right-clicks with this item in hand
    // - Plays a throw sound (client)
    // - Spawns a ThrownLingeringWitchsWartEntity (server)
    // - Shrinks stack unless in creative
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand)
    {
        ItemStack stack = user.getItemInHand(hand);
        if (level.isClientSide)
        {
            // Slight pitch randomization to mimic vanilla splash potion behavior
            user.playSound(
                    SoundEvents.SPLASH_POTION_THROW,
                    1.0F,
                    0.5F + level.random.nextFloat() * 0.1F
            );
        }
        if (!level.isClientSide)
        {
            ThrownLingeringWitchsWartEntity projectile = new ThrownLingeringWitchsWartEntity(level, user);
            projectile.setItem(stack);
            projectile.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 0.6F, 1.0F);
            level.addFreshEntity(projectile);
        }
        if (!user.getAbilities().instabuild)    // Player is in creative mode
        {
            stack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
