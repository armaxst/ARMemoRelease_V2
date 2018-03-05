//
//  MasTrackerManager.h
//  MaxstAR
//
//  Created by Kimseunglee on 2017. 12. 7..
//  Copyright © 2017년 Maxst. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MasTrackingState.h"
#import "MasTrackingResult.h"
#import "MasSurfaceThumbnail.h"
#import "MasSurfaceMesh.h"

/**
 * @brief Control AR Engine
 */
@interface MasTrackerManager : NSObject

/**
 * @enum TrackerType
 * @brief Tracker Type
 * @constant TRACKER_TYPE_CODE_SCANNER Code scanner
 * @constant TRACKER_TYPE_IMAGE Planar image tracker
 * @constant TRACKER_TYPE_OBJECT Object tracker (Object data should be created via SLAM tracker)
 * @constant TRACKER_TYPE_SLAM Visual slam tracker (Can create surface data and save it)
 * @constant TRACKER_TYPE_INSTANT Instant tracker
 */
typedef NS_ENUM(int, TrackerType) {
    TRACKER_TYPE_CODE_SCANNER = 0x01,
    TRACKER_TYPE_IMAGE = 0x02,
    TRACKER_TYPE_OBJECT = 0X08,
    TRACKER_TYPE_SLAM = 0x10,
    TRACKER_TYPE_INSTANT = 0x20,
};

/**
 * @enum TrackingOption
 * @brief Additional tracking option.
 * @constant NORMAL_TRACKING Normal Tracking (Image Tracker Only)
 * @constant EXTENDED_TRACKING Extended Tracking (Image Tracker Only)
 * @constant MULTI_TRACKING Multi Target Tracking (Image Tracker Only)
 */
typedef NS_ENUM(int, TrackingOption) {
    NORMAL_TRACKING = 0x01,
    EXTENDED_TRACKING = 0x02,
    MULTI_TRACKING = 0x04,
};

/**
 * @brief Start AR engine. Only one tracking engine could be run at one time
 * @param trackerMask tracking engine type
 */
- (void) startTracker:(TrackerType)trackerMask;

/**
 * @brief Stop tracking engine
 */
- (void) stopTracker;

/**
 * @brief Remove all tracking data (Map data and tracking result)
 */
- (void) destroyTracker;

/**
 * @brief Add map file to candidate list.
 * @param trackingFileName absolute file path
 */
- (void) addTrackerData:(NSString*)trackingFileName;

/**
 * @brief Remove map file from candidate list.
 * @param trackingFileName map file name. This name should be same which added.
 * If set "" (empty) file list will be cleared.
 */
- (void) removeTrackerData:(NSString*)trackingFileName;

/**
 * @brief Load map files in candidate list to memory. This method don't block main(UI) thread
 */
- (void) loadTrackerData;

/**
 * @brief Get map files loading state. This is for UI expression.
 * @return true if map loading is completed
 */
- (bool) isTrackerDataLoadCompleted;

/**
 * @briedf Update tracking state. This function should be called before getTrackingResult and background rendering
 * @return Tracking state container
 */
- (MasTrackingState*) updateTrackingState;

/**
 * @brief Start to find the surface of an environment from a camera image
 */
- (void) findSurface;

/**
 * @brief Stop to find the surface
 */
- (void) quitFindingSurface;

/**
 * @brief Save the surface data to file
 * @param outputFileName file path (should be absolute path)
 * @return MasSurfaceThumbnail instance if true else null
 */
- (MasSurfaceThumbnail*) saveSurfaceData:(NSString*)outputFileName;

/**
 * @brief Get 3d world coordinate corresponding to given 2d screen position
 * @param screen screen touch x, y position
 * @param world world position x, y, z
 */
- (void) getWorldPositionFromScreenCoordinate:(float*)screen world:(float*)world;

/**
 * @brief Get the number of keyframes included in surface data
 * @return key frame count
 */
- (int) getKeyframeCount;

/**
 * @brief Get the number of features included in surface data
 * @return feature point count
 */
- (int) getFeatureCount;

/**
 * @brief Get surface mesh information of the found surface after the findSurface method has been called
 * @return MasSurfaceMesh instance
 */
- (MasSurfaceMesh*) getSurfaceMesh;

/**
 * @brief Set tracking options. 1, 2, 4 cannot run simultaneously.
 * @param option
 *        1 : Normal Tracking (Image Tracker Only)
 *        2 : Extended Tracking (Image Tracker Only)
 *        4 : Multiple Target Tracking (Image Tracker Only)
 */
- (void) setTrackingOption:(TrackingOption)option;
- (void) saveFrames;

@end

