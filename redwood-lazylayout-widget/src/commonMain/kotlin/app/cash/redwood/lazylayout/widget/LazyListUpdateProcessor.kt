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
public abstract class LazyListUpdateProcessor<C : Any, W : Any> {

  /** Loaded cells that may or may not have UI attached. */
  private var loadedCells = mutableListOf<Cell<C, W>>()

  /** Pool of placeholder widgets. */
  private val placeholdersQueue = ArrayDeque<Widget<W>>()

  private val itemsBefore = SparseList<Cell<C, W>?>()
  private var itemsAfter = SparseList<Cell<C, W>?>()

  /** These updates will all be processed in batch in [onEndChanges]. */
  private var newItemsBefore = 0
  private var newItemsAfter = 0
  private val edits = mutableListOf<Edit<W>>()

  /** We expect placeholders to be added early and to never change. */
  public val placeholder: Widget.Children<W> = object : Widget.Children<W> {
    override fun insert(index: Int, widget: Widget<W>) {
      placeholdersQueue += widget
    }

    override fun move(fromIndex: Int, toIndex: Int, count: Int) {
      error("unexpected call")
    }

    override fun remove(index: Int, count: Int) {
      error("unexpected call")
    }

    override fun onModifierUpdated() {
    }
  }

  /** Changes to this list are collected and processed in batch once all changes are received. */
  public val items: Widget.Children<W> = object : Widget.Children<W> {
    override fun insert(index: Int, widget: Widget<W>) {
      val last = edits.lastOrNull()
      if (last is Edit.Insert && index in last.index until last.index + last.widgets.size + 1) {
        // Grow the preceding insert. This makes promotion logic easier.
        last.widgets.add(index - last.index, widget)
      } else {
        edits += Edit.Insert(index, mutableListOf(widget))
      }
    }

    override fun move(fromIndex: Int, toIndex: Int, count: Int) {
      edits += Edit.Move(fromIndex, toIndex, count)
    }

    override fun remove(index: Int, count: Int) {
      val last = edits.lastOrNull()
      if (last is Edit.Remove && index in last.index - count until last.index + 1) {
        // Grow the preceding remove. This makes promotion logic easier.
        if (index < last.index) last.index = index
        last.count += count
      } else {
        edits += Edit.Remove(index, count)
      }
    }

    override fun onModifierUpdated() {
    }
  }

  public val size: Int
    get() = itemsBefore.size + loadedCells.size + itemsAfter.size

  public fun itemsBefore(itemsBefore: Int) {
    this.newItemsBefore = itemsBefore
  }

  public fun itemsAfter(itemsAfter: Int) {
    this.newItemsAfter = itemsAfter
  }

  public fun onEndChanges() {
    for (e in edits.indices) {
      val edit = edits[e]

      // Attempt to absorb this change into the before range. This reduces structural updates.
      if (
        newItemsBefore < itemsBefore.size &&
        edit is Edit.Insert &&
        edit.index == 0
      ) {
        // The before window is shrinking. Promote inserts into loads.
        val toPromoteCount = minOf(edit.widgets.size, itemsBefore.size - newItemsBefore)
        for (i in edit.widgets.size - 1 downTo edit.widgets.size - toPromoteCount) {
          val placeholder = itemsBefore.removeLast()
          loadedCells.add(0, placeholderToLoaded(placeholder, edit.widgets[i]))
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
          val removed = loadedCells.removeAt(0)
          itemsBefore.add(loadedToPlaceholder(removed))
          edit.count--
        }
      }

      // Attempt to absorb this change into the after range. This reduces structural updates.
      if (
        newItemsAfter < itemsAfter.size &&
        edit is Edit.Insert &&
        edit.index == loadedCells.size
      ) {
        // The after window is shrinking. Promote inserts into loads.
        val toPromoteCount = minOf(edit.widgets.size, itemsAfter.size - newItemsAfter)
        for (i in 0 until toPromoteCount) {
          val widget = edit.widgets.removeFirst()
          val placeholder = itemsAfter.removeAt(0)
          loadedCells.add(placeholderToLoaded(placeholder, widget))
          edit.index++
        }
      } else if (
        newItemsAfter > itemsAfter.size &&
        edit is Edit.Remove &&
        edit.index + edit.count == loadedCells.size
      ) {
        // The after window is growing. Demote loaded cells into placeholders.
        val toDemoteCount = minOf(edit.count, newItemsAfter - itemsAfter.size)
        for (i in edit.count - 1 downTo edit.count - toDemoteCount) {
          val removed = loadedCells.removeLast()
          itemsAfter.add(0, loadedToPlaceholder(removed))
          edit.count--
        }
      }

      // Process regular edits.
      when (edit) {
        is Edit.Insert -> {
          for (i in 0 until edit.widgets.size) {
            val index = itemsBefore.size + edit.index + i
            loadedCells.add(edit.index + i, Cell(this, edit.widgets[i]))

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
            loadedCells.removeAt(edit.index)

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
        val index = itemsBefore.size + loadedCells.size + itemsAfter.size - delta
        itemsAfter.removeRange(itemsAfter.size - delta, itemsAfter.size)
        deleteRows(index, delta)
      }
      newItemsAfter > itemsAfter.size -> {
        // Grow the after window.
        val delta = newItemsAfter - itemsAfter.size
        val index = itemsBefore.size + loadedCells.size + itemsAfter.size
        itemsAfter.addNulls(itemsAfter.size, delta)
        insertRows(index, delta)
      }
    }

    edits.clear()
  }

  private fun placeholderToLoaded(
    placeholder: Cell<C, W>?,
    widget: Widget<W>,
  ): Cell<C, W> {
    // No placeholder for this index. Create a new cell.
    if (placeholder == null) {
      return Cell(this, widget)
    }

    // We have a placeholder. Promote it to a loaded cell.
    require(placeholder.isPlaceholder)
    placeholdersQueue += placeholder.widget
    placeholder.isPlaceholder = false
    placeholder.widget = widget
    setWidget(placeholder.view!!, widget)
    return placeholder
  }

  private fun loadedToPlaceholder(loaded: Cell<C, W>): Cell<C, W>? {
    require(!loaded.isPlaceholder)

    // If there's no UI for this cell, we're done.
    val ui = loaded.view ?: return null

    // Replace the loaded UI with a placeholder.
    val widget = takePlaceholder()
    setWidget(ui, widget)
    loaded.widget = widget
    loaded.isPlaceholder = true
    return loaded
  }

  public fun getCell(index: Int): C {
    return when {
      index < itemsBefore.size -> getOrCreatePlaceholder(itemsBefore, index, index)
      index < itemsBefore.size + loadedCells.size -> getLoadedCell(index)
      else -> getOrCreatePlaceholder(itemsAfter, index - itemsBefore.size - loadedCells.size, index)
    }
  }

  private fun getLoadedCell(index: Int): C {
    val result = loadedCells[index - itemsBefore.size]

    var view = result.view
    if (view == null) {
      // Bind the cell to this view.
      view = createCell(result, result.widget, index)
      result.bind(view)
    }

    return view
  }

  private fun getOrCreatePlaceholder(
    placeholders: SparseList<Cell<C, W>?>,
    placeholderIndex: Int,
    cellIndex: Int,
  ): C {
    var result = placeholders[placeholderIndex]

    // Return an existing placeholder.
    if (result != null) return result.view!!

    // Create a new placeholder cell and bind it to the view.
    result = Cell(
      processor = this,
      widget = takePlaceholder(),
      isPlaceholder = true,
    )
    val view = createCell(result, result.widget, cellIndex)
    result.bind(view)
    placeholders.set(placeholderIndex, result)
    return view
  }

  private fun takePlaceholder(): Widget<W> {
    return placeholdersQueue.removeFirstOrNull()
      ?: throw IllegalStateException("no more placeholders!")
  }

  protected abstract fun createCell(cell: Cell<C, W>, widget: Widget<W>, index: Int): C

  protected abstract fun setWidget(cell: C, widget: Widget<W>)

  protected abstract fun insertRows(index: Int, count: Int)

  protected abstract fun deleteRows(index: Int, count: Int)

  /**
   * This class keeps track of whether a cell is bound to an on-screen UI.
   *
   * While it's bound to the UI, its contents can change:
   *
   *  - A placeholder can be swapped for a loaded widget, if the loaded window moves to include
   *    this cell.
   *  - Symmetrically, a loaded widget can be swapped for a placeholder, if the loaded window moves
   *    to exclude this cell.
   *
   * If it currently holds a placeholder, this placeholder must be released to the processor's
   * placeholder queue when it is no longer needed. This will occur if cell is reused (due to view
   * recycling), or because it is discarded (due to the table discarding it). This class assumes
   * that a cell that is discarded will never be bound again.
   */
  public class Cell<C : Any, W : Any> internal constructor(
    internal val processor: LazyListUpdateProcessor<C, W>,
    internal var widget: Widget<W>,
    internal var isPlaceholder: Boolean = false,
  ) {
    public var view: C? = null
      private set

    private var bindCount = 0
    private var unbindCount = 0

    public val isBound: Boolean
      get() = bindCount > unbindCount

    public fun bind(view: C) {
      require(bindCount == unbindCount) { "already bound" }

      bindCount++
      this.view = view
    }

    public fun unbind() {
      if (bindCount == unbindCount) return
      unbindCount++

      // Detach the display.
      view = null

      if (isPlaceholder) {
        // This placeholder is no longer needed.
        val itemsBeforeIndex = processor.itemsBefore.indexOfFirst { it === this }
        if (itemsBeforeIndex != -1) processor.itemsBefore.set(itemsBeforeIndex, null)

        val itemsAfterIndex = processor.itemsAfter.indexOfFirst { it === this }
        if (itemsAfterIndex != -1) processor.itemsAfter.set(itemsAfterIndex, null)

        // When a placeholder is reused, recycle its widget.
        processor.placeholdersQueue += widget
      }
    }
  }

  /** Note that edit instances are mutable. This avoids allocations during scrolling. */
  private sealed class Edit<W> {
    class Insert<W : Any>(var index: Int, val widgets: MutableList<Widget<W>>) : Edit<W>()
    class Move<W : Any>(val fromIndex: Int, val toIndex: Int, val count: Int) : Edit<W>()
    class Remove<W : Any>(var index: Int, var count: Int) : Edit<W>()
  }
}
