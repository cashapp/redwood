package io.github.orioncraftmc.meditate.internal.detail;

import java.util.Objects;

public class StyleEnumFlagsKey {
  private final Class<?> enumClazz;
  private final int index;

  public StyleEnumFlagsKey(Class<?> enumClazz, int index) {
    this.enumClazz = enumClazz;
    this.index = index;
  }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StyleEnumFlagsKey that = (StyleEnumFlagsKey) o;
        return index == that.index && Objects.equals(enumClazz, that.enumClazz);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enumClazz, index);
    }

    public Class<?> getEnumClazz() {
        return enumClazz;
    }

    public int getIndex() {
        return index;
    }
}
