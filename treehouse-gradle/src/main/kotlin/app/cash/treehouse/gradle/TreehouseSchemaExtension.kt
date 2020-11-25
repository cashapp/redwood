package app.cash.treehouse.gradle

interface TreehouseSchemaExtension {
  var schema: String?
}

// Gradle requires this type to be open since it runtime extends it.
internal open class TreehouseSchemaExtensionImpl @JvmOverloads constructor(
  override var schema: String? = null,
) : TreehouseSchemaExtension
