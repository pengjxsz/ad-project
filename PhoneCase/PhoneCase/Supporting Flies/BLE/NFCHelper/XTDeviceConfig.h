//
//  XTDeviceConfig.h
//  EinkAPP
//
//  Created by xthh on 2025/1/21.
//  Copyright © 2025 复旦微电子集团股份科技有限公司. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "XTEnum.h"

NS_ASSUME_NONNULL_BEGIN

//读取设备配置
@interface XTDeviceConfig : NSObject<NSSecureCoding>

//根据NFC tag 判断是不是纯NFC
@property (nonatomic, assign) XTBrushType brushType;

//操作结果，YES成功，NO失败
//@property (nonatomic, assign) BOOL isSuccess;

//厂商代码
@property (nonatomic, copy) NSString *manufacturerCode;
//厂商名称
@property (nonatomic, copy) NSString *manufacturerName;
//颜色支持代码
@property (nonatomic, copy) NSString *screenColorCode;
//颜色支持名称
@property (nonatomic, copy) NSString *screenColorName;
//屏幕宽度
@property (nonatomic, assign) NSInteger screenWidth;
//屏幕高度
@property (nonatomic, assign) NSInteger screenHeight;

@property (nonatomic, assign) NSInteger deviceNumber;

//图片容量
@property (nonatomic, assign) NSInteger imageCount;
//扫描方式，00垂直扫描，01水平扫描
@property (nonatomic, assign) ScanType scanType;
//图像合成模式
@property (nonatomic, assign) CombineType combineType;
//支持传输模式
@property (nonatomic, assign) NSInteger transMode;
//支持颜色数量
@property (nonatomic, assign) NSInteger colorCount;
//颜色值定义字典，单色只用0,1，双色用00,01,10,11
@property (nonatomic, strong) NSDictionary *colorDic;
//appID(老版本)/OTP(新版本)
@property (nonatomic, copy) NSString *appID;
//uuID
@property (nonatomic, copy) NSString *uuID;
//是否需要验pin
@property (nonatomic, assign) BOOL checkPin;
//有源还是无源模式
@property (nonatomic, assign) BOOL hasPower;
//是否支持压缩
@property (nonatomic, assign) BOOL supportCompress;
//版本号，20 第二代
@property (nonatomic, assign) NSInteger versionCode;
@property (nonatomic, copy) NSString *version;
//写几个图像区
@property (nonatomic, assign) NSInteger writeImageCount;
//pin码
@property (nonatomic, copy) NSString *pin;
//是否需要水平翻转图片 YES 需要水平翻转 NO 不需要水平翻转
@property (nonatomic, assign) BOOL flipHorizontal;
//是否需要垂直翻转图片 YES 需要垂直翻转 NO 不需要垂直翻转
@property (nonatomic, assign) BOOL flipVertical;

//序列化该类的对象
+(void)saveLocalCfg:(XTDeviceConfig *)XTDeviceConfig;

//反序列化该类的对象
+ (XTDeviceConfig *)readLocalCfg;

+ (BOOL)isPrismDevice;

//是不是手机壳
- (BOOL)isPhoneCaseDevice;

//一般用于nfc供电 发送13指令
- (BOOL)isDeviceSend13cmd;
    
/**
 *  判断当前设备是否需要通过旋转来刷新图像（刷图）
 *
 *  @return YES表示需要旋转刷图，NO表示不需要
 */
- (BOOL)needsRotationToRefreshImage;

/**
 *  NFC读取设备信息时，判断其传图方式是否为蓝牙
 *
 *  @return YES表示是蓝牙传图，NO表示不是
 */
- (BOOL)isBluetoothTransferWhenNFCReading;

+ (int)getDeviceColorType:(NSString *)screenColorCode;

+ (int)getDeviceBleMaxMtu:(NSString *)screenColorCode;

@end

NS_ASSUME_NONNULL_END
