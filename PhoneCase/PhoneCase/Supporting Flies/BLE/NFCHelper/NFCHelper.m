//
//  NFCHelper.m
//
//  Created by lubozhi on 2019/7/19.
//  Copyright © 2018年 复旦微电子集团股份科技有限公司. All rights reserved.
//

#import "NFCHelper.h"
#import "XTCommonUtils.h"
#import "BleProtocol.h"
#import "XTUtilsMacros.h"
#import "XTMBManager.h"

API_AVAILABLE(ios(13.0))
@interface NFCHelper()<NFCTagReaderSessionDelegate, CBCentralManagerDelegate,CBPeripheralDelegate>

// NFC会话
@property (nonatomic, strong) NFCTagReaderSession *session;

// 操作类型
@property (nonatomic, assign) OperationType opType;

// 图片数据数组
@property (nonatomic, strong) NSMutableArray *imageDataXTArr;
@property (nonatomic, strong) NSMutableArray *signHexArr;

// 写屏时的设备配置
@property (nonatomic, strong) XTDeviceConfig *screenCfg;

// 蓝牙中央管理器（管理扫描和连接）
@property (nonatomic, strong) CBCentralManager *centralManager;

// 存储发现的蓝牙设备（强引用避免被释放）
@property (nonatomic, strong) NSMutableArray<CBPeripheral *> *scanPeripherals;

// 蓝牙管理器状态
@property (nonatomic, assign) CBManagerState peripheralState;

// 当前检测到的NFC标签
@property (nonatomic, strong) id<NFCTag> currentNFCTag;

// 是否可以启动NFC
@property (nonatomic, assign) BOOL isNfcCanUsed;

// 以下为原静态变量转换的属性
#pragma mark - 原静态变量属性
// 页大小
@property (nonatomic, assign) int globaPageSize;
// 时间间隔
@property (nonatomic, assign) float globaIntervalTime;
// 写服务UUID
@property (nonatomic, strong) NSString *kWriteServerUUID;
// 写特征值UUID
@property (nonatomic, strong) NSString *kWriteCharacteristicUUID;
// 读特征UUID
@property (nonatomic, strong) NSString *kReadCharacteristicUUID;
// 需要刷图的MAC地址
@property (nonatomic, strong) NSString *strUploadDataMac;
// 待发送的数据包
@property (nonatomic, strong) NSMutableArray *arraySendDataPage;
// 补包数据
@property (nonatomic, strong) NSMutableArray *arraySubPageData;
// 应答等待状态
@property (nonatomic, assign) int globaWaitAnswerRunning;
// 删除FLASH的长度
@property (nonatomic, assign) int nDeleteFlashLen;
// 蓝牙连接计数
@property (nonatomic, assign) int nBleConnectCount;
// 扫描标签时间
@property (nonatomic, assign) int nScanTagTime;
// 蓝牙连接状态
@property (nonatomic, assign) int nBleConnectState;

// 扫描设备耗时
@property (nonatomic, assign) double lScanDeviceTime;
// 发送数据耗时
@property (nonatomic, assign) double lDeviceSendTime;
// 刷图耗时
@property (nonatomic, assign) double lFrushPicTime;

// 蓝牙读特征（用于接收设备数据）
@property (nonatomic, strong) CBCharacteristic *readCharacteristic;

// 蓝牙写特征（用于向设备发送数据）
@property (nonatomic, strong) CBCharacteristic *writeCharacteristic;

@property (nonatomic, copy) PhoneCaseInitializeCompletion initBlock;
@property (nonatomic, copy) NfcReadConfigCompletionBlock bindDeviceBlock;
@property (nonatomic, copy) ImportImageCompletionBlock   writeBlock;
@property (nonatomic, copy) ImportImageCompletionBlock   otaBlock;

@property (nonatomic, assign) int failedNum;
@property (nonatomic, strong) NSData *backData;
@property (nonatomic, assign) BOOL isSendSign;

@end

@implementation NFCHelper

+ (instancetype)shareInstance {
    static dispatch_once_t onceToken;
    static NFCHelper *nfcHelper;
    dispatch_once(&onceToken, ^{
        nfcHelper = [[NFCHelper alloc] init];
    });
    return nfcHelper;
}

- (instancetype)init {
    if (self = [super init]) {
        [self centralManager];
      
        // 初始化原静态变量对应的属性（保持原有初始值）
        _globaPageSize = 244;
        _globaIntervalTime = 0.01;

        _strUploadDataMac = @"00000000000";
        _arraySendDataPage = [NSMutableArray array];
        _arraySubPageData = [NSMutableArray array];
        _globaWaitAnswerRunning = 1;
        _nDeleteFlashLen = 0;
        _nBleConnectCount = 0;
        _nScanTagTime = 0;
        _nBleConnectState = 0;
        _lScanDeviceTime = 0;
        _lDeviceSendTime = 0;
        _lFrushPicTime = 0;
        
        // 获取服务UUID和读特证码及写特证码
        _kWriteServerUUID = [BleProtocol getBLEServiceUUID];
        _kWriteCharacteristicUUID = [BleProtocol getBLEWriteCharacteristicUUID];
        _kReadCharacteristicUUID = [BleProtocol getBLEReadCharacteristicUUID];
        _isReading = NO;
        _isNfcCanUsed = YES;
        _failedNum = 9999;

    }
    return self;
}

- (void)initializePhoneCaseDeviceWithCompletion:(PhoneCaseInitializeCompletion)completion {
    _opType = initDevice;
    _initBlock = completion;
    _failedNum = 9999;
    _backData = nil;
    [self startNDEF:initDevice];
}

/**
 读取设备配置参数
 
 @param completion 读取完成的回调 Block，返回设备配置对象（XTDeviceConfig）
 */
- (void)getbindDeviceWithPublicKey:(NSMutableArray *)PublicKey
                        completion:(NfcReadConfigCompletionBlock)completion {
    _opType = bindDevice;
    _failedNum = 9999;
    _backData = nil;
    _isSendSign = NO;

    NSMutableArray *arr = [NSMutableArray array];
    for (NSString *hex in PublicKey) {
        [arr addObject: [NSNumber numberWithInt:(int)[XTCommonUtils convertHexStringToDecimal:hex]]];
    }
    _imageDataXTArr = arr;
    _bindDeviceBlock = completion;
    [self startNDEF:bindDevice];
}

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
                                    completion:(ImportImageCompletionBlock)completion {
    
    if(!imageDataHexArr || imageDataHexArr.count == 0){
        NSError *error = [NSError errorWithDomain:@"ImportImageError"
                                             code:-1001
                                         userInfo:@{NSLocalizedDescriptionKey: @"数据为空"}];
        completion(nil,error);
        return;
    }
    _writeBlock = completion;
    _opType = WriteScreen;
    _imageDataXTArr = imageDataHexArr;
    NSMutableArray *arr = [NSMutableArray array];
    for (NSString *hex in signHexArr) {
        [arr addObject: [NSNumber numberWithInt:(int)[XTCommonUtils convertHexStringToDecimal:hex]]];
    }
    [self.signHexArr setArray:arr];
    
    _screenCfg = screenCfg;
    _globaPageSize = pageSize;
    [self startNDEF:WriteScreen];
}

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
                             completion:(ImportImageCompletionBlock)completion {
    if(!otaDataHexArr || otaDataHexArr.count == 0){
        NSError *error = [NSError errorWithDomain:@"OTAError"
                                             code:-1001
                                         userInfo:@{NSLocalizedDescriptionKey: @"数据为空"}];
        completion(nil,error);
        return;
    }
    _otaBlock = completion;
    _opType = OTA;
    _imageDataXTArr = otaDataHexArr;
    NSMutableArray *arr = [NSMutableArray array];
    for (NSString *hex in signHexArr) {
        [arr addObject: [NSNumber numberWithInt:(int)[XTCommonUtils convertHexStringToDecimal:hex]]];
    }
    [self.signHexArr setArray:arr];
    _screenCfg = screenCfg;
    _globaPageSize = pageSize;
    [self startNDEF:OTA];
}

//MARK: - NFCTagReaderSessionDelegate
#pragma mark - 检测到NFC标签的回调（NDEF读写代理）
- (void)tagReaderSession:(NFCTagReaderSession *)session didDetectTags:(NSArray<__kindof id<NFCTag>> *)tags API_AVAILABLE(ios(13.0)) {
    NSLog(@"[NFC] 检测到标签，数量：%lu", (unsigned long)tags.count);
    
    // 处理多标签情况：提示存在多个标签并继续扫描
    if (tags.count > 1) {
        [self showMsgOnSession:session message:@"区域内存在多个标签" isContinueScan:YES];
        return;
    }
    
    // 根据当前操作类型执行对应逻辑
    switch (self.opType) {
        case OTA:
            [self handleOTATagDetected:session tags:tags];
            break;
            
        case WriteScreen:
            [self handleWriteScreenTagDetected:session tags:tags];
            break;
            
        case bindDevice:
            [self handleReadConfigTagDetected:session tags:tags];
            break;
            
        case initDevice:
            [self handleReadConfigTagDetected:session tags:tags];
            break;
            
        default:
            NSLog(@"[NFC] 未知操作类型：%lu", (unsigned long)self.opType);
            break;
    }
}


#pragma mark - 私有处理方法
/// 处理OTA操作时检测到标签的逻辑
- (void)handleOTATagDetected:(NFCTagReaderSession *)session tags:(NSArray<__kindof id<NFCTag>> *)tags {
    NSLog(@"[NFC-OTA] 检测到标签，开始处理");
    // 记录设备MAC和扫描时间
    _strUploadDataMac = _screenCfg.appID;
    _lScanDeviceTime = (long)[[NSDate date] timeIntervalSince1970];
    
    // 停止当前蓝牙扫描，显示OTA进度提示
    [self.centralManager stopScan];
    [self nfcAlertMsg:[NSString stringWithFormat:@"正在OTA... 2%%"] success:YES delay:0];
    NSLog(@"[NFC-OTA] 目标设备MAC：%@", _strUploadDataMac);
    
    // 检查蓝牙状态并重新扫描外设
    if (self.peripheralState == CBManagerStatePoweredOn) {
        _nScanTagTime = 0;
        [self.centralManager scanForPeripheralsWithServices:nil options:nil];
    } else {
        [self nfcAlertMsg:@"蓝牙状态异常" success:NO delay:1];
    }
    
    // 保存当前检测到的标签（单标签场景）
    if (tags.count == 1) {
        self.currentNFCTag = tags.firstObject;
    }
}

/// 处理写屏操作时检测到标签的逻辑
- (void)handleWriteScreenTagDetected:(NFCTagReaderSession *)session tags:(NSArray<__kindof id<NFCTag>> *)tags {
    NSLog(@"[NFC-WriteScreen] 检测到标签，蓝牙连接计数：%d", _nBleConnectCount);
 
        // 记录设备MAC和扫描时间
    _strUploadDataMac = _screenCfg.appID;
    _lScanDeviceTime = (long)[[NSDate date] timeIntervalSince1970];
    
    // 停止当前蓝牙扫描，显示准备刷图提示
    [self.centralManager stopScan];
    [self nfcAlertMsg:@"正在准备刷图" success:YES delay:0];
    NSLog(@"[NFC-WriteScreen] 准备扫描设备：%@", _strUploadDataMac);
        
    // 检查蓝牙状态并重新扫描外设
    if (self.peripheralState == CBManagerStatePoweredOn) {
        _nScanTagTime = 0;
        [self.centralManager scanForPeripheralsWithServices:nil options:nil];
    } else {
        [self nfcAlertMsg:@"蓝牙状态异常" success:NO delay:1];
    }
    
    // 保存当前检测到的标签（单标签场景）
    if (tags.count == 1) {
        self.currentNFCTag = tags.firstObject;
    }
}

- (void)handleReadConfigTagDetected:(NFCTagReaderSession *)session tags:(NSArray<__kindof id<NFCTag>> *)tags {
    NSLog(@"[NFC-ReadConfig] 检测到标签，开始读取配置");
    
    dispatch_async(dispatch_get_main_queue(), ^{
        // 场景1：操作类型已变（并发安全校验）
        if (self.opType != bindDevice && self.opType != initDevice) {
            NSError *typeError = [NSError errorWithDomain:@"NFCReadError"
                                                    code:1000
                                                userInfo:@{NSLocalizedDescriptionKey: @"操作类型已变更，取消读取"}];
            [self notifyReadConfigError:typeError];
            return;
        }
        
        // 场景2：多标签（无法处理）
        if (tags.count > 1) {
            NSError *multiTagError = [NSError errorWithDomain:@"NFCReadError"
                                                        code:1001
                                                    userInfo:@{NSLocalizedDescriptionKey: @"区域内存在多个标签，无法读取"}];
            [self notifyReadConfigError:multiTagError];
            return;
        }
        
        id tag = tags.firstObject;
        [session connectToTag:tag completionHandler:^(NSError * _Nullable error) {
            // 场景3：连接标签失败
            if (error) {
                NSError *connectError = [NSError errorWithDomain:@"NFCReadError"
                                                             code:1002
                                                         userInfo:@{NSLocalizedDescriptionKey: [NSString stringWithFormat:@"连接NFC标签失败：%@", error.localizedDescription]}];
                [self notifyReadConfigError:connectError];
                return;
            }
            
            // 查询NDEF状态
            [tag queryNDEFStatusWithCompletionHandler:^(NFCNDEFStatus status, NSUInteger capacity, NSError * _Nullable error) {
                // 场景4：查询状态失败
                if (error) {
                    NSError *queryError = [NSError errorWithDomain:@"NFCReadError"
                                                               code:1003
                                                           userInfo:@{NSLocalizedDescriptionKey: [NSString stringWithFormat:@"查询标签状态失败：%@", error.localizedDescription]}];
                    [self notifyReadConfigError:queryError];
                    return;
                }
                
                // 场景5：标签不支持NDEF格式
                if (status == NFCNDEFStatusNotSupported) {
                    NSError *unsupportedError = [NSError errorWithDomain:@"NFCReadError"
                                                                     code:1004
                                                                 userInfo:@{NSLocalizedDescriptionKey: @"当前标签不支持NDEF格式，无法读取配置"}];
                    [self notifyReadConfigError:unsupportedError];
                    return;
                }
                
                // 读取NDEF数据
                [tag readNDEFWithCompletionHandler:^(NFCNDEFMessage * _Nullable message, NSError * _Nullable error) {
                    // 场景6：读取数据失败
                    if (error) {
                        NSError *readError = [NSError errorWithDomain:@"NFCReadError"
                                                               code:1005
                                                           userInfo:@{NSLocalizedDescriptionKey: [NSString stringWithFormat:@"读取标签数据失败：%@", error.localizedDescription]}];
                        [self notifyReadConfigError:readError];
                        return;
                    }
                    
                    // 场景7：标签数据为空
                    if (!message || message.records.count == 0) {
                        NSError *emptyError = [NSError errorWithDomain:@"NFCReadError"
                                                                code:1006
                                                            userInfo:@{NSLocalizedDescriptionKey: @"NFC标签为空，无设备配置数据"}];
                        [self notifyReadConfigError:emptyError];
                        return;
                    }
                    
                    if (message != NULL && message.records != NULL) {
                        NSUInteger count = message.records.count;
                        for (int i = 0; i < count; i++) {
                            NFCNDEFPayload *ary = message.records[i];
                            NSString *inputStr = [[NSString alloc] initWithData:ary.payload encoding:NSUTF8StringEncoding];
                            XTLog(@"NFC read info ---%@",inputStr);
                            [self bandDeviceAndWriteNFCToCache:inputStr fromQR:NO];
                        }
                    } else {
                        [self nfcAlertMsg:@"读取信息失败" success:NO delay:1];
                    }
                }];
            }];
        }];
    });
}

/// 通知读取配置失败（专注报错：日志+提示+代理）
/// @param error 具体错误信息（含错误码和描述）
- (void)notifyReadConfigError:(NSError *)error {
    // 1. 报错日志：打印错误详情（方便调试定位问题）
    if (error) {
        NSLog(@"[NFC-ReadConfig-Error] 失败：%@（错误码：%ld）",
              error.localizedDescription,
              (long)error.code);
    }
    
    // 2. 用户可见报错：弹出提示（失败必显，成功不调用此方法）
    if (error) {
        [self nfcAlertMsg:error.localizedDescription success:NO delay:1];
    }
    
    if (_bindDeviceBlock) {
        _bindDeviceBlock(nil, nil,error);
    }
}

- (void)bandDeviceAndWriteNFCToCache:(NSString *)inputStr fromQR:(BOOL)fromQR {
    TagBindInfo tagInfo = [BleProtocol getNFCTagBindInfo:inputStr];
    if (tagInfo.result) {
        XTDeviceConfig *cdevice = [[XTDeviceConfig alloc] init];
        cdevice.screenColorName = [XTCommonUtils getScreenColorByCode:cdevice.screenColorCode];
        cdevice.screenHeight = tagInfo.height;
        cdevice.screenWidth = tagInfo.width;
        cdevice.deviceNumber = tagInfo.deviceNumber;
        
        cdevice.brushType = [cdevice isBluetoothTransferWhenNFCReading] ? brushWithBle : brushWithNfcAndBle;
        cdevice.appID = tagInfo.mac;
        cdevice.uuID = tagInfo.mac;
        
        if([tagInfo.color isEqualToString:@"BW"]) {
            cdevice.screenColorCode = COLOR_BW_CODE;
            cdevice.colorCount = 2;
            NSMutableDictionary *colorDic = [[NSMutableDictionary alloc] init];
            [colorDic setValue: @"0" forKey:DIC_KEY_BLACK];
            [colorDic setValue: @"1" forKey:DIC_KEY_WHITE];
            cdevice.colorDic = colorDic;
        } else if([tagInfo.color isEqualToString:@"BWR"]) {
            cdevice.screenColorCode = COLOR_BWR_CODE;
            cdevice.colorCount = 3;
            NSMutableDictionary *colorDic = [[NSMutableDictionary alloc] init];
            [colorDic setValue: @"0" forKey:DIC_KEY_BLACK];
            [colorDic setValue: @"1" forKey:DIC_KEY_WHITE];
            [colorDic setValue: @"3" forKey:DIC_KEY_RED];
            cdevice.colorDic = colorDic;
        } else if([tagInfo.color isEqualToString:@"BWY"]) {
            cdevice.screenColorCode = COLOR_BWY_CODE;
            cdevice.colorCount = 3;
            NSMutableDictionary *colorDic = [[NSMutableDictionary alloc] init];
            [colorDic setValue: @"0" forKey:DIC_KEY_BLACK];
            [colorDic setValue: @"1" forKey:DIC_KEY_WHITE];
            [colorDic setValue: @"2" forKey:DIC_KEY_YELLOW];
            cdevice.colorDic = colorDic;
        } else if([tagInfo.color isEqualToString:@"BWRY"]) {
            cdevice.screenColorCode = COLOR_BWRY_CODE;
            cdevice.colorCount = 4;
            NSMutableDictionary *colorDic = [[NSMutableDictionary alloc] init];
            [colorDic setValue: @"0" forKey:DIC_KEY_BLACK];
            [colorDic setValue: @"1" forKey:DIC_KEY_WHITE];
            [colorDic setValue: @"3" forKey:DIC_KEY_RED];
            [colorDic setValue: @"2" forKey:DIC_KEY_YELLOW];
            cdevice.colorDic = colorDic;
        } else if([tagInfo.color isEqualToString:@"BWRYGB"]) {
            cdevice.screenColorCode = COLOR_BWRYGB_CODE;
            cdevice.colorCount = 6;
            NSMutableDictionary *colorDic = [[NSMutableDictionary alloc] init];
            [colorDic setValue: @"0" forKey:DIC_KEY_BLACK];
            [colorDic setValue: @"1" forKey:DIC_KEY_WHITE];
            [colorDic setValue: @"3" forKey:DIC_KEY_RED];
            [colorDic setValue: @"2" forKey:DIC_KEY_YELLOW];
            [colorDic setValue: @"6" forKey:DIC_KEY_GREEN];
            [colorDic setValue: @"5" forKey:DIC_KEY_BLUE];
            cdevice.colorDic = colorDic;
        }
        
        switch (tagInfo.rotationType){
            case 0:{
                cdevice.scanType = HorizontalType;
                break;
            }
            case 1:{
                cdevice.scanType = VerticalType;
                break;
            }
            case 2:{
                cdevice.scanType = HorizontalType;
                break;
            }
            case 3:{
                cdevice.scanType = VerticalType;
                break;
            }
        }
        
        cdevice.combineType = GeneralType;
        cdevice.writeImageCount = 1;
        cdevice.imageCount = [XTCommonUtils convertHexStringToDecimal:@"1"];
        cdevice.hasPower = YES;
        cdevice.supportCompress = YES;
        cdevice.manufacturerCode = @"0";
        cdevice.manufacturerName = @"0";
        
        _screenCfg = cdevice;
        
        if (_opType == bindDevice) {
            [self beginSendPublicKeyWithDevice:cdevice];
        } else if (_opType == initDevice) {
            [self beginInitDevice:cdevice];
        }
    } else {
        NSError *emptyError = [NSError errorWithDomain:@"NFCReadError"
                                                code:1006
                                            userInfo:@{NSLocalizedDescriptionKey: @"信息解析错误，请检查设备NFC写入参数"}];
        [self notifyReadConfigError:emptyError];
    }
}

- (void)tagReaderSessionDidBecomeActive:(NFCTagReaderSession *)session API_AVAILABLE(ios(13.0)) {
    //    NSLog(@"readerSessionDidBecomeActive");
}

- (void)tagReaderSession:(NFCTagReaderSession *)session didInvalidateWithError:(NSError *)error API_AVAILABLE(ios(13.0)){
    NSLog(@"didInvalidateWithError:%@",error);
    _isNfcCanUsed = NO; //置为不可用
    NSString *msg = @"";
    switch (error.code) {
        case NFCReaderErrorUnsupportedFeature: {
            NSLog(@"NFCReaderErrorUnsupportedFeature");
            msg = @"NFC读取会话不支持此功能";
        }
            break;
        case NFCReaderErrorSecurityViolation: {
            NSLog(@"NFCReaderErrorSecurityViolation");
            msg = @"NFC与读取会话相关的安全违规已发生";
        }
            break;
        case NFCReaderErrorInvalidParameter: {
            NSLog(@"NFCReaderErrorInvalidParameter");
            msg = @"NFC输入参数无效";
        }
            break;
        case NFCReaderErrorInvalidParameterLength: {
            NSLog(@"NFCReaderErrorSecurityViolation");
            msg = @"NFC输入参数的长度无效";
        }
            break;
        case NFCReaderErrorParameterOutOfBound: {
            NSLog(@"NFCReaderErrorSecurityViolation");
            msg = @"NFC参数值超出可接受的范围";
        }
            break;
        case NFCReaderErrorRadioDisabled: {
            NSLog(@"NFCReaderErrorSecurityViolation");
            msg = @"设备上的 NFC 无线功能已禁用";
        }
            break;
        case NFCReaderTransceiveErrorTagConnectionLost: {
            NSLog(@"NFCReaderTransceiveErrorTagConnectionLost");
            msg = @"NFC读取器与标签失去连接";
        }
            break;
        case NFCReaderTransceiveErrorSessionInvalidated: {
            NSLog(@"NFCReaderTransceiveErrorTagConnectionLost");
            msg = @"NFC读取会话无效";
        }
            break;
        case NFCReaderTransceiveErrorTagNotConnected: {
            NSLog(@"NFCReaderTransceiveErrorTagConnectionLost");
            msg = @"NFC标签未处于连接状态";
        }
            break;
        case NFCReaderTransceiveErrorPacketTooLong: {
            NSLog(@"NFCReaderTransceiveErrorTagConnectionLost");
            msg = @"NFC数据包长度超过了标签支持的限制";
        }
            break;
        case NFCReaderTransceiveErrorRetryExceeded: {
            NSLog(@"NFCReaderTransceiveErrorRetryExceeded");
            msg = @"NFC已发生过多的重试";
        }
            break;
        case NFCReaderTransceiveErrorTagResponseError: {
            NSLog(@"NFCReaderTransceiveErrorTagResponseError");
            msg = @"NFC标签响应了一个错误";
        }
            break;
        case NFCReaderSessionInvalidationErrorUserCanceled: {
            NSLog(@"NFCReaderSessionInvalidationErrorUserCanceled");
        }
            break;
        case NFCReaderSessionInvalidationErrorSessionTimeout: {
            NSLog(@"NFCReaderSessionInvalidationErrorSessionTimeout");
            if (!(_screenCfg.brushType == brushWithNfc)) {
                msg = @"NFC会话超时";
            }
        }
            break;
        case NFCReaderSessionInvalidationErrorSessionTerminatedUnexpectedly: {
            NSLog(@"NFCReaderSessionInvalidationErrorSessionTerminatedUnexpectedly");
            msg = @"系统NFC繁忙，请稍后再试";
        }
            break;
        case NFCReaderSessionInvalidationErrorSystemIsBusy: {
            NSLog(@"NFCReaderSessionInvalidationErrorSystemIsBusy");
            msg = @"系统NFC繁忙，请稍后再试";
        }
            break;
        case NFCReaderSessionInvalidationErrorFirstNDEFTagRead: {
            NSLog(@"NFCReaderSessionInvalidationErrorFirstNDEFTagRead");
            msg = @"此NFC会话读取的第一个NDEF标签无效";
        }
            break;
        case NFCTagCommandConfigurationErrorInvalidParameters: {
            NSLog(@"NFCTagCommandConfigurationErrorInvalidParameters");
            msg = @"NFC标签已使用无效参数进行配置";
        }
            break;
        case NFCNdefReaderSessionErrorTagNotWritable: {
            NSLog(@"NFCTagCommandConfigurationErrorInvalidParameters");
            msg = @"NDEF 标签不可写";
        }
            break;
        case NFCNdefReaderSessionErrorTagUpdateFailure: {
            NSLog(@"NFCTagCommandConfigurationErrorInvalidParameters");
            msg = @"NFC读取会话未能更新NDEF标签";
        }
            break;
        case NFCNdefReaderSessionErrorTagSizeTooSmall: {
            NSLog(@"NFCTagCommandConfigurationErrorInvalidParameters");
            msg = @"NDEF标签的内存大小太小，无法存储数据";
        }
            break;
        case NFCNdefReaderSessionErrorZeroLengthMessage: {
            NSLog(@"NFCTagCommandConfigurationErrorInvalidParameters");
            msg = @"NDEF标签不包含NDEF消息";
        }
            break;
        default:
            break;
    }
    if (_centralManager) {
        [_centralManager stopScan];
    }
    if (_session) {
        [self nfcAlertMsg:msg success:NO delay:0.01];
        if (![NFCHelper isBlankString:msg]) {
            [XTMBManager showAlertViewWithText:msg delayHid:5.0];
        }
        NSLog(@"brush failed，time ：%f", [[NSDate date] timeIntervalSince1970] - _lFrushPicTime);
    }
}

//MARK: CBCentralManagerDelegate
// 蓝牙开关状态更新时调用
- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    switch (central.state) {
        case CBManagerStateUnknown:{
            [self nfcAlertMsg:@"未知状态" success:YES delay:0];
            self.peripheralState = central.state;
        }
            break;
        case CBManagerStateResetting:{
            [self nfcAlertMsg:@"重置状态" success:YES delay:0];
            self.peripheralState = central.state;
        }
            break;
        case CBManagerStateUnsupported:{
            [self nfcAlertMsg:@"不支持状态" success:YES delay:0];
            self.peripheralState = central.state;
        }
            break;
        case CBManagerStateUnauthorized:{
            [self nfcAlertMsg:@"未授权状态" success:YES delay:0];
            self.peripheralState = central.state;
        }
            break;
        case CBManagerStatePoweredOff:{
            [self nfcAlertMsg:@"关闭状态" success:YES delay:0];
            self.peripheralState = central.state;
        }
            break;
        case CBManagerStatePoweredOn:{
            [self nfcAlertMsg:@"请靠近设备" success:YES delay:0];
            self.peripheralState = central.state;
        }
            break;
        default:
            [self nfcAlertMsg:@"其它状态" success:YES delay:0];
            break;
    }
    
    NSLog(@"[蓝牙] 状态更新为：%ld | %@", (long)central.state, [NSDate date]);
}

/**
 扫描到设备
 @param central 中心管理者
 @param peripheral 扫描到的设备
 @param advertisementData 广告信息
 @param RSSI 信号强度
 */
- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary<NSString *,id> *)advertisementData RSSI:(NSNumber *)RSSI{
    
    NSString * mac = [advertisementData objectForKey:@"kCBAdvDataLocalName"];
    if ([mac isEqualToString:_strUploadDataMac]){
        NSLog(@"had found current device mac: %@，RSSI: %@",mac, RSSI);
        
        NSString *ver = [BleProtocol getVersionFromCBAdvDataManufacturerData:[advertisementData objectForKey:@"kCBAdvDataManufacturerData"]];
        NSLog(@"current version = %@", ver);
        
        XTDeviceConfig *info = [XTDeviceConfig readLocalCfg];
        info.version = ver;
        [XTDeviceConfig saveLocalCfg:info];
        
        BOOL result = [ver caseInsensitiveCompare:@"1.0.0"] == NSOrderedSame;
        if (_opType == OTA && result) {
            if (_screenCfg.deviceNumber == 71 || _screenCfg.deviceNumber == 46) {//因为46、71的 1.0.0 与1.0.1不兼容，升级会变成砖 //70待定
                [self nfcAlertMsg:@"当前版本禁止OTA" success:NO delay:1];
                return;
            }
        }
        _nScanTagTime = 1;
        
        if (_opType == WriteScreen) {
            [self nfcAlertMsg:[NSString stringWithFormat:@"%@ 20%%",@"正在传输图片数据"] success:YES delay:0];
        } else if (_opType == OTA) {
            [self nfcAlertMsg:[NSString stringWithFormat:@"%@ 20%%",@"正在传输OTA数据"] success:YES delay:0];
        }
        
        // 停止扫描
        [self.centralManager stopScan];

        if (![self.scanPeripherals containsObject:peripheral]){
            [self.scanPeripherals addObject: peripheral];
            NSLog(@"connectPeripheral: %@", peripheral);
            [self.centralManager connectPeripheral:peripheral options:nil];
        } else {
            [self.centralManager connectPeripheral:peripheral options:nil];
        }
    }
}

/**
 连接失败
 
 @param central 中心管理者
 @param peripheral 连接失败的设备
 @param error 错误信息
 */

- (void)centralManager:(CBCentralManager *)central didFailToConnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error{
    NSLog(@" centralManager connected failed");
    [self nfcAlertMsg:@"传输数据失败" success:NO delay:1];
    _nBleConnectState = 0;
}

/**
 连接断开
 @param central 中心管理者
 @param peripheral 连接断开的设备
 @param error 错误信息
 */

- (void)centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error{
    NSLog(@"centralManager disconnect %@", [NSString stringWithFormat:@" -- %@",error]);
    _nBleConnectState = 0;
}

/**
 连接成功
 @param central 中心管理者
 @param peripheral 连接成功的设备
 */
- (void)centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral{
    NSLog(@"Peripheral connect scucess，ready discover services");
    if (_opType == WriteScreen) {
        [self nfcAlertMsg:[NSString stringWithFormat:@"%@ 25%%",@"正在传输图片数据"] success:YES delay:0];
    } else if (_opType == OTA) {
        [self nfcAlertMsg:[NSString stringWithFormat:@"%@ 25%%",@"正在传输OTA数据"] success:YES delay:0];
    }
    
    // 设置设备的代理
    peripheral.delegate = self;
    // services:传入nil  代表扫描所有服务
    [peripheral discoverServices:nil];
}

//MARK: CBPeripheralDelegate
/**
 扫描到服务
 @param peripheral 服务对应的设备
 @param error 扫描错误信息
 */
- (void)peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error{
    // 遍历所有的服务
    for (CBService *service in peripheral.services){
        NSLog(@"kWriteServerUUID: %@",service.UUID.UUIDString);
        // 获取对应的服务
        if ([service.UUID.UUIDString isEqualToString:_kWriteServerUUID]){
            // 根据服务去扫描特征
            [peripheral discoverCharacteristics:nil forService:service];
        }
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didModifyServices:(NSArray<CBService *> *)invalidatedServices {
    
}

/**
 扫描到对应的特征
 
 @param peripheral 设备
 @param service 特征对应的服务
 @param error 错误信息
 */
- (void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error{
    // 遍历所有的特征
    int a1 = 0, a2 = 0;
    for (CBCharacteristic *characteristic in service.characteristics){
        NSLog(@"kWriteCharacteristicUUID:%@",characteristic.UUID.UUIDString);
        if ([characteristic.UUID.UUIDString isEqualToString:_kReadCharacteristicUUID]) {
            _connectedPeripheral = peripheral;
            _readCharacteristic = characteristic;
            a1 = 1;
            [_connectedPeripheral setNotifyValue:YES forCharacteristic:_readCharacteristic];
            [_connectedPeripheral readValueForCharacteristic:_readCharacteristic];
        }
        
        if ([characteristic.UUID.UUIDString isEqualToString:_kWriteCharacteristicUUID]) {
            _connectedPeripheral = peripheral;
            _writeCharacteristic = characteristic;
            a2 = 1;
        }
    }
    
    NSLog(@"ble writed characteristic state %d,%d", a1, a2);
    if (_nBleConnectCount == 0 && a1 == 1 && a2 == 1) {
        NSLog(@"ble writed characteristic sucess");
        [self nfcAlertMsg:[NSString stringWithFormat:@"%@ 30%%",@"正在传输图片数据"] success:YES delay:0];
        // 这里开始刷图
        _nBleConnectCount = 2;
        _nBleConnectState = 1;
        
        if (_opType == initDevice) {
            [self beginInitDevice];
        } else if (_opType == bindDevice) {
            [self beginBindDevice];
        } else if (_opType == OTA) {
            [self beginBrushOTA];
        } else {
            // 开始刷图
            [self beginBrushPic];
        }
    }
}

//写数据之后的回调   只有设置为CBCharacteristicWriteWithResponse之后才调用
-(void)peripheral:(CBPeripheral *)peripheral didWriteValueForCharacteristic:(nonnull CBCharacteristic *)characteristic error:(nullable NSError *)error{
    // 不会进入这里
    if (error) {
        NSLog(@"write fail");
        NSLog(@"Error writing characteristic value: %@",[error localizedDescription]);
    } else{
        NSData *value = characteristic.value;
        NSLog(@"write success, value: %@ lenth = %ld",value, value.length);
    }
}

//读数据之后的回调
- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(nonnull CBCharacteristic *)characteristic error:(nullable NSError *)error{
    if (error) {
        NSLog(@"Error reading characteristic value: %@", [error localizedDescription]);
    } else{
        NSData *data = characteristic.value;
        NSString *resultCmd = [XTCommonUtils convertDataToHexString:data];;
        NSLog(@"-------- ble data back :%@ ----------------------------------",resultCmd);
        
        // 处理接收到的数据
        NSMutableArray *arrarResult = [BleProtocol getBleSendResult:data];
        if (arrarResult && arrarResult.count >= 2) {
            _globaWaitAnswerRunning = [XTCommonUtils convertNumberToInt:arrarResult[0]];
            if (_opType == WriteScreen || _opType == OTA) {
                if (!_isSendSign) {
                    _arraySubPageData = arrarResult[1];
                    if (_arraySubPageData != NULL ) {
                        NSLog(@"ble need fix data: -- %ld", _arraySubPageData.count);
                    }
                } else {
                    if ([arrarResult[1] isKindOfClass:[NSNumber class]]) {
                        _failedNum = [arrarResult[1] intValue];
                    }
                    if (arrarResult.count >= 3) {
                        _backData = arrarResult[2];
                    }
                }
            } else if (_opType == initDevice) {
                if ([arrarResult[1] isKindOfClass:[NSNumber class]]) {
                    _failedNum = [arrarResult[1] intValue];
                }
            } else if (_opType == bindDevice) {
                if ([arrarResult[1] isKindOfClass:[NSNumber class]]) {
                    _failedNum = [arrarResult[1] intValue];
                }
                if (arrarResult.count >= 3) {
                    _backData = arrarResult[2];
                }
            }
        } else {
            NSLog(@" ble failed: %@", arrarResult);
        }
    }
}

//MARK: Methods
//重开NFC
- (void)showMsgOnSession:(NFCReaderSession *)session
                 message:(NSString *)msg
          isContinueScan:(BOOL)isContinueScan {
    if(isContinueScan){
        session.alertMessage = msg;
        [NSThread sleepForTimeInterval:0.2];//0.2
        if(_session){
            _isReading = YES;
            [_session restartPolling];
        }
    } else{
        if(_session){
            _isReading = NO;
            [_session invalidateSessionWithErrorMessage:msg];
            _session = nil;
        }
    }
}

//NFC 弹窗
- (void)nfcAlertMsg:(NSString *)msg success:(BOOL)success delay:(CGFloat)delay {
    dispatch_async(dispatch_get_main_queue(), ^{
        if (success) {
            self.session.alertMessage = msg;
        } else {
            [self.session invalidateSessionWithErrorMessage:msg];
        }
    });
    if (delay != 0) {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delay * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [self removeSession];
        });
    }
}

//移除NFC session
- (void)removeSession {
    NSLog(@"NFC removeSession");
    if(_session){
        [_session invalidateSession];
        _session = nil;
        NSLog(@"NFC really removeSession");

        [NSThread sleepForTimeInterval:2];
    }
    if(_centralManager && _connectedPeripheral && _nBleConnectState == 1){
        [_centralManager cancelPeripheralConnection:_connectedPeripheral];
        _connectedPeripheral = nil;
    }
    _isReading = NO;
}

- (void)alertMsg:(NSString *)msg {
    [XTMBManager showAlertViewWithText:msg delayHid:1.5];
}

//MARK:  ============================= 蓝牙发送 =============================
- (int)senDataToDevice:(NSString *)cmdStr {
    _globaWaitAnswerRunning = 1;
    NSData *data = [XTCommonUtils convertHexStringToData:cmdStr];
    [_connectedPeripheral writeValue:data forCharacteristic:_writeCharacteristic type:CBCharacteristicWriteWithoutResponse];
    
    //等待5s
    int nGetTimeout = 0;
    while (_globaWaitAnswerRunning == 1){
        [NSThread sleepForTimeInterval:0.01];
        nGetTimeout++;
        if (nGetTimeout > 500) {
            break;
        }
    }
    switch (_globaWaitAnswerRunning) {
        case 1:
            XTLog(@"cmd send time out");
            return 2;
        case 2:
            XTLog(@"cmd send failed");
            return 3;
        case 104:
            XTLog(@"Command execution failed");
            return 104;
        case 205:
            XTLog(@"RSA security authentication failed");
            return 205;
        case 206:
            XTLog(@"ECDSA signature verification failed");
            return 206;
        default:
            XTLog(@"cmd send success");
            return 1;
    }
}


- (void)senDataToDeviceWithNoNeedResponse:(NSString *)cmdStr {
    NSData *data = [XTCommonUtils convertHexStringToData:cmdStr];
    [_connectedPeripheral writeValue:data forCharacteristic:_writeCharacteristic type:CBCharacteristicWriteWithoutResponse];
}

//MARK:  ============================= 开始刷图 =============================
- (void)beginBrushPic {
    NSLog(@"---------- begin brush image -----------");
    [self nfcAlertMsg:[NSString stringWithFormat:@"%@ 40%%",_opType == OTA ? @"正在准备写入数据" : @"正在传输图片数据"] success:YES delay:0];
    // 异步发送
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{

        [self nfcAlertMsg:@"正在下发签名" success:YES delay:0];
        self.isSendSign = YES;
        
        //写
        NSString *order = [BleProtocol getVerifySignatureOrderForPhoneCaseWithParam:self.signHexArr];
        NSLog(@"---------- send 19 send sign hash cmd %@ -----------",order);
        [self senDataToDeviceWithNoNeedResponse:order];
        
        self.globaWaitAnswerRunning = 999;
        int nGetTimeout = 0;
        while (self.globaWaitAnswerRunning == 999) {
            [NSThread sleepForTimeInterval:0.02]; // 每次等待0.02秒
            nGetTimeout++;
            
            // 检查超时（250次×0.02秒=5秒超时）
            if (nGetTimeout > 250) {
                break;
            }
            
            // 检查NFC是否可用，不可用则提示并返回
            if (!self.isNfcCanUsed) {
                NSLog(@"NFC disconnected - brush failed 222");
                [self nfcAlertMsg:@"NFC暂不可用" success:NO delay:1];
                return;
            }
        }
        
        if (self.globaWaitAnswerRunning != 0xFF) {
            [self nfcAlertMsg:@"发送签名指令超时/失败" success:NO delay:1];
            if (self.writeBlock) {
                NSError *error = [NSError errorWithDomain:@"brushError"
                                                     code: self.failedNum
                                                 userInfo:@{NSLocalizedDescriptionKey: @"发送签名读取指令超时/失败"}];
                self.writeBlock(nil,error);
            }
            return;
        }
        self.isSendSign = NO;

        // 获取删除FLASH指令
        NSString *deleteFlashOrder = [BleProtocol getDeleteFlashOrder:self.nDeleteFlashLen];
        // 发送指令
        int value = [self senDataToDevice:deleteFlashOrder];
        NSLog(@"11FLASH delete state %@ = %d = %d", deleteFlashOrder,value,self.nDeleteFlashLen);
        if (value != 1) {
            [self hanleErrwithValue:value cmd:0x01];
            return;
        }
        
        [self nfcAlertMsg:[NSString stringWithFormat:@"%@ %d%%",self.opType == OTA ? @"正在准备写入数据" : @"正在传输图片数据", value == 1 ? 41 : 39] success:YES delay:0];
        
        self.lScanDeviceTime = (long)[[NSDate date] timeIntervalSince1970] - self.lScanDeviceTime;
        self.lDeviceSendTime = (long)[[NSDate date] timeIntervalSince1970];
        NSLog(@"globaIntervalTime:%f", self.globaIntervalTime);
        
        // 先设置为全部都需要补包
        self.arraySubPageData = [NSMutableArray array];
        for (int i = 0; i < self.arraySendDataPage.count; i++){
            [self.arraySubPageData addObject:[NSNumber numberWithInt:0]];
        }
        NSUInteger x1 = [self.connectedPeripheral maximumWriteValueLengthForType:CBCharacteristicWriteWithoutResponse];
        NSLog(@"maximumWriteValueLengthForType = %ld", x1);
        
        // 共可以补包5次
        for (int failCount = 0; failCount < 5; failCount++){
            // 发送数据包
            for (int i = 0; i < self.arraySendDataPage.count; i++){
                if (self.arraySubPageData != NULL && i < self.arraySubPageData.count) {
                    int indexValue = [self.arraySubPageData[i] intValue];
                    if (indexValue == 0){
                        // 这里是表示需要补包的
                        NSMutableArray *page = self.arraySendDataPage[i];
                        for (int j = 0; j < page.count; j++) {
                            if (self.nBleConnectState == 1) {
                                NSData *data = page[j];
                                // 发送数据
                                NSLog(@"--- sendData - All %d - current %d - %d - %lu - %@",j, i, (int)self.arraySendDataPage.count,(unsigned long)data.length,[XTCommonUtils convertDataToHexString:data]);
                                
                                [self.connectedPeripheral writeValue:data forCharacteristic:self.writeCharacteristic type:CBCharacteristicWriteWithoutResponse];
                                
                                // globaPageSize
                                float intervalTime = 0.015;//5
                                [NSThread sleepForTimeInterval:intervalTime];
                            }
                        }
                        int t;
                        for(t = 0; t < 50; t++) {
                            if (self.connectedPeripheral.canSendWriteWithoutResponse == true) {
                                break;
                            }
                            [NSThread sleepForTimeInterval:0.001];
                        }
                        NSLog(@"ready to write %d", t);
                        if (self.nBleConnectState == 0) {
                            NSLog(@"ble disconnected, brush failed");
                            [self nfcAlertMsg:@"蓝牙已断开-刷图失败" success:NO delay:1];
                            return;
                        }
                    }
                }
            }
            
            // 清空需要补包的
            self.arraySubPageData = [NSMutableArray array];
            
            // 获取连接指令（获取是否补包数据）
            NSString *orderUpdataOrder = [BleProtocol getUpdateOrder:self.opType == WriteScreen ? 0 : 2 value:0]; // 0-pic 2-ota;
  
            // 发送
            NSLog(@"send 04 cmd =%@",orderUpdataOrder);
            value = [self senDataToDevice:orderUpdataOrder];
            if (value == 2){
                value = [self senDataToDevice:orderUpdataOrder];
                NSLog(@"send 04 cmd again = %d", value);
            } else if (value == 3){
                NSLog(@"brush failed, data not complete");
                [self nfcAlertMsg:[NSString stringWithFormat:@"%@ 55%%",@"正在传输图片数据"] success:YES delay:0];
            } else if (value == 1){
                if (self.opType == WriteScreen) {
                    // pic
                    self.globaWaitAnswerRunning = 1;
                    [self nfcAlertMsg:[NSString stringWithFormat:@"%@ 56%%",@"正在传输图片数据"] success:YES delay:0];
                    
                    NSLog(@"connect ble time：%f", self.lScanDeviceTime);
                    NSLog(@"send file time：%f", [[NSDate date] timeIntervalSince1970] - self.lDeviceSendTime);
                    
                    [self showMsgOnSession:self.session message:[NSString stringWithFormat:@"%@ 60%%",@"正在传输图片数据"] isContinueScan:YES];
                    NSLog(@"NFC restartPolling");
                    [NSThread sleepForTimeInterval:0.2];// 间隔1秒
                    
                    self.isSendSign = YES;
                    
                    // 发个13指令
                    NSString *flushPicOrder = [BleProtocol getPicRefreshOrder];
                    NSLog(@"send image brush cmd 13 back = %d - %@", value, flushPicOrder);
                    [self senDataToDeviceWithNoNeedResponse:flushPicOrder];

                    //开始刷图
                    [self nfcAlertMsg:@"正在刷新设备..." success:YES delay:0];
                    self.lFrushPicTime = [[NSDate date] timeIntervalSince1970];
                    
                    // 等待13命令返回成功
                    self.globaWaitAnswerRunning = 1;
                    for (int nw = 0; nw < 200; nw++){        //niel25 ---》 50
                        if (self.globaWaitAnswerRunning == 0){
                            break;
                        }
                        [NSThread sleepForTimeInterval:0.1];// 间隔1秒   70 + 30 * 0.64
                        if (self.isNfcCanUsed == NO) {
                            NSLog(@"NFC disconnected - brush failed 222");
                            [self nfcAlertMsg:@"NFC暂不可用" success:NO delay:1];
                            return;
                        }
                    }
                    NSLog(@"brush sueccss. time：%f", [[NSDate date] timeIntervalSince1970] - self.lFrushPicTime);
                    
                    if (self.globaWaitAnswerRunning != 0) {
                        [self nfcAlertMsg:@"下发刷图指令超时/失败" success:NO delay:1];
                        if (self.writeBlock) {
                            NSError *error = [NSError errorWithDomain:@"brushError"
                                                                 code: self.failedNum
                                                             userInfo:@{NSLocalizedDescriptionKey: @"下发刷图指令超时/失败"}];
                            self.writeBlock(nil,error);
                        }
                        return;
                    }
                    
                    if (self.failedNum != 0) {
                        [self nfcAlertMsg:@"下发刷图返回失败" success:NO delay:1];
                        if (self.writeBlock) {
                            NSError *error = [NSError errorWithDomain:@"brushError"
                                                                 code: self.failedNum
                                                             userInfo:@{NSLocalizedDescriptionKey: @"下发刷图返回失败"}];
                            self.writeBlock(nil,error);
                        }
                        return;
                    }
                    
                    if (self.backData.length != 112) {
                        [self nfcAlertMsg:@"下发图片返回验证签名信息返回长度不够" success:NO delay:1];
                        if (self.writeBlock) {
                            NSError *error = [NSError errorWithDomain:@"brushError"
                                                                 code: self.failedNum
                                                             userInfo:@{NSLocalizedDescriptionKey: @"下发图片返回验证签名信息返回长度不够"}];
                            self.writeBlock(nil,error);
                        }
                        return;
                    }
                    
                    NSLog(@"bind == %d %d %@",self.globaWaitAnswerRunning,self.failedNum,self.backData);
                    
                    if (self.writeBlock) {
                        self.writeBlock(self.backData, nil);
                    }
                    self.backData = nil;
                    self.failedNum = 999;
                    [self nfcAlertMsg:@"刷图成功" success:YES delay:0.1];
                    return;
                } else {
                    //发送个05通知主板 OTA升级
                    NSString *otaUpdateStr = [BleProtocol startHardwareUpdateOrder];
                    value = [self senDataToDevice:otaUpdateStr];
                    NSLog(@"send hardware ota begin cmd 05 back = %d", value);
                    if (value == 1) {
                        // ota
                        // 必须要先断开蓝牙在停5秒
                        [self nfcAlertMsg:@"正在OTA..." success:YES delay:0];
                        if (self.nBleConnectState == 1) {
                            [self.centralManager cancelPeripheralConnection:self.connectedPeripheral];
                        }
                        [NSThread sleepForTimeInterval:5];// 间隔1秒
                        
                        NSLog(@"OTA sueccss");
                        [self nfcAlertMsg:@"OTA成功" success:YES delay:1];
                    } else if (value == 2) {
                        value = [self senDataToDevice:otaUpdateStr];
                        NSLog(@"send hardware ota begin cmd 05 again = %d", value);
                    } else if (value == 3){
                        [self nfcAlertMsg:@"升级OTA失败" success:NO delay:1];
                    }
                    return;
                }
            }
        }
        NSLog(@"bursh failed");
        [self nfcAlertMsg:@"NFC暂不可用" success:NO delay:1];
    });
}

- (void)beginBrushOTA  {
    NSLog(@"---------- begin brush OTA -----------");
    [self nfcAlertMsg:@"正在准备写入数据 40%%" success:YES delay:0];
    // 异步发送
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{

        [self nfcAlertMsg:@"正在下发签名" success:YES delay:0];
        self.isSendSign = YES;
        
        //写
        NSString *order = [BleProtocol getVerifySignatureOrderForPhoneCaseWithParam:self.signHexArr];
        NSLog(@"---------- send 19 send sign hash cmd %@ -----------",order);
        [self senDataToDeviceWithNoNeedResponse:order];
        
        self.globaWaitAnswerRunning = 999;
        int nGetTimeout = 0;
        while (self.globaWaitAnswerRunning == 999) {
            [NSThread sleepForTimeInterval:0.02]; // 每次等待0.02秒
            nGetTimeout++;
            
            // 检查超时（250次×0.02秒=5秒超时）
            if (nGetTimeout > 250) {
                break;
            }
            
            // 检查NFC是否可用，不可用则提示并返回
            if (!self.isNfcCanUsed) {
                NSLog(@"NFC disconnected - brush failed 222");
                [self nfcAlertMsg:@"NFC暂不可用" success:NO delay:1];
                return;
            }
        }
        
        if (self.globaWaitAnswerRunning != 0xFF) {
            [self nfcAlertMsg:@"发送签名指令超时/失败" success:NO delay:1];
            if (self.otaBlock) {
                NSError *error = [NSError errorWithDomain:@"brushError"
                                                     code: self.failedNum
                                                 userInfo:@{NSLocalizedDescriptionKey: @"发送签名读取指令超时/失败"}];
                self.otaBlock(nil,error);
            }
            return;
        }
        self.isSendSign = NO;

        // 获取删除FLASH指令
        NSString *deleteFlashOrder = [BleProtocol getDeleteFlashOrder:self.nDeleteFlashLen];
        // 发送指令
        int value = [self senDataToDevice:deleteFlashOrder];
        NSLog(@"11FLASH delete state %@ = %d = %d", deleteFlashOrder,value,self.nDeleteFlashLen);
        if (value != 1) {
            [self hanleErrwithValue:value cmd:0x01];
            return;
        }
        
        [self nfcAlertMsg:@"正在准备写入数据41%" success:YES delay:0];
        
        self.lScanDeviceTime = (long)[[NSDate date] timeIntervalSince1970] - self.lScanDeviceTime;
        self.lDeviceSendTime = (long)[[NSDate date] timeIntervalSince1970];
        NSLog(@"globaIntervalTime:%f", self.globaIntervalTime);
        
        // 先设置为全部都需要补包
        self.arraySubPageData = [NSMutableArray array];
        for (int i = 0; i < self.arraySendDataPage.count; i++){
            [self.arraySubPageData addObject:[NSNumber numberWithInt:0]];
        }
        NSUInteger x1 = [self.connectedPeripheral maximumWriteValueLengthForType:CBCharacteristicWriteWithoutResponse];
        NSLog(@"maximumWriteValueLengthForType = %ld", x1);
        
        // 共可以补包5次
        for (int failCount = 0; failCount < 5; failCount++){
            // 发送数据包
            for (int i = 0; i < self.arraySendDataPage.count; i++){
                if (self.arraySubPageData != NULL && i < self.arraySubPageData.count) {
                    int indexValue = [self.arraySubPageData[i] intValue];
                    if (indexValue == 0){
                        // 这里是表示需要补包的
                        NSMutableArray *page = self.arraySendDataPage[i];
                        for (int j = 0; j < page.count; j++) {
                            if (self.nBleConnectState == 1) {
                                NSData *data = page[j];
                                // 发送数据
                                NSLog(@"--- sendData - All %d - current %d - %d - %lu - %@",j, i, (int)self.arraySendDataPage.count,(unsigned long)data.length,[XTCommonUtils convertDataToHexString:data]);
                                
                                [self.connectedPeripheral writeValue:data forCharacteristic:self.writeCharacteristic type:CBCharacteristicWriteWithoutResponse];
                                
                                // globaPageSize
                                float intervalTime = 0.015;//5
                                [NSThread sleepForTimeInterval:intervalTime];
                            }
                        }
                        int t;
                        for(t = 0; t < 50; t++) {
                            if (self.connectedPeripheral.canSendWriteWithoutResponse == true) {
                                break;
                            }
                            [NSThread sleepForTimeInterval:0.001];
                        }
                        NSLog(@"ready to write %d", t);
                        if (self.nBleConnectState == 0) {
                            NSLog(@"ble disconnected, brush failed");
                            [self nfcAlertMsg:@"蓝牙已断开-刷图失败" success:NO delay:1];
                            return;
                        }
                    }
                }
            }
            
            // 清空需要补包的
            self.arraySubPageData = [NSMutableArray array];
            
            // 获取连接指令（获取是否补包数据）
            NSString *orderUpdataOrder = [BleProtocol getUpdateOrder:self.opType == WriteScreen ? 0 : 2 value:0]; // 0-pic 2-ota;
  
            // 发送
            NSLog(@"send 04 cmd =%@",orderUpdataOrder);
            value = [self senDataToDevice:orderUpdataOrder];
            if (value == 2){
                value = [self senDataToDevice:orderUpdataOrder];
                NSLog(@"send 04 cmd again = %d", value);
            } else if (value == 3){
                NSLog(@"brush failed, data not complete");
                [self nfcAlertMsg:[NSString stringWithFormat:@"%@ 55%%",@"正在传输图片数据"] success:YES delay:0];
            } else if (value == 1){
                //发送个05通知主板 OTA升级
                NSString *otaUpdateStr = [BleProtocol startHardwareUpdateOrder];
                value = [self senDataToDevice:otaUpdateStr];
                NSLog(@"send hardware ota begin cmd 05 back = %d", value);
                if (value == 1) {
                    // ota
                    // 必须要先断开蓝牙在停5秒
                    [self nfcAlertMsg:@"正在OTA..." success:YES delay:0];
                    if (self.nBleConnectState == 1) {
                        [self.centralManager cancelPeripheralConnection:self.connectedPeripheral];
                    }
                    [NSThread sleepForTimeInterval:5];// 间隔1秒
                    
                    NSLog(@"OTA sueccss");
                    [self nfcAlertMsg:@"OTA成功" success:YES delay:1];
                } else if (value == 2) {
                    value = [self senDataToDevice:otaUpdateStr];
                    NSLog(@"send hardware ota begin cmd 05 again = %d", value);
                } else if (value == 3){
                    [self nfcAlertMsg:@"升级OTA失败" success:NO delay:1];
                }
                
                //逻辑自定义
//                if (_otaBlock) {
//                    _otaBlock
//                }
                
                return;
            }
        }
        NSLog(@"bursh failed");
        [self nfcAlertMsg:@"NFC暂不可用" success:NO delay:1];
    });
}

- (void)beginInitDevice {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [self nfcAlertMsg:@"正在初始化" success:YES delay:0];

        //只发送，然后直接回复包
        NSString *order = [BleProtocol getConfigKeyOrderForPhoneCaseWithWithType:0x00 param:nil];
        NSLog(@"---------- send 18 00 read cmd %@ -----------",order);
        
        [self senDataToDeviceWithNoNeedResponse:order];
        
        self.globaWaitAnswerRunning = 999;
        int nGetTimeout = 0;
        while (self.globaWaitAnswerRunning == 999) {
            [NSThread sleepForTimeInterval:0.02]; // 每次等待0.02秒
            nGetTimeout++;
            
            // 检查超时（250次×0.02秒=5秒超时）
            if (nGetTimeout > 250) {
                break;
            }
            
         
            
            // 检查NFC是否可用，不可用则提示并返回
            if (!self.isNfcCanUsed) {
                NSLog(@"NFC disconnected - brush failed 222");
                [self nfcAlertMsg:@"NFC暂不可用" success:NO delay:1];
                return;
            }
        }
        
        if (self.globaWaitAnswerRunning == 999) {
            [self nfcAlertMsg:@"发送初始化指令超时" success:NO delay:1];
        } else {
            [self nfcAlertMsg:@"初始化成功" success:YES delay:1];
            if (self->_initBlock) {
                self->_initBlock(self.failedNum);
            }
        }
    });
}

- (void)beginBindDevice {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [self nfcAlertMsg:@"正在绑定设备" success:YES delay:0];

        //写
        NSString *order = [BleProtocol getConfigKeyOrderForPhoneCaseWithWithType:0x01 param:self.imageDataXTArr];
        NSLog(@"---------- send 18 01 send public key cmd %@ -----------",order);
        
        [self senDataToDeviceWithNoNeedResponse:order];
        
        self.globaWaitAnswerRunning = 999;
        int nGetTimeout = 0;
        while (self.globaWaitAnswerRunning == 999) {
            [NSThread sleepForTimeInterval:0.02]; // 每次等待0.02秒
            nGetTimeout++;
            
            // 检查超时（250次×0.02秒=5秒超时）
            if (nGetTimeout > 250) {
                break;
            }
            
            // 检查NFC是否可用，不可用则提示并返回
            if (!self.isNfcCanUsed) {
                NSLog(@"NFC disconnected - brush failed 222");
                [self nfcAlertMsg:@"NFC暂不可用" success:NO delay:1];
                return;
            }
        }
        
        if (self.globaWaitAnswerRunning != 0xFF) {
            [self nfcAlertMsg:@"发送指令超时/失败" success:NO delay:1];
            if (self.bindDeviceBlock) {
                NSError *error = [NSError errorWithDomain:@"bindError"
                                                     code: self.failedNum
                                                 userInfo:@{NSLocalizedDescriptionKey: @"发送绑定读取指令超时/失败"}];
                self.bindDeviceBlock(self.screenCfg,nil,error);
            }
            return;
        }
        
        [self nfcAlertMsg:@"开始读取手机壳设备公钥" success:YES delay:0];

        //读
        NSString *readorder = [BleProtocol getConfigKeyOrderForPhoneCaseWithWithType:0x02 param:nil];
        NSLog(@"---------- send 18 02 send public key cmd %@ -----------",readorder);
        [self senDataToDeviceWithNoNeedResponse:readorder];
        
        self.globaWaitAnswerRunning = 999;
        
        int nreadGetTimeout = 0;
        while (self.globaWaitAnswerRunning == 999) {
            [NSThread sleepForTimeInterval:0.02]; // 每次等待0.02秒
            nreadGetTimeout++;
            
            // 检查超时（250次×0.02秒=5秒超时）
            if (nreadGetTimeout > 250) {
                break;
            }
            
            // 检查NFC是否可用，不可用则提示并返回
            if (!self.isNfcCanUsed) {
                NSLog(@"NFC disconnected - brush failed 222");
                [self nfcAlertMsg:@"NFC暂不可用" success:NO delay:1];
                return;
            }
        }
        
        if (self.globaWaitAnswerRunning != 0xFF) {
            [self nfcAlertMsg:@"发送读取指令超时/失败" success:NO delay:1];
            if (self.bindDeviceBlock) {
                NSError *error = [NSError errorWithDomain:@"bindError"
                                                     code: self.failedNum
                                                 userInfo:@{NSLocalizedDescriptionKey: @"发送绑定读取指令超时/失败"}];
                self.bindDeviceBlock(self.screenCfg,nil,error);
            }
            return;
        }
        
        if (self.failedNum != 0) {
            [self nfcAlertMsg:@"读取指令固件返回失败" success:NO delay:1];
            if (self.bindDeviceBlock) {
                NSError *error = [NSError errorWithDomain:@"bindError"
                                                     code: self.failedNum
                                                 userInfo:@{NSLocalizedDescriptionKey: @"读取指令固件返回失败"}];
                self.bindDeviceBlock(self.screenCfg,nil,error);
            }
            return;
        }
        
        if (self.backData.length != 80) {
            [self nfcAlertMsg:@"读取设备信息返回长度不够" success:NO delay:1];
            if (self.bindDeviceBlock) {
                NSError *error = [NSError errorWithDomain:@"bindError"
                                                     code: self.failedNum
                                                 userInfo:@{NSLocalizedDescriptionKey: @"读取设备信息返回长度不够"}];
                self.bindDeviceBlock(self.screenCfg,nil,error);
            }
            return;
        }
        
        NSLog(@"bind == %d %d %@",self.globaWaitAnswerRunning,self.failedNum,self.backData);
        
        if (self.bindDeviceBlock) {
            self.bindDeviceBlock(self.screenCfg, self.backData, nil);
        }
        [self nfcAlertMsg:@"绑定成功" success:YES delay:0.01];
    });
}

- (void)beginSendPublicKeyWithDevice:(XTDeviceConfig *)dev {
    if (!dev) {
        [self nfcAlertMsg:@"mac地址获取失败" success:YES delay:0.01];
        return;
    }
    
    [self nfcAlertMsg:@"获取mac地址成功" success:YES delay:0];
    // 记录设备MAC和扫描时间
    _strUploadDataMac = _screenCfg.appID;
    _lScanDeviceTime = (long)[[NSDate date] timeIntervalSince1970];

    // 停止当前蓝牙扫描，显示准备刷图提示
    [self.centralManager stopScan];
    [self nfcAlertMsg:@"正在绑定手机壳" success:YES delay:0];
    NSLog(@"[NFC-WriteScreen] 准备扫描设备：%@", _strUploadDataMac);
    
    // 检查蓝牙状态并重新扫描外设
    if (self.peripheralState == CBManagerStatePoweredOn) {
        _nScanTagTime = 0;
        [self.centralManager scanForPeripheralsWithServices:nil options:nil];
    } else {
        [self nfcAlertMsg:@"蓝牙状态异常" success:NO delay:1];
    }
}

- (void)beginInitDevice:(XTDeviceConfig *)dev {
    if (!dev) {
        [self nfcAlertMsg:@"mac地址获取失败" success:YES delay:0.01];
        return;
    }
    
    [self nfcAlertMsg:@"获取mac地址成功" success:YES delay:0];
    // 记录设备MAC和扫描时间
    _strUploadDataMac = _screenCfg.appID;
    _lScanDeviceTime = (long)[[NSDate date] timeIntervalSince1970];

    // 停止当前蓝牙扫描，显示准备刷图提示
    [self.centralManager stopScan];
    [self nfcAlertMsg:@"正在初始化手机壳" success:YES delay:0];
    NSLog(@"[NFC-WriteScreen] 准备扫描设备：%@", _strUploadDataMac);
    
    // 检查蓝牙状态并重新扫描外设
    if (self.peripheralState == CBManagerStatePoweredOn) {
        _nScanTagTime = 0;
        [self.centralManager scanForPeripheralsWithServices:nil options:nil];
    } else {
        [self nfcAlertMsg:@"蓝牙状态异常" success:NO delay:1];
    }
}

//启动NDEF读写功能
- (void)startNDEF:(OperationType)opType {
    if (!NFCTagReaderSession.readingAvailable) {
        [self alertMsg:@"该机型不支持NFC功能"];
        return;
    }
    //初始化
    if (!self.session) {
        [self alertMsg:@"NFC创建失败"];
        return;
    }
    
    _opType = opType;
    _isNfcCanUsed = YES;
    
    if (_opType == WriteScreen){
        [self nfcAlertMsg:@"正在准备写入数据" success:YES delay:0];
        // 获取所有的分包数据
        int deviceColorType = 3; //3四色，5六色
        if([_screenCfg.screenColorCode isEqualToString:COLOR_BWRY_CODE]){
            deviceColorType = 3;
        } else if([_screenCfg.screenColorCode isEqualToString:COLOR_BWRYGB_CODE]){
            deviceColorType = 5;
        }
        _globaPageSize = 490;
        _arraySendDataPage = [BleProtocol getPicSendPageData:_imageDataXTArr
                                                   picWidth:(int)_screenCfg.screenWidth
                                                  picHeight:(int)_screenCfg.screenHeight
                                            deviceColorType:deviceColorType
                                                   pageSize:_globaPageSize
                                             isNeedCompress:YES
                                               deviceNumber:_screenCfg.deviceNumber];
        
        // 获取需要清空的空间大小
        _nDeleteFlashLen = (int)[BleProtocol getDeleteFlashLength:_arraySendDataPage];
        NSLog(@"nDeleteFlashLen-finish:%d", _nDeleteFlashLen);
    } else if (_opType == OTA) {
        [self nfcAlertMsg:[NSString stringWithFormat:@"%@ 2%%",@"准备OTA"] success:YES delay:0];
        _globaPageSize = 490;
        _arraySendDataPage = [BleProtocol getOTASendPageData:_imageDataXTArr
                                                    pageSize:_globaPageSize];
        
        // 获取需要清空的空间大小
        _nDeleteFlashLen = (int)[BleProtocol getDeleteFlashLength:_arraySendDataPage];
        NSLog(@"nDeleteFlashLen-finish:%d", _nDeleteFlashLen);
    } else if (_opType == bindDevice || _opType == initDevice) {
        [self nfcAlertMsg:@"正在读取参数" success:YES delay:0];
    }
    _nBleConnectCount = 0;
    _isReading = YES;
    [_session beginSession];
}

- (void)hanleErrwithValue:(NSInteger)value cmd:(NSInteger)cmd {
    [self removeSession];
    if (value == 206) {
        [self alertMsg:@"需要签名认证"];
    } else if (value == 206) {
        [self alertMsg:@"安全认证失败"];
    } else if (value == 104) {
        [self alertMsg:@"指令执行失败"];
    }
}

//MARK: lazyload
- (NSMutableArray *)scanPeripherals {
    if (!_scanPeripherals) {
        _scanPeripherals = [NSMutableArray array];
    }
    return _scanPeripherals;
}

- (CBCentralManager *)centralManager {
    if (!_centralManager){
        _centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
    }
    return _centralManager;
}

- (NFCTagReaderSession *)session {
    if (!_session) {
        _session = [[NFCTagReaderSession alloc] initWithPollingOption:NFCPollingISO14443
                                                             delegate:self
                                                                queue:dispatch_get_global_queue(0, 0)];
    }
    return _session;
}

+ (BOOL)isBlankString:(NSString *)string {
    if (string == nil || string == NULL || [string isKindOfClass:[NSNull class]] || [string isEqualToString:@""] ||[string isEqualToString:@"null"] || [string isEqualToString:@"NULL"] || [string isEqualToString:@"(null)"] || [string isEqualToString:@"（null）"] || [[string stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]] length] == 0) {
        return YES;
    }
    return NO;
}

- (NSMutableArray *)signHexArr {
    if (!_signHexArr) {
        _signHexArr = [NSMutableArray array];
    }
    return _signHexArr;
}

@end

