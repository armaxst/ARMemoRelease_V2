#pragma once

#include "Export.h"
#include "ResultCode.h"

extern "C" {
	/*
	* return code
	* 0 : success
	* 1 : fail(not error)
	*
	* 2 ~ 79 : common error. 
	*		memory, input error etc.
	*	99 : undefine error.
	*
	*	70 : initialize(). already called.
	*	80 : checkLearnable(). pixel format error
	*
	* 100 ~ 199 : learner function error.
	*	100 : learn(). already called Learn(). plase clearLearnedTrackable().
	*	101 : learn(). pixel format error.
	*	102 : learn(). stroke is empty.
	*	103 : learn(). stroke is overflow.
	*	110 : trackable is not exist.
	*	120 : getLearnedTrackableArray(). trackable is not exist.
	*	130 : clearLearnedTrackable(). trackable is not exist.
	*
	* 200 ~ 299 : trainer function error.
	*	200 : startTracking(). already called startTracking().
	*	210 : stopTracking(). already called stopTracking() or startTracking() didn't called.
	*	220 : inputTrackingImage(). pixel format error.
	*	230 : setTrackingTrackableArray(). trackable is already exist.
	*	240 : getTrackingResult(). tracking result is not exist.
	*	250 : clearTrackingTrackable(). trackable is not exist.
	*
	*/


	/*****************************************************************************/
	/*                             Shared function.                              */
	/*****************************************************************************/
	/**
	* @brief Get Engine version.
	* @return Engine version.
	*/
	ARDRAWING_API char* getEngineVersion();

	/**
	* @brief Initialize engine.
	*/
	ARDRAWING_API int initialize();

	/**
	* @brief destroy engine.
	*/
	ARDRAWING_API int destroy();

	/**
	* @brief check image blur for learning.
	* @param img image data buffer
	* @param width image width
	* @param height image height
	* @param pixelFormat image pixel format
	* pixelFormat = 0 : GRAY
	* pixelFormat = 1 : RGB
	* pixelFormat = 2 : YUV
	* @param threshold learnable threshold, Default value is 70.0f.
	* @return True when input image has less blur and fit complexity.\n
	*	      False when input image has blur or less complexity.
	*/
	ARDRAWING_API int checkLearnable(unsigned char *img, int width, int height, int pixelFormat, float threshold);

	/*****************************************************************************/
	/*                             Learner function.                             */
	/*****************************************************************************/
	// learner
	
	/**
	* @brief learn data using input image and stroke.
	* @param img image data buffer
	* @param width image width
	* @param height image height
	* @param pixelFormat image pixel format
	* pixelFormat = 0 : GRAY
	* pixelFormat = 1 : RGB
	* pixelFormat = 2 : YUV
	* @param stroke stroke data
	* @param size stroke data size
	* @param millis output learning time
	*/
	ARDRAWING_API int learn(unsigned char* img, int width, int height, int pixelFormat, int* stroke, int size, int* millis);

	/**
	* @brief get trackable data(learning data) size.
	* @param byteSize trackable data byte size
	*/
	ARDRAWING_API int getLearnedTrackableArraySize(int* byteSize);

	/**
	* @brief get trackable data(learning data).
	* @param trackableBytes output trackable data buffer
	*/
	ARDRAWING_API int getLearnedTrackableArray(unsigned char *trackableBytes);

	// result
	/**
	* @brief delete all of leaner's trackable(learning data). (Ex, for helper part)
	*/
	ARDRAWING_API int clearLearnedTrackable();

	/*****************************************************************************/
	/*                             Tracker function.                             */
	/*****************************************************************************/
	// Tracker
	/**
	* @brief start Tracking thread.
	*/
	ARDRAWING_API int startTracking();

	/**
	* @brief stop Tracking thread.
	*/
	ARDRAWING_API int stopTracking();

	/**
	* @brief set recognition delay.
	* @param delayMillis delay milliseconds
	*/
	ARDRAWING_API int setRecognitionDelay(unsigned int delayMillis);

	/**
	* @brief set image for tracking.
	* @param img image data buffer
	* @param width image width
	* @param height image height
	* @param pixelFormat image pixel format
	* @param imageIndex image frame index
	* pixelFormat = 0 : GRAY
	* pixelFormat = 1 : RGB
	* pixelFormat = 2 : YUV
	*/
	ARDRAWING_API int inputTrackingImage(unsigned char *img, int width, int height, int pixelFormat, int imageIndex);

	/**
	* @brief set trackable data(learning data) for tracking.
	* @param trackable trackable data buffer
	* @param size trackable data buffer size
	*/
	ARDRAWING_API int setTrackingTrackableArray(unsigned char *trackable, int size);

	/**
	* @brief get tracking result.
	* @param transformMatrix3x3 Get result 3x3 transform matrix(image coordinate).
	* @param millis output tracking time
	*/
	ARDRAWING_API int getTrackingResult(float* transformMatrix3x3, int* millis, int * imageIndex);

	/**
	* @brief get tracking result.
	* @param transformMatrix3x3 Get result 3x3 transform matrix(image coordinate).
	* @param millis output tracking time
	* @param resultImage output tracking Image with stroke. That is always 1280*720 gray image.
	*/
	ARDRAWING_API int getTrackingResultWithImage(float* transformMatrix3x3, int* millis, int * imageIndex, unsigned char* resultImage);

	/**
	* @brief delete all of tracker's trackable(learning data). (Ex, for worker part)
	*/
	ARDRAWING_API int clearTrackingTrackable();
}