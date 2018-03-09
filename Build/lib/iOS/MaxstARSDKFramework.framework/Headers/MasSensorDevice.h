//
//  MasSensorDevice.h
//  MaxstARSDKFramework
//
//  Created by Kimseunglee on 2017. 12. 8..
//  Copyright © 2017년 Maxst. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 * @brief Control device sensor
 */
@interface MasSensorDevice : NSObject

/**
 * @brief Start device sensor
 */
- (void) start;

/**
 * @brief Stop device sensor
 */
- (void) stop;

/**
 * @brief Set new sensor data
 * @param data rotation data float[9]
 */
- (void) setNewSensorData:(float*)data;
@end
