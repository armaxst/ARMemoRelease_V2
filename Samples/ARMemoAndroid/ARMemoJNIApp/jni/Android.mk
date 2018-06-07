LOCAL_PATH := $(call my-dir)

MY_LOCAL_PATH := $(LOCAL_PATH)
ARMEMO_PATH := $(LOCAL_PATH)/../../../../Build/lib/Android

include $(CLEAR_VARS)
include $(ARMEMO_PATH)/Android.mk

include $(CLEAR_VARS)
LOCAL_PATH := $(MY_LOCAL_PATH)
LOCAL_MODULE := ARMemoJNI
LOCAL_SRC_FILES := ARMemoJNI.cpp
LOCAL_CPPFLAGS += -std=c++11 -frtti -Wno-switch-enum -Wno-switch
LOCAL_SHARED_LIBRARIES := MaxstAR-prebuilt ARMemo-prebuilt
include $(BUILD_SHARED_LIBRARY)