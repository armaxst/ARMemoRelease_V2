//
//  MasBackgroundTexture.h
//  MaxstAR
//
//  Created by Kimseunglee on 2017. 11. 23..
//  Copyright © 2017년 Maxst. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 * @brief Contains information of background rendering texture.
 */
@interface MasBackgroundTexture : NSObject
- (instancetype) init:(void*)texture;

/**
 * @brief texture id for background rendering
 */
- (unsigned int) getId;

/**
 * @return texture width
 */
- (int) getWidth;

/**
 * @return texture height
 */
- (int) getHeight;
@end
