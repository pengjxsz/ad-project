//
//  BleProtocol.h
//  BleProtocol
//
//  Created by xthh on 2024/6/19.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "XTSendPicDataInfo.h"
#import "XTBleResultDataModel.h"

@interface BleProtocol : NSObject

// 定义一个结构体 TagBindInfo，用于存储设备的绑定信息
typedef struct TagBindInfo {
    // 设备的 MAC 地址，使用 NSString 类型存储
    NSString *mac;
    // 设备的颜色信息，使用 NSString 类型存储
    NSString *color;
    // 设备编号，使用 NSInteger 类型存储
    NSInteger deviceNumber;
    // 设备屏幕的宽度，使用 NSInteger 类型存储
    NSInteger width;
    // 设备屏幕的高度，使用 NSInteger 类型存储
    NSInteger height;
    // 屏幕类型，0 表示点阵屏，1 表示 prism 屏
    NSInteger screenType;
    // 旋转类型，0 表示 0 度，1 表示 90 度，2 表示 180 度，3 表示 70 度
    NSInteger rotationType;
    // 水平镜像类型，1 表示镜像
    NSInteger mirrorHorType;
    // 垂直镜像类型，1 表示镜像
    NSInteger mirrorVerType;
    // 操作结果，true 表示获取成功
    bool result;
} TagBindInfo;

// MARK: ========================================    数据处理    ========================================

/**
 * 从 NFC 中获取需要的绑定信息
 * @param info NFC 中的信息
 * @return 解析得到的绑定信息结构体
 */
+ (TagBindInfo)getNFCTagBindInfo:(NSString *)info;

/**
 * 获取兴泰 BLE 蓝牙的服务 UUID
 * @return 兴泰 BLE 蓝牙的服务 UUID 字符串
 */
+ (NSString *)getBLEServiceUUID;

/**
 * 获取兴泰 BLE 蓝牙的读特征 UUID
 * @return 兴泰 BLE 蓝牙的读特征 UUID 字符串
 */
+ (NSString *)getBLEReadCharacteristicUUID;

/**
 * 获取兴泰 BLE 蓝牙的写特征 UUID
 * @return 兴泰 BLE 蓝牙的写特征 UUID 字符串
 */
+ (NSString *)getBLEWriteCharacteristicUUID;

/**
 * 导入图片数据（二维数组），传出分包后的图片数据
 * 示例 表示图片宽5像素，高2像素，其中每一行颜色分别是“黑白红黄黑白”
 012301
 012301
 *
 * 默认 RGB 值与序号对应关系如下 黑 0，白 1，红 2，黄 3
 * 颜色类型，黑白 - 0，黑白黄 - 1，黑白红 - 2，黑白红黄 - 3，NCOLOR - 4，E6 - 5
 * @param data 传入的图片数据，二维数组形式
 * @param width 图片的宽度
 * @param height 图片的高度
 * @param deviceColorType 设备的颜色类型
 * @param pageSize 每个包的大小
 * @param isNeedCompress 是否需要压缩
 * @param deviceNumber 设备编号
 * @return 分包后的图片数据，可变数组形式 传出的数组，表示分包的数据，每一包还分<=5个子包
 */
+ (NSMutableArray *)getPicSendPageData:(NSMutableArray *)data
                              picWidth:(int)width
                             picHeight:(int)height
                       deviceColorType:(int)deviceColorType
                              pageSize:(int)pageSize
                        isNeedCompress:(BOOL)isNeedCompress
                          deviceNumber:(NSInteger)deviceNumber;

//原生数据
+ (NSMutableArray *)getPicData:(NSMutableArray *)data
                      picWidth:(int)width
                     picHeight:(int)height
               deviceColorType:(int)deviceColorType
                      pageSize:(int)pageSize
                isNeedCompress:(BOOL)isNeedCompress
                  deviceNumber:(NSInteger)deviceNumber;


/**
 * 导入图片数据（二维数组），传出分包后的图片数据
 * 示例 表示图片宽5像素，高2像素，其中每一行颜色分别是“黑白红黄黑白”
 012301
 012301
 *
 * 默认 RGB 值与序号对应关系如下 黑 0，白 1，红 2，黄 3
 * 颜色类型，黑白 - 0，黑白黄 - 1，黑白红 - 2，黑白红黄 - 3，NCOLOR - 4，E6 - 5
 *
 * @param dataOne  图片1数据 （双面屏使用，不管是A面还是B面，单张图的时候赋值都是picOne）
 * @param dataTwo  图片2数据
 *
 * @return 分包后的图片数据，可变数组形式 传出的数组，表示分包的数据，每一包还分<=5个子包
 */
+ (NSMutableArray *)getPicSendPageDataOne:(XTSendPicDataInfo *)dataOne
                                  dataTwo:(XTSendPicDataInfo *)dataTwo;

/**
 * 获取 Prism 屏刷图的分包数据
 * @param data 传入的图片数据
 * @param pageSize 每个包的大小
 * @return 分包后的图片数据，可变数组形式
 */
+ (NSMutableArray *)getPrismSendPageData:(NSMutableArray *)data pageSize:(int)pageSize;

/**
 * 获取 OTA 升级的分包数据
 * @param data 传入的升级数据
 * @param pageSize 每个包的大小
 * @return 分包后的升级数据，可变数组形式
 */
+ (NSMutableArray *)getOTASendPageData:(NSMutableArray *)data pageSize:(int)pageSize;

/**
 * 获取写入防丢器 Token的分包数据
 * @param token 写入的token
 * @param pageSize 每个包的大小
 * @return 分包后的token数据，可变数组形式
 */
+ (NSMutableArray *)getFindMyTokenSendPageData:(NSString *)token pageSize:(int)pageSize;

/**
 * 获取需要清空的缓冲区长度
 * @param data 传入的数据
 * @return 需要清空的缓冲区长度
 */
+ (NSInteger)getDeleteFlashLength:(NSMutableArray *)data;

// MARK: ========================================    抖点    ========================================
/**
 * 将 UIImage 转换为适合 传输的数据格式
 *
 * 该方法处理输入图像，应用指定的算法和转换，生成可写入的二进制数据
 * 同时生成预览图像
 *
 * @param inputImage                 需要转换的原始图像
 * @param previewImage             输出参数，用于返回处理后的预览图
 * @param colorDic                      颜色调整字典  =====>  传当前设备支持的颜色
 * @param brushType                    电子纸刷图方式  =====>  (0=nfc供电+蓝牙刷图   1=nfc供电+nfc刷图  2=外置电源+蓝牙刷图)
 * @param scanType                      扫描模式  =====>   扫描方式，(0=垂直扫描，1=水平扫描)
 * @param combineType               图像合成模式 =====>  （0=替换，1=叠加，2=混合）
 * @param writeImageCount      写入图像序列中的位置索引
 * @param flipHorizontal        是否水平翻转图像（0=否，1=是）
 * @param flipVertical             是否垂直翻转图像（0=否，1=是）
 * @param screenColorCode      设备颜色支持代码（"20" 黑白，@"30" 黑白红 ，@"31" 黑白黄，@"40" 黑白红黄 ，@"50" 黑白红黄绿蓝）
 * @param algType                        算法类型 （1=抖点亮色，2=色阶，3=抖点暗色）
 *
 * @return 转换后的二进制数据，如果发生错误则返回nil
 *
 */
+ (NSData *)convertToUploadData:(UIImage *)inputImage
                   previewImage:(UIImage **)previewImage
                       colorDic:(NSDictionary *)colorDic
                      brushType:(int)brushType
                       scanType:(int)scanType
                    combineType:(int)combineType
                writeImageCount:(int)writeImageCount
                 flipHorizontal:(BOOL)flipHorizontal
                   flipVertical:(BOOL)flipVertical
                screenColorCode:(NSString *)screenColorCode
                        algType:(NSString *)algType;

/**
 * 查找最接近的颜色
 * @param rgbPixel 像素的 RGB 值数组
 * @param rgbTripleArray 颜色数组
 * @return 最接近颜色的序号
 */
+ (UInt8)findNearestColor:(NSArray *)rgbPixel rgbTripleArray:(NSArray *)rgbTripleArray;

/**
 * 对两个无符号 8 位整数进行加法运算，并处理溢出
 * @param a 第一个无符号 8 位整数
 * @param b 第二个整数
 * @return 加法运算结果，处理溢出
 */
+ (UInt8)plusTruncateUChar:(UInt8)a b:(int)b;

// MARK: ========================================    指令    ========================================

/**
 * 0x01指令
 * 获取删除 FLASH 的指令
 * @param size 删除的大小
 * @return 删除 FLASH 的指令字符串
 */
+ (NSString *)getDeleteFlashOrder:(int)size;

/**
 * 0x04指令
 * 获取 prism 屏刷图的临时指令
 * @param index 指令的索引
 * @return prism 屏刷图的临时指令字符串
 */
+ (NSString *)getPrismBrushOrder:(int)index;

/**
 * 0x04指令
 * 获取通知屏幕刷新、获取补包信息指令
 * @param type 刷新的类型
 * @param value 刷新的值
 * @return 刷新指令字符串
 */
+ (NSString *)getUpdateOrder:(int)type value:(int)value;

/**
 * 0x05指令
 * 启动升级 OTA 指令
 * @return 启动升级 OTA 的指令字符串
 */
+ (NSString *)startHardwareUpdateOrder;

/**
 * 13指令
 * 获取刷图指令
 * @return 刷图指令字符串
 */
+ (NSString *)getPicRefreshOrder;

/**
 * 0x16命令（15 命令冲突废弃）
 * findmy断开命令
 * @param cmd 0x16命令
 * @param opAndParam 操作类型+附加参数：
 *                   - 传 XT_OP_DISCONNECT（1）= 断开连接（无需附加参数）
 *                   - 传 XT_OP_READ_PARAM（2）= 读取参数（无需附加参数）
 *                   - 传 XT_OP_WRITE_PARAM（3）= 写入参数 (无需附加参数）
 * @return 指令字符串
 */
+ (NSString *)startFindMyOperationOrderWithCmd:(int)cmd
                                    opAndParam:(int)opAndParam;

/**
 * 0x0E 命令
 * 桌牌清屏命令 0x0E 命令
 * @return 桌牌清屏命令
 */
+ (NSString *)getNameCardClearOrder;

/**
 * 0x18 命令
 * 配置密钥命令（命令标识：0x18，仅用于手机壳项目）
 * @param type   //0x00: init key;      //0x01: write APP public key;       //0x02: read device public key;
 * @param param  //init: 0B        //write: APP public key :  64B       // read: 0B
 *
 * 指令返回
 * 错误码（第7字节） 255指令执行成功    104指令执行失败   105执行添加成功，对于不能立即返回结果的使用，比如蓝牙连接  106返回广播包   205安全认证失败
 * 失败原因（第8字节）
 * 成功： 初始化、写操作：0； 读操作：设备公钥，64B 、芯片ID，16B；
 * 失败: 失败原因,1B；
 *
 * @return 配置密钥的命令字符串
 */
+ (NSString *)getConfigKeyOrderForPhoneCaseWithWithType:(int)type
                                                  param:(NSMutableArray *)param;

/**
 * 0x19 命令
 * 验证签名（0x19，仅用于手机壳项目）
 * @param param 100B数据   / Hash data: 32B  Time stamp: 4B   Signature: 64B //顺序不能乱
 *
 * 指令返回
 * 错误码（第7字节） 255指令执行成功    104指令执行失败   105执行添加成功，对于不能立即返回结果的使用，比如蓝牙连接  106返回广播包   205安全认证失败
 * 失败原因（第8字节）
 * 成功： 成功： 0；
 * 失败: 失败原因,1B；
 *
 * @return 验证签名的命令字符串
 */
+ (NSString *)getVerifySignatureOrderForPhoneCaseWithParam:(NSMutableArray *)param;

// MARK: ========================================    蓝牙广播解析    ========================================

/**
 * 根据广播内容包获取 MAC 地址
 * @param data 广播内容包数据
 * @return 解析得到的 MAC 地址字符串
 */
+ (NSString *)getBleMacFromCBAdvDataManufacturerData:(NSData *)data;

/**
 * 根据广播名字获取 MAC 地址和设备 ID
 * @param name 广播名字
 * @return 包含 MAC 地址和设备编号的数组，索引 0 为 MAC 地址，索引 1 为设备编号
 */
+ (NSArray *)getBleMacAndDeviceNumberFromCBAdvDataLocalName:(NSString *)name;

/**
 * 广播内容包解析设备信息 所有数据
 * @param data 广播内容包数据
 * @return 包含设备信息的可变字典
 */
+ (NSMutableDictionary *)getDeviceBleInfoFromCBAdvDataManufacturerData:(NSData *)data;

/**
 * 获取版本号
 * @param data 广播内容包数据
 * @return 解析得到的版本号字符串
 */
+ (NSString *)getVersionFromCBAdvDataManufacturerData:(NSData *)data;

/**
 * 根据设备编号返回标签的设备信息
 * @param deviceNumber 设备编号
 * @return 包含设备详细信息的数组
 */
+ (NSArray *)getDeviceDetailInfoWithDeviceNumber:(NSInteger)deviceNumber;

// MARK: ========================================    蓝牙返回解析    ========================================

/**
 * 获取发送时返回的结果，删除 FLASH 时返回 1 表示成功，刷图时，如果成功返回 1 个，失败返回 2 个
 * @param data 发送时返回的数据
 * @return 包含发送结果的可变数组
 */
+ (NSMutableArray *)getBleSendResult:(NSData *)data;

/// 解析蓝牙返回的 NSData，返回解析对象和状态字符串
/// @param data 蓝牙原始数据
/// @param resultStr 传出参数，接收解析状态（错误信息或成功提示）
+ (XTBleResultDataModel *)parseBleData:(NSData *)data resultString:(NSString **)resultStr;

// MARK: ========================================    日志    ========================================

/**
 * 测试日志接口，用于输出日志信息
 * 暂时无用
 * @param info 要输出的日志信息
 * @param index 日志的索引
 * @return 返回一个 NSInteger 类型的值，可能用于表示日志输出的状态
 */
+ (NSInteger)XTLog:(NSString *)info index:(NSInteger)index;

// MARK: ========================================    测试数据    ========================================
/**
 * 获取临时图片数据，图片规格为 240*416，颜色模式为黑白红黄，用于测试
 * @return 包含图片数据的可变数组
 */
+ (NSMutableArray *)getTemporaryPicData;

/**
 * 获取临时图片数据，图片规格为 768*528，颜色模式为黑白红黄
 * @return 包含图片数据的可变数组
 */
+ (NSMutableArray *)getTemporaryPicData1;

/**
 * 获取临时图片数据，图片规格为 768*528，颜色模式为黑白红黄
 * @return 包含图片数据的可变数组
 */
+ (NSMutableArray *)getTemporaryPicData2;

/**
 * 获取临时图片数据，图片规格为 768*528，颜色模式为黑白红黄
 * @return 包含图片数据的可变数组
 */
+ (NSMutableArray *)getTemporaryPicData3;

/**
 * 获取临时图片数据，图片规格为 768*528，颜色模式为黑白红黄
 * @return 包含图片数据的可变数组
 */
+ (NSMutableArray *)getTemporaryPicData4;

/**
 * 获取临时图片数据，图片规格为 240*416，颜色模式为 4 色
 * @return 包含图片数据的可变数组
 */
+ (NSMutableArray *)getTemporaryPicData10;

/**
 * 获取临时图片数据，图片规格为 240*416，颜色模式为 4 色
 * @return 包含图片数据的可变数组
 */
+ (NSMutableArray *)getTemporaryPicData11;

/**
 * 获取临时图片数据，图片规格为 240*416，颜色模式为 4 色
 * @return 包含图片数据的可变数组
 */
+ (NSMutableArray *)getTemporaryPicData12;

/**
 * 获取临时图片数据，图片规格为 400*600，颜色模式为 6 色
 * @return 包含图片数据的可变数组
 */
+ (NSMutableArray *)getTemporaryPicData20;

/**
 * 获取临时图片数据，图片规格为 400*600，颜色模式为 6 色
 * @return 包含图片数据的可变数组
 */
+ (NSMutableArray *)getTemporaryPicData21;

/**
 * 获取临时图片数据，图片规格为 400*600，颜色模式为 6 色
 * @return 包含图片数据的可变数组
 */
+ (NSMutableArray *)getTemporaryPicData22;

@end

