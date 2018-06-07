#include <jni.h>
#include <ARMemo.h>
#include <ResultCode.h>

static JavaVM * javaVM = 0;

using namespace armemo;

extern "C"
{
	JNIEXPORT jint JNICALL Java_com_maxst_armemo_ARMemoJNI_initialize
		(JNIEnv * env, jclass obj, jobject context, jstring appKey)
	{
#ifdef __ANDROID__
		return ResultCode::INVALID_APP;
#else
		return armemo::initialize();
#endif
	}

	JNIEXPORT jint JNICALL Java_com_maxst_armemo_ARMemoJNI_destroy
		(JNIEnv * env, jclass obj)
	{
		return armemo::destroy();
	}

	JNIEXPORT jint JNICALL Java_com_maxst_armemo_ARMemoJNI_start
		(JNIEnv * env, jclass obj)
	{
		return armemo::start();
	}

	JNIEXPORT jint JNICALL Java_com_maxst_armemo_ARMemoJNI_stop
		(JNIEnv * env, jclass obj)
	{
		return armemo::stop();
	}

	JNIEXPORT jint JNICALL Java_com_maxst_armemo_ARMemoJNI_checkLearnable
		(JNIEnv * env, jclass thiz, jbyteArray camImage, jint length, jint width, jint height, jint pixelFormat)
	{
		jbyte* nativeBytes = (jbyte *)env->GetByteArrayElements(camImage, 0);
		int result = armemo::checkLearnable((unsigned char*)nativeBytes, length, width, height, pixelFormat);
		env->ReleaseByteArrayElements(camImage, nativeBytes, JNI_ABORT);
		return result;
	}

	JNIEXPORT jint JNICALL Java_com_maxst_armemo_ARMemoJNI_learn
		(JNIEnv * env, jclass thiz, jbyteArray camImage, jint length, jint width, jint height, jint pixelFormat, jintArray stroke, jint size)
	{
		jbyte* nativeBytes = (jbyte *)env->GetByteArrayElements(camImage, 0);
		jint * nativeInts = (jint *)env->GetIntArrayElements(stroke, 0);
		int result = armemo::learn((unsigned char*)nativeBytes, length, width, height, pixelFormat, (int *)nativeInts, size);
		env->ReleaseIntArrayElements(stroke, nativeInts, JNI_ABORT);
		env->ReleaseByteArrayElements(camImage, nativeBytes, JNI_ABORT);
		return result;
	}

	JNIEXPORT jint JNICALL Java_com_maxst_armemo_ARMemoJNI_saveLearnedFile
		(JNIEnv * env, jclass thiz, jstring filePath)
	{
		const char * tempFilePath = env->GetStringUTFChars(filePath, 0);
		int result = armemo::saveLearnedFile((char *)tempFilePath);
		env->ReleaseStringUTFChars(filePath, tempFilePath);
		return result;
	}

	JNIEXPORT jint JNICALL Java_com_maxst_armemo_ARMemoJNI_clearLearnedTrackable
		(JNIEnv * env, jclass obj)
	{
		return armemo::clearLearnedTrackable();
	}

	JNIEXPORT jint JNICALL Java_com_maxst_armemo_ARMemoJNI_inputTrackingImage
		(JNIEnv * env, jclass thiz, jbyteArray camImage, jint length, jint width, jint height, jint pixelFormat)
	{
		jbyte* nativeBytes = (jbyte *)env->GetByteArrayElements(camImage, 0);
		int result = armemo::inputTrackingImage((unsigned char*)nativeBytes, length, width, height, pixelFormat);
		env->ReleaseByteArrayElements(camImage, nativeBytes, JNI_ABORT);
		return result;
	}

	JNIEXPORT jint JNICALL Java_com_maxst_armemo_ARMemoJNI_setTrackingFile
		(JNIEnv * env, jclass thiz, jstring filePath)
	{
		const char * tempFilePath = env->GetStringUTFChars(filePath, 0);
		int result = armemo::setTrackingFile((char *)tempFilePath);
		env->ReleaseStringUTFChars(filePath, tempFilePath);
		return result;
	}

	JNIEXPORT jint JNICALL Java_com_maxst_armemo_ARMemoJNI_getTrackingResult
		(JNIEnv * env, jclass thiz, jfloatArray transformMatrix3x3)
	{
		jfloat * nativeFloat = (jfloat*)env->GetPrimitiveArrayCritical(transformMatrix3x3, 0);
		int result = armemo::getTrackingResult((float*)nativeFloat);
		env->ReleasePrimitiveArrayCritical(transformMatrix3x3, nativeFloat, JNI_ABORT);
		return result;
	}

	JNIEXPORT jint JNICALL Java_com_maxst_armemo_ARMemoJNI_clearTrackingTrackable
		(JNIEnv * env, jclass obj)
	{
		return armemo::clearTrackingTrackable();
	}
}
