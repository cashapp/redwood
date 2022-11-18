# Redwood Layout UIKit

This artifact includes `Row` and `Column` widget implementations for UIKit.

Currently, `UIViewRedwoodLayoutWidgetFactory` requires a `UIScrollView` factory due to Kotlin-Swift interop issues. Here's an implementation you can copy into your Swift project:

```swift
private class UIScrollViewFactory: Redwood_layout_uiviewRedwoodUIScrollViewFactory {
    func create(delegate: Redwood_layout_uiviewRedwoodUIScrollViewDelegate) -> UIScrollView {
        return DelegateUIScrollView(delegate)
    }
}

private class DelegateUIScrollView : UIScrollView {
    private var layoutDelegate: Redwood_layout_uiviewRedwoodUIScrollViewDelegate

    init(_ delegate: Redwood_layout_uiviewRedwoodUIScrollViewDelegate) {
        self.layoutDelegate = delegate
        super.init(frame: .zero)
    }

    required init?(coder: NSCoder) {
        fatalError("unimplemented")
    }

    override var intrinsicContentSize: CGSize {
        let outputSize = layoutDelegate.intrinsicContentSize
        return CGSize(width: outputSize.width, height: outputSize.height)
    }

    override func sizeThatFits(_ size: CGSize) -> CGSize {
        let inputSize = Redwood_layout_uiviewUnsafeSize(width: size.width, height: size.height)
        let outputSize = layoutDelegate.sizeThatFits(size: inputSize)
        return CGSize(width: outputSize.width, height: outputSize.height)
    }

    override func setNeedsLayout() {
        super.setNeedsLayout()
        layoutDelegate.setNeedsLayout()
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        layoutDelegate.layoutSubviews()
    }
}
```

Then add the widget factory to your main widget factory:

```swift
var RedwoodLayout: WidgetRedwoodLayoutWidgetFactory =
    Redwood_layout_uiviewUIViewRedwoodLayoutWidgetFactory(viewFactory: UIScrollViewFactory())
```
