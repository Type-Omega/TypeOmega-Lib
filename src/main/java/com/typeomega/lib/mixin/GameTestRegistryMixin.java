package com.typeomega.lib.mixin;

import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraftforge.gametest.ForgeGameTestHooks;

import com.typeomega.lib.test.GameTestFilter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

// EN: Forge fires RegisterGameTestsEvent before scanning @GameTestHolder classes and passes GameTestRegistry
// directly to GameTestServer without another hook, so a small mixin here is required to filter all dependent mods.
// PT: O Forge dispara o RegisterGameTestsEvent antes de varrer classes @GameTestHolder e passa o GameTestRegistry
// direto ao GameTestServer sem outro gancho, então um mixin pequeno aqui é necessário para filtrar todos os mods.
// ZH: Forge 在扫描 @GameTestHolder 类之前触发 RegisterGameTestsEvent，随后直接将 GameTestRegistry 传给
// GameTestServer 且没有其他钩子，因此需要通过此小型 mixin 来过滤所有依赖模组的测试。
@Mixin(GameTestRegistry.class)
public abstract class GameTestRegistryMixin {

    @Shadow
    @Final
    private static Collection<TestFunction> TEST_FUNCTIONS;

    @Shadow
    @Final
    private static Set<String> TEST_CLASS_NAMES;

    @Unique
    private static final ThreadLocal<Integer> TYPEOMEGALIB_BEFORE_SIZE = new ThreadLocal<>();

    @Unique
    private static final ThreadLocal<Boolean> TYPEOMEGALIB_HAD_CLASS = new ThreadLocal<>();

    @Inject(method = "register(Ljava/lang/reflect/Method;Ljava/util/Set;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private static void typeomegalib$filterBeforeRegister(Method testMethod, Set<String> allowedNamespaces,
                                                          CallbackInfo ci) {
        GameTestFilter filter = GameTestFilter.fromSystemProperty();
        boolean hasGameTest = testMethod.isAnnotationPresent(GameTest.class);
        boolean hasGenerator = testMethod.isAnnotationPresent(GameTestGenerator.class);
        boolean hasBatchHook = testMethod.isAnnotationPresent(BeforeBatch.class) ||
                testMethod.isAnnotationPresent(AfterBatch.class);

        if (hasGameTest && !hasGenerator && !hasBatchHook) {
            boolean namespaceAllowed = allowedNamespaces.isEmpty() ||
                    allowedNamespaces.contains(ForgeGameTestHooks.getTemplateNamespace(testMethod));
            if (namespaceAllowed && !filter.matchesMethod(testMethod)) {
                GameTestFilter.recordSkipped(1);
                ci.cancel();
                return;
            }
        }

        TYPEOMEGALIB_BEFORE_SIZE.set(TEST_FUNCTIONS.size());
        TYPEOMEGALIB_HAD_CLASS.set(TEST_CLASS_NAMES.contains(testMethod.getDeclaringClass().getSimpleName()));
    }

    @Inject(method = "register(Ljava/lang/reflect/Method;Ljava/util/Set;)V",
            at = @At("RETURN"),
            remap = false)
    private static void typeomegalib$filterAfterRegister(Method testMethod, Set<String> allowedNamespaces,
                                                         CallbackInfo ci) {
        Integer beforeObj = TYPEOMEGALIB_BEFORE_SIZE.get();
        Boolean hadClassObj = TYPEOMEGALIB_HAD_CLASS.get();
        TYPEOMEGALIB_BEFORE_SIZE.remove();
        TYPEOMEGALIB_HAD_CLASS.remove();
        if (beforeObj == null) {
            return;
        }

        int before = beforeObj;
        boolean hadClass = Boolean.TRUE.equals(hadClassObj);
        if (TEST_FUNCTIONS.size() <= before) {
            return;
        }

        if (testMethod.isAnnotationPresent(GameTestGenerator.class) &&
                TEST_FUNCTIONS instanceof List<TestFunction> list) {
            GameTestFilter filter = GameTestFilter.fromSystemProperty();
            for (int i = list.size() - 1; i >= before; i--) {
                TestFunction fn = list.get(i);
                if (!filter.matchesGenerated(testMethod, fn)) {
                    list.remove(i);
                    GameTestFilter.recordSkipped(1);
                }
            }
            if (list.size() == before && !hadClass) {
                TEST_CLASS_NAMES.remove(testMethod.getDeclaringClass().getSimpleName());
            }
        }

        int added = TEST_FUNCTIONS.size() - before;
        if (added > 0) {
            GameTestFilter.recordRegistered(added);
        }
    }
}
