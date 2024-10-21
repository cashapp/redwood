import Foundation
import UIKit
import CounterKt

class CounterViewController : UIViewController {
    private var delegate: CounterViewControllerDelegate!

    override func viewDidLoad() {
        let redwoodUIView = RedwoodUIView()

        let rootView = redwoodUIView.value
        rootView.translatesAutoresizingMaskIntoConstraints = false

        view.addSubview(rootView)
        rootView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        rootView.topAnchor.constraint(equalTo: view.topAnchor).isActive = true
        rootView.widthAnchor.constraint(equalTo: view.widthAnchor).isActive = true
        rootView.heightAnchor.constraint(equalTo: view.heightAnchor).isActive = true

        self.delegate = CounterViewControllerDelegate(redwoodUIView: redwoodUIView)
    }

    deinit {
        delegate.dispose()
    }
}
