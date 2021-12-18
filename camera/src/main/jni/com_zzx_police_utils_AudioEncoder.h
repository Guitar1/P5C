/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
#include <android/log.h>
/* Header for class com_zzx_police_utils_AudioEncoder */

#ifndef _Included_com_zzx_police_utils_AudioEncoder
#define _Included_com_zzx_police_utils_AudioEncoder
#ifdef __cplusplus
extern "C" {
#endif
#ifndef TAG
#define TAG "police"
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, TAG, fmt, ##args)
#define LOGW(fmt, args...) __android_log_print(ANDROID_LOG_WARNING, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)
#endif
/*
 * Class:     com_zzx_police_utils_AudioEncoder
 * Method:    encoder
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_zzx_police_utils_AudioEncoder_encoder
  (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     com_zzx_police_utils_AudioEncoder
 * Method:    initEncoder
 * Signature: (III)V
 */
JNIEXPORT void JNICALL Java_com_zzx_police_utils_AudioEncoder_initEncoder
  (JNIEnv *, jobject, jint, jint, jint);

#ifdef __cplusplus
}
#endif
#endif
