//
//  NFCHelper.h
//
//  Created by lubozhi on 2018/7/9.
//  Copyright © 2018年 复旦微电子集团股份科技有限公司. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <CoreNFC/CoreNFC.h>
#import <CoreBluetooth/CoreBluetooth.h>
#import "XTDeviceConfig.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSUInteger, OperationType) {
    initDevice              = 0,        //初始化手机壳
    bindDevice              = 1,        //bind设备配置
    WriteScreen             = 2,        //写屏+刷屏
    OTA                     = 3         //ota
};

// 定义初始化完成的回调Block（参数为错误码，0表示成功）
typedef void(^PhoneCaseInitializeCompletion)(NSInteger errorCode);

// 定义读取设备配置完成的回调 Block
// sdata 读操作：设备公钥，64B 、芯片ID，16B
typedef void(^NfcReadConfigCompletionBlock)(XTDeviceConfig *_Nullable config, NSData *_Nullable sdata, NSError * _Nullable error);

// 导入图片完成的回调 Block
// result：成功时返回的结果信息（原方法的 NSString 返回值），失败时为 nil
// error：失败时返回的错误信息，成功时为 nil
typedef void(^ImportImageCompletionBlock)(NSData * _Nullable signData, NSError * _Nullable error);

@interface NFCHelper: NSObject

//强持有
@property(nonatomic, strong) CBPeripheral  *connectedPeripheral;

//nfc reader是否正在读取
@property(nonatomic, assign) BOOL isReading;

/**
 单例
 */
+ (instancetype)shareInstance;

/**
 初始化手机壳设备（让手机壳生成key）

 @param completion 初始化完成的回调，参数为错误码：
                   0 - 成功
                   1 - 无效参数
                   2 - 写Flash失败
                   3 - Flash被保护
                   4 - 未找到密钥
                   5 - 密码操作失败
                   6 - 无效签名
                   7 - 操作无效
                   8 - 密钥已存在
 */
- (void)initializePhoneCaseDeviceWithCompletion:(PhoneCaseInitializeCompletion)completion;

/**
 读取设备配置参数
 
 @param completion 读取完成的回调 Block，返回设备配置对象（XTDeviceConfig）
 */
- (void)getbindDeviceWithPublicKey:(NSMutableArray *)PublicKey
                        completion:(NfcReadConfigCompletionBlock)completion;

/**
 导入图片到设备
 
 @param imageDataHexArr 图片数据的十六进制数组
 @param screenCfg 设备屏幕配置
 @param pageSize 页大小
 @param completion 完成回调（成功时 error 为 nil，失败时 result 为 nil）
 */
- (void)importImageToDeviceWithImageDataHexArr:(NSMutableArray *)imageDataHexArr
                                    signHexArr:(NSMutableArray *)signHexArr
                                     screenCfg:(XTDeviceConfig *)screenCfg
                                      pageSize:(int)pageSize
                                    completion:(ImportImageCompletionBlock)completion;

/**
 导入OTA文件到设备
 
 @param otaDataHexArr OTA数据的十六进制数组
 @param screenCfg 设备屏幕配置
 @param pageSize 页大小
 @param completion 完成回调（成功时 error 为 nil，失败时 result 为 nil）
 */
- (void)importOtaDataToDeviceWithHexArr:(NSMutableArray *)otaDataHexArr
                              screenCfg:(XTDeviceConfig *)screenCfg
                             signHexArr:(NSMutableArray *)signHexArr
                               pageSize:(int)pageSize
                             completion:(ImportImageCompletionBlock)completion;


@end

NS_ASSUME_NONNULL_END
