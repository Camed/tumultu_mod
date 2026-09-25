package com.tumultu.core.event;

import com.tumultu.affix.AffixData;
import com.tumultu.affix.AffixDefinition;
import com.tumultu.affix.AffixType;
import com.tumultu.affix.ItemRarity;
import com.tumultu.affix.RolledAffix;
import com.tumultu.affix.effect.AffixEffect;
import com.tumultu.affix.effect.AreaMiningEffect;
import com.tumultu.core.event.AreaMiningEventHandler.MiningSize;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AreaMiningEventHandlerTest {

    private record NotAreaMining() implements AffixEffect {
        @Override
        public String typeId() {
            return "not_area_mining";
        }

        @Override
        public Component describe(double rolledValue) {
            return Component.empty();
        }

        @Override
        public boolean isPercentage() {
            return false;
        }
    }

    @Mock
    private Registry<AffixDefinition> registry;

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("tumultu", path);
    }

    @Test
    void findAreaMiningSizeReturnsNullWhenNoAffixesRolled() {
        assertNull(AreaMiningEventHandler.findAreaMiningSize(AffixData.EMPTY, registry));
    }

    @Test
    void findAreaMiningSizeIgnoresNonAreaMiningAffixes() {
        Identifier otherId = id("something_else");
        when(registry.getValue(otherId)).thenReturn(
                new AffixDefinition(AffixType.PREFIX, "Other", new NotAreaMining(), List.of(), 100, "g", List.of()));
        AffixData data = new AffixData(ItemRarity.MAGIC, List.of(new RolledAffix(otherId, 0, 5.0)), false);

        assertNull(AreaMiningEventHandler.findAreaMiningSize(data, registry));
    }

    @Test
    void findAreaMiningSizeDecodesTheRolledTierIntoDimensions() {
        Identifier excavationId = id("excavation");
        when(registry.getValue(excavationId)).thenReturn(
                new AffixDefinition(AffixType.PREFIX, "Excavating", new AreaMiningEffect(false), List.of(), 100, "g", List.of()));
        AffixData data = new AffixData(ItemRarity.MAGIC, List.of(new RolledAffix(excavationId, 0, 3.0)), false);

        MiningSize size = AreaMiningEventHandler.findAreaMiningSize(data, registry);

        assertEquals(new MiningSize(3, 3), size);
    }

    @Test
    void findAreaMiningSizeTakesTheLargestAreaAmongMultipleMatches() {
        Identifier smallId = id("small_excavation");
        Identifier bigId = id("big_excavation");
        when(registry.getValue(smallId)).thenReturn(
                new AffixDefinition(AffixType.PREFIX, "Small", new AreaMiningEffect(false), List.of(), 100, "g1", List.of()));
        when(registry.getValue(bigId)).thenReturn(
                new AffixDefinition(AffixType.SUFFIX, "Big", new AreaMiningEffect(false), List.of(), 100, "g2", List.of()));
        AffixData data = new AffixData(ItemRarity.RARE, List.of(
                new RolledAffix(smallId, 0, 1.0),  // {2, 2} = area 4
                new RolledAffix(bigId, 0, 5.0)     // {4, 4} = area 16
        ), false);

        MiningSize size = AreaMiningEventHandler.findAreaMiningSize(data, registry);

        assertEquals(new MiningSize(4, 4), size);
    }

    @Test
    void offsetsInPlaneForOddDimensionsIsCenteredOnTheTarget() {
        BlockPos center = new BlockPos(10, 64, 10);

        List<BlockPos> positions = toList(AreaMiningEventHandler.offsetsInPlane(center, Direction.Axis.Z, 3, 3));

        assertEquals(9, positions.size());
        assertTrue(positions.contains(center));
        assertTrue(positions.contains(center.offset(-1, -1, 0)));
        assertTrue(positions.contains(center.offset(1, 1, 0)));
        assertTrue(positions.stream().allMatch(p -> p.getZ() == center.getZ()));
    }

    @Test
    void offsetsInPlaneForEvenDimensionPutsTheExtraBlockOnThePositiveSide() {
        BlockPos center = new BlockPos(0, 0, 0);

        List<BlockPos> positions = toList(AreaMiningEventHandler.offsetsInPlane(center, Direction.Axis.Z, 2, 2));

        assertEquals(4, positions.size());
        assertTrue(positions.contains(center));
        assertTrue(positions.contains(center.offset(1, 0, 0)));
        assertTrue(positions.contains(center.offset(0, 1, 0)));
        assertTrue(positions.contains(center.offset(1, 1, 0)));
        assertFalse(positions.stream().anyMatch(p -> p.getX() < center.getX() || p.getY() < center.getY()));
    }

    @Test
    void offsetsInPlaneSupportsRectangularNonSquareSizes() {
        BlockPos center = new BlockPos(0, 0, 0);

        List<BlockPos> positions = toList(AreaMiningEventHandler.offsetsInPlane(center, Direction.Axis.Z, 4, 3));

        assertEquals(12, positions.size());
    }

    @Test
    void offsetsInPlaneKeepsTheXAxisFixedWhenFacingAlongX() {
        BlockPos center = new BlockPos(0, 0, 0);

        List<BlockPos> positions = toList(AreaMiningEventHandler.offsetsInPlane(center, Direction.Axis.X, 3, 3));

        assertFalse(positions.stream().anyMatch(p -> p.getX() != center.getX()));
    }

    private static List<BlockPos> toList(Iterable<BlockPos> offsets) {
        List<BlockPos> list = new ArrayList<>();
        offsets.forEach(list::add);
        return list;
    }
}
