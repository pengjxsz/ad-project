#import <UIKit/UIKit.h>
#import "PhoneShell.h"
#import "XTImageDitherUtils.h"
#import "BleProtocol.h"
#import "XTCommonUtils.h"
#import "XTDeviceConfig.h"
#import "NFCHelper.h"
#import "ECCKeyGenerator.h"
#import "DataSigner.h"


//设备适配系数（以iPhone6尺寸为基准）
#define DSColor(r,g,b) [UIColor colorWithRed:(r)/255.0 green:(g)/255.0 blue:(b)/255.0 alpha:1.0]
#define DSBlueColor [UIColor colorWithRed:20.0f/255.0f green:200.0f/255.0f blue:255.0f/255.0f alpha:1.0]
#define DSBackgroundColor [UIColor colorWithRed:246.0f/255.0f green:246.0f/255.0f blue:246.0f/255.0f alpha:1.0]

//图像生成算法
#define ALG_TYPE_ATKINSON           @"1"               //Atkinson抖动
#define ALG_TYPE_COLOR              @"2"               //色阶
#define ALG_TYPE_FLOYD              @"3"               //Floyd-Steinberg抖动

//@interface PhoneShell()<NFCHelperDelegate>
@interface PhoneShell()   //No necessary to implement NFCHelperDelegate

//屏幕配置
@property(nonatomic, strong) XTDeviceConfig *deviceConfig;

@property (nonatomic ,assign) BOOL isband;
//写图像的区域号
@property(nonatomic, assign) NSInteger imageSectionNumber;

//内容
@property(nonatomic, strong) UITextView *contentFieldPageSize;
@property(nonatomic, strong) NSString *contentPageSize;
@property(nonatomic, strong) UITextView *contentFieldIntervalTime;
@property(nonatomic, strong) NSString *contentIntervalTime;

#pragma mark - 2. 密钥相关（手动管理SecKeyRef内存）
@property (nonatomic, assign) SecKeyRef hPrivateKey;      // 私钥
@property (nonatomic, assign) SecKeyRef hPublicKey;       //

#pragma mark - 3. 业务数据
//@property (nonatomic, strong) XTDeviceConfig *deviceConfig;// 绑定后设备配置
@property (nonatomic, assign) NSInteger seqNum;           // 序列编号（预留）
@property (nonatomic, assign) BOOL isBound;               // 是否已绑定设备

#pragma mark - 4. 工具实例（单例复用）
@property (nonatomic, strong) NFCHelper *nfcHelper;       // NFC/蓝牙工具类

#pragma mark - 5. 返回数据
@property (nonatomic, strong) NSData *devicePublicKey; // 二进制公钥
//@property (nonatomic, copy) NSString *devicePublicKeyHex; // 十六进制字符串公钥（可选，方便日志/传输）
//@property (nonatomic, strong) NSData *chipID; // 芯片ID（可选，按需保存）

@end

@implementation PhoneShell



/**
  fisrt time, create the singleton instance, the protocolRecievier should be the view which is caller
   later, the protocolReceiver can be nil   : (id<NFCHelperDelegate>) protocolReceiver
 */
+ (instancetype)sharedInstance {
    static dispatch_once_t onceToken;
    static PhoneShell *phoneShell = nil;
    dispatch_once(&onceToken, ^{
        phoneShell = [[self alloc] init];
    });

    //NSLog(@"shareIntanceGet,  %@, %p",phoneShell, phoneShell.deviceConfig);

    return phoneShell;
}

// Prevents instantiation with alloc/init.
// cause self alloc to enter here
//+ (instancetype)allocWithZone:(struct _NSZone *)zone {
//    return [self sharedInstance];
//}


- (id)copyWithZone:(NSZone *)zone {
    return self;
}

- (id)mutableCopyWithZone:(NSZone *)zone {
    return self;
}

- (instancetype)init  {
    self = [super init];
    if (self) {
        self.phoneScreenHeight = [UIScreen mainScreen].bounds.size.height;
        self.phoneScreenWidth = [UIScreen mainScreen].bounds.size.width;
        self.DSAdaptCoefficient = ([[UIScreen mainScreen] bounds].size.width)/375.0;
        self.previewImage = nil;
        self.orignImage = nil;
        self.outputData = nil;

        self.contentPageSize = @"244";
        self.contentIntervalTime = @"10";
        self.nfcHelper = [NFCHelper shareInstance];

        self.hPublicKey = nil;
        self.hPrivateKey = nil;
        self.masterPK = nil;
        self.masterKey = nil;
        self.chatPK = nil;
        self.chatKey = nil;
        self.userId = @"ddddd";
        //NSLog(@"self .userid %@", self.userId);
        //get app key pair first
        [self getGeneatedKeyPair];
        
        //check if ever bound, if true, set deviceConfig
        [self isEverBound];
//        if (![self isEverBound]){
//            if (!(protocolReceiver==nil))
//            self.protocolReceiver = protocolReceiver;
//            [self BindNFCDevice];
//        }
        
    }
    return self;
}

-(void)saveRegisterInfo{
    NSLog(@"--save userId: %@ \n", self.userId);
    NSLog(@"--save,masterkey,pub: %@, %@, chatkey, pub %@, %@\n", self.masterKey, self.masterPK, self.chatKey, self.chatPK);
//    --save,masterkey,pub:
//    51f05f60b268225e1ca977bc330a9c1419ab923c0c188fb51e1ab5786790798b, 0x049eaa8cadd686c23ec1bb0b4a309c465204acefd2f837c481dae8d3da61ad64e960b14bd5f66b9115d3beb580928e8374cafbf84c6ac3348e42c66a85067f3053,
//    chatkey, pub
//    3b789c7d222f16709419ae2dd4e28ee5c640d68c789ca94779f1f7ce328e0900,
//    0x048ad166f88a2b6548e266697baf97a602b7eeb19d3c74d9fc8a0e45c6ee6dcb88fabb8dc27b2e035ef3eef194b944b44476e92e6d35010e4042bdd968635a55d7
    
    //self.deviceScreenWidth = self.deviceConfig.screenWidth;
    //self.deviceScreenHeight = self.deviceConfig.screenHeight;

    //self.userId = [ECCKeyGenerator loadUserIdFromKeychain];
    [ECCKeyGenerator saveUserIdToKeychain:self.userId];
    
    //self.devicePKHex = [ECCKeyGenerator loadDeviceInfoFromKeychain:true];
    //self.chipIDHex = [ECCKeyGenerator loadDeviceInfoFromKeychain:false];
    
    //tempary: restore userId
    //if ([self.chipIDHex isEqualToString: @"42503152343538050064C23656036F78"])
    //    self.userId = @"efb51d4d17c82643081725b090006be8f76ee0adef93f888a9411a5bcd508ec9";
    
    //self.masterKey = [ECCKeyGenerator loadMasterKeyFromKeychain: false];
    //self.masterPK = [ECCKeyGenerator loadMasterKeyFromKeychain: true];
    [ECCKeyGenerator saveMasterKeyToKeychain:self.masterKey PBULICKEY:self.masterPK];

    //self.chatPK = [ECCKeyGenerator loadChatKeyFromKeychain:true];
    //self.chatKey = [ECCKeyGenerator loadChatKeyFromKeychain:false];
    [ECCKeyGenerator saveChatKeyToKeychain:self.chatKey PBULICKEY:self.chatPK];

}


-(void)loadDeviceInfo{
    self.deviceScreenWidth = self.deviceConfig.screenWidth;
    self.deviceScreenHeight = self.deviceConfig.screenHeight;
    self.deviceScreenColors = self.deviceConfig.colorCount;
    
    self.userId = [ECCKeyGenerator loadUserIdFromKeychain];
    
    self.devicePKHex = [ECCKeyGenerator loadDeviceInfoFromKeychain:true];
    self.chipIDHex = [ECCKeyGenerator loadDeviceInfoFromKeychain:false];
    
    //tempary: restore userId
    //if ([self.chipIDHex isEqualToString: @"42503152343538050064C23656036F78"])
    //    self.userId = @"efb51d4d17c82643081725b090006be8f76ee0adef93f888a9411a5bcd508ec9";
    
    self.masterKey = [ECCKeyGenerator loadMasterKeyFromKeychain: false];
    self.masterPK = [ECCKeyGenerator loadMasterKeyFromKeychain: true];

    self.chatPK = [ECCKeyGenerator loadChatKeyFromKeychain:true];
    self.chatKey = [ECCKeyGenerator loadChatKeyFromKeychain:false];
    NSLog(@"--load: devicePK,chidId, %@, %@\n", self.devicePKHex, self.chipIDHex);
    NSLog(@"--loadL userid %@\n", self.userId);
    NSLog(@"--load: masterkey,pub: %@, %@, chatkey, pub %@, %@\n", self.masterKey, self.masterPK, self.chatKey, self.chatPK);

}

- (Boolean)isEverBound {
    //NSLog(@"test if  ever 绑定设备");

    if (self.deviceConfig){
        //NSLog(@"设备CONFIG EVER,  %@",self.deviceConfig);
        //NSLog(@"设备CONFIG EVER,  Loaded Already");
        //self.deviceScreenWidth = self.deviceConfig.screenWidth;
        //self.deviceScreenHeight = self.deviceConfig.screenHeight;

        return true;
    }
//    self.userId = [ECCKeyGenerator loadUserIdFromKeychain];
//    
//    self.devicePKHex = [ECCKeyGenerator loadDeviceInfoFromKeychain:true];
//    self.chipIDHex = [ECCKeyGenerator loadDeviceInfoFromKeychain:false];
//    
//    self.masterKey = [ECCKeyGenerator loadMasterKeyFromKeychain: false];
//    self.masterPK = [ECCKeyGenerator loadMasterKeyFromKeychain: true];
//
//    self.chatPK = [ECCKeyGenerator loadChatKeyFromKeychain:true];
//    self.chatKey = [ECCKeyGenerator loadChatKeyFromKeychain:false];
//    NSLog(@"--%@, %@, %@, %@", self.devicePKHex, self.chipIDHex, self.masterPK, self.chatPK);
//    
    //是否已经生成配置文件
    XTDeviceConfig *info = [XTDeviceConfig readLocalCfg];
    if (info) {
        self.deviceConfig = info;
//        _isband = YES;
        //[self refreshDeviceInfoUI:info];
        NSLog(@"ever 绑定设备 device config file existed");
       
        [self loadDeviceInfo];
        return true;
    }
    NSLog(@"not ever 绑定设备");

    return false;
}

/// 释放当前密钥（避免内存泄漏）
- (void)releaseCurrentKeys {
    if (self.hPublicKey) {
        CFRelease(self.hPublicKey);
        self.hPublicKey = NULL;
    }
    if (self.hPrivateKey) {
        CFRelease(self.hPrivateKey);
        self.hPrivateKey = NULL;
    }
}


// SecKeyRef公钥转NSData（ECC-P256：65字节，0x04头+32x+32y）
- (NSData *)getPublicKeyData:(SecKeyRef)publicKey {
    if (!publicKey) return nil;
    // __bridge_transfer：将CFDataRef转为ARC管理的NSData，无需手动CFRelease
    CFDataRef pubDataRef = SecKeyCopyExternalRepresentation(publicKey, NULL);
    return (__bridge_transfer NSData *)pubDataRef;
}

/// 核心转换：SecKeyRef → 64元素16进制字符串数组（x32字节 + y32字节）
- (NSArray<NSString *> *)convertPublicKeyToHexArray:(SecKeyRef)publicKey {
    if (!publicKey) {
        NSLog(@"❌ 公钥为空，无法转换16进制数组");
        return nil;
    }
    
    // 1. 先转NSData（ECC-P256未压缩公钥：65字节 = 0x04头 + 32x + 32y）
    NSData *pubData = [self getPublicKeyData:publicKey];
    if (!pubData || pubData.length != 65) {
        NSLog( @"%@", [NSString stringWithFormat:@"❌ 公钥格式异常（预期65字节，实际%lu字节）", (unsigned long)pubData.length] );
        return nil;
    }
    
    // 2. 校验头部标识（ECC未压缩公钥必须以0x04开头）
    uint8_t header;
    [pubData getBytes:&header range:NSMakeRange(0, 1)];
    if (header != 0x04) {
        NSLog(@"---%@", [NSString stringWithFormat:@"❌ 公钥非未压缩格式（预期0x04头，实际0x%02X）", header]);
        return nil;
    }
    
    // 3. 提取64字节有效数据（去除0x04头，保留x+y坐标）
    NSData *validPubData = [pubData subdataWithRange:NSMakeRange(1, 64)];
    
    NSLog(@" --- %@",[XTCommonUtils convertDataToHexString:validPubData]);
    NSLog(@" --- %@",[NSString stringWithFormat:@"%@",[XTCommonUtils convertDataToHexString:validPubData]]);
//
//    SecKeyRef testKey = [ECCKeyGenerator createSecKeyFromHexECCPublicKey:[XTCommonUtils convertDataToHexString:validPubData]];
//    if (testKey) {
//        NSLog(@"✅ 自己的 X||Y 成功导入！");
//        CFRelease(testKey);
//    } else {
//        NSLog(@"❌ 自己的 X||Y 也无法导入？！");
//    }
//    
    // 4. 转换为16进制字符串数组（每个字节→2位大写16进制，补0对齐）
    NSMutableArray<NSString *> *hexArray = [NSMutableArray arrayWithCapacity:64];
    for (NSInteger i = 0; i < validPubData.length; i++) {
        uint8_t byte;
        [validPubData getBytes:&byte range:NSMakeRange(i, 1)];
        [hexArray addObject:[NSString stringWithFormat:@"%02X", byte]];
    }
    return hexArray.copy;
}


/// 打印已存储公钥的基础信息（前16字节Base64，避免日志过长）
- (void)logStoredPublicKeyInfo {
    NSData *pubData = [self getPublicKeyData:self.hPublicKey];
    if (!pubData) {
        NSLog(@"❌ 无法获取已存储公钥数据");
        return;
    }
    
    //getPublicKeyData has log the public key already
}


-(Boolean) getGeneatedKeyPair{
    if ([ECCKeyGenerator hasStoredKeyPair]) {
        NSLog(@"✅ 检测到已存储密钥对，开始加载...");
        [self releaseCurrentKeys]; // 先释放旧密钥，避免内存泄漏
        
        // 获取存储的密钥
        SecKeyRef storedPubKey = [ECCKeyGenerator getStoredPublicKey];
        SecKeyRef storedPriKey = [ECCKeyGenerator getStoredPrivateKey];
        NSLog(@"%p, %p", storedPriKey, storedPubKey);
        if (storedPubKey && storedPriKey) {
            //if async, the next call will not get a valid self.hPublicKey
            //dispatch_async(dispatch_get_main_queue(), ^{
                self.hPublicKey = storedPubKey;
                self.hPrivateKey = storedPriKey;
                
                // 打印密钥基础信息
                [self logStoredPublicKeyInfo];
                // 可选：打印公钥16进制数组详情
                //[self logPublicKeyHexArrayDetail:self.hPublicKey];
                //                [self logPublicKeyHexArrayDetail:self.hPrivateKey];
                
                //[self updateStatus]; // 刷新状态标签
            //});
        } else {
            NSLog(@"❌ 错误：检测到存储状态，但无法获取密钥");
            return false;
        }
        return true;
    }
    return false;
}

/**
 * first, check app key pair;
 * if not existed, generate the key paire, assig it to selft.hPulbicKey and hPrivateKey,  then bind the device
 * if existed, then the device must be bound. just get the key pair and assig it to selft.hPulbicKey and hPrivateKey
 */
- (Boolean)BindNFCDevice {
    NSLog(@"🔍 检查ECC-P256密钥对状态...");
    
    // 1. 优先使用已存储的密钥对, no matter if the app key pair is existed, it's bound
    //    if existed, update the map of public keys between app pub key and devicePK
    //    if not existed, do the map of the public keys between app pub key and devicePK
    Boolean keyExisted = (self.hPublicKey != nil);
    if (keyExisted){
        [self internalBindNFCDevice];
        return true;
    }

    // 2. 无存储密钥，生成新密钥对
    NSLog(@"🔄 无已存储密钥，开始生成新ECC-P256密钥对...");
    [self releaseCurrentKeys];
    
    __weak typeof(self) weakSelf = self;
    [ECCKeyGenerator generateECCP256KeyPairWithCompletion:^(SecKeyRef publicKey, SecKeyRef privateKey, NSError *error) {
        if (error) {
            // 生成失败：主线程日志+状态更新
            NSLog(@"---%@",[NSString stringWithFormat:@"❌ 密钥生成失败：%@", error.localizedDescription]);
//            dispatch_async(dispatch_get_main_queue(), ^{
//                [weakSelf updateStatus];
//            });
            return ;
        }
        
        // 生成成功：子线程打印原始密钥信息（不更新UI）
        CFDataRef pubDataRef = SecKeyCopyExternalRepresentation(publicKey, NULL);
        if (pubDataRef) {
            NSData *pubData = CFBridgingRelease(pubDataRef);
            NSLog(@"[密钥生成] 原始公钥长度：%lu字节（ECC-P256未压缩格式）", (unsigned long)pubData.length);
            NSLog(@"[密钥生成] 公钥Base64：%@", [pubData base64EncodedDataWithOptions:NSDataBase64Encoding64CharacterLineLength]);
        }
        
        NSLog(@"✅ ECC-P256密钥对生成成功！");
        weakSelf.hPublicKey = publicKey;  //has been save in keyChain
        weakSelf.hPrivateKey = privateKey;
        [weakSelf internalBindNFCDevice];
        return ;
        // 主线程更新密钥+日志+状态
        //dispatch_async(dispatch_get_main_queue(), ^{
            //weakSelf.hPublicKey = publicKey;
            //weakSelf.hPrivateKey = privateKey;
            
            //NSLog(@"✅ ECC-P256密钥对生成成功！");
            //[weakSelf logGeneratedPublicKeyInfo];
            // 可选：打印公钥16进制数组详情
            //[weakSelf logPublicKeyHexArrayDetail:weakSelf.hPublicKey];
        
            //[weakSelf updateStatus];
        //});
    }];
    return true;
}


/// 绑定设备（核心：传公钥16进制数组，调用NFCHelper新方法）
///  bindDeviceAction
- (void) internalBindNFCDevice{
    // 前置校验1：密钥是否生成
    if (!self.hPublicKey || !self.hPrivateKey) {
        NSLog(@"❌ 请0先生成密钥对（点击\"生成密钥对\"按钮）");
        return;
    }
//    Boolean keyInitialized = [self getGeneratedKeyPair];
//    if (!keyInitialized){
//        NSLog(@"❌ 没有初始化的设备。");
//        return;
//    }
    // 1. 公钥转16进制数组
    NSArray<NSString *> *pubHexArray = [self convertPublicKeyToHexArray:self.hPublicKey];
    if (!pubHexArray || pubHexArray.count != 64) {
        NSLog(@"❌ 公钥转16进制数组失败，无法发起绑定");
        return;
    }
    
    NSLog(@"开始绑定");
    
    // 3. 调用NFCHelper绑定方法（传16进制数组）
    __weak typeof(self) weakSelf = self;
    [self.nfcHelper getbindDeviceWithPublicKey:pubHexArray.mutableCopy
                                    completion:^(XTDeviceConfig * _Nullable config, NSData * _Nullable sdata, NSError * _Nullable error) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if (error) {
                NSString * errMsg = [NSString stringWithFormat:@"❌ 绑定失败：%@", error.localizedDescription];
                weakSelf.bindError = errMsg;
                NSLog(@"%@",errMsg);
            } else if (config && sdata) {
                // 绑定成功：更新业务数据+状态
                //weakSelf.deviceConfig = config; //let deviceConfig nil in order to update the phoneshell property when isEverBound called again
                weakSelf.isBound = YES;
                
                NSLog(@"✅ 设备绑定成功！已获取设备配置");
                NSLog(@"%@",[NSString stringWithFormat:@"📌 设备编号：%ld | 屏幕尺寸：%lux%lu",
                               (long)config.deviceNumber,
                               (unsigned long)config.screenWidth,
                               (unsigned long)config.screenHeight]);
                
                // ---------------- 解析 sdata 并提取公钥 ----------------
                // 1. 校验 sdata 长度是否符合预期（64B公钥 + 16B芯片ID = 80B）
                if (sdata.length != 80) {
                    NSLog(@"%@",[NSString stringWithFormat:@"⚠️ sdata 长度异常！预期80B，实际%luB", (unsigned long)sdata.length]);
                } else {
                    // 2. 提取 64B 公钥（从0开始，长度64）
                    NSData *publicKeyData = [sdata subdataWithRange:NSMakeRange(0, 64)];
                    // 3. 提取 16B 芯片ID（从64开始，长度16）
                    NSData *chipIDData = [sdata subdataWithRange:NSMakeRange(64, 16)];
                    
                    // 4. 保存公钥（持有公钥）
                    weakSelf.devicePublicKey = publicKeyData;
                    // 可选：转成十六进制字符串（方便日志打印或后续字符串形式使用）
                    //      in kotlin uncompressed public key has prefix '04'. add this prefix
                    weakSelf.devicePKHex =  [NSString stringWithFormat:@"04%@", [XTCommonUtils convertDataToHexString:publicKeyData]];
                    
                    // 可选：保存芯片ID
                    weakSelf.chipIDHex = [XTCommonUtils convertDataToHexString:chipIDData];
                    
                    [ECCKeyGenerator saveDeviceInfoToKeychain:weakSelf.devicePKHex CHIPID: weakSelf.chipIDHex];
                    //保存本地设备配置信息
//                    weakSelf._isband = YES;
                    weakSelf.deviceConfig = config;
                    //weakSelf.deviceScreenWidth = config.screenWidth;
                    //weakSelf.deviceScreenHeight = config.screenHeight;	

                    NSLog(@"绑定设备成功-- config %@",self.deviceConfig);
                    [weakSelf loadDeviceInfo];


                    [XTDeviceConfig saveLocalCfg:config];
                    // 5. 打印解析后的日志（验证结果）
                    NSLog(@"%@",[NSString stringWithFormat:@"✅ 解析 sdata 成功："]);
                    NSLog(@"%@",[NSString stringWithFormat:@"   公钥（64B）：%@", weakSelf.devicePKHex]);
                    NSLog(@"%@",[NSString stringWithFormat:@"   芯片ID（16B）：%@", weakSelf.chipIDHex]);
                }
               
            } else {
               NSLog(@"❌ 绑定失败：未获取到设备配置");
                weakSelf.bindError = @"❌ 绑定失败：未获取到设备配置";
            }
            //[weakSelf updateStatus]; // 刷新状态标签
        });
    }];
}


////识别和扫描设备，保存配置文件，保存配置在内存中
////- (void)BindNFCDevice : (id<NFCHelperDelegate>) protocolReceiver{
//- (void)BindNFCDevice {
//
////    if (self.deviceConfig)
////        return;
////
////    //是否已经生成配置文件
////    DeviceConfig *info = [DeviceConfig readLocalCfg];
////    if (info) {
////        self.deviceConfig = info;
////        _isband = YES;
////        //[self refreshDeviceInfoUI:info];
////        return;
////    }
//
//    //通过NFC命令获取
//    NFCHelper *nfcAdapter = [NFCHelper shareInstance];
//    if(nfcAdapter.isReading){
//        return;
//    }
//    NSLog(@"绑定设备command send");
//    nfcAdapter.delegate = self;
//    
////    if (!(protocolReceiver==nil))
////        nfcAdapter.delegate = protocolReceiver;
//    [nfcAdapter getDeviceConfig:NSLocalizedString(@"FM_Close_To_Equipment", nil)];
//}
//
//
//
//- (void)NfcReadConfigComplete:(DeviceConfig *)config {
//    if(!config){
//        return;
//    }
//    _isband = YES;
//    self.deviceConfig = config;
//    
//    NSLog(@"绑定设备成功-- config %@",self.deviceConfig);
//    self.deviceScreenWidth = self.deviceConfig.screenWidth;
//    self.deviceScreenHeight = self.deviceConfig.screenHeight;
//
//    //保存本地设备配置信息
//    [DeviceConfig saveLocalCfg:config];
//    //界面刷新设备配置信息
//    //[self refreshDeviceInfoUI: config];
//}



////图片渲染需要一个bgView
//// view.bgView.scrollView.imageView
////      scrollView: shadowLayer, imageViewLayer
//- (void)previewView : (UIView*) parentView
//{
//    if(!_bgView){
//        if (@available(iOS 11.0, *)){
//            _bgView = [[UIView alloc] initWithFrame:CGRectMake(parentView.safeAreaInsets.left, parentView.safeAreaInsets.top, parentView.bounds.size.width - parentView.safeAreaInsets.left - parentView.safeAreaInsets.right,
//                    parentView.bounds.size.height -parentView.safeAreaInsets.bottom-parentView.safeAreaInsets.top)];
//            [parentView addSubview:_bgView];
//        }
//        else{
//            _bgView = [[UIView alloc] initWithFrame:CGRectMake(0, 88*self.DSAdaptCoefficient, parentView.bounds.size.width,
//            parentView.bounds.size.height - 122*self.DSAdaptCoefficient)];
//        }
//        parentView.backgroundColor = DSBackgroundColor;
//        _bgView.backgroundColor = DSBackgroundColor;
//    }
//}



//(NSData*)convertToNFCData:(UIImage*)inputImage
//               previewImage:(UIImage **)previewImage DeviceConfig:(DeviceConfig *)DeviceConfig
//                    algType:(NSString *)algType
//                     BRIGHT:(int)bright
//                DITHERCOUNT:(int )ditherCount
//                    BaseRGB:(int)baseRGB;


//
//- (void) onChangeSlidreBright :(UISlider*)event {
//    NSLog(@"%f", event.value);
//    nBright = event.value;
//    _titleLabel_bright_value.text = [CommonUtils itoa:nBright];;
//    UIImage *previewImage = nil;
//    _outputData = [ImageConvertUtils convertToNFCData:_orignImage
//                                         previewImage:&previewImage
//                                         DeviceConfig:_currentCfg
//                                              algType:radioCurrent.val
//                                               BRIGHT:nBright
//                                          DITHERCOUNT:nDitherPointCount
//                                              BaseRGB:nBaseRGB];
//    if(!_outputData){
//        [CommonUtils showError:@"图像转换失败" controller:self onClick:nil];
//        return;
//    }
//    _imageView.image = previewImage;
//}

/**
 * 根据投屏算法algType,参数(bright, dithercount, basergb)
 * 计算投屏输出,返回,保存到outputData;同时计算预览图片,保存到previewImage
 *   algType: 1' or '2' or "3"  ALG_TYPE_ATKINSON OR ALG_TYPE_COLOR or ALG_TYPE_FLOYD
 *     now, should '3', i.e ALG_TYPE_FLOYD
 *     BRIGHT, DITHERCOUNT, BaseRGB now are deprecated and not used, just keep here to be compatiable to the former version
*/
- (int)computeNFCData:   (NSString *)algType
                     BRIGHT:(int)nBright
                DITHERCOUNT:(int )nDitherPointCount
                    BaseRGB:(int)nBaseRGB
{
    NSLog(@"computeNFCData before ble call 0");

//    UIImage *previewImageTmp = nil;
//    self.outputData = [ImageConvertUtils convertToNFCData:self.orignImage
//                                         previewImage:&(previewImageTmp)
//                                         DeviceConfig:self.deviceConfig
//                                              algType:algType
//                                               BRIGHT:nBright
//                                          DITHERCOUNT:nDitherPointCount
//                                              BaseRGB:nBaseRGB];
    
    UIImage *ditheredImage = nil;
    [XTImageDitherUtils convertToUploadData:self.orignImage
                               previewImage:&ditheredImage
                                  deviceCfg:self.deviceConfig
                                    //algType:@"3" //should be '3'. to be compatabile to the former version, use param algType
                                    algType:algType
    ];
    if (!ditheredImage) {
        NSLog(@"❌ 刷图失败：图片抖点处理失败（请检查设备配置）");
        return 1;
    }
    
    //self.imageView.image = previewImage;
    NSLog(@"computeNFCData return %p", ditheredImage);

    self.previewImage = ditheredImage; //ditheredImage is also the image used for preview
    return 0;
}




//写屏
//return 0: Ok
//return 1: NFC IS READING
//return 2: NFC ERROR
//replaced by startDrawProcess
- (int)doWriteScreen:(NSString *)pin{
    /*
    NFCHelper *instance = [NFCHelper shareInstance];
    if(instance.isReading){
        // 这里需要添加提示 ，
        //[CommonUtils showError:NSLocalizedString(@"FM_Processing", nil) controller:self onClick:nil];
        return 1;
    }
    instance.delegate = self;
    
    //int pageSize = [CommonUtils atoi:_contentFieldPageSize.text];
    int pageSize =  [CommonUtils atoi:_contentPageSize];

    if (pageSize <= 10)
        pageSize = 10;
    
    //int intervalTime = [CommonUtils atoi:_contentFieldIntervalTime.text];
    int intervalTime = [CommonUtils atoi:_contentIntervalTime];

    NSMutableArray *picData = [ImageConvertUtils convertToNFCData:self.previewImage DeviceConfig:self.deviceConfig];
            
    NSString *response = [instance importImageToDevice:NSLocalizedString(@"FM_Close_To_Equipment", nil) imageData:self.outputData imageDataXT:picData screenCfg:_deviceConfig imageSectionNumber:_imageSectionNumber pin:pin PageSize:pageSize IntervalTime:intervalTime];
    if(response.length>0){
        return 2;
        //[CommonUtils showError:response controller:self onClick:nil];
    }
     */
    return 0;
}


- (int)project2Screen{
//     tag Property:

//     The tag property is an integer value that you can use to identify a view, including a button.
//     It's a convenient way to associate an integer identifier with a UI element.

// Default Value:

//     If you do not explicitly assign a value to the tag property of a UIButton, its default value is 0.

    //_imageSectionNumber = btn.tag;
    
    // //是否需要设置pin
    // if(_deviceConfig.checkPin&&_deviceConfig.pin.length<=0){
    //     FMSinglePwdVC *childVC = [[FMSinglePwdVC alloc] init];
    //     [childVC setModalPresentationStyle:UIModalPresentationOverCurrentContext];
    //     childVC.delegate = self;
    //     [self presentViewController:childVC animated:NO completion:nil];
    //     return;
    // }
    if (self.previewImage ==nil)
        return 3;
    
    //写屏
    [self startDrawProcess];
    return 0;
}

- (int)project2ScreenDefault: (UIImage *)clippedImage
 {
    
    NSLog(@"Image received: %@", clippedImage);
    CGFloat outputWidth = self.deviceConfig.screenWidth;
    CGFloat outputHeight = self.deviceConfig.screenHeight;
    UIImage *inputImage = [self scaledImageFormImage:clippedImage toSize:CGSizeMake(outputWidth, outputHeight)];
    NSLog(@"project2Screen after scaledImageFormImage");
     
    self.orignImage = inputImage;
    [self computeNFCData:  ALG_TYPE_FLOYD BRIGHT:100 DITHERCOUNT:12 BaseRGB: 0];
    int ret = [self project2Screen];
    return ret;
}

/**
    algType should be "3",  the caller arguments  in kotlin has been changed
      BRIGHT, DITHERCOUNT, BaseRGB not used, just keep here
 */
- (UIImage*)projectPreview: (UIImage *)clippedImage
              AlgType:(NSString *)algType
                     BRIGHT:(int)nBright
                DITHERCOUNT:(int )nDitherPointCount
                    BaseRGB:(int)nBaseRGB
 {
    
    NSLog(@"Image received0: %@", clippedImage);
    CGFloat outputWidth = self.deviceConfig.screenWidth;
    CGFloat outputHeight = self.deviceConfig.screenHeight;
    UIImage *inputImage = [self scaledImageFormImage:clippedImage toSize:CGSizeMake(outputWidth, outputHeight)];
    NSLog(@"projectPreview after scaledImageFormImage");
    NSLog(@"Image scaled: %@", inputImage);

    self.orignImage = inputImage;
     [self computeNFCData:  algType BRIGHT:nBright DITHERCOUNT:nDitherPointCount BaseRGB: nBaseRGB];
     //NSLog(@"projectPreview return: %@", self.previewImage);
     NSLog(@"projectPreview return: %u",  self.previewImage==NULL);

     return self.previewImage;
}

- (UIImage *)scaledImageFormImage:(UIImage *)image toSize:(CGSize)size {
    UIGraphicsBeginImageContext(size);
    [image drawInRect:CGRectMake(0,0, size.width, size.height)];
    UIImage *getImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return getImage;
}


/// 开始刷图流程（依赖绑定后的设备配置）
- (void)startDrawProcess {
    // 前置校验1：是否已绑定
    if (!self.deviceConfig) {
        NSLog(@"❌ 请先绑定设备（点击\"绑定设备\"按钮）");
        return;
    }
    // 前置校验2：设备是否在忙
    if (self.nfcHelper.isReading) {
        NSLog(@"❌ 设备正在处理数据，请稍后再试");
        return;
    }
    
    /*
    // 前置校验3：测试图片是否存在
    //UIImage *originImage = [UIImage imageNamed:@"test"];
    UIImage *originImage = self.orignImage;
    if (!originImage) {
        NSLog(@"❌ 刷图失败：未找到图片");
        return;
    }
    
    NSLog(@"🎨 开始刷图流程（图片缩放→抖点→发送）...");
    
    // 1. 缩放图片到设备屏幕尺寸
    CGSize screenSize = CGSizeMake(self.deviceConfig.screenWidth, self.deviceConfig.screenHeight);
    UIImage *scaledImage = [self scaledImageFormImage:originImage toSize:screenSize];
    if (!scaledImage) {
        NSLog(@"❌ 刷图失败：图片缩放失败");
        return;
    }
    
    UIImage *ditheredImage = nil;
    [XTImageDitherUtils convertToUploadData:scaledImage
                               previewImage:&ditheredImage
                                  deviceCfg:self.deviceConfig
                                    algType:@"3"];
    if (!ditheredImage) {
        NSLog(@"❌ 刷图失败：图片抖点处理失败（请检查设备配置）");
        return;
    }
    */
    
    UIImage *ditheredImage  = self.previewImage; //ditheredImage has been created
    
    // 3. 生成刷图数据（XTImageDitherUtils输出Hex数组）
    NSMutableArray *drawData = [XTImageDitherUtils getUploadDataFromDitherImage:ditheredImage
                                                                     deviceCfg:self.deviceConfig];
    if (drawData.count == 0) {
        NSLog(@"❌ 刷图失败：未生成有效刷图数据");
        return;
    }
    // 获取所有的分包数据
    int deviceColorType = 3; //3四色，5六色
    if([_deviceConfig.screenColorCode isEqualToString:COLOR_BWRY_CODE]){
        deviceColorType = 3;
    } else if([_deviceConfig.screenColorCode isEqualToString:COLOR_BWRYGB_CODE]){
        deviceColorType = 5;
    }
    
    NSMutableArray *arraySendDataPage = [BleProtocol getPicData:drawData
                                                       picWidth:(int)_deviceConfig.screenWidth
                                                      picHeight:(int)_deviceConfig.screenHeight
                                                deviceColorType:deviceColorType
                                                       pageSize:490
                                                 isNeedCompress:YES
                                                   deviceNumber:_deviceConfig.deviceNumber];
    // 原始数组（例如存储多个整数）
    NSArray *dataArray = arraySendDataPage;

    NSMutableData *serializedData = [NSMutableData data];
    for (NSNumber *num in dataArray) {
        int value = [num intValue];
        [serializedData appendBytes:&value length:1];
    }
//    SLog(@" --- %@",[XTCommonUtils convertDataToHexString:serializedData]);

    NSData *sendHashData = [DataSigner calculateSHA256HashWithOriginalData:serializedData];
    NSData *sendTimeData = [DataSigner generateTimestampData];
    NSError *err;
    NSData *sendSignData1 = [ECCKeyGenerator signDataToRawFormat:sendHashData withPrivateKey:self.hPrivateKey error:&err];

    if (err) {
        NSLog(@"✅ 生成100B数据失败");
        return;
    }
//
//    NSData *sendSignData = [ECCKeyGenerator signData:sendHashData withPrivateKey:self.hPrivateKey error:&err];
//    NSError *err1;
//    BOOL isyet = [ECCKeyGenerator verifySignature:sendSignData forData:sendHashData withPublicKey:self.hPublicKey error:&err1];
//
//
//    NSData *sendSignData1 = [ECCKeyGenerator signDataToRawFormat:sendHashData withPrivateKey:self.hPrivateKey error:&err];
//    BOOL isyet2 = [ECCKeyGenerator verifyRawSignature:sendSignData1 forData:sendHashData withPublicKey:self.hPublicKey error:&err1];
//
    NSLog(@" --- %@",[XTCommonUtils convertDataToHexString:sendHashData]);
    NSLog(@" --- %@",[XTCommonUtils convertDataToHexString:sendTimeData]);
    NSLog(@" --- %@",[XTCommonUtils convertDataToHexString:sendSignData1]);
    
    NSLog(@" --- %@", [NSString stringWithFormat:@"sendHashData %@",[XTCommonUtils convertDataToHexString:sendHashData]]);
    NSLog(@" --- %@",[NSString stringWithFormat:@"sendTimeData %@",[XTCommonUtils convertDataToHexString:sendTimeData]]);
    NSLog(@" --- %@", [NSString stringWithFormat:@"sendSignData %@",[XTCommonUtils convertDataToHexString:sendSignData1]]);

    NSMutableData *sendData100B = [NSMutableData new];
    [sendData100B appendData:sendHashData];
    [sendData100B appendData:sendTimeData];
    [sendData100B appendData:sendSignData1];
    
    NSLog(@"%@", [NSString stringWithFormat:@"100B hash sign %@",[XTCommonUtils convertDataToHexString:sendData100B]]);

    NSArray<NSString *> *hexArray = [DataSigner convert100BDataToHexArray:sendData100B];
    
    __weak typeof(self) weakSelf = self;

    // 4. 发送数据到设备（调用NFCHelper刷图方法）
    [self.nfcHelper importImageToDeviceWithImageDataHexArr:drawData
                                                signHexArr:hexArray.mutableCopy
                                                 screenCfg:self.deviceConfig
                                                  pageSize:490
                                                completion:^(NSData * _Nullable signData, NSError * _Nullable error) {
        dispatch_async(dispatch_get_main_queue(), ^{
            __strong typeof(weakSelf) strongSelf = weakSelf; // 临时强引用，避免self中途释放
            if (!strongSelf) return;
            
            if (error) {
                NSLog(@" --- %@", [NSString stringWithFormat:@"❌ 刷图失败：%@", error.localizedDescription]);
            } else {
                if (!signData) {
                    NSLog(@"❌ 刷图失败：未返回签名数据");
                } else {
                    if (signData.length != 112) {
                        NSLog(@" --- %@",[NSString stringWithFormat:@"❌ 签名p数据长度异常！预期112B，实际%luB", (unsigned long)signData.length]);
                        return;
                    }
                    
                    NSData *originalData = [signData subdataWithRange:NSMakeRange(0, 48)]; // 原始数据（48B）
                    NSData *signatureData = [signData subdataWithRange:NSMakeRange(48, 64)]; // 签名数据（64B）
                    
                    NSData *hashData = [signData subdataWithRange:NSMakeRange(0, 32)]; // 原始数据（48B）
                    NSData *countData = [signData subdataWithRange:NSMakeRange(32, 16)]; // 原始数据（48B）
                    
                    NSLog(@"✅ %@, %@",self.devicePublicKey, self.devicePKHex);

                    //SLog(@"✅ %@", [XTCommonUtils convertDataToHexString:self.devicePublicKey]);
                    //SecKeyRef publicKeyRef = [ECCKeyGenerator createSecKeyFromHexECCPublicKey:[XTCommonUtils convertDataToHexString:self.devicePublicKey]];
                    
                    SecKeyRef publicKeyRef = [ECCKeyGenerator createSecKeyFromHexECCPublicKey:    [self.devicePKHex substringFromIndex:2]];
                    
//                    SecKeyRef publicKeyRef = [ECCKeyGenerator createSecKeyFromHexECCPublicKey:[XTCommonUtils convertDataToHexString:self.devicePublicKey]];
                                
//                    signatureData 1A27E441FB80CC2418C16B15ECFE7E6E879C4FA600C43417A0C77000BFA2D7753B41A45ACE0497E9E8257F01E5F2B5BE2C437005D586848DAD12BE4D350652A2
//                    originalData 2D1C4FBB28ED7C68A829EA5828C906A45A0BE6AC0CBCAED27C362FBB590F00A601000000000000000000000000000000
//                    hashData 2D1C4FBB28ED7C68A829EA5828C906A45A0BE6AC0CBCAED27C362FBB590F00A6
//                    countData 01000000000000000000000000000000
//
//                    signatureData 3EB820F7A328126FDE8FD32006293078556837301537DDAAEE65CFAC4A77892397E469B280C475C1A74451E49EBCFAB4DE25284A4F1930B9CB74BE7B54FBDEBD
//                    originalData CFCEE8461B50DEE5E3090F1014756BA0D9D7ACE6D7D83B4DC7412706998030EC02000000000000000000000000000000
//                    hashData CFCEE8461B50DEE5E3090F1014756BA0D9D7ACE6D7D83B4DC7412706998030EC
//                    countData 02000000000000000000000000000000
                    
                    if (publicKeyRef) {
                        NSLog(@"✅ 手机壳设备二进制公钥转SecKeyRef成功");
                        // 示例：用转换后的SecKeyRef验证签名
                        NSError *verifyError = nil;
                        BOOL verifySuccess = [ECCKeyGenerator verifySignature:[self convertRawECSignatureToDER:signatureData]
                                                                      forData:originalData
                                                                withPublicKey:publicKeyRef
                                                                       error:&verifyError];
                        
                        //SLog(@"signatureData %@", [XTCommonUtils convertDataToHexString:signatureData]);
                        //SLog(@"originalData %@", [XTCommonUtils convertDataToHexString:originalData]);
                        //SLog(@"hashData %@", [XTCommonUtils convertDataToHexString:hashData]);
                        //SLog(@"countData %@", [XTCommonUtils convertDataToHexString:countData]);
                        
                        //[weakSelf log:[NSString stringWithFormat:@"signatureData %@", [XTCommonUtils convertDataToHexString:signatureData]]];
                        //[weakSelf log:[NSString stringWithFormat:@"originalData %@", [XTCommonUtils convertDataToHexString:originalData]]];

                        NSLog(@" %@",[NSString stringWithFormat:@"hashData %@", [XTCommonUtils convertDataToHexString:hashData]]);
                        NSLog(@" %@",[NSString stringWithFormat:@"countData %@", [XTCommonUtils convertDataToHexString:countData]]);

                        if (verifySuccess) {
                            NSLog(@"✅ 签名验证成功！刷图成功");
                        } else {
                            NSString *errorMsg = verifyError ? verifyError.localizedDescription : @"未知错误";
                            NSLog(@" %@", [NSString stringWithFormat:@"❌ 签名验证失败：%@（刷图失败）", errorMsg]);
                        }
                        CFRelease(publicKeyRef); // 必须释放，避免内存泄漏
                    } else {
                        NSLog(@"❌ 公钥转换失败");
                    }
                }
            }
        });
    }];
}

- (NSData *)convertRawECSignatureToDER:(NSData *)rawSignature {
    if (rawSignature.length != 64) {
        NSLog(@"❌ Raw 签名长度错误，必须是 64 字节（32字节 r + 32字节 s）");
        return nil;
    }

    NSData *rData = [rawSignature subdataWithRange:NSMakeRange(0, 32)];
    NSData *sData = [rawSignature subdataWithRange:NSMakeRange(32, 32)];

    NSData *rDER = [self processECSignatureComponent:rData];
    NSData *sDER = [self processECSignatureComponent:sData];

    if (!rDER || !sDER) {
        NSLog(@"❌ 处理 r 或 s 组件失败");
        return nil;
    }

    NSMutableData *derData = [NSMutableData data];
    [derData appendBytes:&(uint8_t){0x30} length:1]; // SEQUENCE

    NSUInteger totalLength = rDER.length + sDER.length;
    if (totalLength > 0x7F) {
        NSLog(@"❌ 总长度超过127字节，不支持");
        return nil;
    }
    [derData appendBytes:&(uint8_t){(uint8_t)totalLength} length:1];

    [derData appendData:rDER];
    [derData appendData:sDER];

    return derData;
}

- (NSData *)processECSignatureComponent:(NSData *)component {
    if (component.length == 0) return nil;

    // 去前导零
    NSUInteger startIdx = 0;
    const uint8_t *bytes = (const uint8_t *)component.bytes;  // ✅ 强制转换
    while (startIdx < component.length && bytes[startIdx] == 0x00) {
        startIdx++;
    }
    if (startIdx == component.length) {
        startIdx = component.length - 1;
    }
    NSData *trimmed = [component subdataWithRange:NSMakeRange(startIdx, component.length - startIdx)];

    // ✅ 检查最高 bit 是否为 1（即字节值 >= 0x80）
    const uint8_t *trimmedBytes = (const uint8_t *)trimmed.bytes;
    if (trimmed.length > 0 && (trimmedBytes[0] & 0x80)) {
        NSMutableData *fixed = [NSMutableData data];
        [fixed appendBytes:&(uint8_t){0x00} length:1];  // 补 0x00 防止被解析为负数
        [fixed appendData:trimmed];
        trimmed = fixed;
    }

    // 构造 INTEGER: 0x02 + 长度 + 值
    NSMutableData *integerData = [NSMutableData data];
    [integerData appendBytes:&(uint8_t){0x02} length:1];  // INTEGER 标签
    uint8_t lenByte = (uint8_t)trimmed.length;
    [integerData appendBytes:&lenByte length:1];
    [integerData appendData:trimmed];

    return integerData;
}

@end



//获取当前iOS手机屏幕宽高
// CGFloat w = [UIScreen mainScreen].bounds.width;
// CGFloat h = [UIScreen mainScreen].bounds.height;

    //屏幕大小传递给剪裁窗口,用来计算高度
    // imageClipController.deviceConfig = weakSelf.currentInfo;
    // CGFloat maxGridWidth = imageNewWidth;
    // CGFloat maxGridHeight = maxGridWidth*_deviceConfig.screenHeight/_deviceConfig.screenWidth;

    //剪裁完成, 把图片转换成屏幕大小,传递device给writecontroller
    //  CGFloat outputWidth = _currentInfo.screenWidth;
    // CGFloat outputHeight = _currentInfo.screenHeight;
   
    // //将剪裁的图像转换为适合屏幕尺寸的分辨率
    // UIImage *inputImage = [self scaledImageFormImage:clipImage toSize:CGSizeMake(outputWidth, outputHeight)];
    
    // WriteScreenController *childController = [[WriteScreenController alloc] init];
    // childController.orignImage = inputImage;
    // childController.deviceConfig = _currentInfo;
    // [self.navigationController pushViewController:childController animated:YES];


    //写屏控制
    //创建初始图片
    //按照算法 和 参数生成图片
    //写图片到NFC




