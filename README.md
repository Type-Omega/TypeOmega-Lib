<p align="center">
  <img src="docs/assets/logo.png" width="340" alt="TypeOmega Lib logo" />
</p>

# TypeOmega Lib

Shared library of the [Type Omega](https://github.com/Type-Omega) mods (TypeOmegaCore, GTNA, Agronomica and future addons). Minecraft 1.20.1, Forge 47, GTCEu 7.5.3.

> **Status: 0.6.0.** The API may still change before 1.0.

Community: [Discord](https://discord.gg/TW5vugknk5) · questions, bug reports and playtesting.

## What it has

| Package | What it does |
| --- | --- |
| `lang` | `Tr.of(key, en, pt, zh)` declares a text in English, Portuguese and Chinese at once. `TrLanguageProvider` writes the three lang files in datagen, so a text can't be missing in one language |
| `tooltip` | `MultiblockTooltip.builder()`: GTNH-style multiblock tooltip (Machine Type, coloured lines, separators, table rows, modes, pollution, the "Supports ...!" line, mod name). Its structure section (dimensions, controller, block counts, hatches with count and place, blueprint channels) is always shown; `GtceuPatterns.of(definition)` reads it from the GTCEu pattern, so the counts come from the pattern. `MultiblockTooltip.section()` appends it to an existing tooltip. `MultiblockTooltip.of(...)` keeps the older GTNA layout |
| `data` | `Int128` (128-bit counts, from GTLCore via GTNA), `BlockPosLongSet` and `BlockPosHashing` (fast sets and maps of positions for huge structures) |
| `module` | `ModuleRegistry`: multiblock modules (extra structures that unlock abilities when they match), from Java or KubeJS. GTNA checks them on every multiblock; its `GTNASubPatterns` forwards here |
| `perf` | `/typeomegalib bench grid <machine> <count> [input]` places a force-loaded grid of GT machines (kept running when an input item is given) and `/typeomegalib bench mspt <ticks>` logs the average tick time; used by the modpack's `tools/perf_bench.py` |
| `ui` | Illustrated recipe screens in the GTNH style (D-435): `PanelLayout` places slots, tanks and marks over a pixel-art picture of the process; `IllustratedPanel` draws it on the GTCEu bevelled grey with the text lines under it (`InfoLines`: Total, Usage with the tier colour, Time, then the recipe type's own lines). `IllustratedRecipeUI` is a `GTRecipeTypeUI` for `setRecipeUI`; `IllustratedRecipeUI.rtuiLines` gives the same lines to a recipe type whose screen is a `.rtui`; `DisplaySlots` makes read-only slots for an EMI page of its own (LDLib `ModularEmiRecipe`); `IllustratedTab` puts the same panel on a machine GUI. Widgets: `ParticleWidget`, `ArrowWidget`, `ProgressBarWidget`, `InfoLinesWidget` |
| `ui.hud` | Shared moveable HUD overlays (`MoveableHud`, `HudRegistry`, `HudEditorScreen`, `HudPositionStore`): any mod registers HUDs by stable id, positions persist as 0..100% anchors in `config/typeomegalib-huds.json`, and one shared keybind (`H` by default, under `TypeOmega Lib`) opens the drag editor |
| `test` | GameTest kit: `Previews.place` builds a multiblock from its preview; `Sweeps` checks every multiblock, GT recipe, machine tooltip and translation of a mod; `GameTestFilter` (`-Dtypeomega.tests=...`) filters tests by method, class or batch and skips `TestBatches.HEAVY` (`@HeavyTest`) by default; `-Dtypeomegalib.junitReport=<file>` writes JUnit XML for CI |

Planned: registration helpers, batched field sync, recipe helpers and measured performance work. See the [developer docs](https://type-omega.github.io/TypeOmega-Dev-Doc/).

## Use it

```groovy
repositories {
    maven { url = 'https://type-omega.github.io/maven/' }
}
dependencies {
    modImplementation("com.typeomega:typeomega-lib:0.7.0") { transitive = false }
}
```

Releases published before the rename use the legacy coordinates `com.gtinfinityascension:gtialib` and stay available on the same Maven.

```java
Tr name = Tr.of("mymod.thing", "Thing", "Coisa", "东西");

Component[] tooltip = MultiblockTooltip.of("mymod", "big_furnace")
        .fact("Blast furnaces are older than steel", "Altos-fornos são mais antigos que o aço", "高炉比钢更古老")
        .main("Smelts 16 ores at once", "Funde 16 minérios de uma vez", "一次熔炼16个矿石")
        .requirement("Needs a Maintenance Hatch", "Precisa de um Maintenance Hatch", "需要维护仓")
        .build();

MultiblockTooltipBuilder b = MultiblockTooltip.builder().machineType(MACERATOR).line(PURPOSE);
StructureInfo structure = GtceuPatterns.of(MY_MACHINE);
List<Component> lines = b.modes(GtceuPatterns.modes(MY_MACHINE)).separator()
        .structure(structure).hatches(structure).mod("My Mod").build();

@GameTest(template = "empty_16", timeoutTicks = 6000)
public static void everyMultiblockForms(GameTestHelper h) {
    Sweeps.multiblocksFormFromPreview(h, "mymod", 40).assertClean(h, 1);
    h.succeed();
}
```

In datagen: `TrLanguageProvider.addAll(event, "mymod", "mymod.", true)` (pass `false` when Registrate already writes your `en_us`, and call `TrRegistrate.feedEnglish(REGISTRATE, "mymod.")`).

## Filtering GameTests

Any mod depending on `typeomegalib` gets GameTest filtering automatically via `-Dtypeomega.tests=<terms>` (comma-separated, case-insensitive substring match against method name, simple class name or batch).

Mark slow tests with `batch = TestBatches.HEAVY` (or `@HeavyTest` on a method/class). Heavy tests are excluded by default and only run when `typeomega.tests` includes `heavy` or `typeomega.tests=all`.

To pass `-Ptests=...` from Gradle, add this line to your `gameTestServer` run block in ModDevGradle (`net.neoforged.moddev.legacyforge`):

```groovy
runs {
    gameTestServer {
        systemProperty('typeomega.tests', project.findProperty('tests') ?: '')
    }
}
```

Then run:

```sh
./gradlew runGameTestServer -Ptests=nanoforge
./gradlew runGameTestServer -Ptests=nanoforge,tooltip
./gradlew runGameTestServer -Ptests=heavy
./gradlew runGameTestServer -Ptests=all
```

## Building

```sh
./gradlew build runGameTestServer
```

## License

[LGPL-3.0](LICENSE.MD). Written from scratch; no code from decompiled sources.
