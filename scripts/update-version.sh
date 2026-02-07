#!/bin/bash
# Update version.json for SecuOps Android
# Usage: ./scripts/update-version.sh <version> <version_code> [changelog]

set -e

VERSION=$1
VERSION_CODE=$2
CHANGELOG=$3

if [ -z "$VERSION" ] || [ -z "$VERSION_CODE" ]; then
    echo "❌ Usage: ./scripts/update-version.sh <version> <version_code> [changelog]"
    echo "   Example: ./scripts/update-version.sh 0.3.0 4 'Phase 3 complete'"
    exit 1
fi

# Navigate to project root
cd "$(dirname "$0")/.."

APK_FILE="binaries/secuops-android-v$VERSION.apk"

if [ ! -f "$APK_FILE" ]; then
    echo "❌ APK file not found: $APK_FILE"
    echo "   Run ./scripts/build-release.sh $VERSION first"
    exit 1
fi

# Get file size
FILE_SIZE=$(stat -f%z "$APK_FILE" 2>/dev/null || stat -c%s "$APK_FILE")

# Generate changelog from git if not provided
if [ -z "$CHANGELOG" ]; then
    CHANGELOG=$(git log -1 --pretty=%B | head -1)
fi

echo "📝 Updating version.json..."
echo "   Version: $VERSION"
echo "   Code: $VERSION_CODE"
echo "   Size: $((FILE_SIZE / 1024 / 1024)) MB"
echo "   Changelog: $CHANGELOG"

# Create version.json
cat > binaries/version.json <<EOF
{
  "android": {
    "version": "$VERSION",
    "version_code": $VERSION_CODE,
    "download_url": "/binaries/secuops-android-v$VERSION.apk",
    "changelog": "$CHANGELOG",
    "file_size": $FILE_SIZE,
    "min_version": 1
  }
}
EOF

echo ""
echo "✅ version.json updated successfully!"
echo ""
cat binaries/version.json
echo ""
echo "📝 Next steps:"
echo "   1. Test locally: curl http://localhost:8090/api/version"
echo "   2. Deploy: ./scripts/deploy-binaries.sh [dev|production]"
