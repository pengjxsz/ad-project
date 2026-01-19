//
//  DataSigner.m
//  PhoneCase
//
//  Created by XTMacMini on 2025/10/28.
//

#import "DataSigner.h"

@implementation DataSigner

#pragma mark - 1. 计算原始数据的SHA-256哈希
/// 计算原始数据的SHA-256哈希值（32字节）
/// @param originalData 原始数据（非空）
/// @return 32字节哈希数据，失败返回nil
+ (NSData *)calculateSHA256HashWithOriginalData:(NSData *)originalData {
    // 参数校验
    if (!originalData || originalData.length == 0) {
        NSLog(@"❌ 计算哈希失败：原始数据为空");
        return nil;
    }
    
    // 计算SHA-256哈希
    uint8_t hashBytes[CC_SHA256_DIGEST_LENGTH];
    CC_SHA256_CTX hashContext;
    CC_SHA256_Init(&hashContext);
    CC_SHA256_Update(&hashContext, originalData.bytes, (CC_LONG)originalData.length);
    CC_SHA256_Final(hashBytes, &hashContext);
    
    return [NSData dataWithBytes:hashBytes length:CC_SHA256_DIGEST_LENGTH];
}


#pragma mark - 2. 生成时间戳数据（大端序）
/// 生成当前时间的Unix时间戳（秒级），转换为4字节大端序数据
/// @return 4字节大端序时间戳数据
+ (NSData *)generateTimestampData {
    // 获取当前时间戳（秒）
    uint32_t timestamp = (uint32_t)time(NULL);
    // 转换为大端序（网络字节序）
    uint32_t bigEndianTimestamp = CFSwapInt32HostToBig(timestamp);
    // 封装为NSData
    return [NSData dataWithBytes:&bigEndianTimestamp length:4];
}


+ (NSArray<NSString *> *)convert100BDataToHexArray:(NSData *)sendData100B {
    if (!sendData100B || sendData100B.length != 100) {
        NSLog(@"❌ convert100BDataToHexArray: 数据为空或长度不为100B");
        return nil;
    }
    
    const uint8_t *bytes = (const uint8_t *)sendData100B.bytes;
    NSMutableArray<NSString *> *hexArray = [NSMutableArray arrayWithCapacity:100];
    
    for (int i = 0; i < 100; i++) {
        NSString *hexByte = [NSString stringWithFormat:@"%02X", bytes[i]]; // 大写16进制，两位
        [hexArray addObject:hexByte];
    }
    
    NSLog(@"✅ 100B数据已转换为100元素16进制数组");
    return [NSArray arrayWithArray:hexArray];
}

@end

