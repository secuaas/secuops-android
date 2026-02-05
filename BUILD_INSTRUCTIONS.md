# SecuOps Android - Build Instructions

## Prérequis

### 1. Android Studio (Recommandé)

**Télécharger:** https://developer.android.com/studio

Android Studio inclut:
- Android SDK
- Android SDK Build-Tools
- Android Emulator
- Gradle

**Installation:**
```bash
# Linux
wget https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2023.1.1.28/android-studio-2023.1.1.28-linux.tar.gz
tar -xzf android-studio-*.tar.gz
cd android-studio/bin
./studio.sh
```

### 2. Ligne de Commande (Alternative)

**Dépendances:**
- JDK 17 ✅ (Déjà installé: OpenJDK 17.0.18)
- Android SDK Command Line Tools
- Gradle 8.2 ✅ (Wrapper configuré)

**Installer Android SDK:**
```bash
# Télécharger Command Line Tools
mkdir -p ~/Android/Sdk
cd ~/Android/Sdk
wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
unzip commandlinetools-linux-9477386_latest.zip
mkdir -p cmdline-tools/latest
mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null

# Configurer environnement
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools

# Accepter licences
yes | sdkmanager --licenses

# Installer SDK requis
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

**Ajouter à ~/.bashrc:**
```bash
echo 'export ANDROID_HOME=$HOME/Android/Sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools' >> ~/.bashrc
source ~/.bashrc
```

---

## Build avec Android Studio

### 1. Ouvrir le Projet

1. Lancer Android Studio
2. File → Open
3. Sélectionner `/home/ubuntu/projects/secuops-android`
4. Attendre la synchronisation Gradle (première fois: ~5-10 min)

### 2. Configurer l'API URL (Optionnel)

Modifier `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://api.secuops.secuaas.dev\"")
```

### 3. Build APK

**Via Interface:**
- Build → Build Bundle(s) / APK(s) → Build APK(s)
- Attendre la compilation (~3-5 min)
- APK généré dans: `app/build/outputs/apk/debug/app-debug.apk`

**Via Terminal (dans Android Studio):**
```bash
./gradlew assembleDebug
```

### 4. Installer sur Device

**Via Android Studio:**
- Connecter device via USB (ou lancer émulateur)
- Run → Run 'app' (ou Shift+F10)

**Via ADB:**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Build en Ligne de Commande

### 1. Cloner le Repository

```bash
cd /home/ubuntu/projects
git clone https://github.com/secuaas/secuops-android.git
cd secuops-android
```

### 2. Vérifier Gradle

```bash
./gradlew --version
```

**Output attendu:**
```
Gradle 8.2
Kotlin: 1.9.22
JVM: 17.0.18
```

### 3. Clean Build

```bash
./gradlew clean
```

### 4. Build Debug APK

```bash
./gradlew assembleDebug
```

**Durée:** ~3-5 minutes (première fois: ~10-15 min)

**Output:**
```
BUILD SUCCESSFUL in 3m 42s
123 actionable tasks: 123 executed
```

**APK généré:**
```
app/build/outputs/apk/debug/app-debug.apk
```

### 5. Build Release APK (Signé)

#### Créer Keystore (une seule fois)

```bash
keytool -genkey -v -keystore secuops.keystore \
  -alias secuops \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass SecuOps@2024! \
  -keypass SecuOps@2024! \
  -dname "CN=SecuAAS, OU=Engineering, O=SecuAAS, L=Paris, ST=IDF, C=FR"
```

#### Configurer Signing

Créer `app/keystore.properties`:
```properties
storePassword=SecuOps@2024!
keyPassword=SecuOps@2024!
keyAlias=secuops
storeFile=../secuops.keystore
```

Ajouter au `.gitignore`:
```bash
echo "*.keystore" >> .gitignore
echo "keystore.properties" >> .gitignore
```

Modifier `app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("app/keystore.properties")
            val keystoreProperties = Properties()
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))

            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

#### Build Release

```bash
./gradlew assembleRelease
```

**APK signé:**
```
app/build/outputs/apk/release/app-release.apk
```

---

## Installer l'APK

### Via ADB (Android Debug Bridge)

**Connecter device:**
```bash
# Vérifier connexion
adb devices

# Devrait afficher:
# List of devices attached
# ABCD1234    device
```

**Installer:**
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Options:**
- `-r`: Remplacer l'app si déjà installée
- `-d`: Installer sur device USB
- `-e`: Installer sur émulateur

### Via Transfert Manuel

1. Copier APK vers device (USB, email, cloud)
2. Sur device: ouvrir Fichiers
3. Cliquer sur APK
4. Autoriser installation sources inconnues
5. Installer

---

## Lancer l'Application

### Sur Device Physique

1. Activer Developer Options:
   - Settings → About phone
   - Tapper 7x sur "Build number"
2. Activer USB Debugging:
   - Settings → Developer Options → USB Debugging
3. Connecter via USB
4. Autoriser debugging sur device

### Sur Émulateur Android

**Créer AVD (Android Virtual Device):**
```bash
# Lister AVD disponibles
emulator -list-avds

# Créer nouveau AVD
avdmanager create avd -n Pixel_7_API_34 -k "system-images;android-34;google_apis;x86_64"

# Lancer émulateur
emulator -avd Pixel_7_API_34
```

**Via Android Studio:**
1. Tools → Device Manager
2. Create Device
3. Sélectionner: Pixel 7
4. Image système: API 34 (Android 14)
5. Launch

---

## Tester l'Application

### Credentials de Test

**Backend API:** `https://api.secuops.secuaas.dev`

**Login:**
- Email: `admin@secuaas.com`
- Password: `SecuaaS@2024!`

### Workflow de Test

1. **Login Screen**
   - Entrer credentials
   - Vérifier stockage JWT token
   - Navigation vers Dashboard

2. **Dashboard**
   - Vérifier affichage 8 cards
   - Tester navigation vers chaque module

3. **Applications**
   - Pull-to-refresh
   - Expand card pour détails
   - Test Restart button

4. **Infrastructure**
   - Tester 4 tabs (Pods, Services, Ingresses, Certificates)
   - Filtrer par environment
   - Vérifier status colors

5. **Deployments**
   - Filtrer par status
   - Expand pour voir commit details
   - Test Retry sur deployment failed

6. **Projects**
   - Voir liste projets
   - Expand pour repos
   - Vérifier catégories

7. **Domains**
   - Filtrer par zone/type
   - Test delete record
   - Vérifier confirmation dialog

8. **Servers**
   - Expand pour détails serveur
   - Test reboot
   - Vérifier coûts

9. **Billing**
   - Vérifier summary card
   - Liste invoices
   - Test download PDF

---

## Troubleshooting

### Build Failures

**Erreur: SDK not found**
```bash
# Installer Android SDK
sdkmanager "platforms;android-34"
```

**Erreur: Gradle sync failed**
```bash
# Clean et rebuild
./gradlew clean
./gradlew build --refresh-dependencies
```

**Erreur: Out of memory**
```bash
# Augmenter heap dans gradle.properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m
```

### Runtime Issues

**Erreur: Network error**
- Vérifier connexion internet
- Vérifier URL API dans build.gradle.kts
- Vérifier backend API accessible

**Erreur: Login failed**
- Vérifier credentials
- Check backend logs
- Vérifier JWT token format

**Erreur: App crash au démarrage**
- Check logcat: `adb logcat | grep SecuOps`
- Vérifier permissions dans AndroidManifest.xml

---

## Logs et Debug

### Logcat

```bash
# Tous les logs
adb logcat

# Filter par app
adb logcat | grep com.secuaas.secuops

# Filter par tag
adb logcat -s SecuOps

# Clear logs
adb logcat -c
```

### Debug Build

```bash
# Build avec stack traces
./gradlew assembleDebug --stacktrace

# Build avec info level
./gradlew assembleDebug --info

# Build avec debug level
./gradlew assembleDebug --debug
```

---

## Performance

### Build Time

- **Clean build:** ~10-15 minutes
- **Incremental build:** ~1-3 minutes
- **APK size:** ~15-20 MB

### Optimizations

**Enable Build Cache:**
```properties
# gradle.properties
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.daemon=true
```

**Use Kotlin Compiler Daemon:**
```properties
kotlin.compiler.execution.strategy=in-process
kotlin.incremental=true
```

---

## CI/CD (Future)

### GitHub Actions

Créer `.github/workflows/android.yml`:
```yaml
name: Android CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'adopt'
    - name: Build with Gradle
      run: ./gradlew assembleDebug
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

---

## Distribution

### Google Play Store (Production)

1. Créer compte Google Play Developer ($25)
2. Créer app dans Console
3. Build Release APK signé
4. Upload APK
5. Remplir store listing
6. Submit pour review

### Internal Testing (Beta)

1. Build signed APK
2. Distribuer via:
   - Firebase App Distribution
   - Google Play Internal Testing
   - Direct APK download

---

## Support

**Questions:** Consulter CLAUDE.md et README.md
**Repository:** https://github.com/secuaas/secuops-android
**Issues:** https://github.com/secuaas/secuops-android/issues
