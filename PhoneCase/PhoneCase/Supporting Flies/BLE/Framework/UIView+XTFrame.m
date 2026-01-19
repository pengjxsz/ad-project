//
//  UIView+XTFrame.m
//  NewTool
//
//  Created by XTMacMini on 2025/2/17.
//

#import "UIView+XTFrame.h"

@implementation UIView (XTFrame)

- (void)setXt_x:(CGFloat)xt_x {
    CGRect frame = self.frame;
    frame.origin.x = xt_x;
    self.frame = frame;
}
- (CGFloat)xt_x {
    return self.frame.origin.x;
}

- (void)setXt_y:(CGFloat)xt_y {
    CGRect frame = self.frame;
    frame.origin.y = xt_y;
    self.frame = frame;
}
- (CGFloat)xt_y {
    return self.frame.origin.y;
}

- (void)setXt_width:(CGFloat)xt_w {
    CGRect frame = self.frame;
    frame.size.width = xt_w;
    self.frame = frame;
}
- (CGFloat)xt_width {
    return self.frame.size.width;
}

- (void)setXt_height:(CGFloat)xt_h {
    CGRect frame = self.frame;
    frame.size.height = xt_h;
    self.frame = frame;
}
- (CGFloat)xt_height {
    return self.frame.size.height;
}

- (void)setXt_size:(CGSize)xt_size {
    CGRect frame = self.frame;
    frame.size = xt_size;
    self.frame = frame;
}
- (CGSize)xt_size {
    return self.frame.size;
}

- (void)setXt_centerX:(CGFloat)xt_centerX {
    CGPoint center = self.center;
    center.x = xt_centerX;
    self.center = center;
}
- (CGFloat)xt_centerX {
    return self.center.x;
}

- (void)setXt_centerY:(CGFloat)xt_centerY {
    CGPoint center = self.center;
    center.y = xt_centerY;
    self.center = center;
}
- (CGFloat)xt_centerY {
    return self.center.y;
}

- (void)setXt_origin:(CGPoint)xt_origin {
    CGRect frame = self.frame;
    frame.origin = xt_origin;
    self.frame = frame;
}
- (CGPoint)xt_origin {
    return self.frame.origin;
}

- (CGFloat)xt_left {
    return self.frame.origin.x;
}
- (void)setXt_left:(CGFloat)left {
    CGRect frame = self.frame;
    frame.origin.x = left;
    self.frame = frame;
}

- (CGFloat)xt_right {
    return CGRectGetMaxX(self.frame);
}

-(void)setXt_right:(CGFloat)right {
    CGRect frame = self.frame;
    frame.origin.x = right - frame.size.width;
    self.frame = frame;
}

- (CGFloat)xt_top {
    return self.frame.origin.y;
}

- (void)setXt_top:(CGFloat)top {
    CGRect frame = self.frame;
    frame.origin.y = top;
    self.frame = frame;
}

- (CGFloat)xt_bottom {
    return CGRectGetMaxY(self.frame);
}

- (void)setXt_bottom:(CGFloat)bottom {
    CGRect frame = self.frame;
    frame.origin.y = bottom - frame.size.height;
    self.frame = frame;
}


@end
