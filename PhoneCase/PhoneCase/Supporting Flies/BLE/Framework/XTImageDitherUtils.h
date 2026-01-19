
#import <UIKit/UIKit.h>
#import "XTDeviceConfig.h"

@interface XTImageDitherUtils : NSObject

/**
 @brief  根据图片算法生成相应图片
 @param inputImage             转化前的图像
 @param previewImage        转化后的图像
 @param deviceCfg               电子纸设备配置信息
 @param algType                    算法选择
 */
+ (NSData*)convertToUploadData:(UIImage*)inputImage
                  previewImage:(UIImage **)previewImage
                     deviceCfg:(XTDeviceConfig *)deviceCfg
                       algType:(NSString *)algType;

/**
 @brief  将算法生成的图片转换为电子纸刷图数据
 @param ditherImage             算法生成的图片
 @param deviceCfg               电子纸设备配置信息
 @return  刷图数据
 @discussion
 示例：
 ===> 2*3 像素的纯黑色图片 转换出来的数据如下
 [111]
 [111]
 
 ===> 3*2 像素的纯白色图片 转换出来的数据如下
 [000]
 [000]
 [000]
 */
+ (NSMutableArray *)getUploadDataFromDitherImage:(UIImage *)ditherImage
                                       deviceCfg:(XTDeviceConfig *)deviceCfg;

@end
