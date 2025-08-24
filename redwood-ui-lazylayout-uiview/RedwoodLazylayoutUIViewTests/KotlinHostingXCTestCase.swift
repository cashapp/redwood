import Foundation
import XCTest

/**
 * An abstract base class for hosting Kotlin test classes in Swift projects.
 *
 * Subclasses must override `initTest(name:)` to create instances of `KotlinType`.
 */
open class KotlinHostingXCTestCase<KotlinTest>: KotlinHostingXCTestCaseHelper where KotlinTest: NSObject {
    /**
     * Create an instance of `KotlinTest` for invoking test method `name`.
     */
    open class func initTest(name: String) -> KotlinTest {
        fatalError("Missing override for initTest(name:)")
    }

    public override class func createTestMethods() {
        var targetClass: AnyClass = KotlinTest.self
        while (targetClass != NSObject.self) {
            var methodCount: UInt32 = 0
            let methodList = class_copyMethodList(targetClass, &methodCount)
            if let methodList = methodList {
                defer { free(methodList) }
                for i in 0..<Int(methodCount) {
                    let selector = method_getName(methodList[i])
                    let selectorName = NSStringFromSelector(selector)
                    if selectorName.hasPrefix("test") && !selectorName.contains(":") {
                        let kotlinTest = initTest(name: selectorName)
                        createTestMethod(target: kotlinTest, selector: selector)
                    }
                }
            }

            guard let nextClass = class_getSuperclass(targetClass) else {
                // Should be impossible, all types will bottom out in NSObject first.
                fatalError("For \(KotlinTest.self), type \(targetClass)'s superclass returned nil")
            }
            targetClass = nextClass
        }
    }

    private static func createTestMethod(target: KotlinTest, selector: Selector) {
        let block: @convention(block) () -> Void = {
            if target.responds(to: #selector(setUp)) {
                target.perform(#selector(setUp))
            }
            defer {
                if target.responds(to: #selector(tearDown)) {
                    target.perform(#selector(tearDown))
                }
            }

            target.perform(selector)
        }
        let implementation = imp_implementationWithBlock(block)
        class_addMethod(self, selector, implementation, "v@:")
    }

    public override class var defaultTestSuite: XCTestSuite {
        createTestMethods()
        return super.defaultTestSuite
    }
}
