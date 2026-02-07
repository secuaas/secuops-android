# Répertoire Binaries - SecuOps Android

Ce répertoire contient les binaires de l'application SecuOps Android (APK).

## ⚠️ IMPORTANT: Système de Distribution

Les fichiers de ce répertoire sont des **sources de développement**.

**Pour que l'API SecuOps serve ces fichiers, ils doivent être déployés vers un serveur web accessible.**

## 📱 Standard SecuAAS pour Applications Android

Ce système suit le **standard SecuAAS** pour toutes les applications Android:
- **CCL Manager** (Claude Code Launcher)
- **SecuOps Android** (cette application)
- **Futures applications Android SecuAAS**

### Composants Requis

1. **version.json** - Métadonnées de version pour auto-update
2. **APK files** - Binaires Android signés/unsigned
3. **API endpoint** - `/api/version` pour vérifier les mises à jour
4. **Static files endpoint** - `/binaries/` pour servir les APK
5. **UpdateManager** - Composant Android pour gérer les MAJ

## Structure des Fichiers

### version.json

Fichier de métadonnées **REQUIS** pour les mises à jour automatiques.

**Format standard SecuAAS:**
```json
{
  "android": {
    "version": "X.Y.Z",
    "version_code": N,
    "download_url": "/binaries/secuops-android-vX.Y.Z.apk",
    "changelog": "...",
    "file_size": BYTES,
    "min_version": N
  }
}
```

**Champs:**
- `version` (string) - Version sémantique (MAJOR.MINOR.PATCH)
- `version_code` (int) - Code numérique incrémental (utilisé pour comparaison)
- `download_url` (string) - Chemin relatif vers l'APK
- `changelog` (string) - Nouveautés de cette version (multi-lignes OK)
- `file_size` (long) - Taille en bytes pour affichage
- `min_version` (int) - Version code minimale supportée

### APK Files

Convention de nommage **standard SecuAAS:**
```
<app-name>-android-v<VERSION>.apk
```

**Exemples:**
- `secuops-android-v0.2.3.apk` ✅
- `ccl-manager-v1.8.2.apk` ✅
- `secuscan-android-v1.0.0.apk` ✅

**Types de build:**
- **Debug** - Pour développement et test (non signé)
- **Release** - Pour production (signé avec keystore)

## Workflow de Release

### 1. Build de l'APK

**Debug build (développement):**
```bash
cd /home/ubuntu/projects/secuops-android
./gradlew assembleDebug
```

**Release build (production):**
```bash
cd /home/ubuntu/projects/secuops-android
./gradlew assembleRelease
```

**APK généré dans:**
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

### 2. Copie vers binaries/

```bash
# Pour debug
cp app/build/outputs/apk/debug/app-debug.apk \
   binaries/secuops-android-v0.2.3.apk

# Pour release
cp app/build/outputs/apk/release/app-release.apk \
   binaries/secuops-android-v0.2.3.apk
```

### 3. Mise à jour version.json

```bash
# Éditer manuellement ou via script
nano binaries/version.json

# Incrémenter version_code
# Mettre à jour version
# Ajouter changelog
# Calculer file_size
```

**Script automatique:**
```bash
./scripts/update-version.sh 0.2.4
```

### 4. Déploiement

**Option 1: Déploiement local (pour tests)**
```bash
# Copier dans répertoire servi par API SecuOps
cp binaries/version.json /var/www/secuops/binaries/
cp binaries/secuops-android-v*.apk /var/www/secuops/binaries/
```

**Option 2: Upload vers serveur distant**
```bash
./scripts/deploy-binaries.sh production
```

**Option 3: GitHub Releases (recommandé)**
```bash
# Tag Git
git tag v0.2.3
git push origin v0.2.3

# Upload via GitHub CLI
gh release create v0.2.3 \
  binaries/secuops-android-v0.2.3.apk \
  --title "SecuOps Android v0.2.3" \
  --notes "$(cat binaries/CHANGELOG.md)"
```

### 5. Vérification

```bash
# Vérifier version.json accessible
curl https://api.secuops.secuaas.dev/api/version | jq .

# Vérifier APK téléchargeable
curl -I https://api.secuops.secuaas.dev/binaries/secuops-android-v0.2.3.apk

# Devrait retourner: 200 OK, Content-Type: application/vnd.android.package-archive
```

## API Backend Requise

### Endpoint: GET /api/version

**Public endpoint** (pas d'authentification requise)

**Response:**
```json
{
  "android": {
    "version": "0.2.3",
    "version_code": 3,
    "download_url": "/binaries/secuops-android-v0.2.3.apk",
    "changelog": "...",
    "file_size": 18874368,
    "min_version": 1
  }
}
```

**Implémentation Go (exemple):**
```go
// cmd/api/version_handler.go
func (s *Server) handleVersion(w http.ResponseWriter, r *http.Request) {
    versionFile := filepath.Join(s.config.BinariesPath, "version.json")
    data, err := os.ReadFile(versionFile)
    if err != nil {
        http.Error(w, "Version info not available", http.StatusNotFound)
        return
    }

    w.Header().Set("Content-Type", "application/json")
    w.Write(data)
}
```

### Endpoint: GET /binaries/{filename}

**Public endpoint** pour servir les fichiers statiques

**Implémentation Go (exemple):**
```go
// main.go
r.PathPrefix("/binaries/").Handler(
    http.StripPrefix("/binaries/",
        http.FileServer(http.Dir("~/.secuops/binaries"))
    )
)
```

## UpdateManager dans l'App Android

### Composant Kotlin

Chaque application Android SecuAAS **DOIT** inclure un `UpdateManager` suivant ce pattern:

```kotlin
// data/update/UpdateManager.kt
@Singleton
class UpdateManager @Inject constructor(
    private val context: Context,
    private val repository: Repository  // Votre repository principal
) {
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Vérifier les mises à jour
    suspend fun checkForUpdate() { /* ... */ }

    // Télécharger l'APK
    fun downloadUpdate(versionInfo: AndroidVersionInfo) { /* ... */ }

    // Installer l'APK
    fun installUpdate(filePath: String) { /* ... */ }

    // Version actuelle
    fun getCurrentVersionName(): String { /* ... */ }
    fun getCurrentVersionCode(): Int { /* ... */ }
}
```

**États possibles:**
```kotlin
sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class Available(
        val versionInfo: AndroidVersionInfo,
        val currentVersion: String,
        val currentVersionCode: Int
    ) : UpdateState()
    data class UpToDate(val currentVersion: String) : UpdateState()
    data class Downloading(
        val versionInfo: AndroidVersionInfo,
        val progress: Int
    ) : UpdateState()
    data class ReadyToInstall(
        val versionInfo: AndroidVersionInfo,
        val downloadId: Long,
        val filePath: String
    ) : UpdateState()
    data class Error(val message: String) : UpdateState()
}
```

### UI dans Settings

Chaque app doit avoir un écran Settings avec section "Mise à jour":

```kotlin
@Composable
fun SettingsScreen(updateManager: UpdateManager) {
    val updateState by updateManager.updateState.collectAsState()

    Column {
        Text("Version", style = MaterialTheme.typography.headlineSmall)
        Text("Current: ${updateManager.getCurrentVersionName()}")

        when (val state = updateState) {
            is UpdateState.Available -> {
                Button(onClick = { updateManager.downloadUpdate(state.versionInfo) }) {
                    Text("Update to ${state.versionInfo.version}")
                }
            }
            is UpdateState.Downloading -> {
                LinearProgressIndicator(progress = state.progress / 100f)
            }
            is UpdateState.ReadyToInstall -> {
                Button(onClick = { updateManager.installUpdate(state.filePath) }) {
                    Text("Install Now")
                }
            }
            // ... autres états
        }

        Button(onClick = { scope.launch { updateManager.checkForUpdate() } }) {
            Text("Check for Updates")
        }
    }
}
```

## Permissions Android Requises

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />

<application>
    <!-- FileProvider pour installer APK sur Android 7+ -->
    <provider
        android:name="androidx.core.content.FileProvider"
        android:authorities="${applicationId}.provider"
        android:exported="false"
        android:grantUriPermissions="true">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_paths" />
    </provider>
</application>
```

```xml
<!-- res/xml/file_paths.xml -->
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-files-path
        name="downloads"
        path="Download/" />
</paths>
```

## Sécurité

### Signature APK (Production)

**Générer keystore:**
```bash
keytool -genkey -v \
  -keystore secuops-android.keystore \
  -alias secuops-release \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

**Configuration Gradle:**
```kotlin
// app/build.gradle.kts
android {
    signingConfigs {
        create("release") {
            storeFile = file("../secuops-android.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "secuops-release"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(/* ... */)
        }
    }
}
```

### HTTPS Obligatoire (Production)

```kotlin
// UpdateManager.kt
private val baseUrl = if (BuildConfig.DEBUG) {
    "http://api.secuops.secuaas.dev"  // OK pour debug
} else {
    "https://api.secuops.secuaas.dev" // HTTPS obligatoire en prod
}
```

### Certificate Pinning (Optionnel)

```kotlin
private val httpClient = OkHttpClient.Builder()
    .certificatePinner(
        CertificatePinner.Builder()
            .add("api.secuops.secuaas.dev", "sha256/AAAAAAAAAA...")
            .build()
    )
    .build()
```

## Scripts d'Automatisation

### scripts/build-release.sh

```bash
#!/bin/bash
set -e

VERSION=$1
if [ -z "$VERSION" ]; then
    echo "Usage: ./build-release.sh <version>"
    exit 1
fi

echo "Building SecuOps Android v$VERSION..."

# Clean
./gradlew clean

# Build release APK
./gradlew assembleRelease

# Copy to binaries
cp app/build/outputs/apk/release/app-release.apk \
   binaries/secuops-android-v$VERSION.apk

echo "✅ APK built: binaries/secuops-android-v$VERSION.apk"
```

### scripts/update-version.sh

```bash
#!/bin/bash
set -e

VERSION=$1
VERSION_CODE=$2

if [ -z "$VERSION" ] || [ -z "$VERSION_CODE" ]; then
    echo "Usage: ./update-version.sh <version> <version_code>"
    exit 1
fi

APK_FILE="binaries/secuops-android-v$VERSION.apk"
FILE_SIZE=$(stat -f%z "$APK_FILE" 2>/dev/null || stat -c%s "$APK_FILE")

cat > binaries/version.json <<EOF
{
  "android": {
    "version": "$VERSION",
    "version_code": $VERSION_CODE,
    "download_url": "/binaries/secuops-android-v$VERSION.apk",
    "changelog": "$(git log -1 --pretty=%B)",
    "file_size": $FILE_SIZE,
    "min_version": 1
  }
}
EOF

echo "✅ version.json updated"
```

### scripts/deploy-binaries.sh

```bash
#!/bin/bash
set -e

ENV=${1:-dev}
VERSION=$(jq -r '.android.version' binaries/version.json)

echo "Deploying SecuOps Android v$VERSION to $ENV..."

if [ "$ENV" == "production" ]; then
    SERVER="api.secuops.secuaas.ca"
    DEST_PATH="/var/www/secuops/binaries/"
elif [ "$ENV" == "dev" ]; then
    SERVER="api.secuops.secuaas.dev"
    DEST_PATH="/var/www/secuops-dev/binaries/"
else
    echo "Unknown environment: $ENV"
    exit 1
fi

# Upload via SCP
scp binaries/version.json ubuntu@$SERVER:$DEST_PATH
scp binaries/secuops-android-v*.apk ubuntu@$SERVER:$DEST_PATH

echo "✅ Deployed to $ENV"
echo "   URL: https://$SERVER/binaries/secuops-android-v$VERSION.apk"
```

## Vérification Post-Déploiement

```bash
# 1. Vérifier version.json
curl -s https://api.secuops.secuaas.dev/api/version | jq .android

# 2. Vérifier APK téléchargeable
APK_URL=$(curl -s https://api.secuops.secuaas.dev/api/version | jq -r '.android.download_url')
curl -I "https://api.secuops.secuaas.dev$APK_URL"

# 3. Tester download complet
wget "https://api.secuops.secuaas.dev$APK_URL"

# 4. Vérifier taille fichier
ls -lh secuops-android-v*.apk
```

## Liens Directs de Téléchargement

### Production
- **Version actuelle:** `https://api.secuops.secuaas.ca/api/version`
- **APK direct:** `https://api.secuops.secuaas.ca/binaries/secuops-android-v0.2.3.apk`

### Développement
- **Version actuelle:** `https://api.secuops.secuaas.dev/api/version`
- **APK direct:** `https://api.secuops.secuaas.dev/binaries/secuops-android-v0.2.3.apk`

### QR Code

Générer QR code pour installation rapide:
```bash
qrencode -o binaries/qrcode-v0.2.3.png \
  "https://api.secuops.secuaas.dev/binaries/secuops-android-v0.2.3.apk"
```

Scanner avec téléphone → Download direct → Installer

## Versions Disponibles

| Version | Code | Date | Taille | Changelog |
|---------|------|------|--------|-----------|
| 0.2.3 | 3 | 2026-02-05 | 18 MB | Phase 2 complete - 8 modules |

## Documentation Complète

- **Ce fichier** - Guide complet binaries
- `../ENHANCEMENT_PLAN.md` - Plan Phase 3
- `../VERSION.md` - Historique versions
- `../README.md` - Documentation générale app

---

**Standard SecuAAS Android Apps**
**Version du standard:** 1.0.0
**Dernière mise à jour:** 2026-02-06
**Contact:** devops@secuaas.ca
