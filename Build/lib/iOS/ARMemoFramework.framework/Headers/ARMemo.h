//
//  ARMemoWrapper.h
//  ARMemoWrapper
//
//  Created by 이상훈 on 2018. 2. 13..
//  Copyright © 2018년 Ray. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface ARMemo : NSObject

+ (int) initialize : (NSString *) appSignature;
+ (int) destory;

+ (int) startTracking;
+ (int) stopTracking;

+ (int) checkLearnable : (Byte *) image length:(int) length width:(int) width height:(int) height format:(int) pixelFormat;
+ (int) learn : (Byte *) image length:(int) length width:(int) width height:(int) height format:(int) pixelFormat strokeInfo : (NSArray *) stroke size : (int) size;
+ (int) saveLearnedFile : (NSString *) fileFullPath;
+ (int) clearLearnedTrackable;

+ (int) inputTrackingImage:(Byte *)image length:(int)length width:(int)width height:(int)height format:(int)pixelFormat;
+ (int) setTrackingFile : (NSString *) fileFullPath;
+ (int) getTrackingResult : (float *) transformMatrix3x3;
+ (int) clearTrackingTrackable;

@end
