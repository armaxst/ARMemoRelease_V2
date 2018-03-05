//
//  MasCameraDevice.h
//  MaxstAR
//
//  Created by Kimseunglee on 2017. 11. 23..
//  Copyright © 2017년 Maxst. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MasTrackedImage.h"
#import <simd/SIMD.h>

/**
 * @brief class for camera device handling
 */
@interface MasCameraDevice : NSObject

/**
 * @enum MasFocusMode
 * @brief Camera focus mode
 * @constant FOCUS_MODE_CONTINUOUS_AUTO Continuous focus mode. This focus mode is proper for AR
 * @constant FOCUS_MODE_AUTO Scingle auto focus mode
 */
typedef NS_ENUM(int, MasFocusMode) {
    FOCUS_MODE_CONTINUOUS_AUTO = 1,
    FOCUS_MODE_AUTO = 2,
};

/**
 * @enum MasFlipDirection
 * @brief Video data flip direction
 * @constant HORIZONTAL Flip video horizontally
 * @constant VERTICAL Flip video vertically
 */
typedef NS_ENUM(int, MasFlipDirection) {
    HORIZONTAL = 0,
    VERTICAL = 1,
};

/**
 * @enum MasResultCode
 * @brief Camera Open State
 * @constant Success
 * @constant CameraPermissionIsNotResolved
 * @constant CameraDevicedRestriced
 * @constant CameraPermissionIsNotGranted
 * @constant CameraAlreadyOpened
 * @constant TrackerAlreadyStarted
 * @constant UnknownError
 */
typedef NS_ENUM(int, MasResultCode) {
    Success = 0,
    
    CameraPermissionIsNotResolved = 100,
    CameraDevicedRestriced = 101,
    CameraPermissionIsNotGranted = 102,
    CameraAlreadyOpened = 103,
    
    TrackerAlreadyStarted = 200,
    
    UnknownError = 1000,
};

/**
 * @brief Start camera preview
 * @param cameraId 0 is rear camera, 1 is face camera. camera index may depends on device.
 * @param width prefer camera width
 * @param height prefer camera height
 * @return MasResultCode
 */
- (MasResultCode) start:(int) cameraId width:(int) width height:(int) height;

/**
 * @brief Stop camera preview
 */
- (void) stop;

/**
 * @return camera preview width
 */
- (int) getWidth;

/**
 * @return camera preview height
 */
- (int) getHeight;

/**
 * @return true if focus setting success
 */
- (bool) setFocusMode:(MasFocusMode) mode;

/**
 * @brief Turn on/off flash light
 */
- (bool) setFlashLightMode:(bool) toggle;

/**
 * @brief Turn on/off auto white balance lock
 */
- (bool) setAutoWhiteBalanceLock:(bool) toggle;

/**
 * @brief Flip video
 * @param direction Flip direction
 * @param toggle true for set, false for reset
 */
- (void) flipVideo:(MasFlipDirection) direction toggle:(bool) toggle;

/**
 * @brief Get supported parameter key list
 * @return Parameter key list
 */
- (NSMutableArray*) getParamList;

/**
 * @brief Set camera parameter  (Android only supported now)
 * @param key Parameter key
 * @param toggle Parameter value
 * @return True if setting success
 */
- (bool) setParam:(NSString*) key toggle:(bool) toggle;

/**
 * @brief Set camera parameter (Android only supported now)
 * @param key Parameter key
 * @param value Parameter value
 * @return True if setting success
 */
- (bool) setParam:(NSString*) key value:(int) value;

/**
 * @brief Set camera parameter (Android only supported now)
 * @param key Parameter key
 * @param min Parameter min value
 * @param max Parameter max value
 * @return True if setting success
 */
- (bool) setParam:(NSString*) key min:(int) min max:(int) max;

/**
 * @brief Set camera parameter (Android only supported now)
 * @param key Parameter key
 * @param value Parameter value
 * @return True if setting success
 */
- (bool) setParam:(NSString*) key valueString:(NSString*) value;

/**
 * @brief Set new image data for tracking and background rendering (Only enterprise license key can activate this interface)
 * @param data image data bytes.
 * @param length image length
 * @param width image width
 * @param height image height
 * @param format image format
 */
- (void) setNewFrame:(Byte *) data length:(int) length width:(int) width height:(int) height format:(MasColorFormat) format;

/**
 * @brief Get projection matrix. This is used for augmented objects projection and background rendering
 * @return 4x4 matrix (Column major)
 */
- (matrix_float4x4) getProjectionMatrix;

/**
 * @brief Get projection matrix for background plane rendering
 * @return 4x4 matrix (Column major)
 */
- (matrix_float4x4) getBackgroundPlaneProjectionMatrix;
@end
