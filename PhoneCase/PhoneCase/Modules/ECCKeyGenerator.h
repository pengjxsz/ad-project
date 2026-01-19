//
//  ECCKeyGenerator.h
//  PhoneCase
//
//  Created by XTMacMini on 2025/10/27.
//

#import <Foundation/Foundation.h>
#import <Security/Security.h>

NS_ASSUME_NONNULL_BEGIN

@interface ECCKeyGenerator : NSObject

/**
 * 生成ECC-P256密钥对
 * 该方法使用Security框架生成椭圆曲线P-256密钥对，并将密钥对持久化存储到钥匙串中
 * 如果钥匙串中已存在密钥对，则直接返回已存储的密钥对，避免重复生成
 */
+ (void)generateECCP256KeyPairWithCompletion:(void(^)(SecKeyRef publicKey, SecKeyRef privateKey, NSError *error))completion;

/**
 * 从钥匙串加载公钥
 * @return 公钥引用，如果不存在则返回NULL
 */
+ (SecKeyRef)loadPublicKeyFromKeychain;

/**
 * 从钥匙串加载私钥
 * @return 私钥引用，如果不存在则返回NULL
 */
+ (SecKeyRef)loadPrivateKeyFromKeychain;

/**
 * 检查是否已存储密钥对
 * 通过检查钥匙串中是否存在公钥和私钥来判断密钥对是否已存储
 * @return YES表示已存储，NO表示未存储
 */
+ (BOOL)hasStoredKeyPair;

/**
 * 清除存储的密钥对
 * 从钥匙串中删除已存储的公钥和私钥
 */
+ (void)clearStoredKeyPair;

/**
 * 获取存储的公钥
 * @return 公钥引用，如果不存在则返回NULL
 */
+ (SecKeyRef)getStoredPublicKey;

/**
 * 获取存储的私钥
 * @return 私钥引用，如果不存在则返回NULL
 */
+ (SecKeyRef)getStoredPrivateKey;



+ (BOOL)saveDeviceInfoToKeychain:(NSString *)devicePK
                          CHIPID:(NSString *)chipId;
+ (NSString *)loadDeviceInfoFromKeychain:(Boolean) bDevicePK;
    
+ (BOOL)saveMasterKeyToKeychain:(NSString *)masterKey
                      PBULICKEY:(NSString *)masterPK;
+ (NSString *)loadMasterKeyFromKeychain:(Boolean) bPK;


+ (BOOL)saveChatKeyToKeychain:(NSString *)chatKey
                    PBULICKEY:(NSString *)chatPK ;
+ (NSString *)loadChatKeyFromKeychain:(Boolean) bPK;

+ (BOOL)saveUserIdToKeychain:(NSString *)userId;
+ (NSString *)loadUserIdFromKeychain;




#pragma mark - 签名和验证方法（含X962格式与原始格式）

/**
 * 使用私钥对数据进行签名（X962格式，系统默认支持）
 * @param dataToSign 要签名的数据
 * @param privateKey 私钥引用
 * @param error 错误输出
 * @return 签名数据（X962/DER格式），失败返回nil
 */
+ (NSData *)signData:(NSData *)dataToSign withPrivateKey:(SecKeyRef)privateKey error:(NSError **)error;

/**
 * 使用私钥对数据进行签名（原始格式，64字节：r(32字节)+s(32字节)）
 * @param dataToSign 要签名的数据
 * @param privateKey 私钥引用
 * @param error 错误输出
 * @return 签名数据（原始格式），失败返回nil
 */
+ (NSData *)signDataToRawFormat:(NSData *)dataToSign withPrivateKey:(SecKeyRef)privateKey error:(NSError **)error;

/**
 * 验证签名（X962格式，系统默认支持）
 * @param signature 签名数据（X962/DER格式）
 * @param dataToVerify 待验证的原始数据
 * @param publicKey 公钥引用
 * @param error 错误输出
 * @return 验证成功返回YES，失败返回NO
 */
+ (BOOL)verifySignature:(NSData *)signature forData:(NSData *)dataToVerify withPublicKey:(SecKeyRef)publicKey error:(NSError **)error;

/**
 * 验证签名（原始格式，64字节：r(32字节)+s(32字节)）
 * @param rawSignature 签名数据（原始格式）
 * @param dataToVerify 待验证的原始数据
 * @param publicKey 公钥引用
 * @param error 错误输出
 * @return 验证成功返回YES，失败返回NO
 */
+ (BOOL)verifyRawSignature:(NSData *)rawSignature forData:(NSData *)dataToVerify withPublicKey:(SecKeyRef)publicKey error:(NSError **)error;


/**
 从16进制字符串创建ECC公钥SecKeyRef
 
 @param hexString 16进制格式的ECC公钥字符串（未压缩格式，以04开头）
 @return 成功返回SecKeyRef，失败返回NULL
 */
+ (SecKeyRef)createSecKeyFromHexECCPublicKey:(NSString *)hexString;

@end

NS_ASSUME_NONNULL_END
