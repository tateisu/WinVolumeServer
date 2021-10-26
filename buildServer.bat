@ECHO ON
CALL "C:\Program Files (x86)\Microsoft Visual Studio\2019\Community\Common7\Tools\VsMSBuildCmd.bat"
MSBUILD /t:Rebuild /p:Configuration=Release /p:Platform="Any CPU" WinVolumeServer.sln
