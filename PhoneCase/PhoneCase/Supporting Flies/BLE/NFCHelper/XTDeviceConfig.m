//
//  XTDeviceConfig.m
//  EinkAPP
//
//  Created by xthh on 2025/1/21.
//

#import "XTDeviceConfig.h"

@implementation XTDeviceConfig

-(void)encodeWithCoder:(NSCoder *)aCoder{
    [aCoder encodeObject:self.manufacturerCode forKey:@"manufacturerCode"];
    [aCoder encodeObject:self.manufacturerName forKey:@"manufacturerName"];
    [aCoder encodeObject:self.screenColorCode forKey:@"screenColorCode"];
    [aCoder encodeObject:self.screenColorName forKey:@"screenColorName"];
    [aCoder encodeInteger:self.screenWidth forKey:@"screenWidth"];
    [aCoder encodeInteger:self.screenHeight forKey:@"screenHeight"];
    [aCoder encodeInteger:self.deviceNumber forKey:@"deviceNumber"];
    [aCoder encodeInteger:self.imageCount forKey:@"imageCount"];
    [aCoder encodeInteger:self.scanType forKey:@"scanType"];
    [aCoder encodeInteger:self.combineType forKey:@"combineType"];
    [aCoder encodeInteger:self.transMode forKey:@"transMode"];
    [aCoder encodeInteger:self.colorCount forKey:@"colorCount"];
    [aCoder encodeObject:self.colorDic forKey:@"colorDic"];
    [aCoder encodeObject:self.appID forKey:@"appID"];
    [aCoder encodeObject:self.uuID forKey:@"uuID"];
    [aCoder encodeBool:self.checkPin forKey:@"checkPin"];
    [aCoder encodeBool:self.hasPower forKey:@"hasPower"];
    [aCoder encodeBool:self.supportCompress forKey:@"supportCompress"];
    [aCoder encodeObject:self.pin forKey:@"pin"];
    [aCoder encodeBool:self.flipHorizontal forKey:@"flipHorizontal"];
    [aCoder encodeBool:self.flipVertical forKey:@"flipVertical"];
    [aCoder encodeInteger:self.versionCode forKey:@"versionCode"];
    [aCoder encodeInteger:self.writeImageCount forKey:@"writeImageCount"];
    [aCoder encodeInteger:self.brushType forKey:@"brushType"];
    [aCoder encodeObject:self.version forKey:@"version"];
}

- (instancetype)initWithCoder:(NSCoder *)aDecoder {
    if (self==[super init]) {
        self.manufacturerCode = [aDecoder decodeObjectForKey:@"manufacturerCode"];
        self.manufacturerName = [aDecoder decodeObjectForKey:@"manufacturerName"];
        self.screenColorCode = [aDecoder decodeObjectForKey:@"screenColorCode"];
        self.screenColorName = [aDecoder decodeObjectForKey:@"screenColorName"];
        self.screenWidth = [aDecoder decodeIntegerForKey:@"screenWidth"];
        self.screenHeight = [aDecoder decodeIntegerForKey:@"screenHeight"];
        self.deviceNumber = [aDecoder decodeIntegerForKey:@"deviceNumber"];
        self.imageCount = [aDecoder decodeIntegerForKey:@"imageCount"];
        self.scanType = [aDecoder decodeIntegerForKey:@"scanType"];
        self.combineType = [aDecoder decodeIntegerForKey:@"combineType"];
        self.transMode = [aDecoder decodeIntegerForKey:@"transMode"];
        self.colorCount = [aDecoder decodeIntegerForKey:@"colorCount"];
        if (@available(iOS 14.0, *)) {
            self.colorDic = [aDecoder decodeDictionaryWithKeysOfClass:NSString.class objectsOfClass:NSString.class forKey:@"colorDic"];
        } else {
            self.colorDic = [aDecoder decodeObjectOfClass:[NSDictionary class] forKey:@"colorDic"];
        }
        self.appID = [aDecoder decodeObjectForKey:@"appID"];
        self.uuID = [aDecoder decodeObjectForKey:@"uuID"];
        self.checkPin = [aDecoder decodeBoolForKey:@"checkPin"];
        self.hasPower = [aDecoder decodeBoolForKey:@"hasPower"];
        self.supportCompress = [aDecoder decodeBoolForKey:@"supportCompress"];
        self.pin = [aDecoder decodeObjectForKey:@"pin"];
        self.flipHorizontal = [aDecoder decodeBoolForKey:@"flipHorizontal"];
        self.flipVertical = [aDecoder decodeBoolForKey:@"flipVertical"];
        self.versionCode = [aDecoder decodeIntegerForKey:@"versionCode"];
        self.writeImageCount = [aDecoder decodeIntegerForKey:@"writeImageCount"];
        self.brushType = [aDecoder decodeIntegerForKey:@"brushType"];
        self.version = [aDecoder decodeObjectForKey:@"version"];
    }
    return self;
}

/**
 支持NSSecureCoding协议 必须支持
 
 @return 必须支持
 */
+ (BOOL)supportsSecureCoding {
    return YES;
}

/**
 序列化该类的对象
 
 @param XTDeviceConfig XTDeviceConfig 实例
 */
+ (void)saveLocalCfg:(XTDeviceConfig *)XTDeviceConfig {
    NSString *savePath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *filePath = [savePath stringByAppendingPathComponent:@"XTDeviceConfig.dat"];
    NSData *data=[NSKeyedArchiver archivedDataWithRootObject:XTDeviceConfig requiringSecureCoding:YES error:nil];
    [data writeToFile:filePath atomically:YES];
}

/**
 反序列化该类的对象
 
 @return return XTDeviceConfig 实例
 */
+ (XTDeviceConfig *)readLocalCfg {
    NSString *savePath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *filePath = [savePath stringByAppendingPathComponent:@"XTDeviceConfig.dat"];
    NSData *datData=[NSData dataWithContentsOfFile:filePath];
    NSError *error;
    
    XTDeviceConfig *result = [NSKeyedUnarchiver unarchivedObjectOfClasses:[NSSet setWithObjects:NSString.class, XTDeviceConfig.class, nil] fromData:datData error:&error];
    return result;
}

+ (BOOL)isPrismDevice {
    XTDeviceConfig *current = [XTDeviceConfig readLocalCfg];
    if (![XTDeviceConfig readLocalCfg]) {
        return NO;
    }
    BOOL isOldPrism = current.screenWidth == 64 && (current.screenHeight == 1 || current.screenHeight == 2); //1是动物 2是游鱼
    BOOL isNewPrism = current.deviceNumber == 83 || current.deviceNumber == 89 || current.deviceNumber == 90 || current.deviceNumber == 93;
    return isNewPrism || isOldPrism;
}

//是不是手机壳
- (BOOL)isPhoneCaseDevice {
    if (self.deviceNumber == 46 || self.deviceNumber == 70 || self.deviceNumber == 71 || self.deviceNumber == 97 || self.deviceNumber == 102 || self.deviceNumber == 113) {
        return YES;
    }
    return NO;
}

//一般用于nfc供电 发送13指令
- (BOOL)isDeviceSend13cmd {
    if (self.deviceNumber == 46 || self.deviceNumber == 70 || self.deviceNumber == 71 || self.deviceNumber == 97 || self.deviceNumber == 101 || self.deviceNumber == 113) {
        return YES;
    }
    BOOL isOldPrism = self.screenWidth == 64 && ( self.screenHeight == 1 || self.screenHeight == 2); //1是动物 2是游鱼
    BOOL isNewPrism = self.deviceNumber == 83 || self.deviceNumber == 89 || self.deviceNumber == 90 || self.deviceNumber == 93;
    if (isNewPrism || isOldPrism) {
        return YES;
    }
    return NO;
}

+ (int)getDeviceColorType:(NSString *)screenColorCode {
    int deviceColorType = 3; //3四色，5六色
    if([screenColorCode isEqualToString:COLOR_BWRY_CODE]) {
        deviceColorType = 3;
    } else if([screenColorCode isEqualToString:COLOR_BWRYGB_CODE]) {
        deviceColorType = 5;
    } else if([screenColorCode isEqualToString:COLOR_BWR_CODE]) {
        deviceColorType = 2;
    } else if ([screenColorCode isEqualToString:COLOR_BWY_CODE]){
        deviceColorType = 1;
    }
    return deviceColorType;
}

+ (int)getDeviceBleMaxMtu:(NSString *)screenColorCode {
    int mtu = 244;
    if([screenColorCode isEqualToString:COLOR_BWRY_CODE]) {//3 四色 黑白红黄
        mtu = 490;
    } else if([screenColorCode isEqualToString:COLOR_BWRYGB_CODE]) {//5 黑白红黄绿蓝
        mtu = 490;
    } else if([screenColorCode isEqualToString:COLOR_BWR_CODE]) {//2 黑白红
        mtu = 244;
    } else if ([screenColorCode isEqualToString:COLOR_BWY_CODE]){//5 黑白黄
        mtu = 244;
    }
    return mtu;
}

/**
 *  判断当前设备是否需要通过旋转来刷新图像（刷图）
 *
 *  @return YES表示需要旋转刷图，NO表示不需要
 */
- (BOOL)needsRotationToRefreshImage {
    if (self.deviceNumber == 105 || self.deviceNumber == 113 || self.deviceNumber == 120 || self.deviceNumber == 121) {
        return YES;
    }
    return NO;
}

/**
 *  NFC读取设备信息时，判断其传图方式是否为蓝牙
 *
 *  @return YES表示是蓝牙传图，NO表示不是
 */
- (BOOL)isBluetoothTransferWhenNFCReading {
    if (self.deviceNumber == 102 || self.deviceNumber == 105 || self.deviceNumber == 106 || self.deviceNumber == 119 || self.deviceNumber == 120 || self.deviceNumber == 121 || self.deviceNumber == 122) {
        return YES;
    }
    return NO;
}

//MARK: ================================  重写打印  ================================
- (NSString *)description {
    NSString *scanTypeStr = (self.scanType == VerticalType) ? @"Vertical" : @"Horizontal";
    NSString *combineTypeStr = @"GeneralType";
    NSString *brushTypeStr = (self.brushType == brushWithBle) ? @"brushWithBle" : (self.brushType == brushWithNfc) ? @"brushWithNfc" : @"brushWithNfcAndBle";

    return [NSString stringWithFormat:
            @"\n  brushType: %@\n\n"
            "  manufacturerCode: %@\n"
            "  manufacturerName: %@\n"
            "  screenColorCode: %@\n"
            "  screenColorName: %@\n"
            "  screenWidth: %ld\n"
            "  screenHeight: %ld\n"
            "  deviceNumber: %ld\n"
            "  imageCount: %ld\n"
            "  scanType: %@\n"
            "  combineType: %@\n"
            "  transMode: %ld\n"
            "  colorCount: %ld\n"
            "  colorDic: %@\n"
            "  appID: %@\n"
            "  uuID: %@\n"
            "  checkPin: %@\n"
            "  hasPower: %@\n"
            "  supportCompress: %@\n"
            "  versionCode: %ld\n"
            "  version: %@\n"
            "  writeImageCount: %ld\n"
            "  pin: %@\n"
            "  flipHorizontal: %@\n"
            "  flipVertical: %@\n",
            brushTypeStr,
            self.manufacturerCode,
            self.manufacturerName,
            self.screenColorCode,
            self.screenColorName,
            (long)self.screenWidth,
            (long)self.screenHeight,
            (long)self.deviceNumber,
            (long)self.imageCount,
            scanTypeStr,
            combineTypeStr,
            (long)self.transMode,
            (long)self.colorCount,
            self.colorDic,
            self.appID,
            self.uuID,
            self.checkPin? @"YES" : @"NO",
            self.hasPower? @"YES" : @"NO",
            self.supportCompress? @"YES" : @"NO",
            (long)self.versionCode,
            self.version,
            (long)self.writeImageCount,
            self.pin,
            self.flipHorizontal? @"YES" : @"NO",
            self.flipVertical? @"YES" : @"NO"];
}

@end
