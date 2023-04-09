package io.github.orioncraftmc.meditate.internal;

import static io.github.orioncraftmc.meditate.internal.GlobalMembers.YGNodeClone;
import io.github.orioncraftmc.meditate.internal.enums.YGExperiment;
import io.github.orioncraftmc.meditate.internal.enums.YGLogLevel;
import io.github.orioncraftmc.meditate.internal.interfaces.YGCloneNodeFunc;
import io.github.orioncraftmc.meditate.internal.interfaces.YGLogger;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class YGConfig implements Cloneable //Type originates from: YGConfig.h
{
    private final @NotNull logger_Struct logger_struct = new logger_Struct();
    public boolean useWebDefaults = false;
    public boolean useLegacyStretchBehaviour = false;
    public boolean shouldDiffLayoutWithoutLegacyStretchBehaviour = false;
    public boolean printTree = false;
    public float pointScaleFactor = 1.0f;
    public final @NotNull ArrayList<Boolean> experimentalFeatures = new ArrayList<>();
    public @Nullable Object context = null;
    private cloneNodeCallback_Struct cloneNodeCallback_struct = new cloneNodeCallback_Struct();
    private boolean cloneNodeUsesContext_;
    private boolean loggerUsesContext_;

    public YGConfig(YGLogger logger) //Method definition originates from: YGConfig.cpp
    {
        this.cloneNodeCallback_struct = null;
        logger_struct.noContext = logger;
        loggerUsesContext_ = false;
        for (int i = 0; i < YGExperiment.values().length; i++) {
            experimentalFeatures.add(false);
        }
    }

    @Override
    public @NotNull YGConfig clone() {
        try {
            return (YGConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public final void log(YGConfig config, YGNode node, YGLogLevel logLevel, Object logContext, String format, Object... args) //Method definition originates from: YGConfig.cpp
    {
        if (loggerUsesContext_) {
            logger_struct.withContext.invoke(config, node, logLevel, logContext, format, args);
        } else {
            logger_struct.noContext.invoke(config, node, logLevel, format, args);
        }
    }

    public final void setLogger(YGLogger logger) {
        logger_struct.noContext = logger;
        loggerUsesContext_ = false;
    }

    public final void setLogger(LogWithContextFn logger) {
        logger_struct.withContext = logger;
        loggerUsesContext_ = true;
    }

    public final void setLogger() {
        logger_struct.noContext = null;
        loggerUsesContext_ = false;
    }

    public final @NotNull YGNode cloneNode(YGNode node, YGNode owner, int childIndex, Object cloneContext) //Method definition originates from: YGConfig.cpp
    {
        @Nullable YGNode clone = null;
        if (cloneNodeCallback_struct.noContext != null) {
            clone = cloneNodeUsesContext_ ? cloneNodeCallback_struct.withContext.invoke(node, owner, childIndex,
                    cloneContext) : cloneNodeCallback_struct.noContext.invoke(node, owner, childIndex);
        }
        if (clone == null) {
            clone = YGNodeClone(node);
        }
        return clone;
    }

    public final void setCloneNodeCallback(YGCloneNodeFunc cloneNode) {
        cloneNodeCallback_struct.noContext = cloneNode;
        cloneNodeUsesContext_ = false;
    }

    public final void setCloneNodeCallback(CloneWithContextFn cloneNode) {
        cloneNodeCallback_struct.withContext = cloneNode;
        cloneNodeUsesContext_ = true;
    }

    public final void setCloneNodeCallback() {
        cloneNodeCallback_struct.noContext = null;
        cloneNodeUsesContext_ = false;
    }

    @FunctionalInterface
    public interface LogWithContextFn {
        int invoke(YGConfig config, YGNode node, YGLogLevel level, Object context, String format, Object... args);
    }

    @FunctionalInterface
    public interface CloneWithContextFn {
        @NotNull YGNode invoke(YGNode node, YGNode owner, int childIndex, Object cloneContext);
    }

    private static class cloneNodeCallback_Struct {

        CloneWithContextFn withContext;
        @Nullable YGCloneNodeFunc noContext;

    }

    private static class logger_Struct {

        LogWithContextFn withContext;
        @Nullable YGLogger noContext;

    }
}
