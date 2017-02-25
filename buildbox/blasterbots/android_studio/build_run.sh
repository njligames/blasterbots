#!/bin/bash

./gradlew app:compileReleaseSources
adb uninstall com.njligames.blasterbots
adb install app-release.apk
adb shell am start -n com.njligames.blasterbots/.PTPlayer

