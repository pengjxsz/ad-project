//
//  test.m
//  iosApp
//
//  Created by XAIO on 2/12/25.
//  Copyright © 2025 orgName. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SigAD-Swift.h" // 替换为您的 Product Module Name


// SwiftExportTest.m

// !!! 替换 YOUR_PRODUCT_MODULE_NAME 为您 iosApp Target 的 Product Module Name !!!

void testSwiftExport() {
    // 尝试声明并初始化 IOSAppkitManager
    // 注意：Swift 类在 Objective-C 中是引用类型，使用 *
    IOSAppkitManager *manager;

    // 尝试调用其默认构造函数（假设有一个无参或参数被 ObjC 兼容的构造函数）
    // 如果您的构造函数需要参数，需要按 ObjC 格式传递
    manager = [[IOSAppkitManager alloc] init];
    
    // 如果以上代码能够编译通过（即没有 "Cannot find interface declaration for 'IOSAppkitManager'" 错误）
    // 则证明您的 Swift 类已成功暴露给 Objective-C 桥接层。
    
    NSLog(@"IOSAppkitManager successfully found by Objective-C.");
}
