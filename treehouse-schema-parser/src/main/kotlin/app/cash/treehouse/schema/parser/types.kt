package app.cash.treehouse.schema.parser

/** [Class.packageName] isn't available until Java 9. */
internal fun packageName(schemaType: Class<*>): String {
  require(!schemaType.isPrimitive && !schemaType.isArray)
  return schemaType.name.substringBeforeLast(".", missingDelimiterValue = "")
}
