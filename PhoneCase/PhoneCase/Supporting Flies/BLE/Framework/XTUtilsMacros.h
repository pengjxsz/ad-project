//
//  XTUtilsMacros.h
//  fatereason
//
//  Created by QiGe on 2018/3/2.
//  Copyright © 2018年 mingrikongjian002. All rights reserved.
//

#ifndef XTUtilsMacros_h //工具类的宏
#define XTUtilsMacros_h

/*
 *  常规单列
 */
#pragma mark - 常规单列

/** 整个APP */
#define KApplication                        [UIApplication sharedApplication]

/** AppDelegate */
#define KAppDelegate                        ((AppDelegate *)[UIApplication sharedApplication].delegate)

/** UserDefaults */
#define KUserDefaults                       [NSUserDefaults standardUserDefaults]

/** 获取通知中心 */
#define KNotificationCenter                 [NSNotificationCenter defaultCenter]

/** 主屏 */
#define KWindowView                         [[UIApplication sharedApplication] keyWindow]

/*
 *  常用
 */
#pragma mark - 常用

/** APP名字 */
#define KAppName                            [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleDisplayName"]

/** APP Verison版本号 */
#define KAppVersion                         [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"]

/** APP build版本号 */
#define KLocalCurVersion                    [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleVersion"]

//判断是否为横竖屏
#define KIsPortrait ([UIApplication sharedApplication].statusBarOrientation == UIInterfaceOrientationPortrait || [UIApplication sharedApplication].statusBarOrientation == UIInterfaceOrientationPortraitUpsideDown)

/** 系统版本号 */
#define KSystemVersion                      [[UIDevice currentDevice] systemVersion]

/** 获取当前语言 */
#define KCurrentLanguage                    ([[NSLocale preferredLanguages] objectAtIndex:0])

/** 判断是否为iPhone */
#define KIsiPhone                           (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone)

/** 弱引用 */
#define KweakSelf(weakSelf)                 __weak __typeof(&*self)weakSelf = self

/** 强引用 */
#define KstrongSelf(strongSelf)             __strong __typeof(&*self)strongSelf = self

/** 转成字符串 */
#define KStringWithFormat(format,...)       [NSString stringWithFormat:format,##__VA_ARGS__]

/** 获取图片资源 */
#define kGetImage(imageName)                [UIImage imageNamed:[NSString stringWithFormat:@"%@",imageName]]

/** 把对象负值为nil */
#define CC_SAFE_NULL(p)                     do { if(p) { (p) = nil; } } while(0)

/** 对象是否为空 */
#define IsNilOrNull(_ref)                   (((_ref) == nil) || ([(_ref) isEqual:[NSNull null]]))

/** 角度转弧度 */
#define DEGREES_TO_RADIANS(x)               (M_PI * (x) / 180.0)

/** 角度转弧度 */
#define RADIANS_TO_DEGREES(radian)          (radian * 180.0)/(M_PI)



/** 适配隐藏导航栏有刷新的页面 AdjustsScrollViewInsetNever */
#define AdjustsScrollViewInsetNever(controller,view) (if(@available(iOS 11.0, *)) {view.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;} else if([controller isKindOfClass:[UIViewController class]]) {controller.automaticallyAdjustsScrollViewInsets = false;})

/** 设置view 圆角 */
#define KViewBorderRadius(View, Radius)\
\
[View.layer setCornerRadius:(Radius)];\
[View.layer setMasksToBounds:YES];

/** 设置view 圆角和边框 */
#define KViewBorderRadiusLine(View, Radius, Width, Color)\
\
[View.layer setCornerRadius:(Radius)];\
[View.layer setMasksToBounds:YES];\
[View.layer setBorderWidth:(Width)];\
[View.layer setBorderColor:[Color CGColor]]

/** 单例 */
#define SYNTHESIZE_SINGLETON_FOR_CLASS(classname) \
\
static classname *shared##classname = nil; \
\
+ (classname *)shared##classname \
{ \
@synchronized(self) \
{ \
if (shared##classname == nil) \
{ \
shared##classname = [[self alloc] init]; \
} \
} \
\
return shared##classname; \
} \
\
+ (id)allocWithZone:(NSZone *)zone \
{ \
@synchronized(self) \
{ \
if (shared##classname == nil) \
{ \
shared##classname = [super allocWithZone:zone]; \
return shared##classname; \
} \
} \
\
return nil; \
} \
\
- (id)copyWithZone:(NSZone *)zone \
{ \
return self; \
} \
\

/* tableView不会向下偏移 */
#define  adjustsScrollViewInsets_NO(scrollView,vc)\
do { \
_Pragma("clang diagnostic push") \
_Pragma("clang diagnostic ignored \"-Warc-performSelector-leaks\"") \
if ([UIScrollView instancesRespondToSelector:NSSelectorFromString(@"setContentInsetAdjustmentBehavior:")]) {\
[scrollView   performSelector:NSSelectorFromString(@"setContentInsetAdjustmentBehavior:") withObject:@(2)];\
} else {\
vc.automaticallyAdjustsScrollViewInsets = NO;\
}\
_Pragma("clang diagnostic pop") \
} while (0)


/*
 *  系统版本判断
 */
#pragma mark - 系统版本判断

/** iOS 9~ */
#define KiOS9OrLater                        ([[UIDevice currentDevice].systemVersion doubleValue] >= 9.0)

/** iOS 10~ */
#define KiOS10OrLater                       ([[UIDevice currentDevice].systemVersion doubleValue] >= 10.0)

/** iOS 11~ */
#define KiOS11OrLater                       ([[UIDevice currentDevice].systemVersion doubleValue] >= 11.0)

/** iOS 12~ */
#define KiOS12OrLater                       ([[UIDevice currentDevice].systemVersion doubleValue] >= 12.0)

/** iOS 13~ */
#define KiOS13OrLater                       ([[UIDevice currentDevice].systemVersion doubleValue] >= 13.0)

/** 等于 */
#define SYSTEM_VERSION_EQUAL_TO(v)                  ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] == NSOrderedSame)

/** 大于 */
#define SYSTEM_VERSION_GREATER_THAN(v)              ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] == NSOrderedDescending)

/** 大于等于 */
#define SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(v)  ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] != NSOrderedAscending)

/** 小于 */
#define SYSTEM_VERSION_LESS_THAN(v)                 ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] == NSOrderedAscending)

/** 小于等于 */
#define SYSTEM_VERSION_LESS_THAN_OR_EQUAL_TO(v)     ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] != NSOrderedDescending)

/*
 *   打印
 */
#pragma mark - 打印
/*
 *  自定义log输出，debug时，正常NSLog输出，release状态，为空，不打印
 *
 *  __VA_ARGS__ 是一个可变参数的宏，这个可变参数的宏是新的C99规范中新增的，目前似乎只有gcc支持（VC6.0的编译器不支持）。宏前面加上##的作用在于，当可变参数的个数为0时，这里的##起到把前面多余的","去掉,否则会编译出错。
 *  __FILE__ 宏在预编译时会替换成当前的源文件名
 *  __LINE__宏在预编译时会替换成当前的行号
 *  __FUNCTION__宏在预编译时会替换成当前的函数名称
 */

/** 方式一 */
#ifdef DEBUG
# define ALog(fmt, ...)                    NSLog((@"\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n[File :%s]\n" "[fuc :%s]\n" "[line num :%d]\n" "[print info :]" fmt @"\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n"), __FILE__, __FUNCTION__, __LINE__, ##__VA_ARGS__);
#else
# define ALog(...);
#endif

/** 方式二 */
#ifdef DEBUG
#define XTLog(fmt, ...) do { \
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init]; \
    formatter.dateFormat = @"yyyy-MM-dd HH:mm:ss.SSS"; \
    formatter.locale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"]; \
    NSString *timeStr = [formatter stringFromDate:[NSDate date]]; \
    NSLog(@"[%@] %s [Line %d] " fmt, timeStr, __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__); \
} while(0)
#else
#define XTLog(...)
#endif

/** 方式三 */
#ifdef DEBUG
#define PLog(fmt, ...)                      NSLog((fmt),##__VA_ARGS__);
#else
#define PLog(...)
#endif

/** 控制台打印不完全的解决方法 */
#ifdef DEBUG
#define SLog(format, ...) printf("class: <%p %s:(%d) > method: %s \n%s\n", self, [[[NSString stringWithUTF8String:__FILE__] lastPathComponent] UTF8String], __LINE__, __PRETTY_FUNCTION__, [[NSString stringWithFormat:(format), ##__VA_ARGS__] UTF8String] )
#else
#define SLog(format, ...)
#endif

/** 方式四（弹框打印） */
#ifdef DEBUG
#define ULog(fmt, ...)  {UIAlertView *alert = [[UIAlertView alloc] initWithTitle:[NSString stringWithFormat:@"%s\n [Line %d] ", __PRETTY_FUNCTION__, __LINE__] message:[NSString stringWithFormat:fmt, ##__VA_ARGS__]  delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil]; [alert show];}
#else
#define ULog(...)
#endif

/*
 *  网络请求
 */

/** 获取网络数据，执行请求之后的回调函数 */
#define DECLARE_ASI_FUNCTION(funName, varType) \
- (void)funName##Done:(varType)request; \
- (void)funName##Failed:(varType)request;

/** 执行服务端请求后，回调函数，返回数组 */
#define DECLARE_ASI_ARRAY_FUNCATION(funName) \
- (void)responseWith##funName##NetStatus:(int)netStatus respArray:(NSArray *)respArray;

#define DECLARE_UI_ARRAY_FUNCTION(funName) \
- (void)funName##NetStatus:(int)netStatus respArray:(NSArray *)respArray;

/** 执行服务端请求后，回调函数，返回键值对 */
#define DECLARE_ASI_DICTIONARY_FUNCATION(funName) \
- (void)responseWith##funName##NetStatus:(int)netStatus respDict:(NSDictionary *)respDict;

/** 执行，返回id类型 */
#define DECLARE_ASI_ID_FUNCATION(funName) \
- (void)responseWith##funName##NetStatus:(int)netStatus respId:(id)respId;

#define DECLARE_UI_DICTIONARY_FUNCATION(funName) \
- (void)funName##NetStatus:(int)netStatus respDict:(NSDictionary *)respDict;

#define DECLARE_UI_STRING_FUNCATION(funName) \
- (void)funName##NetStatus:(int)netStatus respStr:(NSString *)respStr;

#define DECLARE_UI_INT_FUNCATION(funName) \
- (void)funName##NetStatus:(int)netStatus respId:(NSInteger)respId;

#define DECLARE_UI_ID_FUNCATION(funName) \
- (void)funName##NetStatus:(int)netStatus respId:(id)respid;

/** 执行服务端请求后，回调函数，返回状态值 */
#define DECLARE_ASI_STATUS_FUNCATION(funName) \
- (void)responseWith##funName##NetStatus:(int)netStatus respStatus:(int)respStatus;

#define DECLARE_UI_STATUS_FUNCATION(funName) \
- (void)funName##NetStatus:(int)netStatus respStatus:(int)respStatus;


//ndef指令格式
#define CMD_CODE_LENGTH             4
#define NDEF_SUCCESS                @"9000"                 //apdu执行成功，返回代码
#define REFRESH_SCREEN_CMD          @"F0D405%@00"           //刷新屏幕图像，参数为屏幕序号，从0开始
#define GET_REFRESH_STATUS          @"F0DE000001"           //无源刷新时获取刷频状态
#define GET_CONFIG_INFO_CMD         @"00D1000000"           //获取设备信息
#define CHECK_NEED_PIN_CMD          @"F0D8000005000000000E" //检查是否需要pin码，黑白双色判断是否四色
#define GET_RANDOM_CMD              @"0084000004"           //获取四字节随机数
#define VERIFY_PIN_CMD              @"00200001%@%@"         //验pin
#define GET_CONVERT_INFO            @"00eb000002"           //获取图像的翻转信息

//TLV标签
#define TLV_A0                      @"A0"               //屏幕信息
#define TLV_A1                      @"A1"               //双色三色颜色参数
#define TLV_B1                      @"B1"               //图片存储数量
#define TLV_B2                      @"B2"               //用户数据长度
#define TLV_B3                      @"B3"               //供电模式
#define TLV_C0                      @"C0"               //APPID
#define TLV_C1                      @"C1"               //UUID
#define TLV_D1                      @"D1"               //RFU
#define TLV_E0                      @"E0"               //三色以上颜色配置

#define VALUE_SINGLE_MODE           1                   //单图模式
#define VALUE_COMPLEX_MODE          2                   //双图模式

//数据传输模式
#define PARALLEL_TRANS_MODE         0                   //并行
#define SERIAL_TRANS_MODE           1                   //串行


#define VALUE_BLACK                 0x00                //黑色
#define VALUE_WHITE                 0x01                //白色
#define VALUE_RED                   0x02                //红色
#define VALUE_YELLOW                0x03                //黄色
#define VALUE_ORANGE                0x04                //橙色
#define VALUE_GREEN                 0x05                //绿色
#define VALUE_QING                  0x06                //青色
#define VALUE_BLUE                  0x07                //蓝色
#define VALUE_PURPLE                0x08                //紫色

#define DIC_KEY_BLACK               @"00"               //黑色
#define DIC_KEY_WHITE               @"01"               //白色
#define DIC_KEY_RED                 @"02"               //红色
#define DIC_KEY_YELLOW              @"03"               //黄色
#define DIC_KEY_ORANGE              @"04"               //橙色
#define DIC_KEY_GREEN               @"05"               //绿色
#define DIC_KEY_QING                @"06"               //青色
#define DIC_KEY_BLUE                @"07"               //蓝色
#define DIC_KEY_PURPLE              @"08"               //紫色

#define SCREEN_SIZE_ERROR           @"R0"               //屏幕尺寸错误
#define COLOR_MODE_ERROR            @"R1"               //颜色模式错误
#define SCAN_MODE_ERROR             @"R2"               //扫描模式错误
#define COMBINE_MODE_ERROR          @"R3"               //混合模式错误
#define COLOR_COUNT_ERROR           @"R4"               //颜色数量错误
#define COLOR_VALUE_ERROR           @"R5"               //颜色值错误
#define PIN_CHECK_ERROR             @"R6"               //是否带pin校验错误
#define VERSION_CHECK_ERROR         @"R7"               //版本号校验错误

//颜色代码
#define COLOR_RGB_BLACK             0                   //黑
#define COLOR_RGB_WHITE             1                   //白
#define COLOR_RGB_THIRD             2                   //红或黄

//像素颜色 //复旦微用
#define COLOR_BW_CODE               @"20"               //黑白
#define COLOR_BWR_CODE              @"30"               //黑白红
#define COLOR_BWY_CODE              @"31"               //黑白黄
#define COLOR_BWRY_CODE             @"40"               //黑白红黄
#define COLOR_BWRYGB_CODE           @"50"               //黑白红黄绿蓝

//新增7色不能用于复旦微
#define COLOR_BWRYGBO_CODE          @"60"               //黑白红黄绿蓝橙

//图像生成算法
#define ALG_TYPE_ATKINSON           @"1"               //Atkinson抖动
#define ALG_TYPE_COLOR              @"2"               //色阶
#define ALG_TYPE_FLOYD              @"3"               //Floyd-Steinberg抖动

//错误码定义
#define BT_SUCCESS                  0   //操作成功
#define BT_OPERATION_FAILED         1   //操作失败

#endif /* XTUtilsMacros_h */
