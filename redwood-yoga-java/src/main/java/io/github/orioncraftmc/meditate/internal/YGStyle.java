package io.github.orioncraftmc.meditate.internal;

import io.github.orioncraftmc.meditate.internal.detail.CompactValue;
import static io.github.orioncraftmc.meditate.internal.detail.GlobalMembers.bitWidthFn;
import static io.github.orioncraftmc.meditate.internal.detail.GlobalMembers.getEnumData;
import static io.github.orioncraftmc.meditate.internal.detail.GlobalMembers.setEnumData;
import io.github.orioncraftmc.meditate.internal.detail.Values;
import io.github.orioncraftmc.meditate.internal.enums.*;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class YGStyle //Type originates from: YGStyle.h
{

    private static final Integer directionOffset = 0;
    private static final Integer flexdirectionOffset = directionOffset + bitWidthFn(YGDirection.class);


    private static final Integer justifyContentOffset = flexdirectionOffset + bitWidthFn(YGFlexDirection.class);
    private static final Integer alignContentOffset = justifyContentOffset + bitWidthFn(YGJustify.class);

    //  ~YGStyle() = default;
    private static final Integer alignItemsOffset = alignContentOffset + bitWidthFn(YGAlign.class);
    private static final Integer alignSelfOffset = alignItemsOffset + bitWidthFn(YGAlign.class);
    private static final Integer positionTypeOffset = alignSelfOffset + bitWidthFn(YGAlign.class);
    private static final Integer flexWrapOffset = positionTypeOffset + bitWidthFn(YGPositionType.class);
    private static final Integer overflowOffset = flexWrapOffset + bitWidthFn(YGWrap.class);
    private static final Integer displayOffset = overflowOffset + bitWidthFn(YGOverflow.class);
    private final Values<YGEdge> margin_ = new Values<>();
    private final Values<YGEdge> position_ = new Values<>();
    private final Values<YGEdge> padding_ = new Values<>();
    private final Values<YGEdge> border_ = new Values<>();
    private final Values<YGDimension> dimensions_ = new Values<>(CompactValue.ofAuto().convertToYgValue());
    private final Values<YGDimension> minDimensions_ = new Values<>();
    private final Values<YGDimension> maxDimensions_ = new Values<>();
    private final Map<Object, Object> flags = new HashMap<>(Map.of());
    private YGFloatOptional aspectRatio_ = new YGFloatOptional();
    private YGFloatOptional flex_ = new YGFloatOptional();
    private YGFloatOptional flexGrow_ = new YGFloatOptional();
    private YGFloatOptional flexShrink_ = new YGFloatOptional();
    private CompactValue flexBasis_ = CompactValue.ofAuto();

    public YGStyle() {
        setEnumData(YGAlign.class, flags, alignContentOffset, YGAlign.YGAlignFlexStart);
        setEnumData(YGAlign.class, flags, alignItemsOffset, YGAlign.YGAlignStretch);
    }

    public void setAspectRatio(YGFloatOptional aspectRatio_) {
        this.aspectRatio_ = aspectRatio_;
    }

    public void setFlex(YGFloatOptional flex_) {
        this.flex_ = flex_;
    }

    public void setFlexGrow(YGFloatOptional flexGrow_) {
        this.flexGrow_ = flexGrow_;
    }

    public void setFlexShrink(YGFloatOptional flexShrink_) {
        this.flexShrink_ = flexShrink_;
    }

    public void setFlexBasis(CompactValue flexBasis_) {
        this.flexBasis_ = flexBasis_;
    }

    public final YGDirection direction() {
        return getEnumData(YGDirection.class, flags, directionOffset);
    }

    public final @NotNull BitfieldRef<YGDirection> directionBitfieldRef() {
        return new BitfieldRef<>(this, directionOffset);
    }

    public final YGFlexDirection flexDirection() {
        return getEnumData(YGFlexDirection.class, flags, flexdirectionOffset);
    }

    public final @NotNull BitfieldRef<YGFlexDirection> flexDirectionBitfieldRef() {
        return new BitfieldRef<>(this, flexdirectionOffset);
    }

    public final YGJustify justifyContent() {
        return getEnumData(YGJustify.class, flags, justifyContentOffset);
    }

    public final @NotNull BitfieldRef<YGJustify> justifyContentBitfieldRef() {
        return new BitfieldRef<>(this, justifyContentOffset);
    }

    public final YGAlign alignContent() {
        return getEnumData(YGAlign.class, flags, alignContentOffset);
    }

    public final @NotNull BitfieldRef<YGAlign> alignContentBitfieldRef() {
        return new BitfieldRef<>(this, alignContentOffset);
    }

    public final YGAlign alignItems() {
        return getEnumData(YGAlign.class, flags, alignItemsOffset);
    }

    public final @NotNull BitfieldRef<YGAlign> alignItemsBitfieldRef() {
        return new BitfieldRef<>(this, alignItemsOffset);
    }

    public final YGAlign alignSelf() {
        return getEnumData(YGAlign.class, flags, alignSelfOffset);
    }

    public final @NotNull BitfieldRef<YGAlign> alignSelfBitfieldRef() {
        return new BitfieldRef<>(this, alignSelfOffset);
    }

    public final YGPositionType positionType() {
        return getEnumData(YGPositionType.class, flags, positionTypeOffset);
    }

    public final @NotNull BitfieldRef<YGPositionType> positionTypeBitfieldRef() {
        return new BitfieldRef<>(this, positionTypeOffset);
    }

    public final YGWrap flexWrap() {
        return getEnumData(YGWrap.class, flags, flexWrapOffset);
    }

    public final @NotNull BitfieldRef<YGWrap> flexWrapBitfieldRef() {
        return new BitfieldRef<>(this, flexWrapOffset);
    }

    public final YGOverflow overflow() {
        return getEnumData(YGOverflow.class, flags, overflowOffset);
    }

    public final @NotNull BitfieldRef<YGOverflow> overflowBitfieldRef() {
        return new BitfieldRef<>(this, overflowOffset);
    }


    public final YGDisplay display() {
        return getEnumData(YGDisplay.class, flags, displayOffset);
    }

    public final @NotNull BitfieldRef<YGDisplay> displayBitfieldRef() {
        return new BitfieldRef<>(this, displayOffset);
    }


    public final YGFloatOptional flex() {
        return flex_;
    }

    public final YGFloatOptional flexGrow() {
        return flexGrow_;
    }

    public final YGFloatOptional flexShrink() {
        return flexShrink_;
    }

    public final CompactValue flexBasis() {
        return flexBasis_;
    }


    public final @NotNull Values<YGEdge> margin() {
        return margin_;
    }


    public final @NotNull Values<YGEdge> position() {
        return position_;
    }


    public final @NotNull Values<YGEdge> padding() {
        return padding_;
    }


    public final @NotNull Values<YGEdge> border() {
        return border_;
    }


    public final @NotNull Values<YGDimension> dimensions() {
        return dimensions_;
    }


    public final @NotNull Values<YGDimension> minDimensions() {
        return minDimensions_;
    }


    public final @NotNull Values<YGDimension> maxDimensions() {
        return maxDimensions_;
    }


    public final YGFloatOptional aspectRatio() {
        return aspectRatio_;
    }

    public static class BitfieldRef<T extends Enum<T>> //Type originates from: YGStyle.h
    {
        public final YGStyle style;
        public final Integer offset;

        public BitfieldRef(YGStyle style, Integer offset) {
            this.style = style;
            this.offset = offset;
        }

        public T getValue(@NotNull Class<T> enumClazz) {
            return getEnumData(enumClazz, style.flags, offset);
        }

        public final @NotNull BitfieldRef<T> setValue(@NotNull T x) {
            setEnumData((Class<T>) x.getClass(), style.flags, offset, x);
            return this;
        }
    }

}
