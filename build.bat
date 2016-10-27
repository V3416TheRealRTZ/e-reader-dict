echo on

SET PREV_PATH=%CD%
cd /d %0\..

rmdir "bin" /S /Q
rmdir "gen" /S /Q
mkdir "bin" || goto EXIT
mkdir "gen" || goto EXIT

SET APP_NAME=e-reader-dict

SET ANDROID_REV=android-16

SET ANDROID_AAPT_ADD="%ANDROID-SDK%\platforms\%ANDROID_REV%\tools\aapt.exe" add

SET ANDROID_AAPT_PACK="%ANDROID-SDK%\platforms\%ANDROID_REV%\tools\aapt.exe" package -v -f -I "%ANDROID-SDK%\platforms\%ANDROID_REV%\android.jar"

SET ANDROID_DX="%ANDROID-SDK%\platform-tools\dx.bat" --dex

SET JAVAC="%JAVABIN%\javac.exe" -classpath "%ANDROID-SDK%\platforms\%ANDROID_REV%\android.jar"
SET JAVAC_BUILD=%JAVAC% -sourcepath "src;gen" -d "bin"

call %ANDROID_AAPT_PACK% -M "AndroidManifest.xml" -A "assets" -S "res" -m -J "gen" -F "bin\resources.ap_" || goto EXIT

call %JAVAC_BUILD% src\com\example\e-reader-dict\*.java || goto EXIT

call %ANDROID_DX% --output="%CD%\bin\classes.dex" %CD%\bin || goto EXIT

copy "%CD%\bin\resources.ap_" "%CD%\bin\%APP_NAME%.ap_" || goto EXIT

call %ANDROID_AAPT_ADD% "%CD%\bin\%APP_NAME%.ap_" "%CD%\bin\classes.dex" || goto EXIT

call "%JAVABIN%\jarsigner" -keystore "%CD%\keystore\my-release-key.keystore" -storepass "password" -keypass "password" -signedjar "%CD%\bin\%APP_NAME%.apk" "%CD%\bin\%APP_NAME%.ap_" "alias_name" || goto EXIT

del "bin\%APP_NAME%.ap_"

:EXIT
cd "%PREV_PATH%"
ENDLOCAL
exit /b %ERRORLEVEL%
