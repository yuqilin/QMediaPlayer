
IJK_LOCAL_REPO=ijkplayer

set -e

ARCHS="armv5 armv7a arm64"

cd $IJK_LOCAL_REPO

./init-android.sh

cd android/contrib
./compile-ffmpeg.sh clean

for ARCH in $ARCHS; do
    ./compile-ffmpeg.sh $ARCH
done
