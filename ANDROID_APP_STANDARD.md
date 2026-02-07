# Standard SecuAAS pour Applications Android
## Guide Complet - Build, Déploiement & Mises à Jour

**Version du standard:** 1.0.0
**Date:** 2026-02-06
**Applications couvertes:** CCL Manager, SecuOps Android, et toutes futures apps Android SecuAAS

---

## 🎯 Vue d'Ensemble

Ce document définit le **standard SecuAAS** pour toutes les applications Android de l'organisation. Suivre ce standard garantit:

✅ Cohérence entre toutes les applications Android
✅ Système de mise à jour automatique unifié
✅ Processus de build et déploiement reproductible
✅ Expérience utilisateur cohérente pour les updates

### Applications Utilisant ce Standard

| Application | Status | Repository |
|-------------|--------|------------|
| **CCL Manager** | ✅ Production | claude-app/ccl-android |
| **SecuOps Android** | ✅ Implémenté | secuops-android |
| Futures apps | 📋 À venir | - |

---

## 📐 Architecture Standard

### 1. Structure de Répertoires

```
<app-name>/
├── app/                              # Code source Android
│   ├── src/main/
│   │   ├── kotlin/com/secuaas/<app>/
│   │   │   ├── data/
│   │   │   │   ├── update/
│   │   │   │   │   └── UpdateManager.kt    # ✅ REQUIS
│   │   │   │   └── model/
│   │   │   │       ├── VersionInfo.kt      # ✅ REQUIS
│   │   │   │       └── UpdateState.kt      # ✅ REQUIS
│   │   │   ├── presentation/
│   │   │   │   └── settings/
│   │   │   │       └── SettingsScreen.kt   # ✅ REQUIS (avec update UI)
│   │   │   └── MainActivity.kt
│   │   ├── res/
│   │   │   └── xml/
│   │   │       └── file_paths.xml          # ✅ REQUIS (FileProvider)
│   │   └── AndroidManifest.xml             # ✅ REQUIS (permissions)
│   └── build.gradle.kts
├── binaries/                         # ✅ REQUIS - Binaires pour distribution
│   ├── version.json                  # ✅ REQUIS - Métadonnées version
│   ├── <app>-android-vX.Y.Z.apk     # APK builds
│   ├── qrcode-vX.Y.Z.png            # QR codes optionnels
│   └── README.md                     # Documentation binaries
├── scripts/                          # ✅ REQUIS - Scripts automatisation
│   ├── build-release.sh              # ✅ REQUIS - Build APK release
│   ├── update-version.sh             # ✅ REQUIS - Update version.json
│   └── deploy-binaries.sh            # ✅ REQUIS - Deploy vers serveur
├── VERSION.md                        # Changelog
├── WORK_IN_PROGRESS.md              # État actuel
├── ANDROID_APP_STANDARD.md          # Ce document (optionnel)
└── README.md
```

---

## 🔧 Composants Requis

### 1. UpdateManager.kt (Obligatoire)

**Emplacement:** `app/src/main/kotlin/com/secuaas/<app>/data/update/UpdateManager.kt`

**Responsabilités:**
- Vérifier les mises à jour via `/api/version`
- Télécharger APK via DownloadManager
- Gérer l'installation APK
- Suivre la progression du téléchargement

**Interface publique minimale:**
```kotlin
@Singleton
class UpdateManager @Inject constructor(
    private val context: Context
) {
    val updateState: StateFlow<UpdateState>

    fun setApiUrl(url: String)
    fun getCurrentVersionName(): String
    fun getCurrentVersionCode(): Int
    suspend fun checkForUpdate()
    fun downloadUpdate(versionInfo: AndroidVersionInfo)
    fun installUpdate(filePath: String)
    fun cancelDownload()
    fun resetState()
    fun formatFileSize(bytes: Long): String
}
```

**Implémentation de référence:**
Voir `/home/ubuntu/projects/secuops-android/app/src/main/kotlin/com/secuaas/secuops/data/update/UpdateManager.kt`

### 2. Modèles de Données (Obligatoire)

**VersionInfo.kt:**
```kotlin
data class VersionInfo(
    @SerializedName("android")
    val android: AndroidVersionInfo?
)

data class AndroidVersionInfo(
    @SerializedName("version")
    val version: String,
    @SerializedName("version_code")
    val versionCode: Int,
    @SerializedName("download_url")
    val downloadUrl: String,
    @SerializedName("changelog")
    val changelog: String,
    @SerializedName("file_size")
    val fileSize: Long,
    @SerializedName("min_version")
    val minVersion: Int
)
```

**UpdateState.kt:**
```kotlin
sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class Available(...) : UpdateState()
    data class UpToDate(...) : UpdateState()
    data class Downloading(...) : UpdateState()
    data class ReadyToInstall(...) : UpdateState()
    data class Error(val message: String) : UpdateState()
}
```

### 3. Settings Screen avec UI Update (Obligatoire)

**Emplacement:** `presentation/settings/SettingsScreen.kt`

**Sections requises:**
- Version actuelle (nom + code)
- Bouton "Check for Updates"
- État de la mise à jour (Available/Downloading/ReadyToInstall/Error)
- Bouton "Download" (si Available)
- Progress bar (si Downloading)
- Bouton "Install Now" (si ReadyToInstall)
- Changelog (si Available ou ReadyToInstall)

**Exemple UI minimal:**
```kotlin
@Composable
fun SettingsScreen(
    updateManager: UpdateManager,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val updateState by updateManager.updateState.collectAsState()
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        // Version section
        Text("Version", style = MaterialTheme.typography.headlineSmall)
        Text("Current: ${updateManager.getCurrentVersionName()}")

        Spacer(modifier = Modifier.height(16.dp))

        // Update status
        when (val state = updateState) {
            UpdateState.Idle -> {
                Button(onClick = { scope.launch { updateManager.checkForUpdate() } }) {
                    Text("Check for Updates")
                }
            }
            UpdateState.Checking -> {
                CircularProgressIndicator()
                Text("Checking...")
            }
            is UpdateState.Available -> {
                Text("Update available: ${state.versionInfo.version}")
                Text("Size: ${updateManager.formatFileSize(state.versionInfo.fileSize)}")
                Text("What's new:\n${state.versionInfo.changelog}")
                Button(onClick = { updateManager.downloadUpdate(state.versionInfo) }) {
                    Text("Download")
                }
            }
            is UpdateState.Downloading -> {
                LinearProgressIndicator(progress = state.progress / 100f)
                Text("Downloading... ${state.progress}%")
                Button(onClick = { updateManager.cancelDownload() }) {
                    Text("Cancel")
                }
            }
            is UpdateState.ReadyToInstall -> {
                Text("Ready to install v${state.versionInfo.version}")
                Button(onClick = { updateManager.installUpdate(state.filePath) }) {
                    Text("Install Now")
                }
            }
            is UpdateState.UpToDate -> {
                Text("✅ You're up to date!")
                Button(onClick = { updateManager.resetState() }) {
                    Text("Check Again")
                }
            }
            is UpdateState.Error -> {
                Text("❌ ${state.message}", color = MaterialTheme.colorScheme.error)
                Button(onClick = { updateManager.resetState() }) {
                    Text("Retry")
                }
            }
        }
    }
}
```

### 4. AndroidManifest.xml (Permissions Requises)

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />

<application>
    <!-- FileProvider pour installer APK (Android 7+) -->
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

### 5. file_paths.xml (Obligatoire)

**Emplacement:** `app/src/main/res/xml/file_paths.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-files-path
        name="downloads"
        path="Download/" />
</paths>
```

---

## 📦 Format version.json (Standard)

**Emplacement:** `binaries/version.json`

**Format obligatoire:**
```json
{
  "android": {
    "version": "X.Y.Z",
    "version_code": N,
    "download_url": "/binaries/<app>-android-vX.Y.Z.apk",
    "changelog": "Multi-line changelog\n- Feature 1\n- Feature 2",
    "file_size": BYTES,
    "min_version": N
  }
}
```

**Validation des champs:**
- `version` (string) - Format sémantique: MAJOR.MINOR.PATCH (ex: "0.2.3")
- `version_code` (int) - Numérique incrémental, TOUJOURS croissant (ex: 3)
- `download_url` (string) - Chemin relatif depuis base URL (commence par /)
- `changelog` (string) - Peut contenir `\n` pour multi-lignes
- `file_size` (long) - Bytes exacts du fichier APK
- `min_version` (int) - Version code minimale compatible

**Exemple réel:**
```json
{
  "android": {
    "version": "0.2.3",
    "version_code": 3,
    "download_url": "/binaries/secuops-android-v0.2.3.apk",
    "changelog": "- Phase 2 Complete: 8 modules fonctionnels\n- 29 endpoints API intégrés\n- Material3 UI avec Dark/Light mode",
    "file_size": 18874368,
    "min_version": 1
  }
}
```

---

## 🌐 API Backend (Endpoints Requis)

### Endpoint: GET /api/version

**Public endpoint** (pas d'authentification requise)

**Response:** Contenu de `version.json`

**Implémentation Go (exemple pour SecuOps):**
```go
// cmd/api/handlers/version.go
func (h *Handler) HandleVersion(w http.ResponseWriter, r *http.Request) {
    versionFile := filepath.Join(h.config.BinariesPath, "version.json")

    data, err := os.ReadFile(versionFile)
    if err != nil {
        http.Error(w, "Version info not available", http.StatusNotFound)
        return
    }

    w.Header().Set("Content-Type", "application/json")
    w.Header().Set("Cache-Control", "no-cache")
    w.Write(data)
}
```

**Route:**
```go
r.HandleFunc("/api/version", h.HandleVersion).Methods("GET")
```

### Endpoint: GET /binaries/{filename}

**Public endpoint** pour servir fichiers statiques (APK, QR codes)

**Implémentation Go:**
```go
// main.go ou router setup
r.PathPrefix("/binaries/").Handler(
    http.StripPrefix("/binaries/",
        http.FileServer(http.Dir(binariesPath))
    )
)
```

**Configuration:**
- Path: `/var/www/secuops/binaries/` (production) ou `~/.secuops/binaries/` (dev)
- Content-Type: `application/vnd.android.package-archive` pour APK
- HTTPS obligatoire en production

---

## 🚀 Processus de Release (Standard)

### 1. Build APK

**Debug (développement):**
```bash
cd <app-directory>
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

**Release (production):**
```bash
cd <app-directory>
./gradlew assembleRelease
# APK: app/build/outputs/apk/release/app-release.apk
```

**Ou via script:**
```bash
./scripts/build-release.sh 0.3.0
```

### 2. Copier dans binaries/

```bash
cp app/build/outputs/apk/release/app-release.apk \
   binaries/<app>-android-v0.3.0.apk
```

### 3. Mettre à jour version.json

**Manuel:**
```bash
nano binaries/version.json
# Éditer version, version_code, download_url, changelog, file_size
```

**Automatique:**
```bash
./scripts/update-version.sh 0.3.0 4 "Phase 3 complete"
```

### 4. Tester Localement

**Installer APK:**
```bash
adb install binaries/<app>-android-v0.3.0.apk
```

**Tester update flow:**
```bash
# Servir version.json localement
cd binaries
python3 -m http.server 8000

# Dans l'app Android:
# Settings → Set API URL: http://<your-ip>:8000
# Settings → Check for Updates
```

### 5. Déployer vers Serveur

**Via script:**
```bash
./scripts/deploy-binaries.sh production
# ou
./scripts/deploy-binaries.sh dev
```

**Manuel (via SCP):**
```bash
# Production
scp binaries/version.json ubuntu@api.secuops.secuaas.ca:/var/www/secuops/binaries/
scp binaries/*.apk ubuntu@api.secuops.secuaas.ca:/var/www/secuops/binaries/

# Dev
scp binaries/version.json ubuntu@api.secuops.secuaas.dev:/var/www/secuops-dev/binaries/
scp binaries/*.apk ubuntu@api.secuops.secuaas.dev:/var/www/secuops-dev/binaries/
```

### 6. Vérification

```bash
# Vérifier version.json accessible
curl -s https://api.secuops.secuaas.dev/api/version | jq .

# Vérifier APK téléchargeable
curl -I https://api.secuops.secuaas.dev/binaries/secuops-android-v0.3.0.apk

# Devrait retourner:
# HTTP/1.1 200 OK
# Content-Type: application/vnd.android.package-archive
# Content-Length: <file_size>
```

### 7. Commit & Tag Git

```bash
git add binaries/version.json binaries/*.apk
git commit -m "release: v0.3.0 - Phase 3 complete"
git tag v0.3.0
git push origin main
git push origin v0.3.0
```

### 8. GitHub Release (Optionnel mais Recommandé)

```bash
gh release create v0.3.0 \
  binaries/<app>-android-v0.3.0.apk \
  --title "SecuOps Android v0.3.0" \
  --notes "$(cat CHANGELOG.md)"
```

---

## 🔐 Sécurité

### 1. Signature APK (Production Obligatoire)

**Générer keystore (une seule fois):**
```bash
keytool -genkey -v \
  -keystore <app>-android.keystore \
  -alias <app>-release \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass <STRONG_PASSWORD> \
  -keypass <STRONG_PASSWORD>

# Sauvegarder PRECIEUSEMENT ce keystore !
# Sans lui, impossible de publier des updates sur Play Store
```

**Configuration Gradle:**
```kotlin
// app/build.gradle.kts
android {
    signingConfigs {
        create("release") {
            storeFile = file("../<app>-android.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "<app>-release"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

**Variables d'environnement (CI/CD):**
```bash
export KEYSTORE_PASSWORD="<strong_password>"
export KEY_PASSWORD="<strong_password>"
./gradlew assembleRelease
```

### 2. HTTPS Obligatoire (Production)

**UpdateManager doit vérifier:**
```kotlin
private val baseUrl = if (BuildConfig.DEBUG) {
    // HTTP OK pour développement local
    "http://api.secuops.secuaas.dev"
} else {
    // HTTPS OBLIGATOIRE en production
    "https://api.secuops.secuaas.ca"
}
```

### 3. Certificate Pinning (Optionnel, Recommandé)

```kotlin
private val httpClient = OkHttpClient.Builder()
    .certificatePinner(
        CertificatePinner.Builder()
            .add("api.secuops.secuaas.ca", "sha256/AAAAAAAAAA...")
            .add("api.secuops.secuaas.dev", "sha256/BBBBBBBBBB...")
            .build()
    )
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()
```

### 4. ProGuard/R8 (Production)

**proguard-rules.pro:**
```proguard
# Keep UpdateManager public API
-keep class com.secuaas.*.data.update.UpdateManager { *; }
-keep class com.secuaas.*.data.model.** { *; }

# Keep Gson models
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
```

---

## 📊 Convention de Nommage

### Fichiers APK

**Format standard:**
```
<app-name>-android-v<VERSION>.apk
```

**Exemples:**
- ✅ `secuops-android-v0.2.3.apk`
- ✅ `ccl-manager-v1.8.2.apk`
- ✅ `secuscan-android-v1.0.0.apk`
- ❌ `app-release.apk` (trop générique)
- ❌ `secuops-0.2.3.apk` (manque -android)
- ❌ `secuops-android-0.2.3.apk` (manque v avant version)

### QR Codes (Optionnel)

**Format:**
```
qrcode-v<VERSION>.png
```

**Exemple:**
```bash
qrencode -o binaries/qrcode-v0.2.3.png \
  "https://api.secuops.secuaas.dev/binaries/secuops-android-v0.2.3.apk"
```

---

## 🛠️ Scripts Standard (Obligatoires)

### scripts/build-release.sh

**Fonction:** Build APK release et copier dans binaries/

**Usage:**
```bash
./scripts/build-release.sh <version>
```

**Template:** Voir `/home/ubuntu/projects/secuops-android/scripts/build-release.sh`

### scripts/update-version.sh

**Fonction:** Mettre à jour version.json avec nouvelles métadonnées

**Usage:**
```bash
./scripts/update-version.sh <version> <version_code> [changelog]
```

**Template:** Voir `/home/ubuntu/projects/secuops-android/scripts/update-version.sh`

### scripts/deploy-binaries.sh

**Fonction:** Déployer binaries vers serveur dev ou production

**Usage:**
```bash
./scripts/deploy-binaries.sh [dev|production]
```

**Template:** Voir `/home/ubuntu/projects/secuops-android/scripts/deploy-binaries.sh`

---

## ✅ Checklist de Conformité

Pour qu'une application Android SecuAAS soit conforme au standard, elle DOIT avoir:

### Code Source
- [ ] `UpdateManager.kt` avec interface publique standard
- [ ] `VersionInfo.kt` et `UpdateState.kt` modèles
- [ ] SettingsScreen avec section update
- [ ] AndroidManifest.xml avec permissions requises
- [ ] file_paths.xml pour FileProvider
- [ ] Dependency Injection (Hilt recommandé)
- [ ] Coroutines + Flow pour async

### Binaries
- [ ] Répertoire `binaries/` à la racine
- [ ] `binaries/version.json` au format standard
- [ ] APK nommé selon convention
- [ ] README.md dans binaries/

### Scripts
- [ ] `scripts/build-release.sh` exécutable
- [ ] `scripts/update-version.sh` exécutable
- [ ] `scripts/deploy-binaries.sh` exécutable
- [ ] Scripts chmod +x

### Backend API
- [ ] Endpoint GET /api/version implémenté
- [ ] Endpoint GET /binaries/{filename} implémenté
- [ ] HTTPS en production
- [ ] CORS configuré si nécessaire

### Documentation
- [ ] VERSION.md avec historique
- [ ] WORK_IN_PROGRESS.md à jour
- [ ] README.md avec instructions build
- [ ] Ce document ou lien vers standard (optionnel)

### Tests
- [ ] APK build sans erreurs
- [ ] Update flow testé manuellement
- [ ] Download + Install fonctionnent
- [ ] version.json accessible depuis app

---

## 📚 Exemples de Référence

### CCL Manager
- **Repository:** `/home/ubuntu/projects/claude-app/ccl-android`
- **Version actuelle:** 1.8.2
- **Binaries:** `/home/ubuntu/projects/claude-app/binaries`
- **API:** `https://dev.secuaas.ovh/api/version`

### SecuOps Android
- **Repository:** `/home/ubuntu/projects/secuops-android`
- **Version actuelle:** 0.2.3
- **Binaries:** `/home/ubuntu/projects/secuops-android/binaries`
- **API (à venir):** `https://api.secuops.secuaas.dev/api/version`

---

## 🆕 Migration d'une App Existante

Si vous avez une application Android existante sans système d'update:

### 1. Copier Composants Standard

```bash
# Depuis SecuOps Android (référence)
cp -r secuops-android/app/src/main/kotlin/com/secuaas/secuops/data/update \
      <your-app>/app/src/main/kotlin/com/secuaas/<yourapp>/data/

cp -r secuops-android/app/src/main/kotlin/com/secuaas/secuops/data/model/Version*.kt \
      <your-app>/app/src/main/kotlin/com/secuaas/<yourapp>/data/model/

cp -r secuops-android/app/src/main/kotlin/com/secuaas/secuops/data/model/UpdateState.kt \
      <your-app>/app/src/main/kotlin/com/secuaas/<yourapp>/data/model/
```

### 2. Adapter les Package Names

```bash
# Remplacer dans tous les fichiers copiés
sed -i 's/com.secuaas.secuops/com.secuaas.<yourapp>/g' \
    <your-app>/app/src/main/kotlin/com/secuaas/<yourapp>/data/**/*.kt
```

### 3. Créer binaries/ et Scripts

```bash
mkdir -p <your-app>/binaries
mkdir -p <your-app>/scripts

cp secuops-android/binaries/README.md <your-app>/binaries/
cp secuops-android/scripts/*.sh <your-app>/scripts/
chmod +x <your-app>/scripts/*.sh

# Adapter les noms d'app dans les scripts
sed -i 's/secuops-android/<yourapp>-android/g' <your-app>/scripts/*.sh
```

### 4. Ajouter Permissions AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />

<application>
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

### 5. Créer file_paths.xml

```bash
mkdir -p <your-app>/app/src/main/res/xml
cat > <your-app>/app/src/main/res/xml/file_paths.xml <<EOF
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-files-path name="downloads" path="Download/" />
</paths>
EOF
```

### 6. Ajouter Update UI dans Settings

```kotlin
// presentation/settings/SettingsScreen.kt
@Composable
fun SettingsScreen(updateManager: UpdateManager = hiltViewModel()) {
    // ... existing settings ...

    // Add update section
    UpdateSection(updateManager)
}

@Composable
fun UpdateSection(updateManager: UpdateManager) {
    val updateState by updateManager.updateState.collectAsState()
    val scope = rememberCoroutineScope()

    // ... implement UI comme dans exemple ci-dessus ...
}
```

### 7. Injecter UpdateManager (Hilt)

```kotlin
// di/AppModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideUpdateManager(
        @ApplicationContext context: Context
    ): UpdateManager {
        return UpdateManager(context)
    }
}
```

### 8. Build Initial Release

```bash
# Build
./scripts/build-release.sh 1.0.0

# Update version.json
./scripts/update-version.sh 1.0.0 1 "Initial release with auto-update support"

# Deploy
./scripts/deploy-binaries.sh production
```

---

## 📞 Support & Questions

**Documentation complète:**
- Ce document
- `/home/ubuntu/projects/secuops-android/binaries/README.md`
- `/home/ubuntu/projects/claude-app/binaries/README.md`

**Contact:**
- Email: devops@secuaas.ca
- Slack: #secuaas-android

---

## 🔄 Historique du Standard

| Version | Date | Changements |
|---------|------|-------------|
| 1.0.0 | 2026-02-06 | Première version officielle basée sur CCL Manager + SecuOps Android |

---

**Fin du Standard SecuAAS pour Applications Android**

Ce standard est **obligatoire** pour toutes les nouvelles applications Android SecuAAS et **fortement recommandé** pour la migration des apps existantes.

Suivre ce standard garantit une expérience utilisateur cohérente et simplifie grandement la maintenance et le déploiement.

---

**Auteur:** Équipe SecuAAS DevOps + Claude Sonnet 4.5
**Dernière mise à jour:** 2026-02-06
**Version du document:** 1.0.0
