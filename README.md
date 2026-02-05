# SecuOps Manager - Android App

Application Android native pour la gestion complète de l'infrastructure SecuOps.

## 🚀 Fonctionnalités

### ✅ Implémentées
- **Authentification JWT** - Login sécurisé avec token
- **Dashboard** - Vue d'ensemble de l'infrastructure
- **Gestion des Applications** - Liste, restart, scale des applications déployées
- **Architecture MVVM** - Clean architecture avec Jetpack Compose

### 🚧 En Cours de Développement
- **Déploiements** - Gestion et monitoring des déploiements
- **Projets** - CRUD des projets
- **Infrastructure** - Monitoring temps réel (Pods, Services, Ingress, Certificates)
- **Domaines DNS** - Gestion des enregistrements DNS
- **Serveurs/VPS** - Liste et gestion des serveurs
- **Facturation** - Suivi des factures et coûts

## 📱 Screenshots

_Coming soon_

## 🛠️ Stack Technique

- **Langage**: Kotlin
- **UI**: Jetpack Compose + Material3
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt
- **Networking**: Retrofit + OkHttp
- **Serialization**: Kotlin Serialization
- **Async**: Kotlin Coroutines + Flow
- **Storage**: DataStore (JWT tokens)
- **Navigation**: Navigation Compose

## 🔧 Setup

### Prérequis
- Android Studio Hedgehog (2023.1.1) ou plus récent
- JDK 17
- Android SDK 34
- Kotlin 1.9.22

### Configuration

1. Cloner le repository:
```bash
git clone https://github.com/secuaas/secuops-android.git
cd secuops-android
```

2. Ouvrir dans Android Studio

3. Synchroniser Gradle

4. Lancer l'application sur un émulateur ou device

### Configuration API

Les URLs de l'API sont configurées dans `app/build.gradle.kts`:
- **API Backend**: `https://api.secuops.secuaas.dev`
- **Engine API**: `https://engine.secuops.secuaas.dev`

Pour modifier, éditer:
```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://api.secuops.secuaas.dev\"")
```

## 🔐 Authentification

L'app utilise JWT pour l'authentification. Les credentials par défaut:
- **Email**: `admin@secuaas.com`
- **Password**: `SecuaaS@2024!`

Le token est stocké de manière sécurisée via DataStore et ajouté automatiquement à chaque requête API.

## 📁 Structure du Projet

```
app/src/main/java/com/secuaas/secuops/
├── data/
│   ├── local/              # Storage local (TokenManager)
│   ├── model/              # Data models (User, Application, etc.)
│   ├── remote/             # API interface (Retrofit)
│   └── repository/         # Repositories (single source of truth)
├── di/                     # Dependency Injection (Hilt modules)
├── domain/                 # Use cases (business logic)
├── presentation/           # UI Layer (Compose)
│   ├── auth/               # Login, Register
│   ├── dashboard/          # Dashboard principal
│   ├── applications/       # Gestion applications
│   ├── deployments/        # Gestion déploiements
│   ├── projects/           # Gestion projets
│   ├── infrastructure/     # Monitoring infrastructure
│   ├── domains/            # Gestion DNS
│   ├── servers/            # Gestion serveurs
│   └── billing/            # Facturation
├── ui/theme/               # Material3 theme
├── utils/                  # Utilities (Resource, Constants)
├── MainActivity.kt         # Point d'entrée + Navigation
└── SecuOpsApplication.kt   # Application class (Hilt)
```

## 🔄 Flux de Données

```
UI (Compose) ← ViewModel ← Repository ← API/Local Storage
      ↓           ↓             ↓
   User Events  StateFlow   Flow<Resource<T>>
```

## 🧪 Tests

_À venir_

## 📦 Build

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

L'APK sera généré dans: `app/build/outputs/apk/`

## 🚀 Déploiement

### Signature de l'APK

1. Créer un keystore:
```bash
keytool -genkey -v -keystore secuops.keystore -alias secuops -keyalg RSA -keysize 2048 -validity 10000
```

2. Configurer dans `app/build.gradle.kts`:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("secuops.keystore")
        storePassword = "password"
        keyAlias = "secuops"
        keyPassword = "password"
    }
}
```

3. Build signed:
```bash
./gradlew assembleRelease
```

## 📊 API Endpoints Utilisés

### Authentification
- `POST /api/auth/login` - Login
- `POST /api/auth/change-password` - Changer mot de passe
- `GET /api/auth/me` - User actuel

### Applications
- `GET /api/applications` - Liste applications
- `GET /api/applications/{name}` - Détail application
- `POST /api/applications/{name}/restart` - Restart
- `POST /api/applications/{name}/scale` - Scale replicas

### Déploiements
- `GET /api/deployments` - Liste déploiements
- `POST /api/deployments/new` - Nouveau déploiement
- `GET /api/deployments/{id}` - Détail déploiement

### Infrastructure
- `GET /api/infrastructure/pods` - Liste pods
- `GET /api/infrastructure/services` - Liste services
- `GET /api/infrastructure/certificates` - Liste certificats

### Autres
- Domaines, Serveurs, Billing, Projects...

## 🤝 Contribution

Contributions bienvenues! Créer une issue ou PR.

## 📄 Licence

Propriété de SecuAAS - Tous droits réservés

## 👨‍💻 Développeur

Développé avec ❤️ par l'équipe SecuAAS

## 📞 Support

support@secuaas.com
