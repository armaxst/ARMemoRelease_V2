//
//  MasTrackingState.h
//  MaxstARSDKFramework
//
//  Created by Kimseunglee on 2017. 12. 8..
//  Copyright © 2017년 Maxst. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MasTrackedImage.h"
#import "MasTrackingResult.h"

/**
 * @brief Tracking state container
 */
@interface MasTrackingState : NSObject

- (instancetype)init:(void*)trackingState;

/**
 * @brief Get image used for tracking
 * @return MasTrackedImage
 */
- (MasTrackedImage*) getImage;

/**
 * @brief Get tracking result
 * @return MasTrackingResult result
 */
- (MasTrackingResult*) getTrackingResult;

/**
 * @brief Get QRCode / Barcode recognition result
 * @return QRCode or barcode text
 */
- (NSString*) getCodeScanResult;
@end
