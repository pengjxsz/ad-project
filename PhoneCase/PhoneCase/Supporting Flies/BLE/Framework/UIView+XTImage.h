//
//  UIView+XTImage.h
//  NewTool
//
//  Created by XTMacMini on 2025/2/17.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

/// 视图转换为Image
@interface UIView (XTImage)

/// 截取视图转Image
/// @param rangeRect 截图区域
- (UIImage *)xt_imageByViewInRect:(CGRect)rangeRect;

- (UIImage *)scaledImageFormImage:(UIImage *)image toSize:(CGSize)size;

@end

NS_ASSUME_NONNULL_END
