#!/bin/bash
# Deploy SecuOps Android binaries to server
# Usage: ./scripts/deploy-binaries.sh [dev|production]

set -e

ENV=${1:-dev}
VERSION=$(jq -r '.android.version' binaries/version.json 2>/dev/null || echo "unknown")

if [ "$VERSION" == "unknown" ]; then
    echo "❌ version.json not found or invalid"
    echo "   Run ./scripts/update-version.sh first"
    exit 1
fi

echo "🚀 Deploying SecuOps Android v$VERSION to $ENV..."
echo ""

# Determine server based on environment
if [ "$ENV" == "production" ]; then
    SERVER="api.secuops.secuaas.ca"
    DEST_PATH="/var/www/secuops/binaries/"
    BASE_URL="https://api.secuops.secuaas.ca"
elif [ "$ENV" == "dev" ]; then
    SERVER="api.secuops.secuaas.dev"
    DEST_PATH="/var/www/secuops-dev/binaries/"
    BASE_URL="https://api.secuops.secuaas.dev"
else
    echo "❌ Unknown environment: $ENV"
    echo "   Valid options: dev, production"
    exit 1
fi

echo "📤 Uploading to $SERVER..."
echo ""

# Create destination directory if not exists
ssh ubuntu@$SERVER "mkdir -p $DEST_PATH"

# Upload version.json
echo "   Uploading version.json..."
scp binaries/version.json ubuntu@$SERVER:$DEST_PATH

# Upload all APK files
echo "   Uploading APK files..."
scp binaries/secuops-android-v*.apk ubuntu@$SERVER:$DEST_PATH

echo ""
echo "✅ Deployed successfully!"
echo ""
echo "📱 Download URLs:"
echo "   Version info: $BASE_URL/api/version"
echo "   APK direct:   $BASE_URL/binaries/secuops-android-v$VERSION.apk"
echo ""
echo "🔍 Verification:"
echo "   curl -s $BASE_URL/api/version | jq ."
echo "   curl -I $BASE_URL/binaries/secuops-android-v$VERSION.apk"
echo ""

# Generate QR code if qrencode is available
if command -v qrencode &> /dev/null; then
    APK_URL="$BASE_URL/binaries/secuops-android-v$VERSION.apk"
    qrencode -o binaries/qrcode-v$VERSION.png "$APK_URL"
    echo "📱 QR Code generated: binaries/qrcode-v$VERSION.png"
    echo "   Scan with phone to download directly"
fi
