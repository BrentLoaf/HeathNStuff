package com.example.examplemod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Map;

public class BandageItem extends Item {

    public BandageItem(Properties p_41383_) {
        super(p_41383_);
    }

    private static final Map<Integer, String> healMessages = Map.of(
            0, "You feel fully healed",
            1, "Your limp stops but you're still in pain",
            2, "The ease of pain makes it easier to move",
            3, "Staying up right is easier now, but you struggle to move still",
            4, "You can move much easier than before, but the pain still aches"
    );

    private static final String stoppedBleeding = ", and the bleeding has stopped";

    private static final String injuryKey = "hns_injury";
    private static final String bleedingKey = "hns_bleeding";

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide) {
            CompoundTag data = player.getPersistentData();

            int injuryIndex = data.getInt(injuryKey);
            boolean isBleeding = data.getBoolean(bleedingKey);

            boolean usedBandage = false;

            String message = "You have no reason to use this bandage";

            if (injuryIndex > 0) {
                usedBandage = true;

                data.putInt(injuryKey, --injuryIndex);
                if (isBleeding) data.remove(bleedingKey);

                message = healMessages.get(data.getInt(injuryKey)) + (isBleeding ? stoppedBleeding : "");
            } else if (isBleeding) {
                usedBandage = true;

                data.remove(bleedingKey);
                message = "The bleeding has stopped";
            }

            if (usedBandage) {
                ItemStack stack = player.getItemInHand(hand);
                stack.shrink(1);
            }

            player.displayClientMessage(Component.nullToEmpty(message), true);
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
