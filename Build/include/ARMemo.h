#include "ARMemoTypes.h"

namespace armemo { 
	/*
	* return code
	* 0 : success
	* 1 : fail(not error)
	*
	* 2 ~ 79 : common error.
	*		memory, input error etc.
	*	2  : memory allocation error.
	*	60 : pixel format error.
	*	61 : input image is empty.
	*	62 : input image resoultion error.
	*	99 : undefine error.
	*
	*	170 : initialize already called.
	*	180 : not initialized.
	*
	* 200 ~ 299 : learner function error.
	*	200 : stroke is empty.
	*	201 : stroke is overflow.
	*
	* 300 ~ 399 : trainer function error.
	*	300 : already called startTracking().
	*	301 : engine is not started.
	*	310 : already called stopTracking() or startTracking() didn't called.
	*	320 : input trackable is empty.
	*
	*/

	/*****************************************************************************/
	/*                             Shared function.                              */
	/*****************************************************************************/
	/**
	* @brief Initialize engine.
	*/
	ARMEMO_API int initialize();

	/**
	* @brief destroy engine.
	*/
	ARMEMO_API int destroy();

	/**
	* @brief start Engine thread.
	*/
	ARMEMO_API int start();

	/**
	* @brief stop Engine thread.
	*/
	ARMEMO_API int stop();

	/*****************************************************************************/
	/*                             Learner function.                             */
	/*****************************************************************************/
	/**
	* @brief check image good for learning.
	* @param img image data buffer
	* @param length image data buffer length(size)
	* @param width image width
	* @param height image height
	* @param pixelFormat image pixel format
	* pixelFormat = 0 : GRAY
	* pixelFormat = 1 : RGB
	* pixelFormat = 2 : YUV
	* @return True when input image has less blur and fit complexity.\n
	*	      False when input image has blur or less complexity.
	*/
	ARMEMO_API int checkLearnable(unsigned char* image, int length, int width, int height, int pixelFormat);

	/**
	* @brief learn data using input image and stroke(center point).
	* @param img image data buffer
	* @param length image data buffer length(size)
	* @param width image width
	* @param height image height
	* @param pixelFormat image pixel format
	* pixelFormat = 0 : GRAY
	* pixelFormat = 1 : RGB
	* pixelFormat = 2 : YUV
	* @param stroke stroke data
	* @param size stroke data size
	*/
	ARMEMO_API int learn(unsigned char* image, int length, int width, int height, int pixelFormat, int* stroke, int size);

	/**
	* @brief save Learned data.
	* @param filepath save file path will located in, it must have ".armemo" extention.
	*/
	ARMEMO_API int saveLearnedFile(char* filepath);

	/**
	* @brief clear Learned data.
	*/
	ARMEMO_API int clearLearnedTrackable();

	/*****************************************************************************/
	/*                             Tracker function.                             */
	/*****************************************************************************/
	/**
	* @brief set image for tracking.
	* @param img image data buffer
	* @param length image data buffer length(size)
	* @param width image width
	* @param height image height
	* @param pixelFormat image pixel format
	* pixelFormat = 0 : GRAY
	* pixelFormat = 1 : RGB
	* pixelFormat = 2 : YUV
	*/
	ARMEMO_API int inputTrackingImage(unsigned char* image, int length, int width, int height, int pixelFormat);

	/**
	* @brief set trackable data(learned data) file for tracking.
	* @param learned file path, it must have ".armemo" extention.
	*/
	ARMEMO_API int setTrackingFile(char* filepath);

	/**
	* @brief get tracking result.
	* @param transformMatrix3x3 Get result 3x3 transform matrix(image coordinate).
	*/
	ARMEMO_API int getTrackingResult(float* transformMatrix3x3);

	/**
	* @brief delete all of tracker's trackable(learned data).
	*/
	ARMEMO_API int clearTrackingTrackable();
}