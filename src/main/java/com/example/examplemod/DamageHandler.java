package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.Random;

import static java.lang.String.format;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class DamageHandler {

    private static final Random random = new Random();

    private static final Map<Integer, String> injuryMessages = Map.of(
            1, "You feel a slight pain and slow slightly",
            2, "You begin to limp. Pain is setting in",
            3, "Your injury is getting worse. Moving and acting is harder",
            4, "You're seriously hurt. You struggle to stay upright",
            5, "You collapse from the pain. You can barely move"
    );

    private static final String bleedMessage = ", and have started bleeding";

    private static final String injuryKey = "hns_injury";
    private static final String bleedingKey = "hns_bleeding";

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level.isClientSide) return;

        if (event.getSource() == BleedingSource.BLEEDING) return;

        CompoundTag data = player.getPersistentData();
        boolean changed = false;
        boolean startedBleeding = false;

        if (random.nextInt(3) == 0) {
            int injuryIndex = data.getInt(injuryKey);

            if (injuryIndex >= 5) return;

            data.putInt(injuryKey, ++injuryIndex);

            if (random.nextInt(3) == 0) {
                boolean isBleeding = data.getBoolean(bleedingKey);

                if (!isBleeding) {
                    data.putBoolean(bleedingKey, true);
                    startedBleeding = true;
                }
            }

            changed = true;
        }

        if (changed) {
            String message = startedBleeding ? injuryMessages.get(data.getInt(injuryKey)) + bleedMessage : injuryMessages.get(data.getInt(injuryKey));
            player.displayClientMessage(Component.nullToEmpty(message), true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;

        CompoundTag data = player.getPersistentData();

        int injuryIndex = data.getInt(injuryKey);
        boolean isBleeding = data.getBoolean(bleedingKey);

        if (injuryIndex > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1, --injuryIndex, true, false));

            if (isBleeding) {
                int randomVal = random.nextInt(50);

                if (randomVal <= injuryIndex * 5) {
                    player.hurt(BleedingSource.BLEEDING, ((float) injuryIndex * 0.5F));
                }
            }
        }
    }

    public class BleedingSource {
        public static final DamageSource BLEEDING = new DamageSource("bleeding").bypassArmor();
    }
}
