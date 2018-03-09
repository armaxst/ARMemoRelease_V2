//
//  MasShaderUtil.h
//  MaxstAR
//
//  Created by Kimseunglee on 2017. 11. 24..
//  Copyright © 2017년 Maxst. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 * @brief Shader compile utility
 */
@interface MasShaderUtil : NSObject
+ (unsigned int) createProgram:(NSString*)vertexString fragment:(NSString*)fragmentString;
+ (unsigned int) loadShader:(unsigned int)shaderType source:(NSString*)pSource;
+ (void) checkGlError:(NSString*)op;
@end
