//
//  MasMatrixUtil.h
//  MaxstAR
//
//  Created by Kimseunglee on 2017. 12. 7..
//  Copyright © 2017년 Maxst. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <simd/SIMD.h>

@interface MasMatrixUtil : NSObject
+ (matrix_float4x4) makeMatrix:(float*) data;
+ (matrix_float4x4) translation:(float)x y:(float)y z:(float)z;
+ (matrix_float4x4) translate:(float)x positionY:(float)y positionZ:(float)z matrix:(matrix_float4x4)matrix;
+ (matrix_float4x4) rotation:(float)x y:(float)y z:(float)z;
+ (matrix_float4x4) rotate:(float)x y:(float)y z:(float)z matrix:(matrix_float4x4)matrix;
+ (matrix_float4x4) scale:(float)x y:(float)y z:(float)z;
+ (const float *) floatArrayForMatrix:(matrix_float4x4)matrix;
@end
