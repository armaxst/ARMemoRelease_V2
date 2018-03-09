//
//  MasTrackingResult.h
//  MaxstAR
//
//  Created by Kimseunglee on 2017. 12. 7..
//  Copyright © 2017년 Maxst. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MasTrackable.h"

/**
 * @brief Contains tracked targets informations
 */
@interface MasTrackingResult : NSObject

- (instancetype)init:(void*)trackingResult;

/**
 * @brief Get tracking target information
 * @param index target index
 * @return MasTrackable class instance
 */
- (MasTrackable*) getTrackable:(int) index;

/**
 * @brief Get tracking target count. Current version ar engine could not track multi target.
 *  That feature will be implemented not so far future.
 * @return tracking target count
 */
- (int) getCount;
@end
