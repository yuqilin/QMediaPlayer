LOCAL_PATH := $(call my-dir)

ifeq ($(TARGET_ARCH_ABI),armeabi)
FFMPEG_INCLUDE_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/ffmpeg-armv5
FFMPEG_LIB_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/build/ffmpeg-armv5/output/lib
FFMPEG_SO_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/build/ffmpeg-armv5/output
# FFMPEG_SRC_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/ffmpeg-armv5
endif

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
FFMPEG_INCLUDE_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/ffmpeg-armv7a
FFMPEG_LIB_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/build/ffmpeg-armv7a/output/lib
FFMPEG_SO_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/build/ffmpeg-armv7a/output
# FFMPEG_SRC_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/ffmpeg-armv7a
endif

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
FFMPEG_INCLUDE_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/ffmpeg-arm64
FFMPEG_LIB_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/build/ffmpeg-arm64/output/lib
FFMPEG_SO_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/build/ffmpeg-arm64/output
# FFMPEG_SRC_PATH := $(LOCAL_PATH)/../../ijkplayer/android/contrib/ffmpeg-arm64
endif

FFMPEG_SRC_PATH := $(LOCAL_PATH)/../../ijkplayer/extra/ffmpeg

# PREBUILT_INCLUDE_PATH := $(LOCAL_PATH)/../../build_out/$(TARGET_ARCH_ABI)/install/include
# PREBUILT_LIB_PATH := $(LOCAL_PATH)/../../build_out/$(TARGET_ARCH_ABI)/install/lib
# PREBUILT_SO_PATH := $(LOCAL_PATH)/../../build_out/$(TARGET_ARCH_ABI)/install

# $(warning FFMPEG_LIB_PATH=$(FFMPEG_LIB_PATH))

# FFMPEG_LIBS := \
# 	$(addprefix $(FFMPEG_LIB_PATH)/, \
# 	libavformat.a \
# 	libavcodec.a \
# 	libavfilter.a \
# 	libswscale.a \
# 	libavutil.a \
# 	libswresample.a )

$(warning FFMPEG_LIBS=$(FFMPEG_LIBS))

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	ffmpeg_android_jni.c \
	$(FFMPEG_SRC_PATH)/cmdutils.c \
	$(FFMPEG_SRC_PATH)/ffmpeg.c \
	$(FFMPEG_SRC_PATH)/ffmpeg_filter.c \
	$(FFMPEG_SRC_PATH)/ffmpeg_opt.c

LOCAL_MODULE    := ffmpeg-android

# other arm ABI LOCAL_ARM_MODE := thumb
# ifeq ($(TARGET_ARCH_ABI),armeabi)
LOCAL_ARM_MODE  := arm
# endif

LOCAL_CFLAGS 	:= -std=c99 -g -O0
LOCAL_LDLIBS 	:= -llog -lz

LOCAL_C_INCLUDES := \
	$(FFMPEG_INCLUDE_PATH)

# LOCAL_STATIC_LIBRARIES := libavfilter libavformat libavcodec libswscale libavutil libswresample 

LOCAL_SHARED_LIBRARIES := ijkffmpeg

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := ijkffmpeg
LOCAL_SRC_FILES := $(FFMPEG_SO_PATH)/libijkffmpeg.so
include $(PREBUILT_SHARED_LIBRARY)

# include $(CLEAR_VARS)
# LOCAL_MODULE := libavcodec
# LOCAL_SRC_FILES := $(FFMPEG_LIB_PATH)/libavcodec.a
# include $(PREBUILT_STATIC_LIBRARY)

# include $(CLEAR_VARS)
# LOCAL_MODULE := libavfilter
# LOCAL_SRC_FILES := $(FFMPEG_LIB_PATH)/libavfilter.a
# include $(PREBUILT_STATIC_LIBRARY)

# include $(CLEAR_VARS)
# LOCAL_MODULE := libavformat
# LOCAL_SRC_FILES := $(FFMPEG_LIB_PATH)/libavformat.a
# include $(PREBUILT_STATIC_LIBRARY)

# include $(CLEAR_VARS)
# LOCAL_MODULE := libavutil
# LOCAL_SRC_FILES := $(FFMPEG_LIB_PATH)/libavutil.a
# include $(PREBUILT_STATIC_LIBRARY)

# include $(CLEAR_VARS)
# LOCAL_MODULE := libswresample
# LOCAL_SRC_FILES := $(FFMPEG_LIB_PATH)/libswresample.a
# include $(PREBUILT_STATIC_LIBRARY)

# include $(CLEAR_VARS)
# LOCAL_MODULE := libswscale
# LOCAL_SRC_FILES := $(FFMPEG_LIB_PATH)/libswscale.a
# include $(PREBUILT_STATIC_LIBRARY)

# Use to safely invoke ffmpeg multiple times from the same Activity
# include $(CLEAR_VARS)

# LOCAL_MODULE := ffmpeg
# LOCAL_ARM_MODE  := arm
# LOCAL_SRC_FILES := ffmpeg_invoke_jni.c
# LOCAL_CFLAGS 	:= -std=c99 -g
# LOCAL_LDLIBS    := -llog

# include $(BUILD_SHARED_LIBRARY)
