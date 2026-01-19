//
//  DataSigner.h
//  PhoneCase
//
//  Created by XTMacMini on 2025/10/28.
//

#import <Foundation/Foundation.h>
#import <CommonCrypto/CommonCrypto.h>
#import <Security/Security.h>

NS_ASSUME_NONNULL_BEGIN

@interface DataSigner : NSObject

/// 计算原始数据的SHA-256哈希值（32字节）
/// @param originalData 原始数据（非空）
/// @return 32字节哈希数据，失败返回nil
+ (NSData *)calculateSHA256HashWithOriginalData:(NSData *)originalData;

#pragma mark - 2. 生成时间戳数据（大端序）
/// 生成当前时间的Unix时间戳（秒级），转换为4字节大端序数据
/// @return 4字节大端序时间戳数据
+ (NSData *)generateTimestampData;

/// 将100B发送数据转换为100元素16进制数组（每个元素为2位大写16进制，如@"A3"）
/// @param sendData100B 生成的100B发送数据
/// @return 100元素NSArray<NSString *>，失败返回nil
+ (NSArray<NSString *> *)convert100BDataToHexArray:(NSData *)sendData100B;

@end

NS_ASSUME_NONNULL_END
