LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := MaxstAR-prebuilt
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/libMaxstAR.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := ARMemo-prebuilt
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/libARMemo.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../../include
include $(PREBUILT_SHARED_LIBRARY)