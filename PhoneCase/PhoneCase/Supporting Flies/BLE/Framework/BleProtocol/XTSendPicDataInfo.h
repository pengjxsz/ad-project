//
//  XTSendPicDataInfo.h
//  ChromaInk
//
//  Created by XTMacMini on 2025/8/26.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

//用于表达发送图片信息
@interface XTSendPicDataInfo : NSObject

// 图片的抖点数据
@property (nonatomic, strong) NSMutableArray *dataArr;

// 设备的颜色信息
@property (nonatomic, assign) NSInteger deviceColorType;

// 设备编号
@property (nonatomic, assign) NSInteger deviceNumber;

// 设备屏幕的宽度
@property (nonatomic, assign) NSInteger width;

// 设备屏幕的高度
@property (nonatomic, assign) NSInteger height;

// 设备屏幕的高度
@property (nonatomic, assign) NSInteger bagSize;

// 设备屏幕的高度
@property (nonatomic, assign) BOOL isNeedCompress;

@end

NS_ASSUME_NONNULL_END
