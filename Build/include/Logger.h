#pragma once

#include "Export.h"

#ifdef __ANDROID__
#include <android/log.h>
#ifndef LOG_TAG
#define LOG_TAG "MaxstAR"
#endif
#ifdef DEBUG
#define Log(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#else
#define Log(...)
#define LogI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#endif
#elif defined(WIN32)
extern MAXSTAR_API void Log(const char* szFormat, ...);
#elif defined(TARGET_OS_IPHONE)
#define Log(...) 
#else
#define Log(...) printf(__VA_ARGS__);
#endif
