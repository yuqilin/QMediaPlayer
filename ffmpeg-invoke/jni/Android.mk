LOCAL_PATH := $(call my-dir)

ifeq ($(TARGET_ARCH_ABI),armeabi)
FFMPEG_INCLUDE_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/ffmpeg-armv5
FFMPEG_SO_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/build/ffmpeg-armv5/output
FFMPEG_SRC_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/ffmpeg-armv5
endif

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
FFMPEG_INCLUDE_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/ffmpeg-armv7a
FFMPEG_SO_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/build/ffmpeg-armv7a/output
FFMPEG_SRC_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/ffmpeg-armv7a
endif

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
FFMPEG_INCLUDE_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/ffmpeg-arm64
FFMPEG_SO_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/build/ffmpeg-arm64/output
FFMPEG_SRC_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/ffmpeg-arm64
endif

# PREBUILT_INCLUDE_PATH := $(LOCAL_PATH)/../../build_out/$(TARGET_ARCH_ABI)/install/include
# PREBUILT_LIB_PATH := $(LOCAL_PATH)/../../build_out/$(TARGET_ARCH_ABI)/install/lib
# PREBUILT_SO_PATH := $(LOCAL_PATH)/../../build_out/$(TARGET_ARCH_ABI)/install

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	ffmpeg_invoke_jni.c \
	$(FFMPEG_SRC_PATH)/cmdutils.c \
	$(FFMPEG_SRC_PATH)/ffmpeg.c \
	$(FFMPEG_SRC_PATH)/ffmpeg_filter.c \
	$(FFMPEG_SRC_PATH)/ffmpeg_opt.c

LOCAL_MODULE    := ffmpeg_invoke

# other arm ABI LOCAL_ARM_MODE := thumb
ifeq ($(TARGET_ARCH_ABI),armeabi)
LOCAL_ARM_MODE  := arm
endif

LOCAL_CFLAGS 	:= -std=c99 -g -O0
LOCAL_LDLIBS 	:= -llog

LOCAL_C_INCLUDES := \
	$(FFMPEG_INCLUDE_PATH)

LOCAL_SHARED_LIBRARIES := libijkffmpeg

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libijkffmpeg
LOCAL_SRC_FILES := $(FFMPEG_SO_PATH)/libijkffmpeg.so
include $(PREBUILT_SHARED_LIBRARY)
