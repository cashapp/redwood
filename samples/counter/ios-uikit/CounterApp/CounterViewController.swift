import Foundation
import UIKit
import CounterKt

class CounterViewController : UIViewController {
    private var delegate: CounterViewControllerDelegate!

    override func viewDidLoad() {
        let root = UIViewRoot()

        let rootView = root.value
        rootView.translatesAutoresizingMaskIntoConstraints = false

        view.addSubview(root.value)
        rootView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        rootView.topAnchor.constraint(equalTo: view.topAnchor).isActive = true
        rootView.widthAnchor.constraint(equalTo: view.widthAnchor).isActive = true
        rootView.heightAnchor.constraint(equalTo: view.heightAnchor).isActive = true

        self.delegate = CounterViewControllerDelegate(root: root)
    }

    deinit {
        delegate.dispose()
    }
}
