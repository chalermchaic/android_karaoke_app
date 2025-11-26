# PowerShell script to run FFmpeg test file generation
# This refreshes the environment to pick up the new PATH

Write-Host "Refreshing environment variables..."
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")

Write-Host "Checking FFmpeg installation..."
ffmpeg -version

Write-Host "`nRunning test file generation script..."
.\create-test-audio-channels.bat
