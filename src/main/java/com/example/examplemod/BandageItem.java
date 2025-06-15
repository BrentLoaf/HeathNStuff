package com.example.examplemod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BandageItem extends Item {

    public BandageItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide) {
            CompoundTag data = player.getPersistentData();

            if (data.getBoolean("is_injured")) {
                data.remove("is_injured");

                ItemStack stack = player.getItemInHand(hand);
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
