package io.github.orioncraftmc.meditate.internal.detail;

import static io.github.orioncraftmc.meditate.internal.GlobalMembers.YGConfigGetDefault;
import io.github.orioncraftmc.meditate.internal.YGConfig;
import io.github.orioncraftmc.meditate.internal.YGNode;
import io.github.orioncraftmc.meditate.internal.enums.YGLogLevel;
import java.util.Arrays;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GlobalMembers {
    public static void vlog(@Nullable YGConfig config, YGNode node, YGLogLevel level, Object context, String format, Object... args) {
        @Nullable YGConfig logConfig = config != null ? config : YGConfigGetDefault();
        logConfig.log(logConfig, node, level, context, format, args);
    }

    public static @NotNull Integer log2ceilFn(Integer n) {
        return n < 1 ? 0 : (1 + log2ceilFn(n / 2));
    }

    public static int mask(Integer bitWidth, Integer index) {
        return ((1 << bitWidth) - 1) << index;
    }

    public static <E extends Enum<E>> int bitWidthFn(@NotNull Class<E> e) {
        return (log2ceilFn(e.getEnumConstants().length - 1));
    }

    public static <E extends Enum<E>> E getEnumData(@NotNull Class<E> e, Map<Object, Object> flags, Integer index) {
        return (E) flags.getOrDefault(new StyleEnumFlagsKey(e, index),
                Arrays.stream(e.getEnumConstants()).findFirst().get());
    }


    public static <E extends Enum<E>> int setEnumData(@NotNull Class<E> e, Map<Object, Object> flags, int index, @NotNull E newValue) {
        flags.put(new StyleEnumFlagsKey(e, index), newValue);
        return 0;
    }

    public static boolean getBooleanData(Map<Object, Object> flags, Integer index) {
        return (boolean) flags.getOrDefault(index, false);
    }

    public static int setBooleanData(Map<Object, Object> flags, int index, boolean value) {
        flags.put(index, value);
        return 0;
    }

    private boolean notEqualsTo(@NotNull CompactValue a, @NotNull CompactValue b) {
        return !CompactValue.equalsTo(a, b);
    }
}
