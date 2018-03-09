//
//  MasImage.h
//  MaxstAR
//
//  Created by Kimseunglee on 2017. 11. 23..
//  Copyright © 2017년 이상훈. All rights reserved.
//
#import <Foundation/Foundation.h>

/**
 * @brief image data which is used for tracker and rendering
 */
@interface MasTrackedImage : NSObject

/**
 * @enum MasColorFormat
 * @brief Pixel Format
 * @constant RGB888
 * @constant YUV420sp
 * @constant YUV420
 * @constant YUV420_888
 * @constant GRAY8
 */
typedef NS_ENUM(int, MasColorFormat) {
    RGB888 = 1,
    YUV420sp = 2,
    YUV420 = 3,
    YUV420_888 = 4,
    GRAY8 = 5
};

- (instancetype)init:(void*)image;

/**
 * @brief Get image width
 */
- (int) getWidth;

/**
 * @brief Get image height
 */
- (int) getHeight;

/**
 * @brief Get image length
 */
- (int) getLength;

/**
 * @brief Get image format
 */
- (MasColorFormat) getForamt;

/**
 * @brief Get image data
 */
- (const unsigned char *) getData;
@end
