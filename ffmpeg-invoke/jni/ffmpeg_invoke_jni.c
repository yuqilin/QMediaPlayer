#include <stdlib.h>
#include <jni.h>
#include <android/log.h>

#define MY_JNI_VERSION  JNI_VERSION_1_4

#define TAG         "ffmpeg-invoke"
#define LOG(...)    __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__);
#define LOGE(...)   __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__);

#define FFMPEG_INVOKE_CLASS     "com/github/yuqilin/qmediaplayer/FFmpegInvoke"

extern int main(int argc, char **argv);

static JavaVM *g_jvm = NULL;

JNIEXPORT void JNICALL FFmpegInvoke_run(JNIEnv *env, jobject obj, jobjectArray args) {
    int i = 0;
    int argc = 0;
    char **argv = NULL;
    char **strObjs = NULL;

    if (args != NULL) {
        argc = (*env)->GetArrayLength(env, args);
        argv = (char **) malloc(sizeof(char *) * argc);
        strObjs = (jstring *)malloc(sizeof(jstring) * argc);

        for(i = 0; i < argc; i++) {
            strObjs[i] = (jstring)(*env)->GetObjectArrayElement(env, args, i);
            argv[i] = (char *)(*env)->GetStringUTFChars(env, strObjs[i], NULL);
        }
    }   

    main(argc, argv);

    for(i = 0; i < argc; i++) {
        (*env)->ReleaseStringUTFChars(env, strObjs[i], argv[i]);
    }

    LOG("FFmpegInvoke_run, 0");
    free(strObjs);
    LOG("FFmpegInvoke_run, 1");
    free(argv);
    LOG("FFmpegInvoke_run, 2");
}


static JNINativeMethod g_nativeMethods[] = {
    {
        "run",
        "([Ljava/lang/String;)V",
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
