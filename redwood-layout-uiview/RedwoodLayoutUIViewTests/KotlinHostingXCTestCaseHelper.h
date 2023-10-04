// Created by Michał Laskowski on 10/02/2020.
// Copyright © 2020 Michał Laskowski. All rights reserved.
// Derived from https://github.com/michallaskowski/kuiks/blob/c8500df2a55fe031a1bcf546c771c3d9f30dbf90/NativeTestBase/ObjC/TestBaseForSelector.h
// Licensed as Apache-2.0.

#ifndef TestBase_h
#define TestBase_h
@import XCTest;

NS_ASSUME_NONNULL_BEGIN

@interface KotlinHostingXCTestCaseHelper : XCTestCase

+(void)createTestMethods;

@end

NS_ASSUME_NONNULL_END

#endif /* TestBase_h */
