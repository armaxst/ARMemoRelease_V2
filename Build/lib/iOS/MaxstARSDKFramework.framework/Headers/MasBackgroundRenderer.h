//
//  MasBackgroundRenderer.h
//  MaxstAR
//
//  Created by Kimseunglee on 2017. 11. 23..
//  Copyright © 2017년 Maxst. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MasBackgroundTexture.h"

/**
 * @brief Do background rendering. Background rendering includes camera image,
 * feature point, slam initialization progress bar, watermark, surface mesh.
 */

@interface MasBackgroundRenderer : NSObject

/**
 * @enum MasRenderingOption
 * @brief Additional rendering option. Slam feature point, Surface mesh, etc.
 * @constant FEATURE_RENDERER Feature point rendering.(Object Tracker and Visual SLAM)
 * @constant PROGRESS_RENDERER Creating slam map progress rendering.(Visual SLAM only)
 * @constant AXIS_RENDERER Slam initial pose coordinate rendering.(Code Tracker is not supported)
 * @constant SURFACE_MESH_RENDERER Slam surface mesh rendering.(Object Tracker and Visual SLAM)
 * @constant VIEW_FINDER_RENDERER Scan view finder rendering.(Code Tracker Only)
 */
typedef NS_OPTIONS(NSUInteger, MasRenderingOption)
{
    FEATURE_RENDERER   = 0x01,
    PROGRESS_RENDERER    = 0x02,
    AXIS_RENDERER     = 0x04,
    SURFACE_MESH_RENDERER  = 0x08,
    VIEW_FINDER_RENDERER   = 0X10,
};

/**
 * @brief Set background rendering option. Should be called after initRendering
 * @param option Option can be multiple (FEATURE_RENDERER | SURFACE_MESH_RENDERER)
 */
- (void) setRenderingOption:(MasRenderingOption) option;

/**
 * @brief Set view frustrum's near and far clipping plane distance.
 * @param nearClipPlane near clipping plane distance
 * @param farClipPlane far end of camera frustum over which background is rendered
 */
- (void) setClippingPlane:(float) nearClipPlane Far:(float) farClipPlane;

/**
 * @brief Get texture for background rendering
 * @return MasBackgroundTexture instance if texture created
 */
- (MasBackgroundTexture *) getBackgroundTexture;

/**
 * @brief Prepare background rendering to BackgroundTexture
 * @param texture get getBackgroundTexture Method
 */
- (void) begin:(MasBackgroundTexture*)texture;

/**
 * @brief Render background to BackgroundTexture. Background can include camera image, feature point, etc.
 */
- (void) renderBackgroundToTexture;

/**
 * @brief End background rendering to BackgroundTexture
 */
- (void) end;

@end
