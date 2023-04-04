/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.layout.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import app.cash.redwood.yoga.YogaConstants
import app.cash.redwood.yoga.YogaMeasureOutput.make
import app.cash.redwood.yoga.YogaNode
import app.cash.redwood.yoga.YogaNodeFactory.create
import app.cash.redwood.yoga.enums.YogaDirection
import app.cash.redwood.yoga.enums.YogaEdge
import app.cash.redwood.yoga.enums.YogaMeasureMode
import app.cash.redwood.yoga.interfaces.YogaMeasureFunction
import app.cash.redwood.yoga.internal.YGSize

/**
 * A `ViewGroup` based on the Yoga layout engine.
 *
 *
 *
 * This class is designed to be as "plug and play" as possible.
 *
 *
 * <pre>`<YogaLayout
 * xmlns:android="http://schemas.android.com/apk/res/android"
 * xmlns:yoga="http://schemas.android.com/apk/com.facebook.yoga.android"
 * android:layout_width="match_owner"
 * android:layout_height="match_owner"
 * yoga:flex_direction="row"
 * yoga:padding_all="10dp"
 * >
 * <TextView
 * android:layout_width="match_owner"
 * android:layout_height="match_owner"
 * android:text="Hello, World!"
 * yoga:flex="1"
 * />
 * </YogaLayout>
`</pre> *
 *
 * Under the hood, all views added to this `ViewGroup` are laid out using flexbox rules
 * and the Yoga engine.
 */
internal class YogaLayout @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    private val mYogaNodes: MutableMap<View, YogaNode?>
    val yogaNode: YogaNode

    init {
        yogaNode = create()
        mYogaNodes = HashMap()
        yogaNode.setData(this)
        yogaNode.setMeasureFunction(ViewMeasureFunction())
        var layoutParams: LayoutParams? = null
        layoutParams = if (attrs != null) {
            LayoutParams(context, attrs)
        } else {
            generateDefaultLayoutParams() as LayoutParams
        }
        applyLayoutParams(layoutParams, yogaNode, this)
    }

    fun getYogaNodeForView(view: View): YogaNode? {
        return mYogaNodes[view]
    }

    /**
     * Adds a child view with the specified layout parameters.
     *
     * In the typical View is added, this constructs a `YogaNode` for this child and applies all
     * the `yoga:*` attributes.  The Yoga node is added to the Yoga tree and the child is added
     * to this ViewGroup.
     *
     * If the child is a [YogaLayout] itself, we do not construct a new Yoga node for that
     * child, but use its root node instead.
     *
     * If the child is a [VirtualYogaLayout], we also use its Yoga node, but we also instruct it
     * to transfer all of its children to this [YogaLayout] while preserving the Yoga tree (so
     * that the layout of its children is correct).  The [VirtualYogaLayout] is then not added
     * to the View hierarchy.
     *
     *
     * **Note:** do not invoke this method from
     * `#draw(android.graphics.Canvas)`, `onDraw(android.graphics.Canvas)`,
     * `#dispatchDraw(android.graphics.Canvas)` or any related method.
     *
     * @param child the child view to add
     * @param index the position at which to add the child or -1 to add last
     * @param params the layout parameters to set on the child
     */
    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        // Internal nodes (which this is now) cannot have measure functions
        yogaNode.setMeasureFunction(null)
        if (child is VirtualYogaLayout) {
            child.transferChildren(this)
            val childNode = child.yogaNode
            yogaNode.addChildAt(childNode, yogaNode.getChildCount())
            return
        }
        super.addView(child, index, params)

        // It is possible that addView is being called as part of a transferal of children, in which
        // case we already know about the YogaNode and only need the Android View tree to be aware
        // that we now own this child.  If so, we don't need to do anything further
        if (mYogaNodes.containsKey(child)) {
            return
        }
        val childNode: YogaNode?
        if (child is YogaLayout) {
            childNode = child.yogaNode
        } else {
            childNode = if (mYogaNodes.containsKey(child)) {
                mYogaNodes[child]
            } else {
                create()
            }
            childNode!!.setData(child)
            childNode.setMeasureFunction(ViewMeasureFunction())
        }
        val lp = child.layoutParams as LayoutParams
        applyLayoutParams(lp, childNode, child)
        mYogaNodes[child] = childNode
        yogaNode.addChildAt(childNode, yogaNode.getChildCount())
    }

    /**
     * Adds a view to this `ViewGroup` with an already given `YogaNode`.  Use
     * this function if you already have a Yoga node (and perhaps tree) associated with the view you
     * are adding, that you would like to preserve.
     *
     * @param child The view to add
     * @param node The Yoga node belonging to the view
     */
    fun addView(child: View, node: YogaNode?) {
        mYogaNodes[child] = node
        addView(child)
    }

    override fun removeView(view: View) {
        removeViewFromYogaTree(view, false)
        super.removeView(view)
    }

    override fun removeViewAt(index: Int) {
        removeViewFromYogaTree(getChildAt(index), false)
        super.removeViewAt(index)
    }

    override fun removeViewInLayout(view: View) {
        removeViewFromYogaTree(view, true)
        super.removeViewInLayout(view)
    }

    override fun removeViews(start: Int, count: Int) {
        for (i in start until start + count) {
            removeViewFromYogaTree(getChildAt(i), false)
        }
        super.removeViews(start, count)
    }

    override fun removeViewsInLayout(start: Int, count: Int) {
        for (i in start until start + count) {
            removeViewFromYogaTree(getChildAt(i), true)
        }
        super.removeViewsInLayout(start, count)
    }

    override fun removeAllViews() {
        val childCount = childCount
        for (i in 0 until childCount) {
            removeViewFromYogaTree(getChildAt(i), false)
        }
        super.removeAllViews()
    }

    override fun removeAllViewsInLayout() {
        val childCount = childCount
        for (i in 0 until childCount) {
            removeViewFromYogaTree(getChildAt(i), true)
        }
        super.removeAllViewsInLayout()
    }

    /**
     * Marks a particular view as "dirty" and to be relaid out.  If the view is not a child of this
     * [YogaLayout], the entire tree is traversed to find it.
     *
     * @param view the view to mark as dirty
     */
    fun invalidate(view: View) {
        if (mYogaNodes.containsKey(view)) {
            mYogaNodes[view]!!.dirty()
            return
        }
        val childCount = yogaNode.getChildCount()
        for (i in 0 until childCount) {
            val yogaNode = yogaNode.getChildAt(i)
            if (yogaNode.getData() is YogaLayout) {
                (yogaNode.getData() as YogaLayout?)!!.invalidate(view)
            }
        }
        invalidate()
    }

    private fun removeViewFromYogaTree(view: View, inLayout: Boolean) {
        val node = mYogaNodes[view] ?: return
        val owner = node.getOwner()
        for (i in 0 until owner!!.getChildCount()) {
            if (owner.getChildAt(i) == node) {
                owner.removeChildAt(i)
                break
            }
        }
        node.setData(null)
        mYogaNodes.remove(view)
        if (inLayout) {
            yogaNode.calculateLayout(YogaConstants.UNDEFINED, YogaConstants.UNDEFINED)
        }
    }

    private fun applyLayoutRecursive(node: YogaNode, xOffset: Float, yOffset: Float) {
        val view = node.getData() as View?
        if (view != null && view !== this) {
            if (view.visibility == GONE) {
                return
            }
            val left = Math.round(xOffset + node.getLayoutX())
            val top = Math.round(yOffset + node.getLayoutY())
            view.measure(
                MeasureSpec.makeMeasureSpec(
                    Math.round(node.getLayoutWidth()),
                    MeasureSpec.EXACTLY
                ),
                MeasureSpec.makeMeasureSpec(
                    Math.round(node.getLayoutHeight()),
                    MeasureSpec.EXACTLY
                )
            )
            view.layout(left, top, left + view.measuredWidth, top + view.measuredHeight)
        }
        val childrenCount = node.getChildCount()
        for (i in 0 until childrenCount) {
            if (this == view) {
                applyLayoutRecursive(node.getChildAt(i), xOffset, yOffset)
            } else if (view is YogaLayout) {
                continue
            } else {
                applyLayoutRecursive(
                    node.getChildAt(i),
                    xOffset + node.getLayoutX(),
                    yOffset + node.getLayoutY()
                )
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // Either we are a root of a tree, or this function is called by our owner's onLayout, in which
        // case our r-l and b-t are the size of our node.
        if (parent !is YogaLayout) {
            createLayout(
                MeasureSpec.makeMeasureSpec(r - l, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(b - t, MeasureSpec.EXACTLY)
            )
        }
        applyLayoutRecursive(yogaNode, 0f, 0f)
    }

    /**
     * This function is mostly unneeded, because Yoga is doing the measuring.  Hence we only need to
     * return accurate results if we are the root.
     *
     * @param widthMeasureSpec the suggested specification for the width
     * @param heightMeasureSpec the suggested specification for the height
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (parent !is YogaLayout) {
            createLayout(widthMeasureSpec, heightMeasureSpec)
        }
        setMeasuredDimension(
            Math.round(yogaNode.getLayoutWidth()),
            Math.round(yogaNode.getLayoutHeight())
        )
    }

    private fun createLayout(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (heightMode == MeasureSpec.EXACTLY) {
            yogaNode.setHeight(heightSize.toFloat())
        }
        if (widthMode == MeasureSpec.EXACTLY) {
            yogaNode.setWidth(widthSize.toFloat())
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            yogaNode.setMaxHeight(heightSize.toFloat())
        }
        if (widthMode == MeasureSpec.AT_MOST) {
            yogaNode.setMaxWidth(widthSize.toFloat())
        }
        yogaNode.calculateLayout(YogaConstants.UNDEFINED, YogaConstants.UNDEFINED)
    }

    override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return LayoutParams(p)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams
    }

    /**
     * `YogaLayout.LayoutParams` are used by views to tell [YogaLayout] how they want to
     * be laid out.  More precisely, the specify the yoga parameters of the view.
     *
     *
     *
     * This is actually mostly a wrapper around a `SparseArray` that holds a mapping between
     * styleable id's (`R.styleable.yoga_yg_*`) and the float of their values.  In cases where
     * the value is an enum or an integer, they should first be cast to int (with rounding) before
     * using.
     */
    class LayoutParams : ViewGroup.LayoutParams {
        /**
         * Constructs a set of layout params from a source set.  In the case that the source set is
         * actually a [YogaLayout.LayoutParams], we can copy all the yoga attributes.  Otherwise
         * we start with a blank slate.
         *
         * @param source The layout params to copy from
         */
        constructor(source: ViewGroup.LayoutParams?) : super(source) {}

        /**
         * Constructs a set of layout params, given width and height specs.  In this case, we can set
         * the `yoga:width` and `yoga:height` if we are given them explicitly.  If other
         * options (such as `match_owner` or `wrap_content` are given, then the owner
         * LayoutParams will store them, and we deal with them during layout. (see
         * [YogaLayout.createLayout])
         *
         * @param width the requested width, either a pixel size, `WRAP_CONTENT` or
         * `MATCH_PARENT`.
         * @param height the requested height, either a pixel size, `WRAP_CONTENT` or
         * `MATCH_PARENT`.
         */
        constructor(width: Int, height: Int) : super(width, height) {}

        /**
         * Constructs a set of layout params, given attributes.  Grabs all the `yoga:*`
         * defined in `ALL_YOGA_ATTRIBUTES` and collects the ones that are set in `attrs`.
         *
         * @param context the application environment
         * @param attrs the set of attributes from which to extract the yoga specific attributes
         */
        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    }

    /**
     * Wrapper around measure function for yoga leaves.
     */
    class ViewMeasureFunction : YogaMeasureFunction {
        /**
         * A function to measure leaves of the Yoga tree.  Yoga needs some way to know how large
         * elements want to be.  This function passes that question directly through to the relevant
         * `View`'s measure function.
         *
         * @param node The yoga node to measure
         * @param width The suggested width from the owner
         * @param widthMode The type of suggestion for the width
         * @param height The suggested height from the owner
         * @param heightMode The type of suggestion for the height
         * @return A measurement output (`YogaMeasureOutput`) for the node
         */
        public override fun measure(
            node: YogaNode,
            width: Float,
            widthMode: YogaMeasureMode,
            height: Float,
            heightMode: YogaMeasureMode
        ): YGSize {
            val view = node.getData() as View?
            if (view == null || view is YogaLayout) {
                return make(0, 0)
            }
            val widthMeasureSpec = MeasureSpec.makeMeasureSpec(
                width.toInt(),
                viewMeasureSpecFromYogaMeasureMode(widthMode)
            )
            val heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                height.toInt(),
                viewMeasureSpecFromYogaMeasureMode(heightMode)
            )
            view.measure(widthMeasureSpec, heightMeasureSpec)
            return make(view.measuredWidth, view.measuredHeight)
        }

        private fun viewMeasureSpecFromYogaMeasureMode(mode: YogaMeasureMode): Int {
            return if (mode === YogaMeasureMode.AT_MOST) {
                MeasureSpec.AT_MOST
            } else if (mode === YogaMeasureMode.EXACTLY) {
                MeasureSpec.EXACTLY
            } else {
                MeasureSpec.UNSPECIFIED
            }
        }
    }

    companion object {
        /**
         * Applies the layout parameters to the YogaNode.  That is, this function is a translator from
         * `yoga:X="Y"` to `yogaNode.setX(Y);`, with some reasonable defaults.
         *
         *
         *
         * If the SDK version is high enough, and the `yoga:direction` is not set on
         * the component, the direction (LTR or RTL) is set according to the locale.
         *
         *
         *
         * The attributes `padding_top`, `padding_right` etc. default to those of the view's
         * drawable background, if it has one.
         *
         * @param layoutParameters The source set of params
         * @param node The destination node
         */
        fun applyLayoutParams(
            layoutParameters: LayoutParams?,
            node: YogaNode?,
            view: View
        ) {
            val configuration = view.resources.configuration
            if (configuration.layoutDirection == LAYOUT_DIRECTION_RTL) {
                node!!.setDirection(YogaDirection.RTL)
            }
            val background = view.background
            if (background != null) {
                val backgroundPadding = Rect()
                if (background.getPadding(backgroundPadding)) {
                    node!!.setPadding(YogaEdge.LEFT, backgroundPadding.left.toFloat())
                    node.setPadding(YogaEdge.TOP, backgroundPadding.top.toFloat())
                    node.setPadding(YogaEdge.RIGHT, backgroundPadding.right.toFloat())
                    node.setPadding(YogaEdge.BOTTOM, backgroundPadding.bottom.toFloat())
                }
            }
        }
    }
}
