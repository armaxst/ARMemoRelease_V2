LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := ARDrawing-prebuilt
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/libARDrawing.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../../include
include $(PREBUILT_SHARED_LIBRARY)
