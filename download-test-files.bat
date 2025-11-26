@echo off
echo ====================================
echo Downloading Test Video Files
echo ====================================
echo.

REM Create test directory
if not exist "test-files" mkdir "test-files"
cd test-files

echo Downloading sample video files for testing...
echo These are standard MP4/MPEG files that should play on ExoPlayer
echo.

REM Download MP4 files from LearningContainer (reliable source)
echo [1/6] Downloading Sample MP4 (531 KB)...
curl -L -o "sample-small.mp4" "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"

echo [2/6] Downloading Sample MP4 (1 MB)...
curl -L -o "sample-1mb.mp4" "https://download.samplelib.com/mp4/sample-5s.mp4"

echo [3/6] Downloading Sample MP4 (10 MB)...
curl -L -o "sample-10mb.mp4" "https://download.samplelib.com/mp4/sample-10s.mp4"

echo [4/6] Downloading Big Buck Bunny (720p trailer)...
curl -L -o "big-buck-bunny-720p.mp4" "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

echo [5/6] Downloading Elephant Dream (720p)...
curl -L -o "elephants-dream-720p.mp4" "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"

echo [6/6] Downloading For Bigger Blazes (720p)...
curl -L -o "for-bigger-blazes-720p.mp4" "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"

echo.
echo ====================================
echo Download Complete!
echo ====================================
echo.
echo Files saved in: test-files\
echo.
echo To test on Android:
echo 1. Connect Android device via USB (enable USB debugging)
echo 2. Run: adb push test-files\*.mp4 /sdcard/Movies/
echo 3. Open the app and tap "Rescan Files"
echo 4. Tap "Test Files" to verify all files play correctly
echo.
echo Note: These are standard MP4 video files (not karaoke)
echo They will test video playback but won't have lyrics overlay
echo Vocal on/off may not have visible effect on non-karaoke files
echo.
pause
