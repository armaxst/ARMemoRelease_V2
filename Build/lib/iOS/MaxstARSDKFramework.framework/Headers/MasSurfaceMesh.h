//
//  MasSurfaceMesh.h
//  MaxstARSDKFramework
//
//  Created by Kimseunglee on 2017. 12. 10..
//  Copyright © 2017년 Maxst. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 * @brief Contains surface's mesh data generated from slam tracking
 */
@interface MasSurfaceMesh : NSObject

- (instancetype)init:(void*)surfeceMesh;

/**
 * @brief Get a percentage of progress during an initialization step of SLAM
 * @return Slam initializing progress
 */
- (float) getInitializingProgress;

/**
 * @return surface mesh vertex count
 */
- (int) getVertexCount;

/**
 * @return surface mesh index count
 */
- (int) getIndexCount;

/**
 * @return surface mesh vertex buffer (Always returns same address so vertex count must be considered)
 */
- (float*) getVertexBuffer;

/**
 * @return surface mesh index buffer (Always returns same address so index count must be considered )
 */
- (unsigned short*) getIndexBuffer;

@end
