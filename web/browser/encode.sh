#!/bin/sh
# Turns the recording into the three assets the site and README use.
#
# Run through `npm run demo:record`, which records first. Needs ffmpeg.
set -e
cd "$(dirname "$0")/.."

test -f recording/demo.webm || { echo "no recording; run npm run demo:record"; exit 1; }

# The site: full length, full size, small enough to stream on arrival.
ffmpeg -y -v error -i recording/demo.webm -an \
  -vf "fps=30,scale=1120:-2:flags=lanczos" \
  -c:v libx264 -crf 24 -preset slow -pix_fmt yuv420p -movflags +faststart \
  ../docs/demo.mp4

# The poster: a frame from near the end, where the tree is complete.
ffmpeg -y -v error -ss 15.4 -i recording/demo.webm -frames:v 1 \
  -vf "scale=1120:-2:flags=lanczos" -q:v 3 ../docs/demo-poster.jpg

# The README: GitHub will not autoplay a video from a repository path, so this
# one has to be a GIF. Trimmed past the setup, slightly quickened, and cut to
# 64 colours -- the interface is flat colour, so the palette costs nothing.
ffmpeg -y -v error -ss 3.2 -i recording/demo.webm \
  -vf "setpts=0.7*PTS,fps=10,scale=780:-2:flags=lanczos,split[a][b];\
[a]palettegen=max_colors=64:stats_mode=diff[p];[b][p]paletteuse=dither=bayer:bayer_scale=3" \
  ../docs/demo.gif

ls -la ../docs/demo.mp4 ../docs/demo.gif ../docs/demo-poster.jpg
