# Plan d'Amélioration SecuOps Android - Phase 3
## Nouvelles Fonctionnalités et Optimisations

**Date:** 2026-02-06
**Version actuelle:** 0.2.3
**Version cible:** 0.3.0
**Status:** ✅ APK fonctionnel existant (18 MB)

---

## 📱 État Actuel de l'Application

### ✅ Fonctionnalités Complètes (v0.2.3)

**8 Modules Opérationnels:**
1. **Authentication** - Login JWT + token storage
2. **Dashboard** - Hub de navigation avec 8 cards
3. **Applications** - Liste, détails, restart, scale
4. **Infrastructure** - Pods, Services, Ingresses, Certificates
5. **Deployments** - Tracking, retry, filtres
6. **Projects** - CRUD, repositories
7. **Domains** - DNS management, delete
8. **Servers** - VPS management, reboot
9. **Billing** - Summary, invoices

**Coverage API:** 29/29 endpoints (100%)
**Architecture:** MVVM + Clean Architecture + Hilt DI
**UI:** Material3 + Jetpack Compose
**Code:** ~4,850 lignes Kotlin

### 📦 APK Disponible
```
Fichier: app/build/outputs/apk/debug/app-debug.apk
Taille: 18 MB
Build: ✅ Successful
Status: ⏳ Non testé (pas d'émulateur/device)
```

---

## 🎯 Objectifs Phase 3 (v0.3.0)

### Priorité 1: Amélioration UX/UI
1. **Dark Mode Toggle**
   - Permettre basculement manuel Light/Dark
   - Préférence sauvegardée dans DataStore
   - Animation smooth de transition

2. **Search & Filters Avancés**
   - Barre de recherche globale dans chaque liste
   - Filtres persistants (sauvegardés)
   - Tri multi-colonnes (nom, date, status)

3. **Notifications Locales**
   - Notifications pour deployments terminés
   - Alertes pour erreurs critiques
   - Badge counter sur Dashboard

4. **Animations & Transitions**
   - Animations d'entrée/sortie des écrans
   - Skeleton loaders au lieu de spinners
   - Swipe gestures (refresh, delete)

### Priorité 2: Fonctionnalités Avancées

5. **Real-Time Monitoring (SSE)**
   - Stream Server-Sent Events pour logs déploiement
   - Update automatique status pods/services
   - Indicateurs "Live" sur Dashboard

6. **Offline Mode Basique**
   - Cache Room pour dernières données
   - Affichage offline avec indicateur
   - Retry automatique en cas de reconnexion

7. **Multi-Language (FR/EN)**
   - Détection locale automatique
   - Toggle manuel FR ↔ EN
   - Strings.xml pour toutes les chaînes

8. **Biometric Authentication**
   - Login avec empreinte digitale
   - Fallback sur PIN/Password
   - Session persistence configurable

### Priorité 3: Performance & Qualité

9. **Optimisations Performance**
   - LazyColumn pagination (infinite scroll)
   - Image caching (Coil library)
   - Réduction taille APK (<15 MB)
   - ProGuard/R8 optimization

10. **Error Handling Amélioré**
    - Toast messages personnalisés
    - Snackbar avec actions (Retry, Details)
    - Logs détaillés dans Logcat

11. **Security Enhancements**
    - Certificate pinning pour API calls
    - Obfuscation du code (R8)
    - Encryption du token JWT local
    - Timeout session configurable

---

## 🛠️ Spécifications Techniques

### 1. Dark Mode Toggle

**Fichiers à créer/modifier:**
```kotlin
// data/local/PreferencesManager.kt
class PreferencesManager @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.dataStore

    val isDarkMode: Flow<Boolean> = dataStore.data.map {
        it[DARK_MODE_KEY] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[DARK_MODE_KEY] = enabled }
    }
}

// presentation/settings/SettingsScreen.kt
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    Column {
        Row {
            Text("Dark Mode")
            Switch(
                checked = isDarkMode,
                onCheckedChange = { viewModel.toggleDarkMode(it) }
            )
        }
    }
}

// MainActivity.kt
MaterialTheme(
    colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()
) {
    // ...
}
```

**Effort:** 2-3 heures
**LOC:** ~150 lignes

---

### 2. Search & Filters Avancés

**Exemple: ApplicationsScreen avec recherche**
```kotlin
// presentation/applications/ApplicationsViewModel.kt
class ApplicationsViewModel @Inject constructor(
    private val repository: SecuOpsRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filterStatus = MutableStateFlow<AppStatus?>(null)
    val filterStatus: StateFlow<AppStatus?> = _filterStatus

    val filteredApps: StateFlow<List<Application>> = combine(
        apps,
        searchQuery,
        filterStatus
    ) { apps, query, status ->
        apps.filter { app ->
            val matchesSearch = app.name.contains(query, ignoreCase = true)
            val matchesStatus = status == null || app.status == status
            matchesSearch && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterStatus(status: AppStatus?) {
        _filterStatus.value = status
    }
}

// presentation/applications/ApplicationsScreen.kt
@Composable
fun ApplicationsScreen(viewModel: ApplicationsViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredApps by viewModel.filteredApps.collectAsState()

    Column {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search applications...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )

        // Filter chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip("All", onClick = { viewModel.setFilterStatus(null) })
            FilterChip("Running", onClick = { viewModel.setFilterStatus(Running) })
            FilterChip("Stopped", onClick = { viewModel.setFilterStatus(Stopped) })
        }

        // List
        LazyColumn {
            items(filteredApps) { app ->
                ApplicationCard(app)
            }
        }
    }
}
```

**Effort:** 3-4 heures (toutes les listes)
**LOC:** ~300 lignes

---

### 3. Real-Time Monitoring (SSE)

**Implémentation Server-Sent Events:**
```kotlin
// data/remote/SSEClient.kt
class SSEClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    fun streamDeploymentLogs(
        deploymentId: String,
        onEvent: (DeploymentLog) -> Unit,
        onError: (Throwable) -> Unit
    ): Job = CoroutineScope(Dispatchers.IO).launch {
        val request = Request.Builder()
            .url("https://engine.secuops.secuaas.dev/api/v1/deployments/$deploymentId/logs/stream")
            .header("Accept", "text/event-stream")
            .build()

        val response = okHttpClient.newCall(request).execute()
        val reader = BufferedReader(InputStreamReader(response.body?.byteStream()))

        var line: String?
        while (reader.readLine().also { line = it } != null) {
            if (line?.startsWith("data:") == true) {
                val data = line!!.substring(5).trim()
                val log = Json.decodeFromString<DeploymentLog>(data)
                onEvent(log)
            }
        }
    }
}

// presentation/deployments/DeploymentsViewModel.kt
class DeploymentsViewModel @Inject constructor(
    private val repository: SecuOpsRepository,
    private val sseClient: SSEClient
) : ViewModel() {
    private val _logs = MutableStateFlow<List<DeploymentLog>>(emptyList())
    val logs: StateFlow<List<DeploymentLog>> = _logs

    fun streamLogs(deploymentId: String) {
        sseClient.streamDeploymentLogs(
            deploymentId = deploymentId,
            onEvent = { log ->
                _logs.value = _logs.value + log
            },
            onError = { error ->
                // Handle error
            }
        )
    }
}

// presentation/deployments/DeploymentDetailsScreen.kt
@Composable
fun DeploymentDetailsScreen(
    deploymentId: String,
    viewModel: DeploymentsViewModel
) {
    val logs by viewModel.logs.collectAsState()

    LaunchedEffect(deploymentId) {
        viewModel.streamLogs(deploymentId)
    }

    LazyColumn {
        items(logs) { log ->
            Text(
                text = "[${log.timestamp}] ${log.message}",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
    }
}
```

**Effort:** 4-5 heures
**LOC:** ~400 lignes
**Dépendances:** OkHttp SSE support

---

### 4. Offline Mode avec Room Cache

**Architecture:**
```kotlin
// data/local/database/SecuOpsDatabase.kt
@Database(
    entities = [
        ApplicationEntity::class,
        ServerEntity::class,
        DomainEntity::class
    ],
    version = 1
)
abstract class SecuOpsDatabase : RoomDatabase() {
    abstract fun applicationDao(): ApplicationDao
    abstract fun serverDao(): ServerDao
    abstract fun domainDao(): DomainDao
}

// data/local/dao/ApplicationDao.kt
@Dao
interface ApplicationDao {
    @Query("SELECT * FROM applications")
    fun getAllApplications(): Flow<List<ApplicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<ApplicationEntity>)

    @Query("DELETE FROM applications")
    suspend fun deleteAll()
}

// data/repository/SecuOpsRepository.kt
class SecuOpsRepository @Inject constructor(
    private val api: SecuOpsApi,
    private val applicationDao: ApplicationDao,
    private val networkMonitor: NetworkMonitor
) {
    fun getApplications(): Flow<Resource<List<Application>>> = flow {
        emit(Resource.Loading())

        // Emit cached data first (offline support)
        val cachedApps = applicationDao.getAllApplications().first()
        if (cachedApps.isNotEmpty()) {
            emit(Resource.Success(cachedApps.map { it.toDomain() }))
        }

        // Fetch from network if online
        if (networkMonitor.isOnline()) {
            try {
                val response = api.getApplications()
                if (response.isSuccessful) {
                    val apps = response.body() ?: emptyList()
                    applicationDao.deleteAll()
                    applicationDao.insertAll(apps.map { it.toEntity() })
                    emit(Resource.Success(apps))
                } else {
                    emit(Resource.Error(response.message()))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.localizedMessage ?: "Unknown error"))
            }
        } else {
            // Offline - use cache
            emit(Resource.Error("Offline - showing cached data"))
        }
    }
}
```

**Effort:** 6-8 heures
**LOC:** ~600 lignes
**Dépendances:** Room, Paging3

---

### 5. Multi-Language Support

**Structure:**
```
app/src/main/res/
├── values/strings.xml (EN - default)
├── values-fr/strings.xml (FR)
└── values-es/strings.xml (ES - optionnel)
```

**Exemple strings.xml:**
```xml
<!-- values/strings.xml (EN) -->
<resources>
    <string name="app_name">SecuOps</string>
    <string name="dashboard_title">Dashboard</string>
    <string name="applications_title">Applications</string>
    <string name="login_button">Login</string>
    <string name="logout_button">Logout</string>
    <string name="error_network">Network error. Please try again.</string>
</resources>

<!-- values-fr/strings.xml (FR) -->
<resources>
    <string name="app_name">SecuOps</string>
    <string name="dashboard_title">Tableau de bord</string>
    <string name="applications_title">Applications</string>
    <string name="login_button">Connexion</string>
    <string name="logout_button">Déconnexion</string>
    <string name="error_network">Erreur réseau. Veuillez réessayer.</string>
</resources>
```

**Utilisation dans Compose:**
```kotlin
@Composable
fun DashboardScreen() {
    Text(text = stringResource(R.string.dashboard_title))
    Button(onClick = {}) {
        Text(stringResource(R.string.login_button))
    }
}
```

**Effort:** 3-4 heures (extraction + traduction)
**LOC:** ~200 lignes (strings resources)

---

### 6. Biometric Authentication

**Implémentation:**
```kotlin
// data/local/BiometricManager.kt
class BiometricManager @Inject constructor(
    private val context: Context
) {
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errString.toString())
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Authenticate to access SecuOps")
            .setNegativeButtonText("Use password")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

// presentation/auth/LoginScreen.kt
@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    Column {
        // Standard login form
        // ...

        // Biometric button
        if (viewModel.isBiometricAvailable) {
            IconButton(
                onClick = {
                    activity?.let {
                        viewModel.authenticateWithBiometric(it)
                    }
                }
            ) {
                Icon(Icons.Default.Fingerprint, "Biometric login")
            }
        }
    }
}
```

**Effort:** 2-3 heures
**LOC:** ~200 lignes
**Dépendances:** androidx.biometric:biometric:1.2.0-alpha05

---

## 📊 Estimation Complète Phase 3

| Fonctionnalité | Effort | LOC | Priorité |
|----------------|--------|-----|----------|
| Dark Mode Toggle | 2-3h | ~150 | HIGH |
| Search & Filters | 3-4h | ~300 | HIGH |
| Notifications Locales | 2-3h | ~200 | MEDIUM |
| Animations & Transitions | 3-4h | ~250 | MEDIUM |
| Real-Time SSE | 4-5h | ~400 | HIGH |
| Offline Mode Room | 6-8h | ~600 | HIGH |
| Multi-Language | 3-4h | ~200 | MEDIUM |
| Biometric Auth | 2-3h | ~200 | MEDIUM |
| Performance Optimizations | 4-5h | ~300 | HIGH |
| Error Handling | 2-3h | ~200 | MEDIUM |
| Security Enhancements | 3-4h | ~250 | HIGH |

**TOTAL:**
- **Effort:** 34-46 heures
- **LOC:** ~3,050 lignes supplémentaires
- **Version finale:** 0.3.0 → ~7,900 lignes totales

---

## 🗓️ Plan d'Implémentation

### Sprint 1 (8-10h) - Fondations
- [x] Dark Mode Toggle
- [x] Search & Filters (Applications + Infrastructure)
- [x] Multi-Language FR/EN

### Sprint 2 (10-12h) - Temps Réel
- [ ] Real-Time SSE pour Deployments
- [ ] Notifications Locales
- [ ] Error Handling amélioré

### Sprint 3 (10-12h) - Offline & Performance
- [ ] Offline Mode avec Room
- [ ] Performance optimizations (pagination, caching)
- [ ] Security enhancements (certificate pinning)

### Sprint 4 (6-8h) - UX Polish
- [ ] Biometric Authentication
- [ ] Animations & Transitions
- [ ] Tests manuels complets
- [ ] Documentation mise à jour

---

## 🧪 Plan de Tests

### Tests Manuels (par fonctionnalité)
1. **Dark Mode:** Toggle et vérifier toutes les screens
2. **Search:** Taper dans chaque liste, vérifier filtrage
3. **SSE:** Lancer déploiement, observer logs en temps réel
4. **Offline:** Désactiver WiFi, vérifier cache
5. **Biometric:** Tester sur device avec capteur empreinte
6. **Multi-language:** Changer locale système FR ↔ EN

### Tests Automatisés (Future Phase 4)
```kotlin
// ApplicationsViewModelTest.kt
@Test
fun `search applications filters correctly`() = runTest {
    val viewModel = ApplicationsViewModel(fakeRepository)

    viewModel.updateSearchQuery("nginx")

    val filteredApps = viewModel.filteredApps.first()
    assertTrue(filteredApps.all { it.name.contains("nginx", ignoreCase = true) })
}
```

---

## 📱 Test sur Device/Émulateur

### Option 1: Émulateur Android Studio
```bash
# Créer AVD (Android Virtual Device)
$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager create avd \
  --name Pixel_7_API_34 \
  --package "system-images;android-34;google_apis;x86_64" \
  --device "pixel_7"

# Lancer émulateur
$ANDROID_HOME/emulator/emulator -avd Pixel_7_API_34

# Installer APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Option 2: Device Physique
```bash
# Activer Developer Options + USB Debugging sur device
# Connecter via USB

# Vérifier device
adb devices

# Installer APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Voir logs
adb logcat | grep SecuOps
```

---

## 🔧 Dépendances Supplémentaires

**build.gradle.kts (app):**
```kotlin
dependencies {
    // Existing dependencies...

    // Room (Offline cache)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Paging3 (Infinite scroll)
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("androidx.paging:paging-compose:3.2.1")

    // Biometric
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // Coil (Image caching)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // DataStore (Preferences)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Network monitoring
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")
}
```

---

## 📄 Documentation à Mettre à Jour

1. **README.md** - Nouvelles fonctionnalités Phase 3
2. **CLAUDE.md** - Patterns techniques avancés
3. **VERSION.md** - Changelog v0.3.0
4. **WORK_IN_PROGRESS.md** - État actuel
5. **BUILD_INSTRUCTIONS.md** - Nouvelles dépendances

---

## 🎯 Critères de Succès v0.3.0

- [ ] Dark mode toggle fonctionnel sur toutes les screens
- [ ] Search fonctionne sur toutes les listes
- [ ] SSE stream logs déploiement en temps réel
- [ ] Cache offline avec Room (min 3 entités)
- [ ] Multi-language FR/EN complet
- [ ] Biometric auth fonctionnel sur device compatible
- [ ] APK < 15 MB (optimisé avec R8)
- [ ] Aucun crash sur tests manuels (30 min de navigation)
- [ ] Documentation complète mise à jour
- [ ] Commits pushés sur GitHub

---

**Auteur:** SecuAAS DevOps Team + Claude Sonnet 4.5
**Version du plan:** 1.0.0
**Dernière mise à jour:** 2026-02-06
**Status:** 📋 Plan prêt pour implémentation
