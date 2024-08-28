import RedwoodLazylayoutUIViewTestKt
import SnapshotTesting
import UIKit

final class SnapshotTestingCallback : UIViewSnapshotCallback {
    private let testName: String
    private let fileName: StaticString

    init(named testName: String, _ fileName: StaticString = #file) {
        self.testName = testName
        self.fileName = fileName
    }

    func verifySnapshot(view: UIView, name: String?, delay: TimeInterval = 0.0) {
        // Set `record` to true to generate new snapshots. Be sure to revert that before committing!
        // Note that tests always fail when `record` is true.
        assertSnapshot(of: view, as: .wait(for: delay, on: .image), named: name, record: false, file: fileName, testName: testName)
    }
}
