#import "XTImageDitherUtils.h"
#import "BleProtocol.h"

@interface XTImageDitherUtils()

@end

@implementation XTImageDitherUtils

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
                       algType:(NSString *)algType {
    if(!inputImage||!deviceCfg){
        return nil;
    }
    
    // 打印所有输入参数
    NSLog(@"=== convertToUploadData 参数开始 ===");
    
    // 打印 inputImage 信息
    if (inputImage) {
        NSLog(@"inputImage: 存在，尺寸: %@", NSStringFromCGSize(inputImage.size));
    } else {
        NSLog(@"inputImage: 为空");
    }
    
    // 打印 previewImage 指针地址（输出参数）
    NSLog(@"previewImage 指针地址: %p", previewImage);
    
    // 打印 deviceCfg 及其属性
    if (deviceCfg) {
        NSLog(@"deviceCfg: 存在");
        NSLog(@"  colorDic: %@", deviceCfg.colorDic);
        NSLog(@"  brushType: %d", (int)deviceCfg.brushType);
        NSLog(@"  scanType: %d", (int)deviceCfg.scanType);
        NSLog(@"  combineType: %d", (int)deviceCfg.combineType);
        NSLog(@"  writeImageCount: %d", (int)deviceCfg.writeImageCount);
        NSLog(@"  flipHorizontal: %@", deviceCfg.flipHorizontal ? @"YES" : @"NO");
        NSLog(@"  flipVertical: %@", deviceCfg.flipVertical ? @"YES" : @"NO");
        NSLog(@"  screenColorCode: %@", deviceCfg.screenColorCode);
    } else {
        NSLog(@"deviceCfg: 为空");
    }
    
    // 打印 algType
    NSLog(@"algType: %@", algType ?: @"空");
    
    NSLog(@"=== convertToUploadData 参数结束 ===");
    
    return [BleProtocol convertToUploadData:inputImage
                               previewImage:previewImage
                                   colorDic:deviceCfg.colorDic
                                  brushType:(int)deviceCfg.brushType
                                   scanType:(int)deviceCfg.scanType
                                combineType:(int)deviceCfg.combineType
                            writeImageCount:(int)deviceCfg.writeImageCount
                             flipHorizontal:deviceCfg.flipHorizontal
                               flipVertical:deviceCfg.flipVertical
                            screenColorCode:deviceCfg.screenColorCode
                                    algType:algType];;
}


/**
 @brief  算法图片 生成刷图数据
 @param inputImage             算法生成的图片
 @param deviceCfg               电子纸设备配置信息
 @return  刷图数据
 */
+ (NSMutableArray *)getUploadDataFromDitherImage:(UIImage *)inputImage
                                       deviceCfg:(XTDeviceConfig *)deviceCfg {
    CGImageRef imageRef = inputImage.CGImage;
    int width  = (int)CGImageGetWidth(imageRef);
    int height = (int)CGImageGetHeight(imageRef);
    CFDataRef effectedData = nil;
    CFDataRef data = nil;
    
    //每像素多少位
    size_t bitsPerPixel = CGImageGetBitsPerPixel(imageRef);
    //每像素多少字节
    size_t bytesPerPixel = bitsPerPixel / 8;
    //每行多少字节
    size_t bytesPerRow = CGImageGetBytesPerRow(imageRef);
    CGDataProviderRef dataProvider = CGImageGetDataProvider(imageRef);
    data = CGDataProviderCopyData(dataProvider);
    UInt8 *buffer = (UInt8*)CFDataGetBytePtr(data);
    effectedData = CFDataCreate(NULL, buffer, CFDataGetLength(data));
    UInt8 *effectedBuffer = (UInt8*)CFDataGetBytePtr(effectedData);
    UInt8 *pixelAddr;
    UInt8 red,green,blue;
    NSMutableArray *array = [NSMutableArray array];
    if (deviceCfg.scanType == VerticalType) {
        for(int y = width - 1; y >= 0; y--){
            NSMutableString *strLine = [NSMutableString string];
            for(int x = 0; x < height; x++){
                //取像素点
                pixelAddr = effectedBuffer + x * bytesPerRow + y * bytesPerPixel;
                blue = *(pixelAddr + 0);
                green = *(pixelAddr + 1);
                red = *(pixelAddr + 2);
                [strLine appendString: [XTImageDitherUtils getPiexsLineWithColorCode:deviceCfg.screenColorCode red:red green:green blue:blue]];
            }
            [array addObject:strLine];
        }
    }
    else {
        for(int y = 0; y < height; y++){
            NSMutableString *strLine = [NSMutableString string];
            for(int x=0; x<width; x++){
                //取像素点
                pixelAddr = effectedBuffer + y * bytesPerRow + x * bytesPerPixel;
                blue = *(pixelAddr + 0);
                green = *(pixelAddr + 1);
                red = *(pixelAddr + 2);
                [strLine appendString: [XTImageDitherUtils getPiexsLineWithColorCode:deviceCfg.screenColorCode red:red green:green blue:blue]];
            }
            [array addObject:strLine];
        }
    }
    return array;
}

+ (NSString *)getPiexsLineWithColorCode:(NSString *)colorCode red:(NSInteger)red green:(NSInteger)green blue:(NSInteger)blue {
    NSString *strLine = [NSString new];
    if([colorCode isEqualToString:COLOR_BWRYGB_CODE]) {// 6 color 黑白红黄绿蓝
        if (red == 255 && green == 255 && blue == 255)
            strLine = [NSString stringWithFormat:@"%@%@", strLine, @"1"];
        else if (red == 255 && green == 0 && blue == 0)
            strLine = [NSString stringWithFormat:@"%@%@", strLine, @"3"];
        else if (red == 255 && green == 255 && blue == 0)
            strLine = [NSString stringWithFormat:@"%@%@", strLine, @"2"];
        else if (red == 0 && green == 255 && blue == 0)
            strLine = [NSString stringWithFormat:@"%@%@", strLine, @"6"];
        else if (red == 0 && green == 0 && blue == 255)
            strLine = [NSString stringWithFormat:@"%@%@", strLine, @"5"];
        else
            strLine = [NSString stringWithFormat:@"%@%@", strLine, @"0"];
    } else if([colorCode isEqualToString:COLOR_BWRY_CODE]) {// 4color 黑白红黄
        if (red == 255 && green == 255 && blue == 255)
            strLine = [NSString stringWithFormat:@"%@%@", strLine, @"1"];
        else if (red == 255 && green == 0 && blue == 0)
            strLine = [NSString stringWithFormat:@"%@%@", strLine, @"3"];
        else if (red == 255 && green == 255 && blue == 0)
            strLine = [NSString stringWithFormat:@"%@%@", strLine, @"2"];
        else
            strLine = [NSString stringWithFormat:@"%@%@", strLine, @"0"];
    } else if([colorCode isEqualToString:COLOR_BWR_CODE]){ // 3color 黑白红
        if (red == 255 && green == 255 && blue == 255)
            strLine = [NSString stringWithFormat:@"%@%@", strLine, @"1"];
        else if (red == 255 && green == 0 && blue == 0)
            strLine = [NSString stringWithFormat:@"%@%@", strLine, @"3"];
        else
            strLine = [NSString stringWithFormat:@"%@%@", strLine, @"0"];
    } else if([colorCode isEqualToString:COLOR_BWY_CODE]){ // 3color 黑白黄
        if (red == 255 && green == 255 && blue == 255)
            strLine = [NSString stringWithFormat:@"%@%@", strLine, @"1"];
        else if (red == 255 && green == 255 && blue == 0)
            strLine = [NSString stringWithFormat:@"%@%@", strLine, @"2"];
        else
            strLine = [NSString stringWithFormat:@"%@%@", strLine, @"0"];
    } else if([colorCode isEqualToString:COLOR_BW_CODE]){ // 2color 黑白
        if (red == 255 && green == 255 && blue == 255)
            strLine = [NSString stringWithFormat:@"%@%@", strLine, @"1"];
        else
            strLine = [NSString stringWithFormat:@"%@%@", strLine, @"0"];
    }
    return strLine;
}

@end
