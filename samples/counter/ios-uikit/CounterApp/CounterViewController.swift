import Foundation
import UIKit
import shared

class CounterViewController : UIViewController {
    private var displayLink: CADisplayLink!
    private var delegate: CounterViewControllerDelegate!

    override func viewDidLoad() {
        let container = UIStackView()
        container.axis = .horizontal
        container.alignment = .center
        container.distribution = .fillEqually
        container.translatesAutoresizingMaskIntoConstraints = false
        
        view.addSubview(container)
        container.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        container.topAnchor.constraint(equalTo: view.topAnchor).isActive = true
        container.widthAnchor.constraint(equalTo: view.widthAnchor).isActive = true
        container.heightAnchor.constraint(equalTo: view.heightAnchor).isActive = true
        
        let delegate = CounterViewControllerDelegate(root: container)
        self.delegate = delegate
    }

    override func viewDidAppear(_ animated: Bool) {
        let displayLink = CADisplayLink.init(target: self, selector: #selector(tickClock))
        displayLink.add(to: .current, forMode: .default)
        self.displayLink = displayLink
    }

    @objc func tickClock() {
        delegate.tickClock()
    }

    override func viewDidDisappear(_ animated: Bool) {
        displayLink.invalidate()
    }

    deinit {
        delegate.dispose()
    }
}
