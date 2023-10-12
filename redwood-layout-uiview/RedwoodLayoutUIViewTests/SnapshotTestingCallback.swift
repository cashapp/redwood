import RedwoodLayoutUIViewTestKt
import SnapshotTesting
import UIKit

final class SnapshotTestingCallback : UIViewSnapshotCallback {
    private let testName: String
    private let fileName: StaticString

    init(named testName: String, _ fileName: StaticString = #file) {
        self.testName = testName
        self.fileName = fileName
    }

    func verifySnapshot(view: UIView, name: String?) {
        assertSnapshot(of: view, as: .image, named: name, file: fileName, testName: testName)
    }
}
