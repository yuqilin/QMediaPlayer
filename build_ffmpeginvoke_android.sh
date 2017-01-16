set -e

#############################
# check Android NDK
#############################
if [ -z "${ANDROID_NDK}" -a -z "${NDK_PATH}" ]; then
    echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
    echo "+     You have to export your ANDROID_NDK or NDK_PATH at first.    +"
    echo "+     They should point to your NDK directories.                   +"
    echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
    echo ""
    exit 1
fi
if [ -z "${ANDROID_NDK}" -a -n "${NDK_PATH}" ]; then
    ANDROID_NDK=${NDK_PATH}
fi
echo "ANDROID_NDK=${ANDROID_NDK}"

THIS_SCRIPT_PATH=$(cd "$(dirname "$0")"; pwd)

cd $THIS_SCRIPT_PATH/ffmpeg-invoke/jni
$ANDROID_NDK/ndk-build clean V=1
$ANDROID_NDK/ndk-build V=1

JNILIBS_PATH=$THIS_SCRIPT_PATH/android/QMediaPlayer/qmediaplayer/src/main/jniLibs

cp $THIS_SCRIPT_PATH/ffmpeg-invoke/libs/armeabi/*.so $JNILIBS_PATH/armeabi/
cp $THIS_SCRIPT_PATH/ffmpeg-invoke/libs/armeabi-v7a/*.so $JNILIBS_PATH/armeabi-v7a/
cp $THIS_SCRIPT_PATH/ffmpeg-invoke/libs/arm64-v8a/*.so $JNILIBS_PATH/arm64-v8a/

cp $JNILIBS_PATH/armeabi/libijkffmpeg.so /Users/yuqilin/mygithub/FFmpegInvoke/app/src/main/jniLibs/armeabi/
cp $JNILIBS_PATH/armeabi-v7a/libijkffmpeg.so /Users/yuqilin/mygithub/FFmpegInvoke/app/src/main/jniLibs/armeabi-v7a/
cp $JNILIBS_PATH/arm64-v8a/libijkffmpeg.so /Users/yuqilin/mygithub/FFmpegInvoke/app/src/main/jniLibs/arm64-v8a/
