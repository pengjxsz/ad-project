//
//  XTMBManager.m
//  MBProgressDemo
//
//  Created by hungryBoy on 16/1/23.
//  Copyright © 2016年 hungryBoy. All rights reserved.
//

#import "XTMBManager.h"
#import "UIImage+GIF.h"

#define kScreen_height  [[UIScreen mainScreen] bounds].size.height

#define kScreen_width   [[UIScreen mainScreen] bounds].size.width

#define kDefaultRect     CGRectMake(0, 0, kScreen_width, kScreen_height)

#define kDefaultView    [XTMBManager getKeyWindow]

#define kGloomyBlackColor  [UIColor colorWithRed:0 green:0 blue:0 alpha:0.5]

#define kGloomyClearCloler  [UIColor colorWithRed:1 green:1 blue:1 alpha:0]

#define XTCOLOR(c)      [UIColor colorWithRed:((float)((c & 0xFF0000) >> 16))/255.0 green:((float)((c & 0xFF00) >> 8))/255.0 blue:((float)(c & 0xFF))/255.0 alpha:1.0f]

#define XTCOLORA(c,a)               [UIColor colorWithRed:((float)((c & 0xFF0000) >> 16))/255.0 green:((float)((c & 0xFF00) >> 8))/255.0 blue:((float)(c & 0xFF))/255.0 alpha:a]

/* 默认网络提示，可在这统一修改 */
static NSString *const kLoadingMessage = @"loading ...";

/* 默认简短提示语显示的时间，在这统一修改 */
static CGFloat const   kShowTime  = 4.0f;

/* 默认超时时间，30s后自动去除提示框 */
static NSTimeInterval const interval = 30.0f;

/* 手势是否可用，默认yes，轻触屏幕提示框隐藏 */
static BOOL isAvalibleTouch = NO;

@implementation XTMBManager

UIView *gloomyView;//深色背景
UIView *prestrainView;//预加载view
BOOL isShowGloomy;//是否显示深色背景.0

#pragma mark -   类初始化
+ (void)initialize {
    if (self == [XTMBManager self]) {
        //该方法只会走一次
        [self customView];
    }
}
#pragma mark - 初始化gloomyView
+ (void)customView {
    dispatch_async(dispatch_get_main_queue(), ^{
        gloomyView = [[GloomyView alloc] initWithFrame:kDefaultRect];
        gloomyView.backgroundColor = kGloomyBlackColor;
        gloomyView.hidden = YES;
        isShowGloomy = NO;
    });
}

+ (void)showGloomy:(BOOL)isShow {
    isShowGloomy = isShow;
}

#pragma mark - 简短提示语
+ (void)showBriefAlert:(NSString *)message view:(UIView *) view{
    isAvalibleTouch = NO;
    [XTMBManager dismiss];
    dispatch_async(dispatch_get_main_queue(), ^{
        prestrainView = view;
        MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo:view ?:kDefaultView animated:YES];
        hud.label.text = message;
        UIFont *font = [UIFont fontWithName:@"PingFangSC-Medium"size:15];//这个是9.0以后自带的平方字体
        if(font != nil) hud.label.font = font;
        hud.contentColor = [UIColor whiteColor];
        hud.animationType = MBProgressHUDAnimationZoom;
        hud.bezelView.style = MBProgressHUDBackgroundStyleSolidColor;
        hud.bezelView.backgroundColor = XTCOLORA(0x000000, .7);
        hud.mode = MBProgressHUDModeText;
        hud.margin = 10.f;
        [hud setOffset:CGPointMake(0, -150)];
        hud.removeFromSuperViewOnHide = YES;
        [hud hideAnimated:YES afterDelay:kShowTime];
    });
}

#pragma mark - 长时间的提示语
+ (void)showPermanentMessage:(NSString *)message view:(UIView *) view{
    isAvalibleTouch = YES;
    [XTMBManager dismiss];

    dispatch_async(dispatch_get_main_queue(), ^{
        prestrainView = view;
        gloomyView.frame = view ? CGRectMake(0, 0, view.frame.size.width, view.frame.size.height):
        kDefaultRect;
        MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo:gloomyView animated:YES];
        hud.bezelView.style = MBProgressHUDBackgroundStyleSolidColor;
        hud.bezelView.backgroundColor = XTCOLORA(0x000000, .7);
        hud.label.text = message;
        hud.label.numberOfLines = 0;
        hud.contentColor = [UIColor whiteColor];
        hud.label.adjustsFontSizeToFitWidth = YES;
        hud.animationType = MBProgressHUDAnimationZoom;
        hud.mode = MBProgressHUDModeCustomView;
        hud.removeFromSuperViewOnHide = YES;
        hud.mode = MBProgressHUDModeText;
        [gloomyView addSubview:hud];
        [self showClearGloomyView];
        [hud showAnimated:YES];
        
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(3 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [self dismiss];
        });
    });
}

#pragma mark - 长时间长文本的提示语
+ (void) showLongTitle:(NSString *)title message:(NSString *)message textAlignment:(NSTextAlignment)textAlignment viewColor:(UIColor *)viewColor wordColor:(UIColor *)wordColor view:(UIView *)view
{
    isAvalibleTouch = YES;
    [XTMBManager dismiss];

    dispatch_async(dispatch_get_main_queue(), ^{
        prestrainView = view;
        gloomyView.frame = view ? CGRectMake(0, 0, view.frame.size.width, view.frame.size.height):
        kDefaultRect;
        MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo:gloomyView animated:YES];
        hud.backgroundView.backgroundColor = [UIColor colorWithRed:0/255.0 green:0/255.0 blue:0/255.0 alpha:0.7];
        
        if (viewColor != nil) {
            hud.bezelView.style = MBProgressHUDBackgroundStyleSolidColor;
            hud.bezelView.backgroundColor = viewColor;
        }
        
        if (wordColor != nil) {
            hud.contentColor = wordColor;//字的颜色
        }else{
            hud.contentColor = [UIColor whiteColor];//字的颜色
        }

        hud.label.text = title;
        hud.label.font = [UIFont systemFontOfSize:20.f];

        hud.detailsLabel.text = message;
        hud.detailsLabel.textAlignment = textAlignment;
        hud.detailsLabel.font = [UIFont systemFontOfSize:16.f];
        
        hud.animationType = MBProgressHUDAnimationZoom;
        hud.mode = MBProgressHUDModeCustomView;
        hud.removeFromSuperViewOnHide = YES;
        hud.mode = MBProgressHUDModeText;
        [gloomyView addSubview:hud];
        [self showClearGloomyView];
        [hud showAnimated:YES];
    });
}

#pragma mark - 网络加载提示用
+ (void)showLoadingInView:(UIView *) view{
    isAvalibleTouch = NO;
    [XTMBManager dismiss];

    dispatch_async(dispatch_get_main_queue(), ^{
        prestrainView = view;
        MBProgressHUD *hud = [[MBProgressHUD alloc] initWithView:gloomyView];
       
        hud.label.text = kLoadingMessage;
        hud.contentColor = [UIColor whiteColor];
        
        hud.removeFromSuperViewOnHide = YES;
        gloomyView.frame = view ? CGRectMake(0, 0, view.frame.size.width, view.frame.size.height):
        kDefaultRect;
        if (isShowGloomy) {
            [self showBlackGloomyView];
        }else {
            [self showClearGloomyView];
        }
        [gloomyView addSubview:hud];
        [hud showAnimated:YES];
        [self dismissDelay];
    });
}

+ (void)showWaitingWithTitle:(NSString *)title view:(UIView *)view delay:(NSInteger)delay {
    isAvalibleTouch = NO;
    [XTMBManager dismiss];

    dispatch_async(dispatch_get_main_queue(), ^{
        prestrainView = view;
        MBProgressHUD *hud = [[MBProgressHUD alloc] initWithView:gloomyView];
        hud.bezelView.style = MBProgressHUDBackgroundStyleSolidColor;
        hud.bezelView.backgroundColor = XTCOLORA(0x000000, 0.7);
        hud.label.text = title;
        hud.contentColor = [UIColor whiteColor];
        hud.backgroundView.color = [UIColor clearColor];

        hud.removeFromSuperViewOnHide = YES;
        gloomyView.frame = view ? CGRectMake(0, 0, view.frame.size.width, view.frame.size.height):
        kDefaultRect;
        if (isShowGloomy) {
            [self showBlackGloomyView];
        }else {
            [self showClearGloomyView];
        }
        [gloomyView addSubview:hud];
        [hud showAnimated:YES];
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delay * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [self dismiss];
        });
    });
}

+ (void)showWaitingWithTitle:(NSString *)title view:(UIView *)view {
    isAvalibleTouch = NO;
    [XTMBManager dismiss];
    dispatch_async(dispatch_get_main_queue(), ^{
        prestrainView = view;
        MBProgressHUD *hud = [[MBProgressHUD alloc] initWithView:gloomyView];
        hud.bezelView.style = MBProgressHUDBackgroundStyleSolidColor;
        hud.bezelView.backgroundColor = XTCOLORA(0x000000, 0.7);
        hud.label.text = title;
        hud.contentColor = [UIColor whiteColor];
        hud.backgroundView.color = [UIColor clearColor];

        hud.removeFromSuperViewOnHide = YES;
        gloomyView.frame = view ? CGRectMake(0, 0, view.frame.size.width, view.frame.size.height):
        kDefaultRect;
        if (isShowGloomy) {
            [self showBlackGloomyView];
        }else {
            [self showClearGloomyView];
        }
        [gloomyView addSubview:hud];
        [hud showAnimated:YES];
    });
}

+(void)showAlertWithCustomImage:(NSString *)imageName title:(NSString *)title view:(UIView *)view imgFrame:(CGRect)imgFrame
{
    isAvalibleTouch = NO;
    [XTMBManager dismiss];

    dispatch_async(dispatch_get_main_queue(), ^{
        prestrainView = view;
        gloomyView.frame = view ? CGRectMake(0, 0, view.frame.size.width, view.frame.size.height):
        kDefaultRect;
        MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo:view ?:kDefaultView animated:YES];
        UIImageView *littleView = [[UIImageView alloc] initWithFrame:imgFrame];
        littleView.image = [UIImage imageNamed:imageName];
        hud.customView = littleView;
        hud.removeFromSuperViewOnHide = YES;
        hud.animationType = MBProgressHUDAnimationZoom;
        hud.bezelView.style = MBProgressHUDBackgroundStyleSolidColor;
        hud.bezelView.backgroundColor = XTCOLORA(0x000000, .7);
        hud.detailsLabel.text = title;
        hud.contentColor = [UIColor whiteColor];
        hud.mode = MBProgressHUDModeCustomView;
        [hud showAnimated:YES];
        [hud hideAnimated:YES afterDelay:kShowTime];
    });
}

+ (void)showLoadingInView:(UIView *)view Y:(CGFloat)Y 
{
    isAvalibleTouch = NO;
    [XTMBManager dismiss];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        prestrainView = view;
        MBProgressHUD *hud = [[MBProgressHUD alloc] initWithView:gloomyView];
        
//        UIImage  *image = [ZM_Tool sd_animatedGIFWithScreenWithNamed:@"loading"];
        UIImage  *image = [UIImage new];
        UIImageView  *gifview = [[UIImageView alloc]initWithFrame:CGRectMake(0,0,image.size.width/2.0, image.size.height/2.0)];
        gifview.contentMode = UIViewContentModeScaleAspectFill;
        gifview.image = image;
        hud.backgroundColor = XTCOLOR(0xf8f8f8);
        hud.bezelView.style = MBProgressHUDBackgroundStyleSolidColor; //去掉背景透明白色layer
        hud.bezelView.backgroundColor = XTCOLOR(0xf8f8f8);//默认颜色
        hud.mode = MBProgressHUDModeCustomView;
        hud.customView = gifview;
        
        hud.removeFromSuperViewOnHide = YES;
        gloomyView.frame = view && view.frame.size.width != 0 ? CGRectMake(0, Y, view.frame.size.width, view.frame.size.height - Y) : kDefaultRect;
        [hud setOffset:CGPointMake(0, -30)];

        if (isShowGloomy) {
            [self showBlackGloomyView];
        }else {
            [self showClearGloomyView];
        }
        [gloomyView addSubview:hud];
        [hud showAnimated:YES];
        [self dismissDelay];
    });
}

+ (void)showLoadingInView:(UIView *)view Y:(CGFloat)Y increment:(CGFloat)increment;
{
    isAvalibleTouch = NO;
    [XTMBManager dismiss];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        prestrainView = view;
        MBProgressHUD *hud = [[MBProgressHUD alloc] initWithView:gloomyView];
        
//        UIImage  *image = [ZM_Tool sd_animatedGIFWithScreenWithNamed:@"loading"];
        UIImage *image = [UIImage new];
        UIImageView  *gifview = [[UIImageView alloc]initWithFrame:CGRectMake(0,0,image.size.width/2.0, image.size.height/2.0)];
        gifview.contentMode = UIViewContentModeScaleAspectFill;
        gifview.image = image;
        hud.backgroundColor = XTCOLOR(0xf8f8f8);
        hud.bezelView.style = MBProgressHUDBackgroundStyleSolidColor; //去掉背景透明白色layer
        hud.bezelView.backgroundColor = XTCOLOR(0xf8f8f8);//默认颜色
        hud.mode = MBProgressHUDModeCustomView;
        hud.customView = gifview;
        
        hud.removeFromSuperViewOnHide = YES;
        gloomyView.frame = view && view.frame.size.width != 0 ? CGRectMake(0, Y, view.frame.size.width, view.frame.size.height - Y) : CGRectMake(0, Y, kScreen_width, kScreen_height - Y - increment);
        [hud setOffset:CGPointMake(0, -30)];

        if (isShowGloomy) {
            [self showBlackGloomyView];
        }else {
            [self showClearGloomyView];
        }
        [gloomyView addSubview:hud];
        [hud showAnimated:YES];
    });
}

///  展示几秒后自动隐藏
/// @param text 文本
/// @param delay 展示时长
+ (void)showAlertViewWithText:(NSString *)text delayHid:(NSTimeInterval)delay {
    [XTMBManager dismiss];
    dispatch_async(dispatch_get_main_queue(), ^{
        MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo:kDefaultView animated:YES];
        hud.detailsLabel.text = text;
        hud.detailsLabel.font = [UIFont fontWithName:@"PingFangSC-Regular"size:14];;
        hud.contentColor = [UIColor whiteColor];
        hud.animationType = MBProgressHUDAnimationZoom;
        hud.bezelView.style = MBProgressHUDBackgroundStyleSolidColor;
        hud.bezelView.backgroundColor = XTCOLORA(0x000000, 0.7);
        hud.mode = MBProgressHUDModeText;
        hud.margin = 10.f;
        hud.removeFromSuperViewOnHide = YES;
        [hud hideAnimated:YES afterDelay:delay];
    });
}

#pragma mark - 加载在window上的提示框
+(void)showLoading{
    [self showLoadingInView:nil];
}

+ (void)showWaitingWithTitle:(NSString *)title{
    [self showWaitingWithTitle:title view:nil];
}

+(void)showBriefAlert:(NSString *)alert{
    [self showBriefAlert:alert view:nil];
}

+(void)showPermanentAlert:(NSString *)alert{
    [self showPermanentMessage:alert view:nil];
}

+(void)showAlertWithCustomImage:(NSString *)imageName title:(NSString *)title imgFrame:(CGRect)imgFrame{
    [self showAlertWithCustomImage:imageName title:title view:nil imgFrame:imgFrame];
}

+(void)showSuccessOK:(NSString *)title{
    [self showAlertWithCustomImage:@"Checkmark" title:title view:nil imgFrame:CGRectMake(0, 0, 37, 37)];
}

+(void)showSuccess:(NSString *)title{
    [self showAlertWithCustomImage:@"success" title:title view:nil imgFrame:CGRectMake(0, 0, 37, 37)];
}

+(void)showError:(NSString *)title{
    [self showAlertWithCustomImage:@"error" title:title view:nil imgFrame:CGRectMake(0, 0, 37, 37)];
}

+(void)showTongxulu:(NSString *)title{
    [self showAlertWithCustomImage:@"mine_tongxunlu_ok" title:title view:nil imgFrame:CGRectMake(0, 0, 37, 37)];
}
#pragma mark -   GloomyView背景色
+ (void)showBlackGloomyView {
    gloomyView.backgroundColor = kGloomyBlackColor;
    [self gloomyConfig];
}
+ (void)showClearGloomyView {
    gloomyView.backgroundColor = kGloomyClearCloler;
    dispatch_async(dispatch_get_main_queue(), ^{
        [self gloomyConfig];
    });
}
#pragma mark -   决定GloomyView add到已给view或者window上
+ (void)gloomyConfig {
    gloomyView.hidden = NO;
    gloomyView.alpha = 1;
    if (prestrainView) {
        [prestrainView addSubview:gloomyView];
    }else {
        UIWindow *window = kDefaultView;
        if (![window.subviews containsObject:gloomyView]) {
            [window addSubview:gloomyView];
        }
    }
}
#pragma mark - 隐藏提示框
+(void)dismiss{
    dispatch_async(dispatch_get_main_queue(), ^{
        MBProgressHUD *hud = [XTMBManager HUDForView:gloomyView];
        if (hud) {
            [hud removeFromSuperview];
            [gloomyView removeFromSuperview];
        }
    });
}

#pragma mark -   超时后（默认30s）自动隐藏加载提示
+ (void)dismissDelay {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(interval * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self dismiss];
    });
}

#pragma mark -   获取view上的hud
+ (MBProgressHUD *)HUDForView:(UIView *)view {
    NSEnumerator *subviewsEnum = [view.subviews reverseObjectEnumerator];
    for (UIView *subview in subviewsEnum) {
        if ([subview isKindOfClass:[MBProgressHUD class]]) {
            return (MBProgressHUD *)subview;
        }
    }
    return nil;
}

#pragma mark ————— 顶部tip —————
+ (void)showTopTipMessage:(NSString *)msg {
    [self showTopTipMessage:msg isWindow:NO];
}

+ (void)showTopTipMessage:(NSString *)msg isWindow:(BOOL) isWindow
{
    [XTMBManager dismiss];
}

+ (UIWindow *)getKeyWindow {
    UIWindow *keyWindow = nil;
    NSArray  *windows = [[UIApplication sharedApplication] windows];
    for (UIWindow *window in windows) {
        if (window.isKeyWindow) {
            keyWindow = window;
            break;
        }
    }
    return keyWindow;
 }

@end

@implementation GloomyView

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    if (isAvalibleTouch) {
        [XTMBManager dismiss];
    }
}


@end
