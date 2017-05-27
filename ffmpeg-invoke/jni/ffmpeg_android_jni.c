#include <stdlib.h>
#include <jni.h>
#include <android/log.h>
#include "libavutil/log.h"

#define MY_JNI_VERSION  JNI_VERSION_1_4

#define TAG         "ffmpeg-android"
#define LOG(...)    __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__);
#define LOGE(...)   __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__);

#define VLOGD(...)    ((void)__android_log_vprint(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__))

#define FFMPEG_ANDROID_CLASS     "com/github/yuqilin/qmediaplayer/FFmpegAndroid"

extern int main(int argc, char **argv);

static JavaVM *g_jvm = NULL;

static void ffp_log_callback_brief(void *ptr, int level, const char *fmt, va_list vl) {
    VLOGD(fmt, vl);
}

JNIEXPORT void JNICALL FFmpeg_run(JNIEnv *env, jobject obj, jobjectArray args) {
    int i = 0;
    int argc = 0;
    char **argv = NULL;
    jstring *strObjs = NULL;

    if (args == NULL) {
        return;
    }

    argc = (*env)->GetArrayLength(env, args);
    argv = (char **) malloc(sizeof(char *) * argc);
    strObjs = (jstring *)malloc(sizeof(jstring) * argc);

    for(i = 0; i < argc; i++) {
        strObjs[i] = (jstring)(*env)->GetObjectArrayElement(env, args, i);
        argv[i] = (char *)(*env)->GetStringUTFChars(env, strObjs[i], NULL);
    }

    av_log_set_level(AV_LOG_DEBUG);
    av_log_set_callback(ffp_log_callback_brief);

    for (i = 0; i < argc; i++) {
        LOG("argv[%d] : %s", i, argv[i]);
    }

    main(argc, argv);

    for(i = 0; i < argc; i++) {
        (*env)->ReleaseStringUTFChars(env, strObjs[i], argv[i]);
    }

    LOG("FFmpeg_run, 0");
    free(strObjs);
    LOG("FFmpeg_run, 1");
    free(argv);
    LOG("FFmpeg_run, 2");
}


static JNINativeMethod g_nativeMethods[] = {
    {
        "run",
        "([Ljava/lang/String;)V",
        (void *) FFmpeg_run
    },
};

extern JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    g_jvm = vm;

    LOG("JNI_OnLoad, g_jvm=%p", g_jvm);

    JNIEnv *env = NULL;
    jint status = (*g_jvm)->GetEnv(g_jvm, (void **)&env, MY_JNI_VERSION);

    jclass cls = (*env)->FindClass(env, FFMPEG_ANDROID_CLASS);
    if (!cls) {
        LOG("error:findclass failed for class'%s'", FFMPEG_ANDROID_CLASS);
    }

    if ((*env)->RegisterNatives(env, cls, g_nativeMethods, sizeof(g_nativeMethods) / sizeof(g_nativeMethods[0])) < 0) {
        LOG("error:register natives failed for class'%s'", FFMPEG_ANDROID_CLASS);
        return -1;
    }

    return MY_JNI_VERSION;
}

extern JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
     LOG("JNI_OnUnload");
}
