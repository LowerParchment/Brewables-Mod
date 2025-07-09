// Import user defined dependencies
package com.LowerParchment.brewables.item;
import com.LowerParchment.brewables.entity.ThrownWitchsWartEntity;

// Import Minecraft and Java dependencies
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

// Represents the throwable splash version of Witch's Wart.
// Behaves like a splash potion with randomized negative effects when thrown.
public class SplashWitchsWartItem extends Item
{
    // Constructor for splash Witch's Wart item.
    // Allows it to be registered via lambda and enforces 16 max stack size for player convenience.
    public SplashWitchsWartItem()
    {
        super(new Item.Properties().stacksTo(16));
    }

    // Defines the localized name to appear in-game
    @Override
    public Component getName(ItemStack stack)
    {
        return Component.translatable("item.brewables.splash_witchs_wart");
    }

    // Adds tooltip lore for the item when hovered over in inventory or hotbar
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag)
    {
        tooltip.add(Component.translatable("item.brewables.witchs_wart.tooltip")
            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }

    // Called when the player right-clicks with the item
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand)
    {
        ItemStack stack = user.getItemInHand(hand);

        // Play a muted throwing sound on the client side only
        if (level.isClientSide)
        {
            user.playSound(
                    SoundEvents.SPLASH_POTION_THROW,
                    1.0F,
                    0.5F + level.random.nextFloat() * 0.1F
            );
        }

        // Spawn the thrown entity server-side only
        if (!level.isClientSide)
        {
            ThrownWitchsWartEntity projectile = new ThrownWitchsWartEntity(level, user);
            projectile.setItem(stack);
            projectile.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 0.6F, 1.0F);
            level.addFreshEntity(projectile);
        }

        // Consume the item unless the player is in Creative mode
        if (!user.getAbilities().instabuild)
        {
            stack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
