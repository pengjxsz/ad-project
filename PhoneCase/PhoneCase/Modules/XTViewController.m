//
//  XTViewController.m
//  PhoneCase
//
//  Created by XTMacMini on 2025/10/27.
//

#import "XTViewController.h"
#import "Masonry.h"
#import "ECCKeyGenerator.h"
#import "NFCHelper.h"
#import "XTDeviceConfig.h"
#import "XTMBManager.h"
#import "XTImageDitherUtils.h"
#import "BleProtocol.h"
#import "DataSigner.h"
#import "XTCommonUtils.h"
#import "PhoneShell.h"

#ifdef DEBUG
#define SLog(format, ...) printf("class: <%p %s:(%d) > method: %s \n%s\n", self, [[[NSString stringWithUTF8String:__FILE__] lastPathComponent] UTF8String], __LINE__, __PRETTY_FUNCTION__, [[NSString stringWithFormat:(format), ##__VA_ARGS__] UTF8String] )
#else
#define SLog(format, ...)
#endif

@interface XTViewController ()<UIDocumentPickerDelegate>

#pragma mark - 1. UI元素（按功能分组）
@property (nonatomic, strong) UILabel *statusLabel;       // 顶部状态提示
@property (nonatomic, strong) UITextView *logTextView;    // 调试日志视图
@property (nonatomic, strong) UIView *buttonContainer;     // 按钮容器（避免subviews索引风险）
@property (nonatomic, strong) UIButton *generateKeyButton;// 生成ECC密钥对
@property (nonatomic, strong) UIButton *intPhoneCaseButton;// 初始化手机壳
@property (nonatomic, strong) UIButton *bindDeviceButton; // 绑定设备（传16进制公钥）
@property (nonatomic, strong) UIButton *startDrawButton;  // 开始刷图
@property (nonatomic, strong) UIButton *otAButton;        // ota
@property (nonatomic, strong) UIButton *cleanUpButton;        //cleanup key pair
@property (nonatomic, strong) UIButton *cleanUserButton;        //cleanup userid
@property (nonatomic, strong) UIButton *cleanMasterButton;        //cleanup userid


#pragma mark - 2. 密钥相关（手动管理SecKeyRef内存）
@property (nonatomic, assign) SecKeyRef hPrivateKey;      // 私钥
@property (nonatomic, assign) SecKeyRef hPublicKey;       // 公钥（ECC-P256）

#pragma mark - 3. 业务数据
@property (nonatomic, strong) XTDeviceConfig *deviceConfig;// 绑定后设备配置
@property (nonatomic, assign) NSInteger seqNum;           // 序列编号（预留）
@property (nonatomic, assign) BOOL isBound;               // 是否已绑定设备

#pragma mark - 4. 工具实例（单例复用）
@property (nonatomic, strong) NFCHelper *nfcHelper;       // NFC/蓝牙工具类

#pragma mark - 5. 返回数据
@property (nonatomic, strong) NSData *devicePublicKey; // 二进制公钥
@property (nonatomic, copy) NSString *devicePublicKeyHex; // 十六进制字符串公钥（可选，方便日志/传输）
@property (nonatomic, strong) NSData *chipID; // 芯片ID（可选，按需保存）

@end

@implementation XTViewController


#pragma mark - 生命周期（内存管理优先）
- (void)dealloc {
    // 释放SecKeyRef：必须手动CFRelease，避免内存泄漏
    if (_hPrivateKey) {
        CFRelease(_hPrivateKey);
        _hPrivateKey = NULL;
    }
    if (_hPublicKey) {
        CFRelease(_hPublicKey);
        _hPublicKey = NULL;
    }
    _nfcHelper = nil;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // 基础页面配置
    self.title = @"H端控制中心";
    self.view.backgroundColor = [UIColor whiteColor];
    self.edgesForExtendedLayout = UIRectEdgeNone;
    
    // 初始化业务数据与工具实例
    self.seqNum = 0;
    self.isBound = NO;
    self.nfcHelper = [NFCHelper shareInstance];
    
    // 初始化流程：UI→布局→状态→业务（默认生成密钥）
    [self setupUI];
    [self setupLayout];
    [self updateStatus];
    
   // [self generateKeyPair];
    PhoneShell *phoneShell = [[PhoneShell sharedInstance] init];
    if (![phoneShell isEverBound]){
        [phoneShell BindNFCDevice];
        //[phoneShell isEverBound];
    }
   // NSLog(@"userId %@", phoneShell.userId);
    //phoneShell.userId = @"abdefg";
    //NSLog(@"userId %@", phoneShell.userId);


}

#pragma mark - UI创建（统一风格+规范命名）
- (void)setupUI {
    // 1. 状态标签（显示密钥/绑定/设备状态）
    self.statusLabel = [[UILabel alloc] init];
    self.statusLabel.font = [UIFont systemFontOfSize:14];
    self.statusLabel.textColor = [UIColor darkGrayColor];
    self.statusLabel.numberOfLines = 0; // 支持多行换行
    [self.view addSubview:self.statusLabel];
    
    // 2. 日志视图（调试信息输出）
    self.logTextView = [[UITextView alloc] init];
    self.logTextView.layer.borderWidth = 1.0;
    self.logTextView.layer.borderColor = [UIColor lightGrayColor].CGColor;
    self.logTextView.editable = NO;
    self.logTextView.font = [UIFont systemFontOfSize:12];
    self.logTextView.text = @"[日志开始] 页面加载完成\n";
    [self.view addSubview:self.logTextView];
    
    // 3. 按钮容器（统一管理按钮，避免索引风险）
    self.buttonContainer = [[UIView alloc] init];
    [self.view addSubview:self.buttonContainer];
    
    // 4. 功能按钮（统一创建方法，保证风格一致）
    self.generateKeyButton = [self createFunctionButtonWithTitle:@"生成密钥对"];
    [self.generateKeyButton addTarget:self action:@selector(generateKeyPair) forControlEvents:UIControlEventTouchUpInside];
    [self.buttonContainer addSubview:self.generateKeyButton];
    
    self.intPhoneCaseButton = [self createFunctionButtonWithTitle:@"初始化手机壳"];
    [self.intPhoneCaseButton addTarget:self action:@selector(initPhoneCaseAction) forControlEvents:UIControlEventTouchUpInside];
    [self.buttonContainer addSubview:self.intPhoneCaseButton];
    
    self.bindDeviceButton = [self createFunctionButtonWithTitle:@"绑定设备"];
    [self.bindDeviceButton addTarget:self action:@selector(bindDeviceAction) forControlEvents:UIControlEventTouchUpInside];
    [self.buttonContainer addSubview:self.bindDeviceButton];
    
    self.startDrawButton = [self createFunctionButtonWithTitle:@"开始刷图"];
    [self.startDrawButton addTarget:self action:@selector(startDrawProcess) forControlEvents:UIControlEventTouchUpInside];
    [self.buttonContainer addSubview:self.startDrawButton];
    
    self.otAButton = [self createFunctionButtonWithTitle:@"OTA"];
    //self.otAButton.hidden = YES;
    [self.otAButton addTarget:self action:@selector(otAButtonProcess:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:self.otAButton];
    
    self.cleanUpButton = [self createFunctionButtonWithTitle:@"Clean"];
    NSLog(@"cleanUpButton frame: %@", NSStringFromCGRect(self.cleanUpButton.frame));
    [self.cleanUpButton addTarget:self action:@selector(cleanupKeyPair) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:self.cleanUpButton];
    
    self.cleanUserButton = [self createFunctionButtonWithTitle:@"Clean UserId"];
    NSLog(@"cleanUserButton frame: %@", NSStringFromCGRect(self.cleanUserButton.frame));
    [self.cleanUserButton addTarget:self action:@selector(cleanupUserId) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:self.cleanUserButton];
    
    self.cleanMasterButton = [self createFunctionButtonWithTitle:@"Clean Master"];
    NSLog(@"cleanMasterButton frame: %@", NSStringFromCGRect(self.cleanMasterButton.frame));
    [self.cleanMasterButton addTarget:self action:@selector(cleanupMasterKeyPair) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:self.cleanMasterButton];
    
    
}

/// 统一创建功能按钮（避免重复代码）
- (UIButton *)createFunctionButtonWithTitle:(NSString *)title {
    UIButton *button = [UIButton buttonWithType:UIButtonTypeSystem];
    [button setTitle:title forState:UIControlStateNormal];
    button.backgroundColor = [UIColor systemBlueColor];
    button.layer.cornerRadius = 4;
    [button setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    button.titleLabel.font = [UIFont systemFontOfSize:14 weight:UIFontWeightMedium];
    button.titleLabel.adjustsFontSizeToFitWidth = YES; // 文字自适应宽度
    button.contentEdgeInsets = UIEdgeInsetsMake(8, 12, 8, 12); // 内边距（避免文字拥挤）
    return button;
}

#pragma mark - 布局（Masonry约束，无硬编码）
- (void)setupLayout {
    CGFloat margin = 15;    // 页面边距
    CGFloat spacing = 10;   // 按钮间距
    CGFloat logHeight = 400;// 日志视图固定高度
    
    // 1. 状态标签（顶部，自适应高度）
    [self.statusLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.view.mas_top).offset(20);
        make.left.right.equalTo(self.view).inset(margin);
        make.height.greaterThanOrEqualTo(@20);
    }];
    
    // 2. 日志视图（状态标签下方，固定高度）
    [self.logTextView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.statusLabel.mas_bottom).offset(20);
        make.left.right.equalTo(self.view).inset(margin);
        make.height.equalTo(@(logHeight));
    }];
    
    // 3. 按钮容器（日志视图下方，底部留空）
    [self.buttonContainer mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.logTextView.mas_bottom).offset(20);
        make.left.right.equalTo(self.view).inset(margin);
        make.height.equalTo(@40); // 按钮固定高度
        make.bottom.lessThanOrEqualTo(self.view.mas_bottom).offset(-30);
    }];
    
    // 4. 按钮均分布局（4个按钮，间距10）
    [self.generateKeyButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.buttonContainer);
        make.centerY.equalTo(self.buttonContainer);
        // 宽度计算：容器宽度/4 - 总间距*(4-1)/4（均分+间距补偿）
        make.width.equalTo(self.buttonContainer.mas_width).dividedBy(4.0).offset(-spacing * 3 / 4.0);
    }];
    
    [self.intPhoneCaseButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.generateKeyButton.mas_right).offset(spacing);
        make.centerY.equalTo(self.buttonContainer);
        make.width.equalTo(self.generateKeyButton);
    }];
    
    [self.bindDeviceButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.intPhoneCaseButton.mas_right).offset(spacing);
        make.centerY.equalTo(self.buttonContainer);
        make.width.equalTo(self.generateKeyButton);
    }];
    
    [self.startDrawButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.bindDeviceButton.mas_right).offset(spacing);
        make.right.equalTo(self.buttonContainer);
        make.centerY.equalTo(self.buttonContainer);
        make.width.equalTo(self.generateKeyButton);
    }];
    
    [self.otAButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.buttonContainer.mas_bottom).offset(20);
        make.left.equalTo(self.buttonContainer);
        make.height.equalTo(self.generateKeyButton); // 按钮固定高度
        make.width.equalTo(self.generateKeyButton);
    }];
    [self.cleanUpButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.buttonContainer.mas_bottom).offset(20);
        make.left.equalTo(self.otAButton.mas_right).offset(spacing);
        make.height.equalTo(self.generateKeyButton); // 按钮固定高度
        make.width.equalTo(self.generateKeyButton);
    }];
    [self.cleanUserButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.buttonContainer.mas_bottom).offset(20);
        make.left.equalTo(self.cleanUpButton.mas_right).offset(spacing);
        make.height.equalTo(self.generateKeyButton); // 按钮固定高度
        make.width.equalTo(self.generateKeyButton);
    }];
    [self.cleanMasterButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.buttonContainer.mas_bottom).offset(20);
        make.left.equalTo(self.cleanUserButton.mas_right).offset(spacing);
        make.height.equalTo(self.generateKeyButton); // 按钮固定高度
        make.width.equalTo(self.generateKeyButton);
    }];
}

#pragma mark - 状态更新与日志（统一入口，主线程安全）
/// 更新顶部状态标签（密钥/绑定/设备连接状态）
- (void)updateStatus {
    // 设备连接状态
    NSString *nfcState = self.nfcHelper.connectedPeripheral ? @"已连接" : @"未连接";
    // 密钥生成状态
    NSString *keyState = (self.hPublicKey && self.hPrivateKey) ? @"已生成密钥" : @"未生成密钥";
    // 设备绑定状态
    NSString *bindState = self.isBound ? @"已绑定" : @"未绑定";
    
    self.statusLabel.text = [NSString stringWithFormat:@"状态: %@ | 绑定: %@ | 设备: %@",
                             keyState, bindState, nfcState];
}

/// 打印日志（主线程更新，避免UI卡顿，自动滚动到最新）
- (void)log:(NSString *)message {
    dispatch_async(dispatch_get_main_queue(), ^{
        NSString *timestamp = [self getCurrentTimestamp];
        // 新日志追加到末尾（符合阅读习惯）
        self.logTextView.text = [self.logTextView.text stringByAppendingFormat:@"[%@] %@\n", timestamp, message];
        // 滚动到最后一行
        [self.logTextView scrollRangeToVisible:NSMakeRange(self.logTextView.text.length, 0)];
    });
}

/// 获取当前时间戳（格式：HH:mm:ss.SSS，避免时区问题）
- (NSString *)getCurrentTimestamp {
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    formatter.dateFormat = @"HH:mm:ss.SSS";
    formatter.locale = [NSLocale localeWithLocaleIdentifier:@"en_US_POSIX"];
    return [formatter stringFromDate:[NSDate date]];
}

#pragma mark - 密钥相关（生成+转换：SecKeyRef → 16进制数组）
/// 生成/加载ECC-P256密钥对
- (void)generateKeyPair {
    [self log:@"🔍 检查ECC-P256密钥对状态..."];
    
    // 1. 优先使用已存储的密钥对
    if ([ECCKeyGenerator hasStoredKeyPair]) {
        [self log:@"✅ 检测到已存储密钥对，开始加载..."];
        [self releaseCurrentKeys]; // 先释放旧密钥，避免内存泄漏
        
        // 获取存储的密钥
        SecKeyRef storedPubKey = [ECCKeyGenerator getStoredPublicKey];
        SecKeyRef storedPriKey = [ECCKeyGenerator getStoredPrivateKey];
        
        if (storedPubKey && storedPriKey) {
            dispatch_async(dispatch_get_main_queue(), ^{
                self.hPublicKey = storedPubKey;
                self.hPrivateKey = storedPriKey;
                
                // 打印密钥基础信息
                [self logStoredPublicKeyInfo];
                // 可选：打印公钥16进制数组详情
                [self logPublicKeyHexArrayDetail:self.hPublicKey];
//                [self logPublicKeyHexArrayDetail:self.hPrivateKey];

                [self updateStatus]; // 刷新状态标签
            });
        } else {
            [self log:@"❌ 错误：检测到存储状态，但无法获取密钥"];
        }
        return;
    }
    
    // 2. 无存储密钥，生成新密钥对
    [self log:@"🔄 无已存储密钥，开始生成新ECC-P256密钥对..."];
    [self releaseCurrentKeys];
    
    __weak typeof(self) weakSelf = self;
    [ECCKeyGenerator generateECCP256KeyPairWithCompletion:^(SecKeyRef publicKey, SecKeyRef privateKey, NSError *error) {
        if (error) {
            // 生成失败：主线程日志+状态更新
            [weakSelf log:[NSString stringWithFormat:@"❌ 密钥生成失败：%@", error.localizedDescription]];
            dispatch_async(dispatch_get_main_queue(), ^{
                [weakSelf updateStatus];
            });
            return;
        }
        
        // 生成成功：子线程打印原始密钥信息（不更新UI）
        CFDataRef pubDataRef = SecKeyCopyExternalRepresentation(publicKey, NULL);
        if (pubDataRef) {
            NSData *pubData = CFBridgingRelease(pubDataRef);
            NSLog(@"[密钥生成] 原始公钥长度：%lu字节（ECC-P256未压缩格式）", (unsigned long)pubData.length);
            NSLog(@"[密钥生成] 公钥Base64：%@", [pubData base64EncodedDataWithOptions:NSDataBase64Encoding64CharacterLineLength]);
        }
        
        // 主线程更新密钥+日志+状态
        dispatch_async(dispatch_get_main_queue(), ^{
            weakSelf.hPublicKey = publicKey;
            weakSelf.hPrivateKey = privateKey;
            
            [weakSelf log:@"✅ ECC-P256密钥对生成成功！"];
            [weakSelf logGeneratedPublicKeyInfo];
            // 可选：打印公钥16进制数组详情
            [weakSelf logPublicKeyHexArrayDetail:weakSelf.hPublicKey];
            
            [weakSelf updateStatus];
        });
    }];
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

-(void)cleanupKeyPair {
       [self log:@"🔍 CLEANUP ECC-P256密钥对..."];
       
      
           [self releaseCurrentKeys]; // 先释放旧密钥，避免内存泄漏
           
           // CLEANUP 存储的密钥
           [ECCKeyGenerator clearStoredKeyPair];
}

-(void)cleanupUserId {
       [self log:@"🔍 CLEANUP ECC-P256密钥对..."];
       
      
           [self releaseCurrentKeys]; // 先释放旧密钥，避免内存泄漏
           
           // CLEANUP 存储的密钥
           [ECCKeyGenerator saveUserIdToKeychain:@""];
}

-(void)cleanupMasterKeyPair {
       [self log:@"🔍 CLEANUP ECC-P256密钥对..."];
       
      
           [self releaseCurrentKeys]; // 先释放旧密钥，避免内存泄漏
           
           // CLEANUP 存储的密钥
           [ECCKeyGenerator saveMasterKeyToKeychain:@"" PBULICKEY:@""];
}

/// SecKeyRef公钥转NSData（ECC-P256：65字节，0x04头+32x+32y）
- (NSData *)getPublicKeyData:(SecKeyRef)publicKey {
    if (!publicKey) return nil;
    // __bridge_transfer：将CFDataRef转为ARC管理的NSData，无需手动CFRelease
    CFDataRef pubDataRef = SecKeyCopyExternalRepresentation(publicKey, NULL);
    return (__bridge_transfer NSData *)pubDataRef;
}

/// 打印已存储公钥的基础信息（前16字节Base64，避免日志过长）
- (void)logStoredPublicKeyInfo {
    NSData *pubData = [self getPublicKeyData:self.hPublicKey];
    if (!pubData) {
        [self log:@"❌ 无法获取已存储公钥数据"];
        return;
    }
    
    if (pubData.length >= 16) {
        NSString *pvb = [NSString stringWithFormat:@"📌 已存储公钥（前16字节Base64）：%@...",
         [pubData base64EncodedStringWithOptions:NSDataBase64Encoding64CharacterLineLength]];
        SLog(@"pvb = %@",pvb);
        [self log:pvb];
    } else {
        [self log:[NSString stringWithFormat:@"📌 已存储公钥（Base64）：%@",
                  [pubData base64EncodedStringWithOptions:NSDataBase64Encoding64CharacterLineLength]]];
    }
}

/// 打印新生成公钥的基础信息
- (void)logGeneratedPublicKeyInfo {
    NSData *pubData = [self getPublicKeyData:self.hPublicKey];
    if (!pubData) {
        [self log:@"❌ 无法获取新生成公钥数据"];
        return;
    }
    
    if (pubData.length >= 16) {
        NSString *pvb = [NSString stringWithFormat:@"📌 新公钥（前16字节Base64）：%@...",
         [pubData base64EncodedStringWithOptions:0]];
        SLog(@"pvb = %@",pvb);
        [self log:pvb];
    } else {
        [self log:[NSString stringWithFormat:@"📌 新公钥（Base64）：%@",
                  [pubData base64EncodedStringWithOptions:0]]];
    }
}

/// 核心转换：SecKeyRef → 64元素16进制字符串数组（x32字节 + y32字节）
- (NSArray<NSString *> *)convertPublicKeyToHexArray:(SecKeyRef)publicKey {
    if (!publicKey) {
        [self log:@"❌ 公钥为空，无法转换16进制数组"];
        return nil;
    }
    
    // 1. 先转NSData（ECC-P256未压缩公钥：65字节 = 0x04头 + 32x + 32y）
    NSData *pubData = [self getPublicKeyData:publicKey];
    if (!pubData || pubData.length != 65) {
        [self log:[NSString stringWithFormat:@"❌ 公钥格式异常（预期65字节，实际%lu字节）", (unsigned long)pubData.length]];
        return nil;
    }
    
    // 2. 校验头部标识（ECC未压缩公钥必须以0x04开头）
    uint8_t header;
    [pubData getBytes:&header range:NSMakeRange(0, 1)];
    if (header != 0x04) {
        [self log:[NSString stringWithFormat:@"❌ 公钥非未压缩格式（预期0x04头，实际0x%02X）", header]];
        return nil;
    }
    
    // 3. 提取64字节有效数据（去除0x04头，保留x+y坐标）
    NSData *validPubData = [pubData subdataWithRange:NSMakeRange(1, 64)];
    
    NSLog(@" --- %@",[XTCommonUtils convertDataToHexString:validPubData]);
    [self log:[NSString stringWithFormat:@"%@",[XTCommonUtils convertDataToHexString:validPubData]]];
    
    SecKeyRef testKey = [ECCKeyGenerator createSecKeyFromHexECCPublicKey:[XTCommonUtils convertDataToHexString:validPubData]];
    if (testKey) {
        NSLog(@"✅ 自己的 X||Y 成功导入！");
        CFRelease(testKey);
    } else {
        NSLog(@"❌ 自己的 X||Y 也无法导入？！");
    }
    
    // 4. 转换为16进制字符串数组（每个字节→2位大写16进制，补0对齐）
    NSMutableArray<NSString *> *hexArray = [NSMutableArray arrayWithCapacity:64];
    for (NSInteger i = 0; i < validPubData.length; i++) {
        uint8_t byte;
        [validPubData getBytes:&byte range:NSMakeRange(i, 1)];
        [hexArray addObject:[NSString stringWithFormat:@"%02X", byte]];
    }
    return hexArray.copy;
}


/// 打印公钥16进制数组详情（x/y坐标分段，调试用）
- (void)logPublicKeyHexArrayDetail:(SecKeyRef)publicKey {
    NSArray<NSString *> *hexArray = [self convertPublicKeyToHexArray:publicKey];
    if (!hexArray) return;
    
    // 拼接日志（x前32元素，y后32元素，每8个换行）
    NSMutableString *logStr = [NSMutableString stringWithString:@"📊 公钥16进制数组（x/y坐标）："];
    
    // x坐标（前32元素）
    [logStr appendString:@"\nx坐标（32字节）："];
    for (int i = 0; i < 32; i++) {
        [logStr appendFormat:@"%@ ", hexArray[i]];
        if ((i + 1) % 8 == 0) [logStr appendString:@"\n       "]; // 排版对齐
    }
    
    // y坐标（后32元素）
    [logStr appendString:@"\ny坐标（32字节）："];
    for (int i = 32; i < 64; i++) {
        [logStr appendFormat:@"%@ ", hexArray[i]];
        if ((i + 1) % 8 == 0) [logStr appendString:@"\n       "];
    }
    
    [self log:logStr];
}

#pragma mark - 设备操作（初始化+绑定+刷图，完整流程）
/// 初始化手机壳（调用NFCHelper，处理错误码回调）
- (void)initPhoneCaseAction {
    // 前置校验：密钥是否生成
    if (!self.hPublicKey || !self.hPrivateKey) {
        [self log:@"❌ 请先生成密钥对（点击\"生成密钥对\"按钮）"];
        return;
    }
//    // 前置校验：设备是否连接
//    if (!self.nfcHelper.connectedPeripheral) {
//        [self log:@"❌ 设备未连接，请先连接NFC设备"];
//        return;
//    }
    
    [self log:@"🔧 开始初始化手机壳..."];
    __weak typeof(self) weakSelf = self;
    
    [self.nfcHelper initializePhoneCaseDeviceWithCompletion:^(NSInteger errorCode) {
        dispatch_async(dispatch_get_main_queue(), ^{
            NSString *resultMsg = [weakSelf messageForErrorCode:(PhoneCaseErrorCode)errorCode];
            [weakSelf log:resultMsg];
            
            // 初始化成功：提示后续操作
            if (errorCode == PhoneCaseSuccess) {
                [weakSelf log:@"✅ 初始化完成，可点击\"绑定设备\"进行公钥交换"];
            }
        });
    }];
}

/// 绑定设备（核心：传公钥16进制数组，调用NFCHelper新方法）
- (void)bindDeviceAction {
    // 前置校验1：密钥是否生成
    if (!self.hPublicKey || !self.hPrivateKey) {
        [self log:@"❌ 请先生成密钥对（点击\"生成密钥对\"按钮）"];
        return;
    }
    
    // 1. 公钥转16进制数组
    NSArray<NSString *> *pubHexArray = [self convertPublicKeyToHexArray:self.hPublicKey];
    if (!pubHexArray || pubHexArray.count != 64) {
        [self log:@"❌ 公钥转16进制数组失败，无法发起绑定"];
        return;
    }
    
    [self log:@"开始绑定"];
    
    // 3. 调用NFCHelper绑定方法（传16进制数组）
    __weak typeof(self) weakSelf = self;
    [self.nfcHelper getbindDeviceWithPublicKey:pubHexArray.mutableCopy
                                    completion:^(XTDeviceConfig * _Nullable config, NSData * _Nullable sdata, NSError * _Nullable error) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if (error) {
                [weakSelf log:[NSString stringWithFormat:@"❌ 绑定失败：%@", error.localizedDescription]];
            } else if (config && sdata) {
                // 绑定成功：更新业务数据+状态
                weakSelf.deviceConfig = config;
                weakSelf.isBound = YES;
                
                [weakSelf log:@"✅ 设备绑定成功！已获取设备配置"];
                [weakSelf log:[NSString stringWithFormat:@"📌 设备编号：%ld | 屏幕尺寸：%lux%lu",
                               (long)config.deviceNumber,
                               (unsigned long)config.screenWidth,
                               (unsigned long)config.screenHeight]];
                
                // ---------------- 解析 sdata 并提取公钥 ----------------
                // 1. 校验 sdata 长度是否符合预期（64B公钥 + 16B芯片ID = 80B）
                if (sdata.length != 80) {
                    [weakSelf log:[NSString stringWithFormat:@"⚠️ sdata 长度异常！预期80B，实际%luB", (unsigned long)sdata.length]];
                } else {
                    // 2. 提取 64B 公钥（从0开始，长度64）
                    NSData *publicKeyData = [sdata subdataWithRange:NSMakeRange(0, 64)];
                    // 3. 提取 16B 芯片ID（从64开始，长度16）
                    NSData *chipIDData = [sdata subdataWithRange:NSMakeRange(64, 16)];
                    
                    // 4. 保存公钥（持有公钥）
                    weakSelf.devicePublicKey = publicKeyData;
                    // 可选：转成十六进制字符串（方便日志打印或后续字符串形式使用）
                    weakSelf.devicePublicKeyHex = [XTCommonUtils convertDataToHexString:publicKeyData];
                    // 可选：保存芯片ID
                    weakSelf.chipID = chipIDData;
                    
                    // 5. 打印解析后的日志（验证结果）
                    [weakSelf log:[NSString stringWithFormat:@"✅ 解析 sdata 成功："]];
                    [weakSelf log:[NSString stringWithFormat:@"   公钥（64B）：%@", weakSelf.devicePublicKeyHex]];
                    [weakSelf log:[NSString stringWithFormat:@"   芯片ID（16B）：%@", [XTCommonUtils convertDataToHexString:chipIDData]]];
                }
            } else {
                [weakSelf log:@"❌ 绑定失败：未获取到设备配置"];
            }
            [weakSelf updateStatus]; // 刷新状态标签
        });
    }];
}

-(void)startDrawProcess{
    PhoneShell *phoneShell = [PhoneShell sharedInstance];
    UIImage *originImage = [UIImage imageNamed:@"test"];
    if (!originImage) {
        [self log:@"❌ 刷图失败：项目中未找到\"test.png\"图片资源"];
        return;
    }

    NSLog(@"PK:  %@, CHIPID: %@",phoneShell.devicePKHex, phoneShell.chipIDHex);

    [phoneShell project2ScreenDefault : originImage];
}

/// 开始刷图流程（依赖绑定后的设备配置）
- (void)startDrawProcess1 {
    // 前置校验1：是否已绑定
    if (!self.deviceConfig) {
        [self log:@"❌ 请先绑定设备（点击\"绑定设备\"按钮）"];
        return;
    }
    // 前置校验2：设备是否在忙
    if (self.nfcHelper.isReading) {
        [self log:@"❌ 设备正在处理数据，请稍后再试"];
        return;
    }
    // 前置校验3：测试图片是否存在
    UIImage *originImage = [UIImage imageNamed:@"test"];
    if (!originImage) {
        [self log:@"❌ 刷图失败：项目中未找到\"test.png\"图片资源"];
        return;
    }
    
    [self log:@"🎨 开始刷图流程（图片缩放→抖点→发送）..."];
    
    // 1. 缩放图片到设备屏幕尺寸
    CGSize screenSize = CGSizeMake(self.deviceConfig.screenWidth, self.deviceConfig.screenHeight);
    UIImage *scaledImage = [self scaledImageFormImage:originImage toSize:screenSize];
    if (!scaledImage) {
        [self log:@"❌ 刷图失败：图片缩放失败"];
        return;
    }
    
    UIImage *ditheredImage = nil;
    [XTImageDitherUtils convertToUploadData:scaledImage
                               previewImage:&ditheredImage
                                  deviceCfg:self.deviceConfig
                                    algType:@"3"];
    if (!ditheredImage) {
        [self log:@"❌ 刷图失败：图片抖点处理失败（请检查设备配置）"];
        return;
    }
    
    // 3. 生成刷图数据（XTImageDitherUtils输出Hex数组）
    NSMutableArray *drawData = [XTImageDitherUtils getUploadDataFromDitherImage:ditheredImage
                                                                     deviceCfg:self.deviceConfig];
    if (drawData.count == 0) {
        [self log:@"❌ 刷图失败：未生成有效刷图数据"];
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
        [self log:@"✅ 生成100B数据失败"];
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
    
    [self log:[NSString stringWithFormat:@"sendHashData %@",[XTCommonUtils convertDataToHexString:sendHashData]]];
    [self log:[NSString stringWithFormat:@"sendTimeData %@",[XTCommonUtils convertDataToHexString:sendTimeData]]];
    [self log:[NSString stringWithFormat:@"sendSignData %@",[XTCommonUtils convertDataToHexString:sendSignData1]]];

    NSMutableData *sendData100B = [NSMutableData new];
    [sendData100B appendData:sendHashData];
    [sendData100B appendData:sendTimeData];
    [sendData100B appendData:sendSignData1];
    
    [self log:[NSString stringWithFormat:@"100B hash sign %@",[XTCommonUtils convertDataToHexString:sendData100B]]];

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
                [strongSelf log:[NSString stringWithFormat:@"❌ 刷图失败：%@", error.localizedDescription]];
            } else {
                if (!signData) {
                    [strongSelf log:@"❌ 刷图失败：未返回签名数据"];
                } else {
                    if (signData.length != 112) {
                        [strongSelf log:[NSString stringWithFormat:@"❌ 签名数据长度异常！预期112B，实际%luB", (unsigned long)signData.length]];
                        return;
                    }
                    
                    NSData *originalData = [signData subdataWithRange:NSMakeRange(0, 48)]; // 原始数据（48B）
                    NSData *signatureData = [signData subdataWithRange:NSMakeRange(48, 64)]; // 签名数据（64B）
                    
                    NSData *hashData = [signData subdataWithRange:NSMakeRange(0, 32)]; // 原始数据（48B）
                    NSData *countData = [signData subdataWithRange:NSMakeRange(32, 16)]; // 原始数据（48B）
                    
                    NSLog(@"✅ %@",self.devicePublicKey);

                    SLog(@"✅ %@", [XTCommonUtils convertDataToHexString:self.devicePublicKey]);
                    SecKeyRef publicKeyRef = [ECCKeyGenerator createSecKeyFromHexECCPublicKey:[XTCommonUtils convertDataToHexString:self.devicePublicKey]];

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
                        
                        SLog(@"signatureData %@", [XTCommonUtils convertDataToHexString:signatureData]);
                        SLog(@"originalData %@", [XTCommonUtils convertDataToHexString:originalData]);
                        SLog(@"hashData %@", [XTCommonUtils convertDataToHexString:hashData]);
                        SLog(@"countData %@", [XTCommonUtils convertDataToHexString:countData]);
                        
                        [weakSelf log:[NSString stringWithFormat:@"signatureData %@", [XTCommonUtils convertDataToHexString:signatureData]]];
                        [weakSelf log:[NSString stringWithFormat:@"originalData %@", [XTCommonUtils convertDataToHexString:originalData]]];

                        [weakSelf log:[NSString stringWithFormat:@"hashData %@", [XTCommonUtils convertDataToHexString:hashData]]];
                        [weakSelf log:[NSString stringWithFormat:@"countData %@", [XTCommonUtils convertDataToHexString:countData]]];

                        if (verifySuccess) {
                            [strongSelf log:@"✅ 签名验证成功！刷图成功"];
                        } else {
                            NSString *errorMsg = verifyError ? verifyError.localizedDescription : @"未知错误";
                            [strongSelf log:[NSString stringWithFormat:@"❌ 签名验证失败：%@（刷图失败）", errorMsg]];
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

- (void)otAButtonProcess:(UIButton *)sender {
    if (!self.deviceConfig) {
        [XTMBManager showAlertViewWithText:@"请先绑定设备" delayHid:1.5];
        return;
    }
    NSArray *allowedTypes = @[
        @"com.apple.binary-data",  // 苹果定义的“二进制数据”UTI，范围比 public.data 窄
        @"public.data"             // 兜底，确保所有二进制文件（包括 .bin）能被识别
    ];
    
    UIDocumentPickerViewController *documentvc = [[UIDocumentPickerViewController alloc] initWithDocumentTypes:allowedTypes inMode:UIDocumentPickerModeOpen];
    documentvc.hidesBottomBarWhenPushed = YES;
    documentvc.delegate = self;
    documentvc.modalPresentationStyle = UIModalPresentationFormSheet;
    [self presentViewController:documentvc animated:YES completion:nil];
}

#pragma mark - UIDocumentPickerDelegate
- (void)documentPicker:(UIDocumentPickerViewController *)controller didPickDocumentsAtURLs:(NSArray<NSURL *> *)urls {
    BOOL fileUrlAuthozied = [urls.firstObject startAccessingSecurityScopedResource];
    if (fileUrlAuthozied) {
        NSFileCoordinator *fileCoordinator = [[NSFileCoordinator alloc] init];
        NSError *error;
        [fileCoordinator coordinateReadingItemAtURL:urls.firstObject options:0 error:&error byAccessor:^(NSURL *newURL) {
            NSString *fileName = [newURL lastPathComponent];
            NSString *suffix = [fileName pathExtension];
            NSError *readError = nil;
            NSData *fileData = [NSData dataWithContentsOfURL:newURL options:NSDataReadingMappedIfSafe error:&readError];
            if (readError) {
                // 读取出错
                [XTMBManager showAlertViewWithText:@"读取OTA文件失败" delayHid:1.5];
            } else {
                // ota
                // 字符串比较
                BOOL result = [suffix caseInsensitiveCompare:@"bin"] == NSOrderedSame;
                if (result) {
                    [self log:[NSString stringWithFormat:@"fileName : %@==%@", fileName, suffix]];
                    [self doWriteScreenOTA:fileData];
                } else {
                    [self log:[NSString stringWithFormat:@"file type error: %@ == %@", fileName, suffix]];
                    [XTMBManager showAlertViewWithText:@"文件类型错误" delayHid:1.5];
                }
            }
            [self dismissViewControllerAnimated:YES completion:NULL];
        }];
        [urls.firstObject stopAccessingSecurityScopedResource];
    } else {
        // 授权失败
        [XTMBManager showAlertViewWithText:@"文件APP授权失败" delayHid:1.5];
    }
}

- (void)doWriteScreenOTA:(NSData *)fileData {
    NSMutableArray *otaData = [NSMutableArray array];
    char *pFileData = (char *)[fileData bytes];
    
    for (int i = 0; i < fileData.length; i++){
        [otaData addObject:[NSNumber numberWithInt:pFileData[i]]];
    }
    
    NSMutableData *serializedData = [NSMutableData data];
    for (NSNumber *num in otaData) {
        int value = [num intValue];
        [serializedData appendBytes:&value length:1];
    }
//    SLog(@" --- %@",[XTCommonUtils convertDataToHexString:serializedData]);

    NSData *sendHashData = [DataSigner calculateSHA256HashWithOriginalData:serializedData];
    NSData *sendTimeData = [DataSigner generateTimestampData];
    NSError *err;
    NSData *sendSignData1 = [ECCKeyGenerator signDataToRawFormat:sendHashData withPrivateKey:self.hPrivateKey error:&err];

    if (err) {
        [self log:@"✅ 生成100B数据失败"];
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
    
    [self log:[NSString stringWithFormat:@"sendHashData %@",[XTCommonUtils convertDataToHexString:sendHashData]]];
    [self log:[NSString stringWithFormat:@"sendTimeData %@",[XTCommonUtils convertDataToHexString:sendTimeData]]];
    [self log:[NSString stringWithFormat:@"sendSignData %@",[XTCommonUtils convertDataToHexString:sendSignData1]]];

    NSMutableData *sendData100B = [NSMutableData new];
    [sendData100B appendData:sendHashData];
    [sendData100B appendData:sendTimeData];
    [sendData100B appendData:sendSignData1];
    
    [self log:[NSString stringWithFormat:@"100B hash sign %@",[XTCommonUtils convertDataToHexString:sendData100B]]];

    NSArray<NSString *> *hexArray = [DataSigner convert100BDataToHexArray:sendData100B];
    
//    __weak typeof(self) weakSelf = self;
    
    NFCHelper *instance = [NFCHelper shareInstance];
    if(instance.isReading){
        // 这里需要添加提示
        [XTMBManager showAlertViewWithText:@"正在工作中。。。" delayHid:1.5];
        return;
    }
    
    [instance importOtaDataToDeviceWithHexArr:otaData
                                    screenCfg:self.deviceConfig
                                   signHexArr:hexArray.mutableCopy
                                     pageSize:490
                                   completion:^(NSData * _Nullable signData, NSError * _Nullable error) {
        
    }];
}

#pragma mark - 辅助方法（功能单一，可复用）
- (UIImage *)scaledImageFormImage:(UIImage *)image toSize:(CGSize)size {
    UIGraphicsBeginImageContext(size);
    [image drawInRect:CGRectMake(0,0, size.width, size.height)];
    UIImage *getImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return getImage;
}

/// 错误码转提示信息（统一错误文案，便于维护）
- (NSString *)messageForErrorCode:(PhoneCaseErrorCode)code {
    switch (code) {
        case PhoneCaseSuccess:
            return @"✅ 操作成功";
        case PhoneCaseInvalidParam:
            return @"❌ 操作失败：无效参数（检查设备配置）";
        case PhoneCaseFlashWriteFailed:
            return @"❌ 操作失败：写Flash失败（可能硬件故障）";
        case PhoneCaseFlashProtected:
            return @"❌ 操作失败：Flash被保护（需解除保护）";
        case PhoneCaseKeyNotFound:
            return @"❌ 操作失败：未找到密钥（设备未生成密钥）";
        case PhoneCasePasswordOpFailed:
            return @"❌ 操作失败：密码操作失败（加密/解密错误）";
        case PhoneCaseInvalidSignature:
            return @"❌ 操作失败：无效签名（设备可能非法）";
        case PhoneCaseOperationInvalid:
            return @"❌ 操作失败：操作无效（设备状态不支持）";
        case PhoneCaseKeyAlreadyExists:
            return @"❌ 操作失败：密钥已存在（无需重复初始化）";
        default:
            return [NSString stringWithFormat:@"❌ 操作失败：未知错误（错误码：%ld）", (long)code];
    }
}

@end
