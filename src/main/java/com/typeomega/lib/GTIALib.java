package com.typeomega.lib;

import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.JUnitLikeTestReporter;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.typeomega.lib.lang.TrLanguageProvider;
import com.typeomega.lib.module.ModuleRegistry;
import com.typeomega.lib.perf.BenchCommand;
import com.typeomega.lib.tooltip.MultiblockTooltip;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(GTIALib.MOD_ID)
@SuppressWarnings("removal")
public final class GTIALib {

    public static final String MOD_ID = "typeomegalib";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    /** {@code -Dtypeomegalib.junitReport=<file>} writes GameTest results as JUnit XML, for CI. */
    public static final String JUNIT_PROPERTY = "typeomegalib.junitReport";

    public GTIALib() {
        MultiblockTooltip.init();
        com.typeomega.lib.ui.UiTexts.init();
        ModuleRegistry.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(GTIALib::gatherData);
        MinecraftForge.EVENT_BUS.addListener(GTIALib::serverStarting);
        MinecraftForge.EVENT_BUS.addListener(BenchCommand::register);
        MinecraftForge.EVENT_BUS.addListener(BenchCommand::onServerTick);
    }

    private static void gatherData(GatherDataEvent event) {
        TrLanguageProvider.addAll(event, MOD_ID, MOD_ID + ".", true);
    }

    private static void serverStarting(ServerStartingEvent event) {
        String report = System.getProperty(JUNIT_PROPERTY);
        if (report == null || report.isBlank()) return;
        try {
            File file = new File(report).getAbsoluteFile();
            file.getParentFile().mkdirs();
            GlobalTestReporter.replaceWith(new JUnitLikeTestReporter(file));
            LOGGER.info("GameTest JUnit report: {}", file);
        } catch (Exception e) {
            LOGGER.error("Could not start the GameTest JUnit report", e);
        }
    }
}
