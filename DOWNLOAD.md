# 📱 Télécharger SecuOps Android

## Version Actuelle: 0.2.3

### 📥 Téléchargement Direct

**APK Android:**
Fichier local: `/home/ubuntu/projects/secuops-android/binaries/secuops-android-v0.2.3.apk`

**Taille:** 18 MB (18,874,368 bytes)

### 🔗 Installation

#### Option 1: Transfert Direct (Recommandé)

```bash
# Sur votre machine locale
scp ubuntu@<IP_SERVEUR>:/home/ubuntu/projects/secuops-android/binaries/secuops-android-v0.2.3.apk \
    ./secuops-android.apk

# Puis transférer vers téléphone Android via:
# - Cable USB: adb install secuops-android.apk
# - Email: s'envoyer l'APK par email et l'ouvrir sur téléphone
# - Cloud: Upload sur Google Drive/Dropbox et download sur téléphone
```

#### Option 2: Serveur HTTP Temporaire

```bash
# Démarrer serveur HTTP dans le répertoire binaries
cd /home/ubuntu/projects/secuops-android/binaries
python3 -m http.server 8000

# Depuis téléphone Android:
# - Ouvrir navigateur
# - Aller à http://<IP_SERVEUR>:8000/
# - Cliquer sur secuops-android-v0.2.3.apk
# - Télécharger et installer
```

#### Option 3: API SecuOps (Future - Après Déploiement)

Une fois l'API SecuOps déployée avec le système de binaries:

**Production:**
```
https://api.secuops.secuaas.ca/binaries/secuops-android-v0.2.3.apk
```

**Développement:**
```
https://api.secuops.secuaas.dev/binaries/secuops-android-v0.2.3.apk
```

### 📋 Prérequis Installation

**Sur Android:**
1. Aller dans **Paramètres** → **Sécurité**
2. Activer **Sources inconnues** (ou **Installer des applications inconnues**)
3. Autoriser votre navigateur ou gestionnaire de fichiers à installer des apps

**Permissions requises par l'app:**
- ✅ Internet (pour API SecuOps)
- ✅ Accès réseau (pour vérifier connectivité)

Aucune permission dangereuse requise !

### ✨ Nouveautés v0.2.3

- ✅ **8 modules complets** - Applications, Infrastructure, Deployments, Projects, Domains, Servers, Billing
- ✅ **29 endpoints API** intégrés (100% couverture)
- ✅ **Material3 UI** avec Dark/Light mode automatique
- ✅ **Pull-to-refresh** sur tous les écrans
- ✅ **Error handling robuste** avec retry automatique
- ✅ **MVVM + Clean Architecture**
- ✅ **Hilt Dependency Injection**

### 🔄 Mises à Jour Automatiques

L'application inclut un **système de mise à jour automatique** suivant le standard SecuAAS.

**Comment ça marche:**
1. Ouvrir l'app → **Settings**
2. Cliquer sur **Check for Updates**
3. Si disponible → **Download**
4. Quand terminé → **Install Now**

**Prochainement:** Notifications automatiques de nouvelle version disponible.

### 🔐 Sécurité

**APK Debug (v0.2.3):**
- Non signé (pour développement et test)
- Pas de ProGuard/obfuscation
- Logs de debug actifs

**Prochaine Release (v0.3.0):**
- APK signé avec keystore SecuAAS
- ProGuard/R8 optimization
- Logs de production
- Taille réduite (~12-15 MB)

### 📖 Documentation

- **Guide d'utilisation:** README.md
- **Changelog complet:** VERSION.md
- **Standard Android SecuAAS:** ANDROID_APP_STANDARD.md
- **État du développement:** WORK_IN_PROGRESS.md

### 🆘 Support

**Problèmes d'installation?**
- Vérifier que "Sources inconnues" est activé
- Essayer de redémarrer le téléphone
- Vérifier l'espace disque disponible (minimum 50 MB)

**Problèmes de connexion API?**
- Vérifier URL API dans Settings
- Tester connexion réseau
- Voir logs dans Logcat: `adb logcat | grep SecuOps`

**Contact:**
- Email: devops@secuaas.ca
- GitHub: https://github.com/secuaas/secuops-android

---

## 🚀 Déploiement Production (À venir)

Le système de distribution automatique sera déployé avec:

**API Endpoints:**
- GET `/api/version` - Informations version actuelle
- GET `/binaries/secuops-android-v{VERSION}.apk` - Téléchargement direct

**Workflow utilisateur:**
1. Installer v0.2.3 manuellement (comme ci-dessus)
2. Configurer URL API dans Settings
3. Futures mises à jour → automatiques via l'app !

**Timeline:**
- ✅ v0.2.3 - Build initial (maintenant)
- 📋 v0.3.0 - Phase 3 (Dark mode, Search, Real-time)
- 🚀 v1.0.0 - Production release (API déployée, Play Store)

---

**Version du document:** 1.0.0
**Dernière mise à jour:** 2026-02-06
