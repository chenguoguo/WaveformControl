LOCAL_PATH := $(call my-dir)

# static library info
include $(CLEAR_VARS)
LOCAL_MODULE := openblas
LOCAL_SRC_FILES := ../prebuild/OpenBLAS/lib/libopenblas.a
LOCAL_EXPORT_C_INCLUDES := ../prebuild/OpenBLAS/include/
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libsnowboy-detect
LOCAL_SRC_FILES := ../prebuild/Snowboy/lib/libsnowboy-detect.a
LOCAL_EXPORT_C_INCLUDES := ../prebuild/Snowboy/include
include $(PREBUILT_STATIC_LIBRARY)

# wrapper info
include $(CLEAR_VARS)
LOCAL_CFLAGS    := -std=c++0x
LOCAL_C_INCLUDES += ../prebuild/include
LOCAL_MODULE    := snowboy-detect-android
LOCAL_SRC_FILES := snowboy-detect-swig.cc
LOCAL_STATIC_LIBRARIES := libsnowboy-detect libopenblas
include $(BUILD_SHARED_LIBRARY)
