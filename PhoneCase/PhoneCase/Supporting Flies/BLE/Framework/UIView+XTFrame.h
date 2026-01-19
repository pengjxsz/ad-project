//
//  UIView+XTFrame.h
//  NewTool
//
//  Created by XTMacMini on 2025/2/17.
//
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UIView (XTFrame)
@property (nonatomic, assign ) CGFloat xt_x;
@property (nonatomic, assign ) CGFloat xt_y;
@property (nonatomic, assign ) CGFloat xt_width;
@property (nonatomic, assign ) CGFloat xt_height;
@property (nonatomic, assign ) CGFloat xt_centerX;
@property (nonatomic, assign ) CGFloat xt_centerY;

@property (nonatomic, assign ) CGSize  xt_size;
@property (nonatomic, assign ) CGPoint xt_origin;

@property (nonatomic, assign) CGFloat  xt_left;
@property (nonatomic, assign) CGFloat  xt_right;
@property (nonatomic, assign) CGFloat  xt_top;
@property (nonatomic, assign) CGFloat  xt_bottom;

@end

NS_ASSUME_NONNULL_END
