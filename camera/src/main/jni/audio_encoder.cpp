#include "com_zzx_police_utils_AudioEncoder.h"
#include "libavcodec/avcodec.h"

AVCodec *avcodec = NULL;
AVCodecContext *c = NULL;

JNIEXPORT jbyteArray JNICALL Java_com_zzx_police_utils_AudioEncoder_encoder(JNIEnv *env, jobject object, jbyteArray array) {
//    avcodec_encode_audio(c, )
}
JNIEXPORT void JNICALL Java_com_zzx_police_utils_AudioEncoder_initEncoder(JNIEnv *env, jobject object, jint bit_rate, jint sample_rate, jint channel_count) {
    if (!avcodec) {
        avcodec = avcodec_find_encoder(AV_CODEC_ID_ADPCM_G726);
    }
    if (!avcodec) {
        LOGI("avcodec create failed");
        exit(1);
    } else {
        LOGI("avcodec create success");
    }
    if (!c) {
        c = avcodec_alloc_context3(avcodec);
    }
    c->bit_rate = bit_rate;
    c->sample_rate = sample_rate;
    c->channels = channel_count;
    c->channel_layout = select_channel_layout(avcodec);
    if (avcodec_open2(c, avcodec, NULL) < 0) {
        LOGI("avcodec open failed");
        exit(1);
    } else {
        LOGI("avcodec open success");
    }
}