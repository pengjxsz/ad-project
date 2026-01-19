//
//  UIView+XTImage.m
//  NewTool
//
//  Created by XTMacMini on 2025/2/17.

#import "UIView+XTImage.h"

@implementation UIView (SLImage)

// View 转 Image
- (UIImage *)xt_imageByViewInRect:(CGRect)rangeRect {
    CGRect newRect = self.bounds;
    /** 参数取整，否则可能会出现1像素偏差 */
    /** 有小数部分才调整差值 */
#define lfme_export_fixDecimal(d) ((fmod(d, (int)d)) > 0.59f ? ((int)(d+0.5)*1.f) : (((fmod(d, (int)d)) < 0.59f && (fmod(d, (int)d)) > 0.1f) ? ((int)(d)*1.f+0.5f) : (int)(d)*1.f))
    newRect.origin.x = lfme_export_fixDecimal(newRect.origin.x);
    newRect.origin.y = lfme_export_fixDecimal(newRect.origin.y);
    newRect.size.width = lfme_export_fixDecimal(newRect.size.width);
    newRect.size.height = lfme_export_fixDecimal(newRect.size.height);
#undef lfme_export_fixDecimal
    CGSize size = newRect.size;
    //1.开启上下文
    UIGraphicsBeginImageContextWithOptions(size, NO, [UIScreen mainScreen].scale);
    CGContextRef context = UIGraphicsGetCurrentContext();
    if (!context) {
        UIGraphicsEndImageContext();
        NSLog(@"Failed to create graphics context.");
        return nil;
    }
    //2.绘制图层
    [self.layer renderInContext:context];
    //3.从上下文中获取新图片
    UIImage *fullScreenImage = UIGraphicsGetImageFromCurrentImageContext();
    //4.关闭图形上下文
    UIGraphicsEndImageContext();
    
    if (CGRectEqualToRect(newRect, rangeRect)) {
        return fullScreenImage;
    }
    
    //上面我们获得了一个全屏的截图，下边的方法是对这个图片进行裁剪。
    CGImageRef imageRef = fullScreenImage.CGImage;
    //注意：这里的宽/高 CGImageGetWidth(imageRef) 是图片的像素宽/高，所以计算截图区域时需要按比例来 * [UIScreen mainScreen].scale；
    rangeRect = CGRectMake(rangeRect.origin.x*[UIScreen mainScreen].scale, rangeRect.origin.y*[UIScreen mainScreen].scale, rangeRect.size.width*[UIScreen mainScreen].scale, rangeRect.size.height*[UIScreen mainScreen].scale-1);
    CGImageRef imageRefRect = CGImageCreateWithImageInRect(imageRef, rangeRect);
    if (!imageRefRect) {
        NSLog(@"Failed to create image from rect.");
        return nil;
    }
    UIImage *image = [[UIImage alloc] initWithCGImage:imageRefRect];
    CGImageRelease(imageRefRect);
    return image;
}

- (UIImage *)scaledImageFormImage:(UIImage *)image toSize:(CGSize)size {
    UIGraphicsBeginImageContext(size);
    [image drawInRect:CGRectMake(0,0, size.width, size.height)];
    UIImage *getImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return getImage;
}

@end
