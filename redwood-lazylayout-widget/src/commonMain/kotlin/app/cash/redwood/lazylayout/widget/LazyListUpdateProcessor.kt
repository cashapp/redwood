/*
 * Copyright (C) 2023 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.redwood.lazylayout.widget

import app.cash.redwood.Modifier
import app.cash.redwood.widget.Widget

/**
 * Our lazy layouts can display arbitrarily large datasets. Instead of loading them all eagerly
 * (which would be extremely slow!), it maintains a window of loaded items.
 *
 * As the user scrolls, the lazy layout shifts (and potentially resizes) its window in an attempt
 * to maintain an illusion that all data is always loaded. If the user scrolls beyond what is
 * loaded, a placeholder is displayed until the row is loaded.
 *
 * The net effect is that there are two windows into the dataset:
 *
 *  - the window of what is loaded
 *  - the window of what the user is looking at
 *
 * To maintain the illusion that everything is loaded, the loaded data window should always contain
 * the visible window.
 *
 * This class keeps track of the two windows, and of firing precise updates as the window changes.
 */
public abstract class LazyListUpdateProcessor<V : Any, W : Any> {

  public val bindings: List<Binding<V, W>>
    get() = itemsBefore.nonNullElements + loadedItems + itemsAfter.nonNullElements

  /** Pool of placeholder widgets. */
  private val placeholdersQueue = ArrayDeque<Widget<W>>()

  /**
   * The first placeholder ever returned. We use it to choose measured dimensions for created
   * placeholders if the pool ever runs out.
   */
  private var firstPlaceholder: Widget<W>? = null

  /** Loaded items that may or may not have a view bound. */
  private var loadedItems = mutableListOf<Binding<V, W>>()

  private val itemsBefore = SparseList<Binding<V, W>?>()
  private val itemsAfter = SparseList<Binding<V, W>?>()

  /** These updates will all be processed in batch in [onEndChanges]. */
  private var newItemsBefore = 0
  private var newItemsAfter = 0
  private val edits = mutableListOf<Edit<W>>()

  /** We expect placeholders to be added early and to never change. */
  public val placeholder: Widget.Children<W> = object : Widget.Children<W> {
    private val _widgets = mutableListOf<Widget<W>>()
    override val widgets: List<Widget<W>> get() = _widgets

    override fun insert(index: Int, widget: Widget<W>) {
      _widgets += widget
      if (firstPlaceholder == null) firstPlaceholder = widget
      placeholdersQueue += widget
    }

    override fun move(fromIndex: Int, toIndex: Int, count: Int) {
      error("unexpected call")
    }

    override fun remove(index: Int, count: Int) {
      error("unexpected call")
    }

    override fun onModifierUpdated(index: Int, widget: Widget<W>) {
    }

    override fun detach() {
      _widgets.clear()

      firstPlaceholder = null
      placeholdersQueue.clear()

      for (binding in itemsBefore.nonNullElements) {
        binding.detach()
      }
      for (binding in itemsAfter.nonNullElements) {
        binding.detach()
      }
    }
  }

  /** Changes to this list are collected and processed in batch once all changes are received. */
  public val items: Widget.Children<W> = object : Widget.Children<W> {
    private val _widgets = mutableListOf<Widget<W>>()
    override val widgets: List<Widget<W>> get() = _widgets

    override fun insert(index: Int, widget: Widget<W>) {
      _widgets.add(index, widget)

      val last = edits.lastOrNull()
      if (last is Edit.Insert && index in last.index until last.index + last.widgets.size + 1) {
        // Grow the preceding insert. This makes promotion logic easier.
        last.widgets.add(index - last.index, widget)
      } else {
        edits += Edit.Insert(index, mutableListOf(widget))
      }
    }

    override fun move(fromIndex: Int, toIndex: Int, count: Int) {
      _widgets.move(fromIndex, toIndex, count)

      edits += Edit.Move(fromIndex, toIndex, count)
    }

    override fun remove(index: Int, count: Int) {
      _widgets.remove(index, count)

      val last = edits.lastOrNull()
      if (last is Edit.Remove && index in last.index - count until last.index + 1) {
        // Grow the preceding remove. This makes promotion logic easier.
        if (index < last.index) last.index = index
        last.count += count
      } else {
        edits += Edit.Remove(index, count)
      }
    }

    override fun onModifierUpdated(index: Int, widget: Widget<W>) {
    }

    override fun detach() {
      _widgets.clear()

      for (binding in loadedItems) {
        binding.detach()
      }

      this@LazyListUpdateProcessor.detach()
    }
  }

  public val size: Int
    get() = itemsBefore.size + loadedItems.size + itemsAfter.size

  public fun itemsBefore(itemsBefore: Int) {
    this.newItemsBefore = itemsBefore
  }

  public fun itemsAfter(itemsAfter: Int) {
    this.newItemsAfter = itemsAfter
  }

  public fun onEndChanges() {
    // Walk through the edits pairwise attempting to swap inserts and removes. By applying removes
    // first we create more opportunities to successfully call maybeShiftLoadedWindow() below.
    //
    // Note that when swapping edits we need to adjust their offsets.
    for (e in 0 until edits.size - 1) {
      val a = edits[e]
      val b = edits[e + 1]

      if (a is Edit.Insert && b is Edit.Remove) {
        // A remove follows an insert. Apply the remove first.
        if (b.index >= a.index + a.widgets.size) {
          b.index -= a.widgets.size
          edits[e] = b
          edits[e + 1] = a
        }
      }
    }

    for (e in edits.indices) {
      val edit = edits[e]

      // Attempt to absorb this change into the before range. This reduces structural updates.
      if (
        newItemsBefore < itemsBefore.size &&
        edit is Edit.Insert &&
        edit.index == 0
      ) {
        maybeShiftLoadedWindow(edit.widgets.size)
        // The before window is shrinking. Promote inserts into loads.
        val toPromoteCount = minOf(edit.widgets.size, itemsBefore.size - newItemsBefore)
        for (i in edit.widgets.size - 1 downTo edit.widgets.size - toPromoteCount) {
          val placeholder = itemsBefore.removeLast()
          loadedItems.add(0, placeholderToLoaded(placeholder, edit.widgets[i]))
          edit.widgets.removeAt(i)
        }
      } else if (
        newItemsBefore > itemsBefore.size &&
        edit is Edit.Remove &&
        edit.index == 0
      ) {
        // The before window is growing. Demote loaded cells into placeholders.
        val toDemoteCount = minOf(edit.count, newItemsBefore - itemsBefore.size)
        for (i in 0 until toDemoteCount) {
          val removed = loadedItems.removeAt(0)
          itemsBefore.add(loadedToPlaceholder(removed))
          edit.count--
        }
      }

      // Attempt to absorb this change into the after range. This reduces structural updates.
      if (
        newItemsAfter < itemsAfter.size &&
        edit is Edit.Insert &&
        edit.index == loadedItems.size
      ) {
        maybeShiftLoadedWindow(newItemsBefore + loadedItems.size)
        // The after window is shrinking. Promote inserts into loads.
        val toPromoteCount = minOf(edit.widgets.size, itemsAfter.size - newItemsAfter)
        for (i in 0 until toPromoteCount) {
          val widget = edit.widgets.removeFirst()
          val placeholder = itemsAfter.removeAt(0)
          loadedItems.add(placeholderToLoaded(placeholder, widget))
          edit.index++
        }
      } else if (
        newItemsAfter > itemsAfter.size &&
        edit is Edit.Remove &&
        edit.index + edit.count == loadedItems.size
      ) {
        // The after window is growing. Demote loaded cells into placeholders.
        val toDemoteCount = minOf(edit.count, newItemsAfter - itemsAfter.size)
        for (i in edit.count - 1 downTo edit.count - toDemoteCount) {
          val removed = loadedItems.removeLast()
          itemsAfter.add(0, loadedToPlaceholder(removed))
          edit.count--
        }
      }

      // Process regular edits.
      when (edit) {
        is Edit.Insert -> {
          for (i in 0 until edit.widgets.size) {
            val index = itemsBefore.size + edit.index + i
            val binding = Binding(this).apply {
              setContent(edit.widgets[i])
            }
            loadedItems.add(edit.index + i, binding)

            // Publish a structural change.
            insertRows(index, 1)
          }
        }

        is Edit.Move -> {
          // TODO(jwilson): support moves!
          error("move unsupported!")
        }

        is Edit.Remove -> {
          for (i in edit.index until edit.index + edit.count) {
            val index = itemsBefore.size + edit.index
            val removed = loadedItems.removeAt(edit.index)
            removed.unbind()

            // Publish a structural change.
            deleteRows(index, 1)
          }
        }
      }
    }

    when {
      newItemsBefore < itemsBefore.size -> {
        // Shrink the before window.
        val delta = itemsBefore.size - newItemsBefore
        itemsBefore.removeRange(0, delta)
        deleteRows(0, delta)
      }

      newItemsBefore > itemsBefore.size -> {
        // Grow the before window.
        val delta = newItemsBefore - itemsBefore.size
        itemsBefore.addNulls(0, delta)
        insertRows(0, delta)
      }
    }

    when {
      newItemsAfter < itemsAfter.size -> {
        // Shrink the after window.
        val delta = itemsAfter.size - newItemsAfter
        val index = itemsBefore.size + loadedItems.size + itemsAfter.size - delta
        itemsAfter.removeRange(itemsAfter.size - delta, itemsAfter.size)
        deleteRows(index, delta)
      }

      newItemsAfter > itemsAfter.size -> {
        // Grow the after window.
        val delta = newItemsAfter - itemsAfter.size
        val index = itemsBefore.size + loadedItems.size + itemsAfter.size
        itemsAfter.addNulls(itemsAfter.size, delta)
        insertRows(index, delta)
      }
    }

    edits.clear()
  }

  /**
   * When the loaded window is empty, the boundary between [itemsBefore] and [itemsAfter] is
   * arbitrary: we can move it without any externally-visible effects.
   *
   * And we do want to move it! In particular, aligning the boundary with an incoming edit gives us
   * the best opportunity to do in-place updates instead of calling [insertRows] and [deleteRows].
   */
  private fun maybeShiftLoadedWindow(splitIndex: Int) {
    if (loadedItems.size != 0) return

    if (splitIndex < itemsBefore.size) {
      val count = itemsBefore.size - splitIndex
      itemsAfter.addRange(0, itemsBefore, splitIndex, count)
      itemsBefore.removeRange(splitIndex, splitIndex + count)
    } else if (splitIndex > itemsBefore.size) {
      val count = minOf(splitIndex - itemsBefore.size, itemsAfter.size)
      itemsBefore.addRange(itemsBefore.size, itemsAfter, 0, count)
      itemsAfter.removeRange(0, count)
    }
  }

  private fun placeholderToLoaded(
    binding: Binding<V, W>?,
    loadedContent: Widget<W>,
  ): Binding<V, W> {
    // No binding for this index. Create one.
    if (binding == null) {
      return Binding(this).apply {
        setContent(loadedContent)
      }
    }

    // We have a binding. Give it loaded content.
    require(binding.isPlaceholder)
    if (binding.isBound) {
      recyclePlaceholder(binding.content)
    }
    binding.isPlaceholder = false
    binding.setContent(loadedContent)
    return binding
  }

  private fun loadedToPlaceholder(loaded: Binding<V, W>): Binding<V, W>? {
    require(!loaded.isPlaceholder)

    // If there's no view for this binding, we're done.
    if (!loaded.isBound) return null

    // Show the placeholder in the bound view.
    val placeholder = loaded.processor.takePlaceholder()
    if (placeholder != null) {
      loaded.setContent(placeholder)
    }
    loaded.isPlaceholder = true
    return loaded
  }

  public fun getOrCreateView(
    index: Int,
    createView: (binding: Binding<V, W>) -> V,
  ): V {
    val binding = requireBindingAtIndex(index)

    var view = binding.view
    if (view == null) {
      view = createView(binding)
      binding.bind(view)
    }

    return view
  }

  public fun bind(index: Int, view: V): Binding<V, W> {
    return requireBindingAtIndex(index).apply {
      bind(view)
    }
  }

  /**
   * Callers must immediately call [Binding.bind] on the result if it isn't already bound. Otherwise
   * we could leak placeholder bindings.
   */
  private fun requireBindingAtIndex(index: Int): Binding<V, W> {
    fun createBinding() = Binding(
      processor = this@LazyListUpdateProcessor,
      isPlaceholder = true,
    ).apply {
      val placeholder = takePlaceholder() ?: return@apply // Detached.
      setContent(placeholder)
    }

    return when {
      index < itemsBefore.size -> itemsBefore.getOrCreate(index, ::createBinding)
      index < itemsBefore.size + loadedItems.size -> loadedItems[index - itemsBefore.size]
      else -> itemsAfter.getOrCreate(index - itemsBefore.size - loadedItems.size, ::createBinding)
    }
  }

  private fun takePlaceholder(): Widget<W>? {
    val result = placeholdersQueue.removeFirstOrNull()
    if (result != null) return result

    val firstPlaceholder = this.firstPlaceholder ?: return null // Detached.
    val sizeOnlyPlaceholderValue = createPlaceholder(firstPlaceholder.value) ?: return null
    return SizeOnlyPlaceholderWidget(
      value = sizeOnlyPlaceholderValue,
      modifier = firstPlaceholder.modifier,
    )
  }

  private fun recyclePlaceholder(placeholder: Widget<W>?) {
    if (placeholder == null) return // Detached.
    if (placeholder !is SizeOnlyPlaceholderWidget) {
      placeholdersQueue += placeholder
    }
  }

  /**
   * Returns an empty widget with the same dimensions as [original].
   *
   * @param original a placeholder provided by the guest code. It is a live view that is currently
   *     in a layout.
   */
  protected open fun createPlaceholder(original: W): W? = null

  protected abstract fun insertRows(index: Int, count: Int)

  protected abstract fun deleteRows(index: Int, count: Int)

  protected abstract fun setContent(view: V, widget: Widget<W>?)

  protected open fun detach(view: V) {
  }

  protected open fun detach() {
  }

  /**
   * Binds a UI-managed view to model-managed content.
   *
   * While view is bound to content, that content can change:
   *
   *  - A placeholder can be swapped for loaded content. This happens when the model's loaded window
   *    moves to include the view.
   *  - Symmetrically, loaded content can be swapped for placeholder content, if the loaded window
   *    moves to exclude the view.
   *
   * If it currently holds placeholder content, that placeholder content must be released to the
   * processor's placeholder queue when it is no longer needed. This will occur if view is reused
   * (due to view recycling), or because it is discarded (due to the view discarding it). This class
   * assumes that a view that is discarded will never be bound again.
   */
  public class Binding<V : Any, W : Any> internal constructor(
    internal val processor: LazyListUpdateProcessor<V, W>,
    internal var isPlaceholder: Boolean = false,
  ) {
    /** The currently-bound view. Null if this is not on-screen. */
    public var view: V? = null
      private set

    public val isBound: Boolean
      get() = view != null

    /**
     * The content of this binding; either a loaded widget, a placeholder, or null.
     *
     * This may be null if its content is a placeholder that is not bound to a view. That way we
     * don't take placeholders from the pool until we need them.
     *
     * This may also be null if it should be a placeholder but the enclosing LazyList is detached.
     * This could happen if the user scrolls the table after the view is detached.
     */
    internal var content: Widget<W>? = null
      private set

    internal fun setContent(content: Widget<W>) {
      this.content = content

      val view = this.view
      if (view != null) processor.setContent(view, content)
    }

    public fun bind(view: V) {
      require(this.view == null) { "already bound" }

      if (isPlaceholder && this.content == null) {
        this.content = processor.takePlaceholder()
      }

      this.view = view
      processor.setContent(view, content)
    }

    public fun unbind() {
      val view = this.view ?: return

      // Unbind the view.
      processor.setContent(view, null)
      this.view = null

      if (isPlaceholder) {
        // This placeholder is no longer needed.
        val itemsBeforeIndex = processor.itemsBefore.indexOfFirst { it === this }
        if (itemsBeforeIndex != -1) processor.itemsBefore.set(itemsBeforeIndex, null)

        val itemsAfterIndex = processor.itemsAfter.indexOfFirst { it === this }
        if (itemsAfterIndex != -1) processor.itemsAfter.set(itemsAfterIndex, null)

        // When a placeholder is reused, recycle its widget.
        processor.recyclePlaceholder(content)
        this.content = null
      }
    }

    public fun detach() {
      val view = this.view
      if (view != null) processor.detach(view)

      this.content = null
      this.view = null
    }
  }

  /** Note that edit instances are mutable. This avoids allocations during scrolling. */
  private sealed class Edit<W> {
    class Insert<W : Any>(var index: Int, val widgets: MutableList<Widget<W>>) : Edit<W>()
    class Move<W : Any>(val fromIndex: Int, val toIndex: Int, val count: Int) : Edit<W>()
    class Remove<W : Any>(var index: Int, var count: Int) : Edit<W>()
  }

  private class SizeOnlyPlaceholderWidget<W : Any>(
    override val value: W,
    override var modifier: Modifier,
  ) : Widget<W>
}
