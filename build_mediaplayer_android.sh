
CURRENT_PATH=$(cd $(dirname "$0"); pwd)
echo $CURRENT_PATH
JNILIBS_PATH=$CURRENT_PATH/android/QMediaPlayer/qmediaplayer/src/main/jniLibs
IJK_LOCAL_REPO=$CURRENT_PATH/ijkplayer
set -e


ARCHS="armv5 armv7a arm64"

cd $IJK_LOCAL_REPO/android
for ARCH in $ARCHS; do
    ./compile-ijk.sh $ARCH
done

mkdir -p $JNILIBS_PATH/{armeabi,armeabi-v7a,arm64-v8a}

cp $IJK_LOCAL_REPO/android/ijkplayer/ijkplayer-armv5/src/main/libs/armeabi/*.so $JNILIBS_PATH/armeabi/
cp $IJK_LOCAL_REPO/android/ijkplayer/ijkplayer-armv7a/src/main/libs/armeabi-v7a/*.so $JNILIBS_PATH/armeabi-v7a/
cp $IJK_LOCAL_REPO/android/ijkplayer/ijkplayer-arm64/src/main/libs/arm64-v8a/*.so $JNILIBS_PATH/arm64-v8a/
