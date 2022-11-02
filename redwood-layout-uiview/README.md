# Redwood Layout UIKit

This artifact includes `Row` and `Column` widget implementations for UIKit.

Currently, `UIViewRedwoodLayoutWidgetFactory` requires a `UIScrollView` factory due to Kotlin-Swift interop issues. Here's an implementation you can copy into your Swift project:

```swift
class UIScrollViewFactoryImpl: Redwood_layout_uiviewRedwoodUIScrollViewFactory {
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
            return _delegate.intrinsicContentSize
        }

        override func sizeThatFits(_ size: CGSize) -> CGSize {
            return _delegate.sizeThatFits(size)
        }

        override func layoutSubviews() {
            super.layoutSubviews()
            _delegate.layoutSubviews()
        }
    }
}
```
