LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := mpeg
LOCAL_SRC_FILES := audio_encoder.cpp
LOCAL_SHARED_LIBRARIES := libavformat libavcodec libswscale libavutil libswresample

include $(BUILD_SHARED_LIBRARY)
$(call import-module,ffmpeg-2.4.2/android/arm)