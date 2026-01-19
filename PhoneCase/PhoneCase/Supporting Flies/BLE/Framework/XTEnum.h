//
//  XTEnum.h
//  NewTool
//
//  Created by XTMacMini on 2025/3/14.
//

#ifndef XTEnum_h
#define XTEnum_h

//像素颜色 //复旦微用
#define COLOR_BW_CODE               @"20"               //黑白
#define COLOR_BWR_CODE              @"30"               //黑白红
#define COLOR_BWY_CODE              @"31"               //黑白黄
#define COLOR_BWRY_CODE             @"40"               //黑白红黄
#define COLOR_BWRYGB_CODE           @"50"               //黑白红黄绿蓝

/**
 @brief 设备支持的颜色
 */
typedef NS_ENUM(NSUInteger, XTDeviceColorType) {
    BLACK_WHITE                 = 0,        /* 黑白 */
    BLACK_WHITE_YELLOW          = 1,        /* 黑白黄 */
    BLACK_WHITE_RED             = 2,        /* 黑白黄 */
    BLACK_WHITE_RED_YELLOW      = 3,        /* 黑白红黄 */
    N_COLOR                     = 4,        /* 黑白红黄蓝绿橙 7色 */
    E6                          = 5,        /* 黑白红黄蓝绿 6色的，231120要求修改 *///
    NONE                        = 6,
};

/**
 @brief 图像扫描模式
 */
typedef NS_ENUM(NSUInteger, ScanType) {
    HorizontalType          = 0,        //水平扫描
    VerticalType            = 1         //垂直扫描
};

/**
 @brief 图像颜色类型
 */
typedef NS_ENUM(NSUInteger, ColorType) {
    BlackColor              = 0,        //黑色
    WhiteColor              = 1,        //白色
    RedColor                = 2,        //红色
    YellowColor             = 3         //黄色
};

/**
 @brief 图像合成模式
 */
typedef NS_ENUM(NSUInteger, CombineType) {
    SingleType              = 0,        //单图合成模式
    ComplexType             = 1,        //双图合成模式
    Color4Type              = 2,        //旧四色模式
    GeneralType             = 3         //三色以上通用模式
};

/**
 @brief 图像刷图模式
 */
typedef NS_ENUM(NSUInteger, XTBrushType) {
    brushWithNfcAndBle      = 0,        //nfc供电+蓝牙刷图
    brushWithNfc            = 1,        //nfc供电 + nfc刷图
    brushWithBle            = 2,        //蓝牙刷图
};

/// 手机壳设备操作错误码
typedef NS_ENUM(NSInteger, PhoneCaseErrorCode) {
    PhoneCaseSuccess = 0,              ///< 成功
    PhoneCaseInvalidParam = 1,         ///< 无效参数
    PhoneCaseFlashWriteFailed = 2,     ///< 写Flash失败
    PhoneCaseFlashProtected = 3,       ///< Flash被保护
    PhoneCaseKeyNotFound = 4,          ///< 未找到密钥
    PhoneCasePasswordOpFailed = 5,     ///< 密码操作失败
    PhoneCaseInvalidSignature = 6,     ///< 无效签名
    PhoneCaseOperationInvalid = 7,     ///< 操作无效
    PhoneCaseKeyAlreadyExists = 8      ///< 密钥已存在（修正重复的6，改为8）
};

#endif /* XTEnum_h */
