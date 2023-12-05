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
        // Set `record` to true to generate new snapshots. Be sure to revert that before committing!
        // Note that tests always fail when `record` is true.
        assertSnapshot(of: view, as: .image, named: name, record: true, file: fileName, testName: testName)
    }
}
