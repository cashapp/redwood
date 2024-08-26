import RedwoodLayoutUIViewTestKt
import UIKit

final class UIViewLazyListTestHost: KotlinHostingXCTestCase<UIViewLazyListTest> {
    override class func initTest(name: String) -> UIViewLazyListTest {
        return UIViewLazyListTest(callback: SnapshotTestingCallback(named: name))
    }
}
