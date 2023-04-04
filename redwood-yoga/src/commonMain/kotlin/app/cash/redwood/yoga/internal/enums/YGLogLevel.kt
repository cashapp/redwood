package app.cash.redwood.yoga.internal.enums

enum class YGLogLevel {
    YGLogLevelError, YGLogLevelWarn, YGLogLevelInfo, YGLogLevelDebug, YGLogLevelVerbose, YGLogLevelFatal;

    fun getValue(): Int {
        return ordinal
    }

    companion object {

        fun forValue(value: Int): YGLogLevel {
            return values()[value]
        }
    }
}
