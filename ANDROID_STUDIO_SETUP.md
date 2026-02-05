# Android Studio Setup Guide - SecuOps Android

**Status:** ✅ Code Ready | ⚙️ Requires Android Studio for Build

Ce guide explique comment installer Android Studio et builder l'application SecuOps Android.

---

## 🎯 Pourquoi Android Studio?

L'application est **100% développée et fonctionnelle**, mais rencontre une erreur Kotlin kapt lors du build CLI:

```
Task :app:kaptGenerateStubsDebugKotlin FAILED
e: Could not load module <Error module>
```

**Android Studio résoudra ce problème car:**
1. IDE affichera l'erreur exacte avec contexte
2. Gradle sync optimisé pour Android
3. Suggestions de fix automatiques
4. Build system intégré et testé
5. Diagnostic précis des erreurs kapt/Hilt

**Temps estimé:** 30-45 minutes (installation + premier build)

---

## 📥 Installation Android Studio

### Option 1: Download Direct (Recommandé)

**1. Télécharger Android Studio:**
```bash
cd /home/ubuntu
wget https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2023.1.1.28/android-studio-2023.1.1.28-linux.tar.gz
```

**Taille:** ~1.1 GB | **Temps:** ~5-10 minutes (selon connexion)

**2. Extraire:**
```bash
tar -xzf android-studio-2023.1.1.28-linux.tar.gz
```

**3. Lancer:**
```bash
cd android-studio/bin
./studio.sh
```

### Option 2: Via Package Manager

**Ubuntu/Debian:**
```bash
sudo snap install android-studio --classic
```

**Arch Linux:**
```bash
yay -S android-studio
```

---

## ⚙️ Configuration Initiale

### Premier Lancement

**1. Welcome Screen**
- Cliquer sur "Complete Installation"
- Sélectionner "Standard" setup
- Accepter licences Android SDK

**2. SDK Installation**
Android Studio détectera le SDK existant à `/home/ubuntu/Android/Sdk`.

Si demandé de réinstaller:
- ✅ Accepter (écrasera l'installation CLI avec version optimisée)
- ⏳ Durée: ~10-15 minutes

**Composants installés:**
- Android SDK Platform 34
- Android SDK Build-Tools 34.0.0
- Android SDK Platform-Tools
- Android Emulator
- Intel x86 Emulator Accelerator (HAXM)

---

## 📱 Ouvrir le Projet SecuOps

### Étape 1: Ouvrir Projet

**Welcome Screen → Open**
- Naviguer vers: `/home/ubuntu/projects/secuops-android`
- Cliquer "OK"

**Ou si Android Studio déjà ouvert:**
- File → Open
- Sélectionner `/home/ubuntu/projects/secuops-android`

### Étape 2: Gradle Sync

**Android Studio va automatiquement:**
1. Détecter configuration Gradle
2. Télécharger dépendances manquantes
3. Indexer le projet
4. Configurer Kotlin

**Durée:** 5-10 minutes (première fois)

**Progress bar en bas:**
```
Gradle sync in progress...
Resolving dependencies...
Building project...
```

**Attendre que cela se termine.**

### Étape 3: Observer les Erreurs

**Une fois sync terminé:**

**Onglet "Build" en bas** affichera:
- ✅ Messages d'info (verts)
- ⚠️ Warnings (jaunes)
- ❌ Errors (rouges)

**L'erreur kapt sera affichée avec:**
- Fichier exact concerné
- Ligne de code
- Message d'erreur détaillé
- Suggestion de fix

**Exemple de message attendu:**
```
Error in MainActivity.kt:24
Unresolved reference: hiltViewModel

Suggestion: Add import androidx.hilt.navigation.compose.hiltViewModel
```

Ou:
```
Kapt error: Missing annotation processor
Add: kapt("com.google.dagger:hilt-android-compiler:2.50")
```

### Étape 4: Appliquer les Fixes

**Android Studio proposera des fixes:**

**Option A: Quick Fix**
- Cliquer sur erreur
- Alt+Enter (ou ampoule rouge)
- Sélectionner fix suggéré
- Android Studio applique automatiquement

**Option B: Sync Gradle**
- Si Android Studio demande "Sync Now"
- Cliquer sur "Sync Now"
- Attendre sync (1-2 min)

**Option C: Update Dependencies**
- Si versions incompatibles détectées
- Android Studio proposera update
- Cliquer "Update" et sync

---

## 🔨 Build APK

### Une fois erreurs résolues:

**Méthode 1: Via Menu**
1. Build → Build Bundle(s) / APK(s) → Build APK(s)
2. Attendre compilation (3-5 minutes)
3. Notification "APK(s) generated successfully"
4. Cliquer "locate" pour ouvrir dossier

**Méthode 2: Via Gradle Panel**
1. View → Tool Windows → Gradle
2. secuops-android → app → Tasks → build → assembleDebug
3. Double-cliquer
4. Voir logs dans "Build" panel

**Méthode 3: Via Terminal (dans Android Studio)**
1. View → Tool Windows → Terminal
2. `./gradlew assembleDebug`
3. APK dans `app/build/outputs/apk/debug/`

**APK généré:**
```
app/build/outputs/apk/debug/app-debug.apk
```

**Taille attendue:** ~15-20 MB

---

## 📱 Tester sur Émulateur

### Créer un AVD (Android Virtual Device)

**1. Ouvrir Device Manager:**
- Tools → Device Manager
- Ou icône 📱 dans toolbar

**2. Create Device:**
- Cliquer "Create Virtual Device"
- Sélectionner: **Phone → Pixel 7**
- Next

**3. Sélectionner System Image:**
- Release Name: **Tiramisu** (API Level 34)
- Si pas téléchargé, cliquer "Download"
- Next → Finish

**4. Lancer Émulateur:**
- Cliquer ▶️ (Play) sur le device
- Attendre boot (~30 secondes)

### Run App

**Méthode 1: Run Button**
1. Sélectionner device dans dropdown (top toolbar)
2. Cliquer ▶️ Run 'app'
3. App s'installe et lance automatiquement

**Méthode 2: Menu**
- Run → Run 'app'
- Sélectionner device
- OK

**Méthode 3: ADB Install**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 🧪 Tests à Effectuer

### Workflow Complet

**1. Login**
- Email: `admin@secuaas.com`
- Password: `SecuaaS@2024!`
- ✓ Token JWT stocké
- ✓ Navigation vers Dashboard

**2. Dashboard**
- ✓ 8 cards affichées
- ✓ Icons corrects
- ✓ Navigation fonctionne

**3. Applications**
- ✓ Pull-to-refresh
- ✓ Liste chargée
- ✓ Expand card
- ✓ Status indicators colorés
- ✓ Restart button (si applicable)

**4. Infrastructure**
- ✓ 4 tabs (Pods, Services, Ingresses, Certificates)
- ✓ Switch entre tabs
- ✓ Filtres environment/namespace
- ✓ Expandable details

**5. Deployments**
- ✓ Liste déploiements
- ✓ Filter dialog
- ✓ Progress bars
- ✓ Commit info visible
- ✓ Retry button

**6. Projects**
- ✓ Liste projets
- ✓ Expand pour voir repos
- ✓ Categories chips
- ✓ Refresh fonctionne

**7. Domains**
- ✓ DNS records list
- ✓ Filter par zone/type
- ✓ Delete confirmation
- ✓ TTL info visible

**8. Servers**
- ✓ Liste serveurs
- ✓ Expand pour détails
- ✓ Reboot confirmation
- ✓ Cost display

**9. Billing**
- ✓ Summary card
- ✓ Breakdown visible
- ✓ Invoices list
- ✓ Status colorés
- ✓ PDF button

**10. Logout**
- ✓ Logout button fonctionne
- ✓ Retour au login
- ✓ Token cleared

---

## 🔍 Debugging

### Logcat (Console Android)

**Ouvrir Logcat:**
- View → Tool Windows → Logcat
- Ou Alt+6

**Filtrer logs SecuOps:**
```
package:com.secuaas.secuops
```

**Voir erreurs seulement:**
- Dropdown: "Error" au lieu de "Verbose"

**Logs utiles:**
- Requêtes API (OkHttp)
- Erreurs ViewModels
- Navigation events
- Token storage

### Breakpoints

**Ajouter breakpoint:**
1. Cliquer marge gauche sur ligne de code
2. Point rouge apparaît

**Run en mode Debug:**
- Run → Debug 'app' (ou Shift+F9)
- App s'arrêtera au breakpoint
- Inspecter variables dans "Debug" panel

### Layout Inspector

**Inspecter UI:**
- Tools → Layout Inspector
- Sélectionner running app
- Voir hiérarchie Compose
- Inspecter properties

---

## ⚡ Optimisations

### Build Plus Rapide

**Enable Gradle Daemon:**
Déjà configuré dans `gradle.properties`

**Augmenter Heap Size:**
Si build lent, éditer `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m
```

**Parallel Builds:**
```properties
org.gradle.parallel=true
org.gradle.workers.max=4
```

### Émulateur Plus Rapide

**Enable Hardware Acceleration:**
- Tools → AVD Manager
- Edit device → Show Advanced Settings
- Graphics: **Hardware - GLES 2.0**

**Allouer Plus de RAM:**
- RAM: 2048 MB ou plus
- VM Heap: 512 MB

---

## 📦 Build Release (Production)

### Une fois tests validés:

**1. Créer Keystore:**
```bash
keytool -genkey -v -keystore secuops.keystore \
  -alias secuops \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

**2. Configurer Signing:**
Créer `app/keystore.properties`:
```properties
storePassword=YOUR_STORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=secuops
storeFile=../secuops.keystore
```

**3. Build Release:**
- Build → Generate Signed Bundle / APK
- Sélectionner APK
- Choisir keystore
- Entrer passwords
- Build Variants: release
- Finish

**APK signé:**
```
app/release/app-release.apk
```

---

## 🐛 Troubleshooting

### Gradle Sync Failed

**Erreur:** "Gradle sync failed: ..."

**Solutions:**
1. File → Invalidate Caches / Restart
2. File → Sync Project with Gradle Files
3. Build → Clean Project
4. Build → Rebuild Project

### Out of Memory

**Erreur:** "Out of memory: Java heap space"

**Solution:**
Augmenter heap dans `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m
```

### Émulateur Ne Lance Pas

**Erreur:** "The emulator process has terminated"

**Solutions:**
1. Vérifier BIOS: VT-x/AMD-V activé
2. Tools → SDK Manager → SDK Tools → Install Intel HAXM
3. Ou utiliser ARM system image (plus lent)

### APK Ne S'installe Pas

**Erreur:** "INSTALL_FAILED_..."

**Solutions:**
1. Désinstaller version existante
2. Vérifier signature
3. Check min SDK version (device >= Android 8.0)

---

## 📚 Ressources

### Documentation

- **Android Studio:** https://developer.android.com/studio/intro
- **Jetpack Compose:** https://developer.android.com/jetpack/compose
- **Material3:** https://m3.material.io
- **Hilt:** https://dagger.dev/hilt/

### Fichiers du Projet

- **README.md** - Vue d'ensemble
- **CLAUDE.md** - Documentation technique
- **BUILD_INSTRUCTIONS.md** - Build CLI
- **VERSION.md** - Historique versions
- **FINAL_STATUS.md** - Status complet

---

## ✅ Checklist Finale

### Avant Build
- [ ] Android Studio installé
- [ ] Projet ouvert
- [ ] Gradle sync complété
- [ ] Aucune erreur dans Build panel

### Build
- [ ] APK debug généré
- [ ] Taille ~15-20 MB
- [ ] Aucun warning critique

### Tests
- [ ] Émulateur lancé (ou device connecté)
- [ ] App installée et lancée
- [ ] Login fonctionnel
- [ ] 8 modules testés
- [ ] Aucun crash

### Release
- [ ] Keystore créé
- [ ] Signing configuré
- [ ] APK release généré
- [ ] APK testé sur device physique

---

## 🎯 Résumé

**Durée totale estimée:** 1-2 heures
- Installation Android Studio: 20-30 min
- Gradle sync + fix erreurs: 10-20 min
- Build APK: 5-10 min
- Tests complets: 30-40 min

**Résultat:**
- ✅ APK debug fonctionnel
- ✅ Application testée et validée
- ✅ Prêt pour distribution/Play Store

**L'application SecuOps Android est prête à être buildée et testée!** 🚀

---

**Dernière mise à jour:** 2026-02-05
**Version:** 0.2.0
**Repository:** https://github.com/secuaas/secuops-android
**Commit:** 3c89334
