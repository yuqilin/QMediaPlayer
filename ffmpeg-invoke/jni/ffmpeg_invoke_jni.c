#include <stdlib.h>
#include <dlfcn.h>
#include <jni.h>
#include <android/log.h>

#define MY_JNI_VERSION  JNI_VERSION_1_4

#define TAG         "ffmpeg-invoke"
#define LOG(...)    __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__);
#define LOGE(...)   __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__);

#define FFMPEG_INVOKE_CLASS     "com/github/yuqilin/qmediaplayer/FFmpegInvoke"

static JavaVM *g_jvm = NULL;

// Use to safely invoke ffmpeg multiple times from the same Activity
JNIEXPORT void JNICALL FFmpegInvoke_run(JNIEnv *env, jobject obj, jstring libffmpeg_path, jobjectArray ffmpeg_args) {
    const char* path;
    void* handle;
    void (*ff_FFmpeg_run)(JNIEnv*, jobject, jobjectArray) = NULL;

    path = (*env)->GetStringUTFChars(env, libffmpeg_path, 0);
    handle = dlopen(path, RTLD_LAZY);
    if (handle == NULL) {
        LOG("error:dlopen ffmpeg failed : %s", path);
        goto RETURN_LABEL;
    }

    ff_FFmpeg_run = dlsym(handle, "FFmpeg_run");
    if (ff_FFmpeg_run != NULL) {
        (*ff_FFmpeg_run)(env, obj, ffmpeg_args);
    } else {
        LOG("error:dlsym FFmpeg_run is NULL !!!");
    }

    LOG("FFmpegInvoke_run 0");

    dlclose(handle);

    LOG("FFmpegInvoke_run 1");

RETURN_LABEL:

    (*env)->ReleaseStringUTFChars(env, libffmpeg_path, path);

    LOG("FFmpegInvoke_run 2");
}

static JNINativeMethod g_nativeMethods[] = {
    {
        "run",
        "(Ljava/lang/String;[Ljava/lang/String;)V",
        (void *) FFmpegInvoke_run
    },
};

extern JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    g_jvm = vm;

    LOG("JNI_OnLoad, g_jvm=%p", g_jvm);

    JNIEnv *env = NULL;
    jint status = (*g_jvm)->GetEnv(g_jvm, (void **)&env, MY_JNI_VERSION);

    jclass cls = (*env)->FindClass(env, FFMPEG_INVOKE_CLASS);
    if (!cls) {
        LOG("error:findclass failed for class'%s'", FFMPEG_INVOKE_CLASS);
    }

    if ((*env)->RegisterNatives(env, cls, g_nativeMethods, sizeof(g_nativeMethods) / sizeof(g_nativeMethods[0])) < 0) {
        LOG("error:register natives failed for class'%s'", FFMPEG_INVOKE_CLASS);
        return -1;
    }

    return MY_JNI_VERSION;
}

extern JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
     LOG("JNI_OnUnload");
}
