import SnapshotTesting
import RedwoodLayoutUIViewTestKt
import UIKit

final class UIViewFlexContainerTestHost: KotlinHostingXCTestCase<UIViewFlexContainerTest> {
    override class func initTest(name: String) -> UIViewFlexContainerTest {
        return UIViewFlexContainerTest(callback: SnapshotTestingCallback(named: name))
    }
}
