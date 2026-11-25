package com.typeomega.lib.tooltip.structure;

import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.predicates.PredicateBlocks;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import com.typeomega.lib.tooltip.StructureInfo;
import com.typeomega.lib.tooltip.StructureInfo.Range;
import com.typeomega.lib.tooltip.StructureTexts;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Reads a GTCEu multiblock pattern into a {@link StructureInfo} for the tooltip structure section (D-430), so
 * dimensions, block counts and hatch limits come from the pattern the game checks. Results are cached per machine.
 * Type Omega does not show the section (D-431) and uses this only for the "Supports" line and the modes.
 */
public final class GtceuPatterns {

    // EN: GTCEu multi-amp energy hatches are registered as energy_input_hatch_4a, _16a and so on; wireless
    // EN: hatches of the addons have "wireless" in their registry name.
    // PT: As escotilhas de energia multi-amp do GTCEu se registram como energy_input_hatch_4a, _16a etc.; as
    // PT: escotilhas sem fio dos addons têm "wireless" no nome de registro.
    // ZH: GTCEu 的多安能源仓注册名为 energy_input_hatch_4a、_16a 等；附属模组的无线仓注册名包含 "wireless"。
    private static final Pattern MULTI_AMP = Pattern.compile(".*_(4|16|64)a$");

    private static final Map<String, StructureInfo> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, PartAbility> ABILITIES = new LinkedHashMap<>();
    private static final Map<String, Component> ABILITY_NAMES = new ConcurrentHashMap<>();
    private static final Field BLOCK_MATCHES;

    static {
        try {
            BLOCK_MATCHES = BlockPattern.class.getDeclaredField("blockMatches");
            BLOCK_MATCHES.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("GTCEu BlockPattern has no blockMatches field", e);
        }
        registerAbilities(PartAbility.class);
    }

    private GtceuPatterns() {}

    /** Adds the {@code public static PartAbility} fields of a mod class, so its hatches get ability names. */
    public static synchronized void registerAbilities(Class<?> holder) {
        for (Field field : holder.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != PartAbility.class) continue;
            try {
                PartAbility ability = (PartAbility) field.get(null);
                ABILITIES.putIfAbsent(ability.getName(), ability);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /** Names a mod's own part ability in the structure section; without it the first hatch's name is used. */
    public static void nameAbility(String ability, Component name) {
        ABILITY_NAMES.put(ability, name);
    }

    /** The recipe types of a machine as translated names, for {@code modes(...)}. */
    public static List<Component> modes(MultiblockMachineDefinition definition) {
        List<Component> out = new ArrayList<>();
        for (GTRecipeType type : definition.getRecipeTypes()) {
            if (type != null) out.add(Component.translatable(type.registryName.toLanguageKey()));
        }
        return out;
    }

    /** The structure of a machine, read once and cached. */
    public static StructureInfo of(MultiblockMachineDefinition definition) {
        return CACHE.computeIfAbsent(definition.getId().toString(), id -> read(definition.getPatternFactory().get()));
    }

    public static StructureInfo read(BlockPattern pattern) {
        TraceabilityPredicate[][][] matches;
        try {
            matches = (TraceabilityPredicate[][][]) BLOCK_MATCHES.get(pattern);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        RelativeDirection[] dirs = pattern.structureDir;
        int aisles = matches.length;
        int rows = aisles == 0 ? 0 : matches[0].length;
        int chars = rows == 0 ? 0 : matches[0][0].length;
        int[][] reps = pattern.aisleRepetitions;
        int depthMin = 0, depthMax = 0;
        for (int[] rep : reps) {
            depthMin += rep[0];
            depthMax += rep[1];
        }
        // index 0 = chars, 1 = rows, 2 = aisles, as in FactoryBlockPattern
        Range[] sizes = { Range.exactly(chars), Range.exactly(rows), new Range(depthMin, depthMax) };
        Range width = null, height = null, depth = null;
        for (int i = 0; i < 3; i++) {
            switch (dirs[i]) {
                case LEFT, RIGHT -> width = sizes[i];
                case UP, DOWN -> height = sizes[i];
                default -> depth = sizes[i];
            }
        }

        Map<TraceabilityPredicate, int[]> positions = new IdentityHashMap<>();
        int[] controller = null;
        for (int a = 0; a < aisles; a++) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < chars; c++) {
                    TraceabilityPredicate p = matches[a][r][c];
                    if (p == null) continue;
                    if (p.isController) {
                        controller = new int[] { c, r, a };
                        continue;
                    }
                    int[] count = positions.computeIfAbsent(p, k -> new int[2]);
                    count[0] += reps[a][0];
                    count[1] += reps[a][1];
                }
            }
        }

        boolean[] flags = { false, false };
        List<PatternAnalyzer.Slot> slots = new ArrayList<>();
        for (var entry : positions.entrySet()) {
            TraceabilityPredicate p = entry.getKey();
            if (p.isAir() || p.isAny()) continue;
            List<PatternAnalyzer.Option> options = new ArrayList<>();
            for (SimplePredicate simple : p.common) addOption(options, simple, false, flags);
            for (SimplePredicate simple : p.limited) addOption(options, simple, true, flags);
            options = mergeBlocks(options);
            if (!options.isEmpty())
                slots.add(new PatternAnalyzer.Slot(entry.getValue()[0], entry.getValue()[1], options));
        }
        Component where = controller == null ? null :
                position(controller, dirs, new int[] { chars, rows, aisles });
        return PatternAnalyzer.analyze(width, height, depth, where, slots, flags[0], flags[1]);
    }

    private static void addOption(List<PatternAnalyzer.Option> options, SimplePredicate simple, boolean limited,
                                  boolean[] flags) {
        if (simple == SimplePredicate.AIR || simple == SimplePredicate.ANY) return;
        List<Block> blocks = candidates(simple);
        int min = limited ? simple.minCount : -1;
        int max = limited ? simple.maxCount : -1;
        if (blocks.isEmpty()) {
            options.add(PatternAnalyzer.Option.block(simple, StructureTexts.SPECIAL_BLOCK.component(), min, max,
                    false));
            return;
        }
        if (blocks.get(0) instanceof IMachineBlock) {
            String ability = abilityOf(blocks);
            for (Block block : blocks) {
                var id = ForgeRegistries.BLOCKS.getKey(block);
                if (id != null && MULTI_AMP.matcher(id.getPath()).matches()) flags[0] = true;
                if (id != null && id.getPath().contains("wireless")) flags[1] = true;
            }
            Component name = ability == null ? null : ABILITY_NAMES.get(ability);
            if (name == null && ability != null && StructureTexts.partName(ability) != null) {
                name = StructureTexts.partName(ability).component();
            }
            if (name == null) name = nameOf(blocks.get(0));
            options.add(PatternAnalyzer.Option.part(ability != null ? ability : blocks.get(0), name, min, max,
                    ability));
            return;
        }
        Object key = blocks.size() == 1 ? blocks.get(0) : List.copyOf(blocks);
        options.add(PatternAnalyzer.Option.block(key, nameOf(blocks.get(0)), min, max, blocks.size() > 1));
    }

    /**
     * Several block predicates OR-ed in one position ({@code blocks(A).or(blocks(B))}) are one choice of the
     * player, so they count as one entry "A or B" instead of each claiming every position.
     */
    private static List<PatternAnalyzer.Option> mergeBlocks(List<PatternAnalyzer.Option> options) {
        List<PatternAnalyzer.Option> blocks = new ArrayList<>();
        List<PatternAnalyzer.Option> out = new ArrayList<>();
        for (PatternAnalyzer.Option option : options) (option.part() ? out : blocks).add(option);
        if (blocks.size() < 2) return options;
        List<Object> keys = new ArrayList<>();
        List<Component> names = new ArrayList<>();
        int min = -1;
        int max = -1;
        for (PatternAnalyzer.Option block : blocks) {
            if (!keys.contains(block.key())) {
                keys.add(block.key());
                names.add(block.name());
            }
            if (block.min() >= 0) min = Math.max(min, 0) + block.min();
            if (block.max() >= 0) max = Math.max(max, 0) + block.max();
        }
        out.add(0, PatternAnalyzer.Option.block(keys, StructureTexts.orList(names), min, max, false));
        return out;
    }

    /** The name the player sees: the item name, which carries the material of frames and other material blocks. */
    private static Component nameOf(Block block) {
        Item item = block.asItem();
        return item == Items.AIR ? block.getName() : new ItemStack(item).getHoverName();
    }

    private static List<Block> candidates(SimplePredicate simple) {
        Set<Block> out = new LinkedHashSet<>();
        if (simple instanceof PredicateBlocks blocks && blocks.blocks != null) {
            for (Block block : blocks.blocks) out.add(block);
        } else if (simple.candidates != null) {
            BlockInfo[] infos = simple.candidates.get();
            if (infos != null) {
                for (BlockInfo info : infos) {
                    if (info != null && info.getBlockState() != null && !info.getBlockState().isAir()) {
                        out.add(info.getBlockState().getBlock());
                    }
                }
            }
        }
        return new ArrayList<>(out);
    }

    /**
     * The ability whose blocks best cover the candidates: the one sharing most blocks with them, so a predicate made
     * of input buses and input hatches is named after the larger group.
     */
    private static synchronized String abilityOf(Collection<Block> blocks) {
        String best = null;
        int bestShared = 0;
        for (var entry : ABILITIES.entrySet()) {
            int shared = 0;
            for (Block block : entry.getValue().getAllBlocks()) if (blocks.contains(block)) shared++;
            if (shared > bestShared) {
                best = entry.getKey();
                bestShared = shared;
            }
        }
        return best;
    }

    /** "Front, bottom, center" from the controller cell and the pattern directions. */
    static Component position(int[] cell, RelativeDirection[] dirs, int[] sizes) {
        Component lateral = null, vertical = null, depthText = null;
        for (int i = 0; i < 3; i++) {
            int n = sizes[i];
            int idx = cell[i];
            switch (dirs[i]) {
                // EN: LEFT means the index grows toward the controller's left, which is the viewer's right.
                // PT: LEFT quer dizer que o índice cresce para a esquerda do controlador, a direita de quem olha.
                // ZH: LEFT 表示索引朝控制器左侧增长，即观察者的右侧。
                case LEFT, RIGHT -> {
                    int fromLeft = dirs[i] == RelativeDirection.LEFT ? idx : n - 1 - idx;
                    lateral = 2 * fromLeft == n - 1 ? StructureTexts.CENTER.component() :
                            StructureTexts.COLUMN_N.component(fromLeft + 1);
                }
                case UP, DOWN -> {
                    int fromBottom = dirs[i] == RelativeDirection.UP ? idx : n - 1 - idx;
                    if (fromBottom == 0) vertical = StructureTexts.BOTTOM.component();
                    else if (fromBottom == n - 1) vertical = StructureTexts.TOP.component();
                    else if (2 * fromBottom == n - 1) vertical = StructureTexts.MIDDLE.component();
                    else vertical = StructureTexts.HEIGHT_N.component(fromBottom + 1);
                }
                default -> {
                    int fromFront = dirs[i] == RelativeDirection.FRONT ? n - 1 - idx : idx;
                    if (fromFront == 0) depthText = StructureTexts.FRONT.component();
                    else if (fromFront == n - 1) depthText = StructureTexts.BACK.component();
                    else depthText = StructureTexts.DEPTH_N.component(fromFront + 1);
                }
            }
        }
        return StructureTexts.POSITION.component(depthText, vertical, lateral);
    }
}
