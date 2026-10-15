package me.lyse.playtimetab.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Uses the vanilla "list" scoreboard slot: the client draws it right-aligned, next to the ping.
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Unique
    private int playtimetab$shownHours = -1;

    @Inject(method = "tick", at = @At("TAIL"))
    private void playtimetab$refresh(CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (playtimetab$shownHours >= 0 && self.tickCount % 1200 != 0) return;
        // PLAY_TIME is in ticks: 20 * 3600 per hour.
        int hours = self.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) / 72000;
        if (hours == playtimetab$shownHours) return;
        playtimetab$shownHours = hours;

        Scoreboard board = self.level().getServer().getScoreboard();
        Objective objective = board.getObjective("playtime");
        if (objective == null) {
            objective = board.addObjective("playtime", ObjectiveCriteria.DUMMY, Component.literal("Playtime"),
                ObjectiveCriteria.RenderType.INTEGER, true, null);
            board.setDisplayObjective(DisplaySlot.LIST, objective);
        }
        ScoreAccess score = board.getOrCreatePlayerScore(self, objective);
        score.set(hours);
        score.numberFormatOverride(new FixedFormat(Component.literal(hours + "h").withStyle(ChatFormatting.GRAY)));
    }
}
