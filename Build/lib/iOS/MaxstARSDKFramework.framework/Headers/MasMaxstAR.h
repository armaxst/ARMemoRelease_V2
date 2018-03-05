//
//  MTMaxstAR.h
//  MTMaxstAR
//
//  Created by Kimseunglee on 2017. 12. 7..
//  Copyright © 2017년 Maxst. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface MasMaxstAR : NSObject

typedef NS_ENUM(int, MasScreenOrientation) {
    UNKNOWN = 0,
    PORTRAIT = 1,
    PORTRAIT_UP = 1,
    PORTRAIT_DOWN = 2,
    LANDSCAPE = 3,
    LANDSCAPE_LEFT = 3,
    LANDSCAPE_RIGHT = 4
};

/**
 * @brief Initialize AR engine
 * @param appKey app key for this app generated from "developer.maxst.com" (Mobile only)
 */
+ (void) init:(NSString*) appKey;

/**
 * @brief Deinitialize AR Engine
 */
+ (void) deinit;

/**
 * @brief Check AR engine has been initialized.
 */
+ (bool) isInitialized;

/**
 * @brief Called when surface created on rendering thread
 */
+ (void) onSurfaceCreated;

/**
 * @brief  Called when rendering surface's size changed (i.e. orientation change, resizing rendering surface)
 * @param viewWidth width size (pixel unit)
 * @param viewHeight height size (pixel unit)
 */
+ (void) onSurfaceChanged:(int)viewWidth height:(int)viewHeight;

/**
 * @brief  Called when rendering surface about to destroyed
 */
+ (void) onSurfaceDestroyed;

/**
 * @brief Set device orientation
 * @param orientation Screen orientation.
 * 2 is Configuration.ORIENTATION_LANDSCAPE, 1 is Configuration.ORIENTATION_PORTRAIT
 */
+ (void) setScreenOrientation:(MasScreenOrientation)orientation;
@end
