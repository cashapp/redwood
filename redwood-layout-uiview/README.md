# Redwood Layout UIKit

This artifact includes `Row` and `Column` widget implementations for UIKit.

Currently, `UIViewRedwoodLayoutWidgetFactory` requires a `UIScrollView` factory due to Kotlin-Swift interop issues. Here's an implementation you can copy into your Swift project:

```swift
// Add the UIView widget factory to your main widget factory.
var RedwoodLayout: WidgetRedwoodLayoutWidgetFactory =
    Redwood_layout_uiviewUIViewRedwoodLayoutWidgetFactory(viewFactory: UIScrollViewFactoryImpl())

// Copy this class into the same file.
class UIScrollViewFactory: Redwood_layout_uiviewRedwoodUIScrollViewFactory {
    func create(delegate: Redwood_layout_uiviewRedwoodUIScrollViewDelegate) -> UIScrollView {
        return DelegateUIScrollView(delegate)
    }

    class DelegateUIScrollView : UIScrollView {
        private var _delegate: Redwood_layout_uiviewRedwoodUIScrollViewDelegate

        init(_ delegate: Redwood_layout_uiviewRedwoodUIScrollViewDelegate) {
            self._delegate = delegate
            super.init(frame: .zero)
        }

        required init?(coder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }

        override var intrinsicContentSize: CGSize {
            let intrinsicContentSize = _delegate.intrinsicContentSize
            return CGSize(width: intrinsicContentSize.width, height: intrinsicContentSize.height)
        }

        override func sizeThatFits(_ size: CGSize) -> CGSize {
            let inputSize = Redwood_layout_uiviewDoubleSize(width: size.width, height: size.height)
            let outputSize = _delegate.sizeThatFits(size: inputSize)
            return CGSize(width: outputSize.width, height: outputSize.height)
        }

        override func layoutSubviews() {
            _delegate.layoutSubviews()
        }
    }
```
