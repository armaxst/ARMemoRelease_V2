//
//  MasSurfaceThumbnail.h
//  MaxstARSDKFramework
//
//  Created by Kimseunglee on 2017. 12. 10..
//  Copyright © 2017년 Maxst. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MasTrackedImage.h"

/**
 * @brief Contains surface thumbnail image information of first keyframe
 */
@interface MasSurfaceThumbnail : NSObject

- (instancetype)init:(void*)surfaceThumbnail;

/**
 * @return image width
 */
- (int) getWidth;

/**
 * @return image height
 */
- (int) getHeight;

/**
 * @return image bytes per pixel
 */
- (int) getBpp;

/**
 * @return image color format
 */
- (MasColorFormat) getFormat;

/**
 * @return image data length
 */
- (int) getLength;

/**
 * @return thumbnail image data pointer
 */
- (unsigned char*) getData;

@end
