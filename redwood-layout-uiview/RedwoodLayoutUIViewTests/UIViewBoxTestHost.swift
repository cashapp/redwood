import RedwoodLayoutUIViewTestKt
import UIKit

final class UIViewBoxTestHost: KotlinHostingXCTestCase<UIViewBoxTest> {
    override class func initTest(name: String) -> UIViewBoxTest {
        return UIViewBoxTest(callback: SnapshotTestingCallback(named: name))
    }
}
