//
//  ECCKeyGenerator.m
//  PhoneCase
//
//  Created by XTMacMini on 2025/10/27.
//

#import "ECCKeyGenerator.h"

@implementation ECCKeyGenerator

static NSString * const kPublicKeyTag = @"xyz.adpal.app.ecc.p256.public";
static NSString * const kPrivateKeyTag = @"xyz.adpal.app.ecc.p256.private";

static NSString * const kMasterPublicKeyTag = @"io.sigworld.master.ecc.k256.public";
static NSString * const kMasterPrivateKeyTag = @"io.sigworld.master.ecc.k256.private";
static NSString * const kChatPublicKeyTag = @"io.sigworld.chat.ecc.k256.public";
static NSString * const kChatPrivateKeyTag = @"io.sigworld.chat.ecc.k256.private";

static NSString * const kAds3UserIDTag = @"io.sigworld.ads3.userid";

static NSString * const kDevicePKTag = @"io.sigworld.device.pk";
static NSString * const kDeviceChipTag = @"io.sigworld.device.chipid";


// io.sigworld.master.ecc.k256.public
// io.sigworld.master.ecc.k256.private
//io.sigworld.chat.ecc.k256.public
//io.sigworld.chat.ecc.k256.private

#pragma mark - 密钥对生成与钥匙串操作

+ (void)generateECCP256KeyPairWithCompletion:(void(^)(SecKeyRef publicKey, SecKeyRef privateKey, NSError *error))completion {
    // 检查是否已存在密钥对，存在则直接返回
    SecKeyRef existingPrivateKey = [self loadPublicKeyFromKeychainWithTag:kPrivateKeyTag];
    SecKeyRef existingPublicKey = [self loadPublicKeyFromKeychainWithTag:kPublicKeyTag];
    
    if (existingPublicKey && existingPrivateKey) {
        if (completion) {
            completion(existingPublicKey, existingPrivateKey, nil);
        }
        CFRelease(existingPublicKey);
        CFRelease(existingPrivateKey);
        return;
    }
    
    OSStatus status = noErr;
    NSError *error = nil;
    
    // 配置密钥生成参数
    NSMutableDictionary *attributes = [NSMutableDictionary dictionary];
    [attributes setObject:(__bridge id)kSecAttrKeyTypeEC forKey:(__bridge id)kSecAttrKeyType];
    [attributes setObject:@256 forKey:(__bridge id)kSecAttrKeySizeInBits];
    
    // 公钥属性配置
    NSMutableDictionary *publicKeyAttrs = [NSMutableDictionary dictionary];
    [publicKeyAttrs setObject:kPublicKeyTag forKey:(__bridge id)kSecAttrApplicationTag];
    [publicKeyAttrs setObject:@YES forKey:(__bridge id)kSecAttrIsPermanent];
    [attributes setObject:publicKeyAttrs forKey:(__bridge id)kSecPublicKeyAttrs];
    
    // 私钥属性配置
    NSMutableDictionary *privateKeyAttrs = [NSMutableDictionary dictionary];
    [privateKeyAttrs setObject:kPrivateKeyTag forKey:(__bridge id)kSecAttrApplicationTag];
    [privateKeyAttrs setObject:@YES forKey:(__bridge id)kSecAttrIsPermanent];
    [attributes setObject:privateKeyAttrs forKey:(__bridge id)kSecPrivateKeyAttrs];
    
    // 生成密钥对
    SecKeyRef publicKey = NULL;
    SecKeyRef privateKey = NULL;
    status = SecKeyGeneratePair((__bridge CFDictionaryRef)attributes, &publicKey, &privateKey);
    
    if (status == errSecSuccess) {
        CFRetain(publicKey); // 保留引用，避免方法结束后释放
        CFRetain(privateKey);
        if (completion) {
            completion(publicKey, privateKey, nil);
        }
    } else {
        error = [NSError errorWithDomain:@"SecurityError"
                                    code:(int)status
                                userInfo:@{NSLocalizedDescriptionKey: @"Key generation failed"}];
        if (completion) {
            completion(NULL, NULL, error);
        }
    }
}

+ (SecKeyRef)loadPublicKeyFromKeychainWithTag:(NSString *)tag {
    NSDictionary *query = @{
        (__bridge id)kSecClass: (__bridge id)kSecClassKey,
        (__bridge id)kSecAttrKeyType: (__bridge id)kSecAttrKeyTypeEC,
        (__bridge id)kSecAttrKeyClass: (__bridge id)kSecAttrKeyClassPublic,
        (__bridge id)kSecAttrApplicationTag: tag,  // ← 使用传入的 tag
        (__bridge id)kSecReturnRef: @YES
    };
    
    SecKeyRef keyRef = NULL;
    OSStatus status = SecItemCopyMatching((__bridge CFDictionaryRef)query, (CFTypeRef *)&keyRef);
    return (status == errSecSuccess) ? keyRef : NULL;
}

+ (SecKeyRef)loadPrivateKeyFromKeychainWithTag:(NSString *)tag {
    NSDictionary *query = @{
        (__bridge id)kSecClass: (__bridge id)kSecClassKey,
        (__bridge id)kSecAttrKeyType: (__bridge id)kSecAttrKeyTypeEC,
        (__bridge id)kSecAttrKeyClass: (__bridge id)kSecAttrKeyClassPrivate,
        (__bridge id)kSecAttrApplicationTag: tag,  // ← 使用传入的 tag
        (__bridge id)kSecReturnRef: @YES
    };
    
    SecKeyRef keyRef = NULL;
    OSStatus status = SecItemCopyMatching((__bridge CFDictionaryRef)query, (CFTypeRef *)&keyRef);
    return (status == errSecSuccess) ? keyRef : NULL;
}

//+ (SecKeyRef)loadPublicKeyFromKeychain {
//    NSDictionary *query = @{
//        (__bridge id)kSecClass: (__bridge id)kSecClassKey,
//        (__bridge id)kSecAttrKeyType: (__bridge id)kSecAttrKeyTypeEC,
//        (__bridge id)kSecAttrKeyClass: (__bridge id)kSecAttrKeyClassPublic,
//        (__bridge id)kSecAttrApplicationTag: kPublicKeyTag,
//        (__bridge id)kSecReturnRef: @YES
//    };
//    
//    SecKeyRef keyRef = NULL;
//    OSStatus status = SecItemCopyMatching((__bridge CFDictionaryRef)query, (CFTypeRef *)&keyRef);
//    return (status == errSecSuccess) ? keyRef : NULL;
//}
//
//+ (SecKeyRef)loadPrivateKeyFromKeychain {
//    NSDictionary *query = @{
//        (__bridge id)kSecClass: (__bridge id)kSecClassKey,
//        (__bridge id)kSecAttrKeyType: (__bridge id)kSecAttrKeyTypeEC,
//        (__bridge id)kSecAttrKeyClass: (__bridge id)kSecAttrKeyClassPrivate,
//        (__bridge id)kSecAttrApplicationTag: kPrivateKeyTag,
//        (__bridge id)kSecReturnRef: @YES
//    };
//    
//    SecKeyRef keyRef = NULL;
//    OSStatus status = SecItemCopyMatching((__bridge CFDictionaryRef)query, (CFTypeRef *)&keyRef);
//    return (status == errSecSuccess) ? keyRef : NULL;
//}

+ (BOOL)hasStoredKeyPair {
    SecKeyRef publicKey = [self loadPublicKeyFromKeychainWithTag:kPublicKeyTag];
    SecKeyRef privateKey = [self loadPrivateKeyFromKeychainWithTag:kPrivateKeyTag];
    
    NSLog(@"✅ 密钥对查询结果：公钥：%@，私钥：%@",
          publicKey ? @"存在" : @"不存在",
          privateKey ? @"存在" : @"不存在");
    
    if (publicKey) CFRelease(publicKey);
    if (privateKey) CFRelease(privateKey);
    return publicKey && privateKey;
}

+ (void)clearStoredKeyPair {
    // 删除公钥
    NSDictionary *publicQuery = @{
        (__bridge id)kSecClass: (__bridge id)kSecClassKey,
        (__bridge id)kSecAttrKeyType: (__bridge id)kSecAttrKeyTypeEC,
        (__bridge id)kSecAttrApplicationTag: kPublicKeyTag
    };
    SecItemDelete((__bridge CFDictionaryRef)publicQuery);
    
    // 删除私钥
    NSDictionary *privateQuery = @{
        (__bridge id)kSecClass: (__bridge id)kSecClassKey,
        (__bridge id)kSecAttrKeyType: (__bridge id)kSecAttrKeyTypeEC,
        (__bridge id)kSecAttrApplicationTag: kPrivateKeyTag
    };
    SecItemDelete((__bridge CFDictionaryRef)privateQuery);
}

+ (SecKeyRef)getStoredPublicKey {
    return [self loadPublicKeyFromKeychainWithTag:kPublicKeyTag];
}

+ (SecKeyRef)getStoredPrivateKey {
    return [self loadPrivateKeyFromKeychainWithTag:kPrivateKeyTag];
}

//save as string in keychain

+ (BOOL)saveStringToKeychain:(NSString *)string
                      forKey:(NSString *)key {
    // 将 NSString 转为 NSData
    NSData *data = [string dataUsingEncoding:NSUTF8StringEncoding];
    
    NSDictionary *query = @{
        (__bridge id)kSecClass: (__bridge id)kSecClassGenericPassword,
        (__bridge id)kSecAttrAccount: key,          // 相当于“键名”
        (__bridge id)kSecValueData: data            // 存储的值
    };
    
    // 先删除已存在的项（避免重复）
    SecItemDelete((__bridge CFDictionaryRef)query);
    
    // 添加新项
    OSStatus status = SecItemAdd((__bridge CFDictionaryRef)query, NULL);
    return (status == errSecSuccess);
}

+ (NSString *)loadStringFromKeychainForKey:(NSString *)key {
    NSDictionary *query = @{
        (__bridge id)kSecClass: (__bridge id)kSecClassGenericPassword,
        (__bridge id)kSecAttrAccount: key,
        (__bridge id)kSecReturnData: @YES,          // 返回 NSData
        (__bridge id)kSecMatchLimit: (__bridge id)kSecMatchLimitOne
    };
    
    CFTypeRef result = NULL;
    OSStatus status = SecItemCopyMatching((__bridge CFDictionaryRef)query, &result);
    
    if (status == errSecSuccess && result) {
        NSData *data = (__bridge NSData *)result;
        return [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    }
    return nil;
}
// 假设你有 Base64 或 PEM 格式的密钥字符串
//NSString *publicKeyStr = @"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE...";
//NSString *privateKeyStr = @"MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQg...";
//
//// 保存
//[YourKeychainHelper saveStringToKeychain:publicKeyStr forKey:@"my_public_key"];
//[YourKeychainHelper saveStringToKeychain:privateKeyStr forKey:@"my_private_key"];
//
//// 读取
//NSString *pub = [YourKeychainHelper loadStringFromKeychainForKey:@"my_public_key"];
//NSString *priv = [YourKeychainHelper loadStringFromKeychainForKey:@"my_private_key"];

+ (BOOL)saveDeviceInfoToKeychain:(NSString *)devicePK
                      CHIPID:(NSString *)chipId {
    [self saveStringToKeychain:devicePK forKey:kDevicePKTag];
    [self saveStringToKeychain:chipId forKey:kDeviceChipTag];
    return true;
}

+ (NSString *)loadDeviceInfoFromKeychain: (Boolean) bDevicePK
{
    if (bDevicePK)
        return [self loadStringFromKeychainForKey:kDevicePKTag];
    else
        return [self loadStringFromKeychainForKey:kDeviceChipTag];
}

    
+ (BOOL)saveMasterKeyToKeychain:(NSString *)masterKey
                      PBULICKEY:(NSString *)masterPK {
    [self saveStringToKeychain:masterKey forKey:kMasterPrivateKeyTag];
    [self saveStringToKeychain:masterPK forKey:kMasterPublicKeyTag];
    return true;
}

+ (NSString *)loadMasterKeyFromKeychain:(Boolean) bPK
{
    if (bPK)
        return [self loadStringFromKeychainForKey:kMasterPublicKeyTag];
    else
        return [self loadStringFromKeychainForKey:kMasterPrivateKeyTag];
}



+ (BOOL)saveChatKeyToKeychain:(NSString *)chatKey
                      PBULICKEY:(NSString *)chatPK {
    [self saveStringToKeychain:chatKey forKey:kChatPrivateKeyTag];
    [self saveStringToKeychain:chatPK forKey:kChatPublicKeyTag];
    return true;
}

+ (NSString *)loadChatKeyFromKeychain:(Boolean) bPK
{
    if (bPK)
        return [self loadStringFromKeychainForKey:kChatPublicKeyTag];
    else
        return [self loadStringFromKeychainForKey:kChatPrivateKeyTag];
}

+ (BOOL)saveUserIdToKeychain:(NSString *)userId
                      {
    [self saveStringToKeychain:userId forKey:kAds3UserIDTag];
    return true;
}

+ (NSString *)loadUserIdFromKeychain
{
        return [self loadStringFromKeychainForKey:kAds3UserIDTag];
   
}


#pragma mark - 签名方法（X962格式 + 原始格式）

+ (NSData *)signData:(NSData *)dataToSign withPrivateKey:(SecKeyRef)privateKey error:(NSError **)error {
    // 输入参数验证
    if (!privateKey || !dataToSign) {
        if (error) {
            *error = [NSError errorWithDomain:@"ECCSignatureError"
                                         code:-1
                                     userInfo:@{NSLocalizedDescriptionKey: @"Invalid input parameters"}];
        }
        return nil;
    }
    
    // 生成X962/DER格式签名
    SecKeyAlgorithm algorithm = kSecKeyAlgorithmECDSASignatureMessageX962SHA256;
    CFErrorRef cfError = NULL;
    CFDataRef signatureRef = SecKeyCreateSignature(privateKey, algorithm, (__bridge CFDataRef)dataToSign, &cfError);
    
    if (signatureRef) {
        NSData *signature = (__bridge NSData *)signatureRef;
        CFRelease(signatureRef);
        return signature;
    } else {
        if (error) {
            *error = (__bridge_transfer NSError *)cfError;
        }
        return nil;
    }
}

+ (NSData *)signDataToRawFormat:(NSData *)dataToSign withPrivateKey:(SecKeyRef)privateKey error:(NSError **)error {
    // 1. 先生成X962/DER格式签名
    NSData *derSignature = [self signData:dataToSign withPrivateKey:privateKey error:error];
    if (!derSignature) return nil;
    
    // 2. 解析DER签名，提取r和s
    NSData *rData = nil;
    NSData *sData = nil;
    BOOL parseSuccess = [self parseDERSignature:derSignature r:&rData s:&sData];
    if (!parseSuccess) {
        if (error) {
            *error = [NSError errorWithDomain:@"ECCSignatureError"
                                         code:-2
                                     userInfo:@{NSLocalizedDescriptionKey: @"Failed to parse DER signature"}];
        }
        return nil;
    }
    
    // 3. 将r和s标准化为32字节（适配P-256曲线）
    NSData *rawR = [self normalizeTo32Bytes:rData];
    NSData *rawS = [self normalizeTo32Bytes:sData];
    if (!rawR || !rawS) {
        if (error) {
            *error = [NSError errorWithDomain:@"ECCSignatureError"
                                         code:-3
                                     userInfo:@{NSLocalizedDescriptionKey: @"r/s must be 32 bytes (for P-256 curve)"}];
        }
        return nil;
    }
    
    // 4. 拼接r和s，生成原始格式签名（64字节）
    NSMutableData *rawSignature = [NSMutableData data];
    [rawSignature appendData:rawR];
    [rawSignature appendData:rawS];
    return rawSignature;
}

#pragma mark - 验证方法（X962格式 + 原始格式）

+ (BOOL)verifySignature:(NSData *)signature forData:(NSData *)dataToVerify withPublicKey:(SecKeyRef)publicKey error:(NSError **)error {
    // 输入参数验证
    if (!publicKey || !signature || !dataToVerify) {
        if (error) {
            *error = [NSError errorWithDomain:@"ECCVerificationError"
                                         code:-1
                                     userInfo:@{NSLocalizedDescriptionKey: @"Invalid input parameters"}];
        }
        return NO;
    }
    
    // 验证X962/DER格式签名
    SecKeyAlgorithm algorithm = kSecKeyAlgorithmECDSASignatureMessageX962SHA256;
    CFErrorRef cfError = NULL;
    BOOL isValid = SecKeyVerifySignature(publicKey, algorithm,
                                         (__bridge CFDataRef)dataToVerify,
                                         (__bridge CFDataRef)signature,
                                         &cfError);
    
    if (cfError) {
        if (error) {
            *error = (__bridge_transfer NSError *)cfError;
        }
        return NO;
    }
    return isValid;
}

+ (BOOL)verifyRawSignature:(NSData *)rawSignature forData:(NSData *)dataToVerify withPublicKey:(SecKeyRef)publicKey error:(NSError **)error {
    // 1. 输入参数与格式验证（原始签名必须为64字节，适配P-256曲线）
    if (!publicKey || !rawSignature || !dataToVerify) {
        if (error) {
            *error = [NSError errorWithDomain:@"ECCVerificationError"
                                         code:-1
                                     userInfo:@{NSLocalizedDescriptionKey: @"Invalid input parameters"}];
        }
        return NO;
    }
    if (rawSignature.length != 64) {
        if (error) {
            *error = [NSError errorWithDomain:@"ECCVerificationError"
                                         code:-2
                                     userInfo:@{NSLocalizedDescriptionKey: @"Raw signature must be 64 bytes (for P-256 curve)"}];
        }
        return NO;
    }
    
    // 2. 拆分原始签名为r（前32字节）和s（后32字节）
    NSData *rData = [rawSignature subdataWithRange:NSMakeRange(0, 32)];
    NSData *sData = [rawSignature subdataWithRange:NSMakeRange(32, 32)];
    
    // 3. 将r和s转换为DER INTEGER格式（处理符号位）
    NSData *derR = [self derIntegerFromRawBytes:rData];
    NSData *derS = [self derIntegerFromRawBytes:sData];
    if (!derR || !derS) {
        if (error) {
            *error = [NSError errorWithDomain:@"ECCVerificationError"
                                         code:-3
                                     userInfo:@{NSLocalizedDescriptionKey: @"Failed to convert r/s to DER INTEGER"}];
        }
        return NO;
    }
    
    // 4. 构建DER SEQUENCE格式签名（适配系统验证API）
    NSData *derSignature = [self derSequenceFromIntegers:@[derR, derS]];
    if (!derSignature) {
        if (error) {
            *error = [NSError errorWithDomain:@"ECCVerificationError"
                                         code:-4
                                     userInfo:@{NSLocalizedDescriptionKey: @"Failed to build DER sequence"}];
        }
        return NO;
    }
    
    // 5. 复用X962格式验证逻辑
    return [self verifySignature:derSignature forData:dataToVerify withPublicKey:publicKey error:error];
}

#pragma mark - 私有工具方法（原始格式与DER格式转换）

/**
 * 解析DER格式签名，提取r和s的原始字节
 */
+ (BOOL)parseDERSignature:(NSData *)derSignature r:(NSData **)r s:(NSData **)s {
    const uint8_t *bytes = derSignature.bytes;
    NSUInteger length = derSignature.length;
    NSUInteger offset = 0;
    
    // 验证DER SEQUENCE标签（0x30）
    if (offset >= length || bytes[offset] != 0x30) return NO;
    offset++;
    
    // 读取SEQUENCE长度（简化处理：长度不超过0x7F）
    if (offset >= length) return NO;
    uint8_t seqLength = bytes[offset];
    offset++;
    if (offset + seqLength > length) return NO;
    
    // 解析r（INTEGER标签：0x02）
    if (offset >= length || bytes[offset] != 0x02) return NO;
    offset++;
    if (offset >= length) return NO;
    uint8_t rLength = bytes[offset];
    offset++;
    if (offset + rLength > length) return NO;
    *r = [NSData dataWithBytes:&bytes[offset] length:rLength];
    offset += rLength;
    
    // 解析s（INTEGER标签：0x02）
    if (offset >= length || bytes[offset] != 0x02) return NO;
    offset++;
    if (offset >= length) return NO;
    uint8_t sLength = bytes[offset];
    offset++;
    if (offset + sLength > length) return NO;
    *s = [NSData dataWithBytes:&bytes[offset] length:sLength];
    offset += sLength;
    
    // 确保解析完所有数据
    return offset == length;
}

/**
 * 将r/s的DER字节标准化为32字节（适配P-256曲线）
 */
+ (NSData *)normalizeTo32Bytes:(NSData *)data {
    NSMutableData *normalized = [NSMutableData dataWithLength:32]; // 初始化32字节空数据
    const uint8_t *srcBytes = data.bytes;
    NSUInteger srcLength = data.length;
    uint8_t *dstBytes = normalized.mutableBytes;
    
    // 去除DER INTEGER的前导0（符号位）
    NSUInteger start = 0;
    while (start < srcLength && srcBytes[start] == 0x00) {
        start++;
    }
    NSUInteger effectiveLength = srcLength - start;
    
    // 有效长度不能超过32字节（P-256曲线限制）
    if (effectiveLength > 32) return nil;
    
    // 填充到32字节（前面补0）
    NSUInteger dstOffset = 32 - effectiveLength;
    memcpy(&dstBytes[dstOffset], &srcBytes[start], effectiveLength);
    
    return normalized;
}

/**
 * 将原始r/s字节转换为DER INTEGER格式（处理符号位：最高位为1时补前导0）
 */
+ (NSData *)derIntegerFromRawBytes:(NSData *)rawBytes {
    const uint8_t *bytes = rawBytes.bytes;
    // 最高位为1时，添加前导0避免被解析为负数（ASN.1 INTEGER为有符号类型）
    if (bytes[0] & 0x80) {
        NSMutableData *derData = [NSMutableData data];
        [derData appendBytes:"\x00" length:1];
        [derData appendData:rawBytes];
        return derData;
    }
    return rawBytes;
}

/**
 * 将r和s的DER INTEGER组装为DER SEQUENCE格式（X962标准）
 */
+ (NSData *)derSequenceFromIntegers:(NSArray<NSData *> *)integers {
    // 拼接所有INTEGER的DER编码（标签+长度+数据）
    NSMutableData *sequenceContent = [NSMutableData data];
    for (NSData *integerData in integers) {
        NSMutableData *derInteger = [NSMutableData data];
        [derInteger appendBytes:"\x02" length:1]; // INTEGER标签（0x02）
        [derInteger appendBytes:&(uint8_t){(uint8_t)integerData.length} length:1]; // 长度
        [derInteger appendData:integerData]; // 数据
        [sequenceContent appendData:derInteger];
    }
    
    // 构建完整SEQUENCE（标签+长度+内容）
    NSMutableData *derSequence = [NSMutableData data];
    [derSequence appendBytes:"\x30" length:1]; // SEQUENCE标签（0x30）
    [derSequence appendBytes:&(uint8_t){(uint8_t)sequenceContent.length} length:1]; // 内容长度
    [derSequence appendData:sequenceContent]; // 内容
    
    return derSequence;
}

#pragma mark - 公钥转换方法

/**
 * 将原始ECC公钥数据转换为DER编码格式
 * P-256公钥为64字节（x32字节 + y32字节），需封装为ANSI X9.62格式
 */
+ (NSData *)derEncodedDataFromRawECCPublicKey:(NSData *)rawPublicKey {
    // 1. 验证输入长度（P-256公钥必须为64字节）
    if (rawPublicKey.length != 64) {
        return nil;
    }
    
    // 2. 分割x和y坐标
    NSData *xCoord = [rawPublicKey subdataWithRange:NSMakeRange(0, 32)];
    NSData *yCoord = [rawPublicKey subdataWithRange:NSMakeRange(32, 32)];
    
    // 3. 构建ANSI X9.62 uncompressed point格式（0x04 + x + y）
    NSMutableData *uncompressedPoint = [NSMutableData data];
    [uncompressedPoint appendBytes:"\x04" length:1]; // 未压缩点标识符
    [uncompressedPoint appendData:xCoord];
    [uncompressedPoint appendData:yCoord];
    
    // 4. 构建ECC公钥的DER编码
    // SEQUENCE {
    //   SEQUENCE {
    //     OBJECT IDENTIFIER 1.2.840.10045.2.1 (ecPublicKey)
    //     OBJECT IDENTIFIER 1.2.840.10045.3.1.7 (secp256r1)
    //   }
    //   BIT STRING (uncompressed point)
    // }
    
    // 构建OID: 1.2.840.10045.2.1 (ecPublicKey)
    uint8_t ecPublicKeyOID[] = {0x06, 0x07, 0x2a, 0x86, 0x48, 0xce, 0x3d, 0x02, 0x01};
    NSMutableData *oid1 = [NSMutableData dataWithBytes:ecPublicKeyOID length:sizeof(ecPublicKeyOID)];
    
    // 构建OID: 1.2.840.10045.3.1.7 (secp256r1)
    uint8_t secp256r1OID[] = {0x06, 0x08, 0x2a, 0x86, 0x48, 0xce, 0x3d, 0x03, 0x01, 0x07};
    NSMutableData *oid2 = [NSMutableData dataWithBytes:secp256r1OID length:sizeof(secp256r1OID)];
    
    // 构建第一个SEQUENCE（算法标识）
    NSMutableData *algorithmSequence = [NSMutableData data];
    [algorithmSequence appendData:oid1];
    [algorithmSequence appendData:oid2];
    
    // 构建BIT STRING（公钥点）
    NSMutableData *bitString = [NSMutableData data];
    [bitString appendBytes:"\x03" length:1]; // BIT STRING标签
    [bitString appendBytes:&(uint8_t){(uint8_t)(uncompressedPoint.length + 1)} length:1]; // 长度（+1 for unused bits byte）
    [bitString appendBytes:"\x00" length:1]; // 未使用位数（0）
    [bitString appendData:uncompressedPoint];
    
    // 构建最外层SEQUENCE
    NSMutableData *outerSequence = [NSMutableData data];
    [outerSequence appendData:algorithmSequence];
    [outerSequence appendData:bitString];
    
    // 最终DER编码：SEQUENCE标签 + 长度 + 内容
    NSMutableData *finalDer = [NSMutableData data];
    [finalDer appendBytes:"\x30" length:1]; // SEQUENCE标签
    [finalDer appendBytes:&(uint8_t){(uint8_t)outerSequence.length} length:1]; // 长度
    [finalDer appendData:outerSequence];
    
    return finalDer;
}


+ (SecKeyRef)createSecKeyFromHexECCPublicKey:(NSString *)hexString {
    if (!hexString || hexString.length != 128) { // 64字节对应128个16进制字符
        NSLog(@"❌ 无效的公钥16进制字符串（长度必须为128）");
        return NULL;
    }
    
    // 1. 将16进制字符串转为NSData（64字节：X||Y）
    NSData *xyData = [self convertHexStringToData:hexString];
    if (!xyData || xyData.length != 64) {
        NSLog(@"❌ 16进制转Data失败或长度异常");
        return NULL;
    }
    
    // 2. 补全ECC未压缩公钥头（0x04），形成65字节完整数据
    NSMutableData *fullPubData = [NSMutableData data];
    uint8_t header = 0x04; // 未压缩公钥标识
    [fullPubData appendBytes:&header length:1];
    [fullPubData appendData:xyData]; // 此时fullPubData为65字节（0x04+X+Y）
    
    // 3. 配置公钥属性（必须指定曲线参数，否则创建失败）
    NSDictionary *keyAttributes = @{
        (__bridge id)kSecAttrKeyType: (__bridge id)kSecAttrKeyTypeEC, // 密钥类型：ECC
        (__bridge id)kSecAttrKeySizeInBits: @256, // 密钥长度：256位
        (__bridge id)kSecAttrKeyClass: (__bridge id)kSecAttrKeyClassPublic, // 公钥
//        (__bridge id)kSecAttrKeyCurve: (__bridge id)kSecP256KeyCurve // 曲线类型：P-256
    };
    
    // 4. 从数据创建SecKeyRef
    SecKeyRef publicKey = SecKeyCreateWithData((__bridge CFDataRef)fullPubData,
                                              (__bridge CFDictionaryRef)keyAttributes,
                                              NULL); // 错误信息（可传入NSError**）
    
    if (!publicKey) {
        NSLog(@"❌ 创建SecKeyRef失败");
        return NULL;
    }
    
    return publicKey;
}

// 辅助方法：16进制字符串转NSData
+ (NSData *)convertHexStringToData:(NSString *)hexString {
    hexString = [hexString stringByReplacingOccurrencesOfString:@" " withString:@""];
    NSMutableData *data = [NSMutableData dataWithCapacity:hexString.length / 2];
    for (NSInteger i = 0; i < hexString.length; i += 2) {
        NSString *substring = [hexString substringWithRange:NSMakeRange(i, 2)];
        NSScanner *scanner = [NSScanner scannerWithString:substring];
        unsigned int byte;
        [scanner scanHexInt:&byte];
        [data appendBytes:&byte length:1];
    }
    return data;
}

@end


