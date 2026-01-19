#import <UIKit/UIKit.h>


@interface PhoneShell : NSObject

//原始裁剪图像
@property(nonatomic, strong) UIImage *orignImage;

@property (nonatomic, assign) CGFloat phoneScreenWidth;
//屏幕高度
@property (nonatomic, assign) CGFloat phoneScreenHeight;

@property (nonatomic, assign) NSInteger deviceScreenWidth;
//屏幕高度	
@property (nonatomic, assign) NSInteger deviceScreenHeight;

@property (nonatomic, assign) NSInteger deviceScreenColors;

@property (nonatomic, copy) NSString *devicePKHex; // 十六进制字符串公钥（可选，方便日志/传输）
@property (nonatomic, strong) NSString *chipIDHex; // 芯片ID（可选，按需保存）

@property (nonatomic, strong) NSString *masterKey;
@property (nonatomic, strong) NSString *masterPK;
@property (nonatomic, strong) NSString *chatPK;
@property (nonatomic, strong) NSString *chatKey;
@property (nonatomic, strong) NSString *userId;

@property (nonatomic, strong) NSString *bindError; // bind error
@property (nonatomic, strong) NSString *projectError; // project error


@property (nonatomic, assign) CGFloat DSAdaptCoefficient;

//写屏数据
@property(nonatomic, strong) NSData *outputData;
@property(nonatomic, strong) UIImage *previewImage;

//@property(nonatomic, strong) UIView *bgView;

+ (instancetype)sharedInstance;

//call twice-----deprecated!!!
//PhoneShell *phoneShell = [[PhoneShell sharedInstance] init];
//if (![phoneShell isEverBound]){
//    [phoneShell BindNFCDevice];
//    [phoneShell isEverBound];
//}
//call twice-----deprecated!!!

//
////load config
-(Boolean) isEverBound;

//-(void)BindNFCDevice : (id<NFCHelperDelegate>) protocolReceiver;
-(void)BindNFCDevice;
-(void)saveRegisterInfo;

//project in default param (type, bright, rgbbase, dithercount)
-(int)project2ScreenDefault: (UIImage *)clippedImage;

//return preview and save the previewImage to the property previewImage
- (UIImage*)projectPreview: (UIImage *)clippedImage
              AlgType:(NSString *)algType
                     BRIGHT:(int)nBright
                DITHERCOUNT:(int )nDitherPointCount
                   BaseRGB:(int)nBaseRGB;

//use the image from property previewImage to project
- (int)project2Screen;

-(Boolean) saveBindInfo: (NSString *) masterKey
MasterPK:(NSString *) masterPK
               ChatKey:(NSString *) chatKey
                 ChatPK:(NSString *) chatPK;

-(Boolean) saveUserId: (NSString *) userId;


@end
