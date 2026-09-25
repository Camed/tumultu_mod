package com.tumultu.core.event;

import com.tumultu.affix.AffixData;
import com.tumultu.affix.AffixDefinition;
import com.tumultu.affix.RolledAffix;
import com.tumultu.affix.effect.AreaMiningEffect;
import com.tumultu.registry.TumultuDataComponents;
import com.tumultu.registry.TumultuRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies {@link AreaMiningEffect} affixes: breaking a block with a tool that has one
 * also breaks the surrounding blocks on the plane matching the face actually being mined
 * covering a width x height rectangle (see {@link AreaMiningEffect#sizeFor}) roughly
 * centered on the targeted block. Sneaking suppresses the effect entirely, breaking
 * only the targeted block. Extra blocks are only broken when the player could
 * actually mine them normally.
 */
public class AreaMiningEventHandler {
    private static final ThreadLocal<Boolean> EXPANDING = ThreadLocal.withInitial(() -> false);
    private static final double FACE_RAYCAST_RANGE = 8.0;

    record MiningSize(int width, int height) {
        int area() {
            return width * height;
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        if (EXPANDING.get()) return;
        if (event.isCanceled()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (player.isShiftKeyDown()) return;

        ItemStack tool = player.getMainHandItem();
        AffixData data = tool.get(TumultuDataComponents.AFFIX_DATA.get());
        if (data == null || data.affixes().isEmpty()) return;

        MiningSize size = findAreaMiningSize(data, player.registryAccess().lookupOrThrow(TumultuRegistries.AFFIX_KEY));
        if (size == null) return;

        BlockPos center = event.getPos();
        Level level = player.level();
        Direction.Axis axis = facingAxis(player, center);

        EXPANDING.set(true);
        try {
            for (BlockPos pos : offsetsInPlane(center, axis, size.width(), size.height())) {
                if (pos.equals(center)) continue;
                if (!canMine(level, pos, player)) continue;
                player.gameMode.destroyBlock(pos);
            }
        } finally {
            EXPANDING.set(false);
        }
    }
    private static Direction.Axis facingAxis(ServerPlayer player, BlockPos center) {
        HitResult hit = player.pick(FACE_RAYCAST_RANGE, 1.0F, false);
        if (hit instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(center)) {
            return blockHit.getDirection().getAxis();
        }
        return player.getDirection().getAxis();
    }

    private static boolean canMine(Level level, BlockPos pos, ServerPlayer player) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;
        if (state.getDestroySpeed(level, pos) < 0) return false;
        return player.isCreative() || state.canHarvestBlock(level, pos, player);
    }

    static MiningSize findAreaMiningSize(AffixData data, Registry<AffixDefinition> registry) {
        MiningSize best = null;
        for (RolledAffix rolled : data.affixes()) {
            AffixDefinition def = registry.getValue(rolled.affixId());
            if (def != null && def.effect() instanceof AreaMiningEffect) {
                int[] dims = AreaMiningEffect.sizeFor(rolled.rolledValue());
                MiningSize candidate = new MiningSize(dims[0], dims[1]);
                if (best == null || candidate.area() > best.area()) {
                    best = candidate;
                }
            }
        }
        return best;
    }

    static Iterable<BlockPos> offsetsInPlane(BlockPos center, Direction.Axis facingAxis, int width, int height) {
        List<BlockPos> positions = new ArrayList<>();
        int hStart = -((width - 1) / 2);
        int vStart = -((height - 1) / 2);
        for (int hi = 0; hi < width; hi++) {
            int h = hStart + hi;
            for (int vi = 0; vi < height; vi++) {
                int v = vStart + vi;
                positions.add(switch (facingAxis) {
                    case X -> center.offset(0, v, h);
                    case Z -> center.offset(h, v, 0);
                    case Y -> center.offset(h, 0, v);
                });
            }
        }
        return positions;
    }
}