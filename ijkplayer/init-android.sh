#! /usr/bin/env bash
#
# Copyright (C) 2013-2015 Zhang Rui <bbcallen@gmail.com>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# IJK_FFMPEG_UPSTREAM=git://git.videolan.org/ffmpeg.git
# IJK_FFMPEG_UPSTREAM=https://github.com/yuqilin/FFmpeg.git
# IJK_FFMPEG_FORK=https://github.com/yuqilin/FFmpeg.git
# QMP_FFMPEG_COMMIT=qmediaplayer-dev
IJK_FFMPEG_LOCAL_REPO=extra/ffmpeg

set -e
TOOLS=tools

git --version

# echo "== pull ffmpeg base =="
# sh $TOOLS/pull-repo-base.sh $IJK_FFMPEG_UPSTREAM $IJK_FFMPEG_LOCAL_REPO

function ffmpeg_fork()
{
    echo "== ffmpeg fork $1 =="
    mkdir -p android/contrib/ffmpeg-$1
    cp -a ${IJK_FFMPEG_LOCAL_REPO}/* android/contrib/ffmpeg-$1/
}

# ARCHS="armv5 armv7a arm64 x86 x86_64"
ARCHS="armv5 armv7a arm64"
for ARCH in $ARCHS; do
    ffmpeg_fork $ARCH
done

./init-config.sh
./init-android-libyuv.sh
