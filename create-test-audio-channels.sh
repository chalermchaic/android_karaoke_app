#!/bin/bash

echo "===================================="
echo "Creating Test Videos for Audio Channel Testing"
echo "===================================="
echo ""
echo "This script requires FFmpeg to be installed"
echo "Download from: https://ffmpeg.org/download.html"
echo ""

# Check if ffmpeg is available
if ! command -v ffmpeg &> /dev/null; then
    echo "ERROR: FFmpeg not found in PATH"
    echo "Please install FFmpeg first"
    exit 1
fi

# Create test directory
mkdir -p test-audio-files
cd test-audio-files

echo ""
echo "Creating test videos with different audio in Left and Right channels..."
echo ""

# Test 1: Different frequencies (440Hz left, 880Hz right)
echo "[1/5] Creating test-stereo-tones.mp4 (440Hz Left, 880Hz Right)..."
ffmpeg -y -f lavfi -i testsrc=duration=10:size=1280x720:rate=30 \
  -f lavfi -i "sine=frequency=440:duration=10" \
  -f lavfi -i "sine=frequency=880:duration=10" \
  -filter_complex "[1:a][2:a]amerge=inputs=2[aout]" \
  -map 0:v -map "[aout]" \
  -c:v libx264 -pix_fmt yuv420p \
  -c:a aac -b:a 192k \
  test-stereo-tones.mp4

# Test 2: Voice on left, Music on right simulation (different tones)
echo "[2/5] Creating test-karaoke-simulation.mp4 (Low tone Left, High tone Right)..."
ffmpeg -y -f lavfi -i testsrc=duration=15:size=1280x720:rate=30 \
  -f lavfi -i "sine=frequency=300:duration=15" \
  -f lavfi -i "sine=frequency=600:duration=15" \
  -filter_complex "[1:a][2:a]amerge=inputs=2[aout]" \
  -map 0:v -map "[aout]" \
  -c:v libx264 -pix_fmt yuv420p \
  -c:a aac -b:a 192k \
  test-karaoke-simulation.mp4

# Test 3: Silence on left, tone on right
echo "[3/5] Creating test-right-only.mp4 (Silent Left, Tone Right)..."
ffmpeg -y -f lavfi -i testsrc=duration=10:size=1280x720:rate=30 \
  -f lavfi -i "anullsrc=duration=10" \
  -f lavfi -i "sine=frequency=1000:duration=10" \
  -filter_complex "[1:a][2:a]amerge=inputs=2[aout]" \
  -map 0:v -map "[aout]" \
  -c:v libx264 -pix_fmt yuv420p \
  -c:a aac -b:a 192k \
  test-right-only.mp4

# Test 4: Tone on left, silence on right
echo "[4/5] Creating test-left-only.mp4 (Tone Left, Silent Right)..."
ffmpeg -y -f lavfi -i testsrc=duration=10:size=1280x720:rate=30 \
  -f lavfi -i "sine=frequency=1000:duration=10" \
  -f lavfi -i "anullsrc=duration=10" \
  -filter_complex "[1:a][2:a]amerge=inputs=2[aout]" \
  -map 0:v -map "[aout]" \
  -c:v libx264 -pix_fmt yuv420p \
  -c:a aac -b:a 192k \
  test-left-only.mp4

# Test 5: Different volumes
echo "[5/5] Creating test-different-volumes.mp4 (Quiet Left, Loud Right)..."
ffmpeg -y -f lavfi -i testsrc=duration=10:size=1280x720:rate=30 \
  -f lavfi -i "sine=frequency=500:duration=10" \
  -f lavfi -i "sine=frequency=500:duration=10" \
  -filter_complex "[1:a]volume=0.3[left];[2:a]volume=1.0[right];[left][right]amerge=inputs=2[aout]" \
  -map 0:v -map "[aout]" \
  -c:v libx264 -pix_fmt yuv420p \
  -c:a aac -b:a 192k \
  test-different-volumes.mp4

echo ""
echo "===================================="
echo "Test Videos Created Successfully!"
echo "===================================="
echo ""
echo "Files created in: test-audio-files/"
echo ""
echo "Test Files:"
echo "1. test-stereo-tones.mp4         - 440Hz Left, 880Hz Right"
echo "2. test-karaoke-simulation.mp4  - Low tone Left, High tone Right"
echo "3. test-right-only.mp4           - Silent Left, Tone Right"
echo "4. test-left-only.mp4            - Tone Left, Silent Right"
echo "5. test-different-volumes.mp4   - Quiet Left, Loud Right"
echo ""
echo "To test on Android:"
echo "1. adb push test-audio-files/*.mp4 /sdcard/Movies/"
echo "2. Open the app and tap 'Rescan Files'"
echo "3. Play a test file"
echo "4. Tap 'Audio: Stereo' button"
echo "5. Try different channel modes:"
echo "   - Stereo: Hear both channels"
echo "   - Left Channel: Hear only left"
echo "   - Right Channel: Hear only right"
echo "   - Mono Mix: Mix both channels"
echo ""
