#!/bin/bash
# Build SecuOps Android Release APK
# Usage: ./scripts/build-release.sh <version>

set -e

VERSION=$1
if [ -z "$VERSION" ]; then
    echo "❌ Usage: ./scripts/build-release.sh <version>"
    echo "   Example: ./scripts/build-release.sh 0.3.0"
    exit 1
fi

echo "🚀 Building SecuOps Android v$VERSION..."
echo ""

# Navigate to project root
cd "$(dirname "$0")/.."

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Build release APK
echo "📦 Building release APK..."
./gradlew assembleRelease

# Check if build succeeded
if [ ! -f "app/build/outputs/apk/release/app-release.apk" ]; then
    echo "❌ Build failed - APK not found"
    exit 1
fi

# Copy to binaries
echo "📂 Copying to binaries/..."
cp app/build/outputs/apk/release/app-release.apk \
   binaries/secuops-android-v$VERSION.apk

# Get file size
FILE_SIZE=$(stat -f%z "binaries/secuops-android-v$VERSION.apk" 2>/dev/null || stat -c%s "binaries/secuops-android-v$VERSION.apk")

echo ""
echo "✅ APK built successfully!"
echo "   File: binaries/secuops-android-v$VERSION.apk"
echo "   Size: $((FILE_SIZE / 1024 / 1024)) MB"
echo ""
echo "📝 Next steps:"
echo "   1. Update version.json: ./scripts/update-version.sh $VERSION <version_code>"
echo "   2. Test APK: adb install binaries/secuops-android-v$VERSION.apk"
echo "   3. Deploy: ./scripts/deploy-binaries.sh [dev|production]"
