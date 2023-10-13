import RedwoodLayoutUIViewTestKt
import UIKit

final class UIViewSpacerTestHost: KotlinHostingXCTestCase<UIViewSpacerTest> {
    override class func initTest(name: String) -> UIViewSpacerTest {
        return UIViewSpacerTest(callback: SnapshotTestingCallback(named: name))
    }
}
