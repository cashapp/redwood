import RedwoodLazylayoutUIViewTestKt
import UIKit

final class UIViewLazyListAsFlexContainerTestHost: KotlinHostingXCTestCase<UIViewLazyListAsFlexContainerTest> {
    override class func initTest(name: String) -> UIViewLazyListAsFlexContainerTest {
        return UIViewLazyListAsFlexContainerTest(callback: SnapshotTestingCallback(named: name))
    }
}
