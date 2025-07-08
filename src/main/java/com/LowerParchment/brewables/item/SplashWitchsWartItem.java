package com.LowerParchment.brewables.item;

import com.LowerParchment.brewables.entity.ThrownWitchsWartEntity;
import com.LowerParchment.brewables.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class SplashWitchsWartItem extends Item
{
    public SplashWitchsWartItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack)
    {
        return Component.translatable("item.brewables.splash_witchs_wart");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag)
    {
        tooltip.add(Component.translatable("item.brewables.witchs_wart.tooltip")
            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide)
        {
            ThrownWitchsWartEntity entity = new ThrownWitchsWartEntity(ModEntities.THROWN_WITCHS_WART_ENTITY.get(), level);
            entity.setOwner(player);
            entity.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            entity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 0.5F, 1.0F);
            level.addFreshEntity(entity);
        }

        if (!player.getAbilities().instabuild)
        {
            itemstack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
