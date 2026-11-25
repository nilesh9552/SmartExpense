#!/usr/bin/env bash

# Gradle Wrapper Script for Smart Expense
set -e

APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
WRAPPER_PROPERTIES="$APP_DIR/gradle/wrapper/gradle-wrapper.properties"
WRAPPER_JAR="$APP_DIR/gradle/wrapper/gradle-wrapper.jar"

# Auto-detect Android SDK if not set
if [ -z "$ANDROID_HOME" ] && [ -d "$HOME/Android/Sdk" ]; then
    export ANDROID_HOME="$HOME/Android/Sdk"
fi
if [ -z "$ANDROID_SDK_ROOT" ] && [ -n "$ANDROID_HOME" ]; then
    export ANDROID_SDK_ROOT="$ANDROID_HOME"
fi

# Locate Java executable
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
elif command -v java >/dev/null 2>&1; then
    JAVACMD="java"
else
    echo "Error: JAVA_HOME is not set and no 'java' command was found in PATH." >&2
    exit 1
fi

# If wrapper jar is present, run standard Gradle wrapper
if [ -f "$WRAPPER_JAR" ]; then
    exec "$JAVACMD" -Dorg.gradle.appname=gradlew -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
fi

# Determine distribution URL from gradle-wrapper.properties
DIST_URL="https://services.gradle.org/distributions/gradle-8.6-bin.zip"
if [ -f "$WRAPPER_PROPERTIES" ]; then
    URL_LINE=$(grep "^distributionUrl=" "$WRAPPER_PROPERTIES" | head -n 1 || true)
    if [ -n "$URL_LINE" ]; then
        DIST_URL=$(echo "$URL_LINE" | cut -d'=' -f2- | sed 's/\\//g')
    fi
fi

# Check if Gradle binary is already downloaded and unpacked
# Configure Gradle User Home (fallback to project directory if home is read-only)
if [ -z "$GRADLE_USER_HOME" ]; then
    if mkdir -p "$HOME/.gradle" 2>/dev/null; then
        GRADLE_USER_HOME="$HOME/.gradle"
    else
        GRADLE_USER_HOME="$APP_DIR/.gradle"
    fi
fi
export GRADLE_USER_HOME
ZIP_BASENAME=$(basename "$DIST_URL")
DIST_DIR_NAME="${ZIP_BASENAME%.zip}"
TARGET_DIR="$GRADLE_USER_HOME/wrapper/dists/$DIST_DIR_NAME"
GRADLE_BIN=$(find "$TARGET_DIR" -name "gradle" -type f -perm -u+x 2>/dev/null | head -n 1 || true)

if [ -z "$GRADLE_BIN" ] || [ ! -x "$GRADLE_BIN" ]; then
    echo "Gradle wrapper JAR not found. Bootstrapping Gradle from $DIST_URL..."
    mkdir -p "$TARGET_DIR"
    TMP_ZIP="$TARGET_DIR/gradle-download.zip"

    if command -v curl >/dev/null 2>&1; then
        curl -sSL -o "$TMP_ZIP" "$DIST_URL"
    elif command -v wget >/dev/null 2>&1; then
        wget -q -O "$TMP_ZIP" "$DIST_URL"
    elif command -v python3 >/dev/null 2>&1; then
        python3 -c "import urllib.request; urllib.request.urlretrieve('$DIST_URL', '$TMP_ZIP')"
    else
        echo "Error: curl, wget, or python3 required to download Gradle." >&2
        exit 1
    fi

    echo "Extracting Gradle archive..."
    if command -v unzip >/dev/null 2>&1; then
        unzip -q -o "$TMP_ZIP" -d "$TARGET_DIR"
    elif command -v python3 >/dev/null 2>&1; then
        python3 -c "import zipfile; zipfile.ZipFile('$TMP_ZIP').extractall('$TARGET_DIR')"
    else
        echo "Error: unzip or python3 required to extract Gradle." >&2
        exit 1
    fi
    rm -f "$TMP_ZIP"

    # Copy wrapper jar for future fast runs
    FOUND_JAR=$(find "$TARGET_DIR" -name "gradle-wrapper*.jar" 2>/dev/null | head -n 1 || true)
    if [ -n "$FOUND_JAR" ] && [ -f "$FOUND_JAR" ]; then
        mkdir -p "$APP_DIR/gradle/wrapper"
        cp "$FOUND_JAR" "$WRAPPER_JAR"
    fi

    GRADLE_BIN=$(find "$TARGET_DIR" -name "gradle" -type f -perm -u+x 2>/dev/null | head -n 1 || true)
fi

if [ -n "$GRADLE_BIN" ] && [ -x "$GRADLE_BIN" ]; then
    exec "$GRADLE_BIN" "$@"
else
    echo "Error: Gradle executable could not be found in $TARGET_DIR" >&2
    exit 1
fi
