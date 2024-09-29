// Created by Michał Laskowski on 10/02/2020.
// Copyright © 2020 Michał Laskowski. All rights reserved.
// Derived from https://github.com/michallaskowski/kuiks/blob/c8500df2a55fe031a1bcf546c771c3d9f30dbf90/NativeTestBase/ObjC/TestBaseForSelector.m
// Licensed as Apache-2.0.

#import <Foundation/Foundation.h>
#include "KotlinHostingXCTestCaseHelper.h"
#import "objc/runtime.h"

@implementation KotlinHostingXCTestCaseHelper

+(void)createTestMethods {
    [NSException raise:@"NotImplemented" format:@"Subclasses must implement a valid createTestMethods method"];
}

/**
 This is overriden in order to be able to trigger tests from Test Navigator panel. Method can not be overriden in Swift, because it needs also
 to override `testCaseWithInvocation:`. And using NSInvocation is not possible in Swift. Hence this Obj-C TestBase class, and this method.
 */
+(instancetype)testCaseWithSelector:(SEL)selector {
    [self createTestMethods];
    return [super testCaseWithSelector:selector];
}

@end
