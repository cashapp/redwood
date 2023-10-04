import SnapshotTesting
import RedwoodLayoutUIViewTestKt
import UIKit

final class UIViewSpacerTestHost: KotlinHostingXCTestCase<UIViewSpacerTest> {
    override class func initTest(name: String) -> UIViewSpacerTest {
        return UIViewSpacerTest(callback: Callback(named: name))
    }
}

private class Callback : UIViewSpacerTestCallback {
    private let name: String

    init(named name: String) {
        self.name = name
    }

    func verifySnapshot(view: UIView) {
        assertSnapshot(of: view, as: .image, named: name)
    }
}
