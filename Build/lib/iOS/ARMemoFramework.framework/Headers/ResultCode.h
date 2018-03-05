//
//  ResultCode.h
//  ARMemoWrapper
//
//  Created by 이상훈 on 2018. 2. 13..
//  Copyright © 2018년 Ray. All rights reserved.
//

typedef NS_ENUM(int, ResultCode) {
    SUCCESS = 0,
    FAIL = 1,
    
    MEMORY_ALLOCATION_ERROR = 2,
    
    INVALID_APP = 10,
    TRACKABLE_ALREADY_EXIST = 50,
    TRACKABLE_IS_NOT_EXIST = 51,
    PIXEL_FORMAT_ERROR = 60,
    INPUT_IMAGE_EMPTY = 61,
    INPUT_IMAGE_RESOLUTION_ERROR = 62,
    
    ENGINE_ALREADY_INITIALIZED = 170,
    ENGINE_IS_NOT_INITIALIZED = 180,
    
    LEARN_STROKE_EMPTY = 200,
    LEARN_STROKE_OVERFLOW = 201,
    
    TRACKER_ALREADY_STARTED = 300,
    TRACKER_IS_NOT_STARTED = 301,
    TRACKER_ALREADY_STOPED = 310,
    
    INPUT_TRACKABLE_EMPTY = 320,
    
    UNDEFINE_ERROR = 99,
};
