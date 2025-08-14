import RedwoodWidgetUIViewTestKt
import UIKit

final class UIViewRedwoodViewFillWrapTestHost: KotlinHostingXCTestCase<UIViewRedwoodViewFillWrapTest> {
    override class func initTest(name: String) -> UIViewRedwoodViewFillWrapTest {
        return UIViewRedwoodViewFillWrapTest(callback: SnapshotTestingCallback(named: name))
    }
}
