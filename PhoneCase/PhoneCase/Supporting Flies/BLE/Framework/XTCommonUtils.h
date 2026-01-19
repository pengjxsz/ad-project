#import <UIKit/UIKit.h>

@interface XTCommonUtils : NSObject

/**
 *  将十六进制字符串转换为NSMutableData对象。
 *
 *  @param hexString 要转换的十六进制字符串。
 *  @return 转换后的NSMutableData对象，如果转换失败可能返回nil。
 */
+ (NSMutableData *)convertHexStringToData:(NSString *)hexString;

/**
 *  将NSData对象转换为十六进制字符串。
 *
 *  @param data 要转换的NSData对象。
 *  @return 转换后的十六进制字符串，如果输入为nil则可能返回空字符串。
 */
+ (NSString *)convertDataToHexString:(NSData *)data;
+ (NSString *)convertDataToHexStringWithSpace:(NSData *)data;

/**
 *  将十进制数的字符串表示转换为二进制数的字符串表示。
 *
 *  @param decimal 十进制数的字符串表示。
 *  @return 对应的二进制数的字符串表示，如果输入不合法可能返回空字符串。
 */
+ (NSString *)convertDecimalStringToBinaryString:(NSString *)decimal;

/**
 *  将二进制数的字符串表示转换为十进制数的字符串表示。
 *
 *  @param binary 二进制数的字符串表示。
 *  @return 对应的十进制数的字符串表示，如果输入不合法可能返回空字符串。
 */
+ (NSString *)convertBinaryStringToDecimalString:(NSString *)binary;


/**
 *  将整数转换为NSString类型的字符串。
 *
 *  @param integer 要转换的整数。
 *  @return 转换后的字符串表示。
 */
+ (NSString *)convertIntToString:(int)integer;

/**
 *  将浮点数转换为NSString类型的字符串。
 *
 *  @param floatNumber 要转换的浮点数。
 *  @return 转换后的字符串表示。
 */
+ (NSString *)convertFloatToString:(float)floatNumber;

/**
 *  将NSString类型的字符串转换为整数。
 *
 *  @param string 要转换的字符串。
 *  @return 转换后的整数，如果转换失败可能返回0。
 */
+ (int)convertStringToInt:(NSString *)string;

/**
 *  对输入的十六进制字符串逐位取反。
 *
 *  @param hexString 要取反的十六进制字符串。
 *  @return 取反后的十六进制字符串，如果输入不合法可能返回原字符串。
 */
+ (NSString *)invertHexadecimalString:(NSString *)hexString;

/**
 *  将十六进制字符串表示的数转换为对应的十进制NSInteger类型的数值。
 *
 *  @param hexString 十六进制字符串。
 *  @return 对应的十进制数值，如果转换失败可能返回0。
 */
+ (NSInteger)convertHexStringToDecimal:(NSString *)hexString;

/**
 *  把十进制整数转换为指定位数的十六进制字符串。
 *
 *  @param decimal 十进制整数。
 *  @param digit 要求的十六进制字符串位数。
 *  @return 转换后的十六进制字符串，如果位数不足会在前面补0。
 */
+ (NSString *)convertDecimalToHexStringWithDigit:(NSInteger)decimal digit:(NSInteger)digit;

/**
 *  对两字节长度的十六进制字符串进行大端和小端的转换。
 *
 *  @param hexSource 两字节的十六进制字符串。
 *  @return 转换后的十六进制字符串，如果输入长度不是4则返回原字符串。
 */
+ (NSString *)revertTwoBytesHexString:(NSString *)hexSource;

/**
 *  将单个十六进制字符转换为对应的十进制数值的字符串表示。
 *
 *  @param hexChar 十六进制字符。
 *  @return 对应的十进制数值的字符串表示，如果输入不是合法十六进制字符可能返回错误信息。
 */
+ (NSString *)convertHexCharToDecimalString:(unichar)hexChar;

/**
 *  将NSNumber对象转换为int类型的数值。
 *
 *  @param number 要转换的NSNumber对象。
 *  @return 转换后的int类型数值。
 */
+ (int)convertNumberToInt:(NSNumber *)number;

/**
 *  将包含数值的可变数组转换为对应的十六进制字符串。
 *
 *  @param array 包含数值的可变数组。
 *  @return 转换后的十六进制字符串，如果数组为空可能返回空字符串。
 */
+ (NSString *)convertArrayToHexString:(NSMutableArray *)array;


/**
 *  获取当前时间的时间戳，单位为秒，以字符串形式返回。
 *
 *  @return 当前时间的时间戳字符串。
 */
+ (NSString *)getCurrentTimestampInSeconds;

/**
 *  把指定格式的时间字符串转换为对应的时间戳（秒数）。
 *
 *  @param formattedTime 时间字符串。
 *  @param format 时间字符串的格式，如 "yyyy-MM-dd HH:mm:ss"。
 *  @return 对应的时间戳，如果转换失败可能返回0。
 */
+ (NSInteger)convertFormattedTimeToTimestamp:(NSString *)formattedTime andFormatter:(NSString *)format;

/**
 *  将时间戳（秒数）转换为指定格式的时间字符串。
 *
 *  @param timestamp 时间戳（秒数）。
 *  @param format 要转换的时间格式，如 "yyyy-MM-dd HH:mm:ss"。
 *  @return 转换后的时间字符串，如果转换失败可能返回空字符串。
 */
+ (NSString *)convertTimestampToFormattedTime:(NSInteger)timestamp andFormatter:(NSString *)format;


/**
 *  在数组中查找与指定文本相等的元素，并返回其索引。
 *
 *  @param array 要查找的数组。
 *  @param text 要查找的文本。
 *  @return 元素的索引，如果未找到则返回NSNotFound。
 */
+ (NSInteger)getIndexInArray:(NSArray *)array forText:(NSString *)text;

/**
 *  数据填充方法，将输入数据复制到缓冲区。
 *
 *  @param data 输入数据。
 *  @param length 输入数据的长度。
 *  @param buffer 目标缓冲区。
 *  @return 填充后的数据长度。
 */
+ (uint32_t)fillData:(uint8_t *)data length:(uint32_t)length buffer:(uint8_t *)buffer;

#pragma mark - 二进制异或
/**
 *  对两个NSData对象进行按位异或操作。
 *
 *  @param sourceData 源数据。
 *  @param xorData 用于异或的数据。
 *  @return 异或后的NSData对象，如果输入长度不一致可能返回nil。
 */
+ (NSData *)performXorOnData:(NSData *)sourceData withXorData:(NSData *)xorData;

/**
 *  根据输入的代码返回对应的屏幕颜色的字符串表示。
 *
 *  @param code 颜色代码。
 *  @return 对应的屏幕颜色字符串，如果代码无匹配可能返回空字符串。
 */
+ (NSString *)getScreenColorByCode:(NSString *)code;

/**
 *  判断当前设备是否为iPhone X系列机型。
 *
 *  @return 如果是iPhone X系列机型返回YES，否则返回NO。
 */
+ (BOOL)isIPhoneXSeries;

/**
 *  根据输入的代码返回对应的厂商名称。
 *
 *  @param code 厂商代码。
 *  @return 对应的厂商名称，如果代码无匹配可能返回空字符串。
 */
+ (NSString *)getManufacturerNameByCode:(NSString *)code;

@end



