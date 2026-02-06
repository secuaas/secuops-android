# SecuOps Manager - Android App

## Contexte Utilisateur et Débugage

**Utilisateur:** Olivier - 25 ans d'expérience, Maîtrise en informatique

### Règles de Débugage
1. **Ne JAMAIS présumer une erreur utilisateur** - Toujours chercher la cause technique
2. **Toujours demander/analyser les logs** - Source de vérité pour comprendre les problèmes
3. **Analyser le code** - Chercher la cause racine, pas des contournements
4. **Être technique** - L'utilisateur comprend les détails, pas besoin de simplifier

---



Application Android native pour la gestion complète de l'infrastructure SecuOps.

## 🎯 Objectif

Fournir une application mobile permettant aux administrateurs de gérer l'intégralité de l'infrastructure SecuOps depuis leur smartphone:
- Authentification sécurisée (JWT)
- Gestion des applications déployées
- Monitoring en temps réel de l'infrastructure Kubernetes
- Gestion des déploiements
- Administration des domaines DNS
- Suivi des serveurs/VPS
- Gestion de la facturation

## 🛠️ Stack Technique

- **Langage**: Kotlin 1.9.22
- **UI Framework**: Jetpack Compose + Material3
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt (Dagger)
- **Networking**: Retrofit 2.9.0 + OkHttp 4.12.0
- **Serialization**: Kotlin Serialization
- **Async Programming**: Kotlin Coroutines + Flow
- **Local Storage**: DataStore (Preferences)
- **Navigation**: Navigation Compose
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

## 📁 Structure du Projet

```
app/src/main/java/com/secuaas/secuops/
├── data/                   # Data Layer
│   ├── local/             # Local storage (TokenManager)
│   ├── model/             # Data models (serializable)
│   ├── remote/            # API interfaces (Retrofit)
│   └── repository/        # Repositories (single source of truth)
├── domain/                 # Domain Layer (use cases)
├── presentation/           # Presentation Layer (UI + ViewModels)
│   ├── auth/              # Authentication screens
│   ├── dashboard/         # Main dashboard
│   ├── applications/      # Applications management
│   ├── deployments/       # Deployments management
│   ├── projects/          # Projects management
│   ├── infrastructure/    # Infrastructure monitoring
│   ├── domains/           # DNS domains management
│   ├── servers/           # Servers/VPS management
│   └── billing/           # Billing management
├── di/                     # Dependency Injection (Hilt modules)
├── ui/theme/               # Material3 theme configuration
├── utils/                  # Utility classes
├── MainActivity.kt         # Main activity + Navigation
└── SecuOpsApplication.kt   # Application class (Hilt entry point)
```

## 🔐 Authentification

L'app utilise l'authentification JWT:
1. Login avec email/password via `/api/auth/login`
2. Token JWT stocké de manière sécurisée dans DataStore
3. Token ajouté automatiquement dans le header `Authorization: Bearer <token>`
4. Gestion du refresh token (TODO)

### Credentials par Défaut
- Email: `admin@secuaas.com`
- Password: `SecuaaS@2024!`

## 🌐 API Endpoints

### Base URLs
- **API Backend**: `https://api.secuops.secuaas.dev`
- **Engine API**: `https://engine.secuops.secuaas.dev`

Configurés dans `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://api.secuops.secuaas.dev\"")
```

### Principaux Endpoints

**Authentification:**
- `POST /api/auth/login` - Login
- `POST /api/auth/change-password` - Changer mot de passe
- `GET /api/auth/me` - User actuel

**Applications:**
- `GET /api/applications` - Liste
- `POST /api/applications/{name}/restart` - Restart
- `POST /api/applications/{name}/scale` - Scale

**Déploiements:**
- `GET /api/deployments` - Liste
- `POST /api/deployments/new` - Nouveau
- `GET /api/deployments/{id}` - Détail

**Infrastructure:**
- `GET /api/infrastructure/pods` - Pods
- `GET /api/infrastructure/services` - Services
- `GET /api/infrastructure/certificates` - Certificats

**Autres:**
- Projects, Domains, Servers, Billing

## 🏗️ Architecture MVVM

### Flux de Données
```
UI (Compose) ← ViewModel ← Repository ← API/Local Storage
      ↓           ↓             ↓
   User Events  StateFlow   Flow<Resource<T>>
```

### Resource Wrapper
Toutes les réponses API sont wrappées dans `Resource<T>`:
```kotlin
sealed class Resource<T> {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String) : Resource<T>(message = message)
    class Loading<T> : Resource<T>()
}
```

### Exemple ViewModel
```kotlin
@HiltViewModel
class ApplicationsViewModel @Inject constructor(
    private val repository: SecuOpsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ApplicationsState>(ApplicationsState.Loading)
    val state: StateFlow<ApplicationsState> = _state.asStateFlow()

    fun loadApplications() {
        viewModelScope.launch {
            repository.getApplications().collect { resource ->
                _state.value = when (resource) {
                    is Resource.Loading -> ApplicationsState.Loading
                    is Resource.Success -> ApplicationsState.Success(resource.data)
                    is Resource.Error -> ApplicationsState.Error(resource.message)
                }
            }
        }
    }
}
```

## 🎨 UI/UX Guidelines

### Material3 Design
- **Theme**: Material3 avec support Dark/Light mode
- **Colors**:
  - Primary: Green (#4CAF50) - SecuOps brand color
  - Secondary: Teal (#03DAC5)
- **Typography**: Default Material3 typography
- **Components**: Material3 components (Card, Button, TextField, etc.)

### Composables Pattern
```kotlin
@Composable
fun MyScreen(
    onNavigateBack: () -> Unit,
    viewModel: MyViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(topBar = { /* AppBar */ }) { paddingValues ->
        // Content
    }
}
```

## 📦 Dépendances Principales

```kotlin
// Core
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

// Compose
implementation(platform("androidx.compose:compose-bom:2024.02.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.7")

// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")

// Hilt
implementation("com.google.dagger:hilt-android:2.50")
kapt("com.google.dagger:hilt-android-compiler:2.50")

// DataStore
implementation("androidx.datastore:datastore-preferences:1.0.0")
```

## 🚀 Build & Deploy

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Signature APK
1. Créer keystore:
```bash
keytool -genkey -v -keystore secuops.keystore -alias secuops -keyalg RSA -keysize 2048 -validity 10000
```

2. Configurer signing dans `app/build.gradle.kts`

3. Build signed:
```bash
./gradlew assembleRelease --stacktrace
```

APK généré dans: `app/build/outputs/apk/release/app-release.apk`

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

## 🔧 Standards de Développement SecuAAS

Voir `/home/ubuntu/projects/CLAUDE.md` pour les standards généraux.

### Spécifiques à Android

**Code Style:**
- Kotlin Official Code Style
- 4 spaces indentation
- Max line length: 120 characters

**Naming:**
- ViewModels: `<Feature>ViewModel.kt`
- Screens: `<Feature>Screen.kt`
- Models: PascalCase, data classes
- Functions: camelCase, verbs
- Composables: PascalCase

**Best Practices:**
- Use Hilt for DI everywhere
- StateFlow for UI state
- Flow for async streams
- Composables should be stateless when possible
- Extract logic to ViewModels
- Use sealed classes for states
- Handle all Resource states (Loading, Success, Error)

## 📝 TODO / Roadmap

### Phase 1 - Core (✅ Complété)
- [x] Setup projet Android avec Gradle
- [x] Architecture MVVM + Clean Architecture
- [x] Authentification JWT
- [x] Dashboard avec navigation
- [x] Gestion des applications

### Phase 2 - Management Modules (🚧 En cours)
- [ ] Infrastructure monitoring (Pods, Services, Certificates)
- [ ] Deployments management
- [ ] Projects CRUD
- [ ] Domains DNS management
- [ ] Servers/VPS management
- [ ] Billing management

### Phase 3 - Features Avancées
- [ ] Real-time monitoring (SSE)
- [ ] Push notifications
- [ ] Offline mode avec Room cache
- [ ] Pull-to-refresh partout
- [ ] Search & filters
- [ ] Dark mode toggle
- [ ] Multi-language (FR/EN)

### Phase 4 - Polish
- [ ] Unit tests (ViewModels, Repository)
- [ ] UI tests (Compose)
- [ ] Performance optimization
- [ ] Analytics integration
- [ ] Crash reporting (Firebase Crashlytics)
- [ ] CI/CD pipeline

## 🐛 Known Issues

Aucun pour le moment.

## 📞 Support

Pour toute question sur le développement Android:
- Consulter la documentation Jetpack Compose: https://developer.android.com/jetpack/compose
- Consulter la documentation Material3: https://m3.material.io/

## 🔑 Clés API / Secrets

**NE JAMAIS committer de secrets dans le code!**

### Stockage Sécurisé
- Tokens JWT: DataStore (chiffré automatiquement)
- API Keys: À stocker dans OVH KMS
- Signing keys: Local uniquement, .gitignore

### Configuration en Production
Utiliser BuildConfig ou local.properties pour les secrets:
```kotlin
buildConfigField("String", "API_KEY", "\"${project.findProperty("API_KEY")}\"")
```

## 📄 Licence

Propriété de SecuAAS - Tous droits réservés

## 👨‍💻 Développement

Développé avec Kotlin + Jetpack Compose
Architecture MVVM + Clean Architecture
Dependency Injection avec Hilt

---

**Version**: 1.0.0
**Dernière mise à jour**: 2026-02-05
**Développeur**: Équipe SecuAAS + Claude Sonnet 4.5
