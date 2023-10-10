import SnapshotTesting
import RedwoodLayoutUIViewTestKt
import UIKit

final class UIViewFlexContainerTestHost: KotlinHostingXCTestCase<UIViewFlexContainerTest> {
    override class func initTest(name: String) -> UIViewFlexContainerTest {
        return UIViewFlexContainerTest(callback: Callback(named: name))
    }
}

private class Callback : UIViewFlexContainerTestCallback {
    private let testName: String

    init(named testName: String) {
        self.testName = testName
    }

    func verifySnapshot(view: UIView, name: String?) {
        var snapshotName = testName
        if (name != nil) {
            snapshotName = "\(testName)-\(name!)"
        }
        assertSnapshot(of: view, as: .image, named: snapshotName, record: true)
    }
}
