import RedwoodWidgetUIViewTestKt
import UIKit

final class UIViewRedwoodViewTestHost: KotlinHostingXCTestCase<UIViewRedwoodViewTest> {
    override class func initTest(name: String) -> UIViewRedwoodViewTest {
        return UIViewRedwoodViewTest(callback: SnapshotTestingCallback(named: name))
    }
}
