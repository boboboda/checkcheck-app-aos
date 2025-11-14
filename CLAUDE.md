# CLAUDE.md - CheckCheck Android App

> **AI Assistant Guide for the CheckCheck Android Codebase**
> Last Updated: 2025-11-14

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture & Design Patterns](#architecture--design-patterns)
3. [Tech Stack](#tech-stack)
4. [Project Structure](#project-structure)
5. [Feature Modules](#feature-modules)
6. [Code Conventions](#code-conventions)
7. [Development Workflows](#development-workflows)
8. [Key Patterns to Follow](#key-patterns-to-follow)
9. [Testing Strategy](#testing-strategy)
10. [Common Tasks & Examples](#common-tasks--examples)
11. [Important Gotchas](#important-gotchas)
12. [Quick Reference](#quick-reference)

---

## Project Overview

**CheckCheck (체크체크)** is a collaborative habit and task tracking Android application designed for groups such as families, couples, study groups, and project teams.

- **Package**: `com.buyoungsil.checkcheck`
- **Language**: Kotlin 2.0.21
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36
- **Build System**: Gradle Kotlin DSL

### Core Features
- **Personal & Group Habits**: Track habits individually or as a group
- **Group Tasks**: Assign and track tasks with reminders
- **Coin System**: Gamification with family coins and reward coins
- **Real-time Sync**: Firebase Firestore for instant updates across users
- **Statistics**: Personal and group analytics
- **Social Sharing**: Kakao Talk integration

---

## Architecture & Design Patterns

### Clean Architecture (Strict 3-Layer)

```
┌─────────────────────────────────────────┐
│         PRESENTATION LAYER              │
│  (Compose UI, ViewModels, UiState)     │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│          DOMAIN LAYER                   │
│  (Models, Repository Interfaces,        │
│   Use Cases - Business Logic)           │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│           DATA LAYER                    │
│  (DTOs, Entities, DAOs,                 │
│   Repository Implementations)           │
└─────────────────────────────────────────┘
```

### MVVM Pattern with Unidirectional Data Flow

```
User Action → ViewModel → UseCase → Repository → Data Source
                ↑                                      ↓
           StateFlow ←──────────────────────────── Flow/Result
                ↓
           Compose UI (Recompose)
```

### Feature-Modular Organization

Each feature is self-contained with all three layers:

```
feature/{feature_name}/
├── data/
│   ├── firebase/      # Firestore DTOs
│   ├── local/         # Room entities & DAOs
│   └── repository/    # Repository implementations
├── domain/
│   ├── model/         # Business models (immutable data classes)
│   ├── repository/    # Repository interfaces
│   └── usecase/       # Business logic encapsulation
└── presentation/
    ├── {screen}/      # Per-screen organization
    │   ├── Screen.kt      # Composable UI
    │   ├── ViewModel.kt   # State & event handling
    │   └── UiState.kt     # UI state model
```

---

## Tech Stack

### Core Technologies
- **Kotlin** 2.0.21
- **Jetpack Compose** (BOM 2024.09.00) - Declarative UI
- **Material 3** - Design system
- **Gradle Kotlin DSL** 8.12.3

### Dependency Injection
- **Hilt** 2.48 (Dagger-based)
- Modules: `DatabaseModule`, `FirebaseModule`, `RepositoryModule`, `CoinModule`, `UserRepositoryModule`

### Database & Backend
- **Firebase Auth** - Anonymous authentication with optional linking
- **Cloud Firestore** - Primary data source (real-time sync)
- **Room** 2.6.0 - Local persistence (available but not actively used)
- **Firebase Cloud Messaging** - Push notifications

### Async & State Management
- **Kotlin Coroutines** - Async operations
- **Flow & StateFlow** - Reactive streams
- **callbackFlow** - Firestore real-time listeners

### Background Processing
- **WorkManager** 2.9.0 with Hilt integration
- `TaskReminderWorker` - Scheduled notifications

### Additional Libraries
- **Phosphor Icons** 1.0.0 - Icon library
- **Material Icons Extended** 1.7.5
- **Kakao SDK** 2.20.0 - Social sharing

---

## Project Structure

### Root Package Structure

```
com.buyoungsil.checkcheck/
├── core/                    # Shared code across features
│   ├── data/               # Shared data components
│   │   ├── firebase/       # FirebaseAuthManager, UserFirestoreDto
│   │   ├── local/          # AppDatabase, shared DAOs
│   │   └── repository/     # UserFirestoreRepository
│   ├── domain/             # Shared domain models
│   │   ├── model/          # User
│   │   ├── repository/     # UserRepository interface
│   │   └── usecase/        # InitializeUserUseCase, etc.
│   ├── notification/       # Notification system
│   │   ├── domain/         # Reminder model
│   │   ├── worker/         # TaskReminderWorker
│   │   ├── CheckCheckMessagingService.kt
│   │   ├── NotificationHelper.kt
│   │   └── TaskReminderScheduler.kt
│   ├── ui/                 # Shared UI components
│   │   ├── components/     # Reusable Composables
│   │   ├── navigation/     # NavGraph, Screen sealed class
│   │   └── theme/          # (Note: theme is in separate ui/ package)
│   ├── util/               # Utilities
│   └── docs/               # Project documentation (Korean)
├── di/                     # Hilt modules
│   ├── DatabaseModule.kt
│   ├── FirebaseModule.kt
│   ├── RepositoryModule.kt
│   ├── UserRepositoryModule.kt
│   └── CoinModule.kt
├── feature/                # Feature modules
│   ├── auth/
│   ├── coin/
│   ├── group/
│   ├── habit/
│   ├── home/
│   ├── settings/
│   ├── statistics/
│   └── task/
├── ui/                     # App-level theme
│   └── theme/
│       ├── Color.kt        # Orange theme colors
│       ├── Shapes.kt
│       ├── Theme.kt
│       └── Type.kt
├── CheckCheckApplication.kt
└── MainActivity.kt
```

### Database Structure (Room - Available but not primary)

```kotlin
@Database(
    entities = [
        HabitEntity::class,
        HabitCheckEntity::class,
        GroupEntity::class,
        TaskEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase()
```

**Note**: Currently using Firestore as primary data source. Room entities exist for potential offline support.

---

## Feature Modules

### 1. Auth (`feature.auth`)
- **Purpose**: Firebase anonymous authentication & account linking
- **Key Use Cases**: Auto-login on app start
- **Screens**: `LinkAccountScreen`

### 2. Habit (`feature.habit`)
- **Purpose**: Personal and group habit tracking
- **Models**: `Habit`, `HabitCheck`, `HabitStatistics`
- **Key Features**:
  - Streak tracking
  - Completion rate statistics
  - Daily/weekly habit patterns
- **Use Cases**:
  - `CreateHabitUseCase`
  - `ToggleHabitCheckUseCase`
  - `GetHabitStatisticsUseCase`
  - `GetPersonalHabitsUseCase`
  - `GetGroupHabitsUseCase`

### 3. Group (`feature.group`)
- **Purpose**: Group creation and management
- **Models**: `Group`, `GroupMember`
- **Group Types**: `Family`, `Couple`, `Study`, `Exercise`, `Project`, `Custom`
- **Key Features**:
  - Invite code system
  - Member nickname management
  - Group ownership
- **Use Cases**:
  - `CreateGroupUseCase`
  - `JoinGroupUseCase`
  - `GetMyGroupsUseCase`
  - `UpdateGroupMemberNicknameUseCase`
  - `LeaveGroupUseCase`

### 4. Task (`feature.task`)
- **Purpose**: Group task assignment and tracking
- **Models**: `Task`, `TaskStatus`, `TaskPriority`
- **Task Status**: `Pending`, `InProgress`, `Completed`, `Expired`
- **Priority Levels**: `Urgent`, `Normal`, `Low`
- **Key Features**:
  - Task assignment to group members
  - Reminder notifications via WorkManager
  - Due date tracking
- **Use Cases**:
  - `CreateTaskUseCase`
  - `CompleteTaskUseCase`
  - `GetGroupTasksUseCase`

### 5. Coin (`feature.coin`)
- **Purpose**: Virtual currency gamification system
- **Models**: `CoinWallet`, `CoinTransaction`, `TransactionType`
- **Coin Types**:
  - **Family Coins**: Shared group currency
  - **Reward Coins**: Personal achievement currency
- **Use Cases**:
  - `RewardHabitCompletionUseCase`
  - `GiftCoinsUseCase`
  - `GetCoinWalletUseCase`

### 6. Home (`feature.home`)
- **Purpose**: Main dashboard
- **Features**: Personal & group habit overview, quick actions

### 7. Statistics (`feature.statistics`)
- **Purpose**: Analytics and progress tracking
- **Features**: Personal stats, group stats, achievement tracking

### 8. Settings (`feature.settings`)
- **Purpose**: User preferences and account management

---

## Code Conventions

### Naming Conventions

| Type | Convention | Example |
|------|-----------|---------|
| **Firestore DTOs** | `{Model}FirestoreDto` | `HabitFirestoreDto` |
| **Room Entities** | `{Model}Entity` | `HabitEntity` |
| **DAOs** | `{Model}Dao` | `HabitDao` |
| **Repository Interface** | `{Model}Repository` | `HabitRepository` |
| **Repository Impl** | `{Model}FirestoreRepository` | `HabitFirestoreRepository` |
| **Use Cases** | `{Action}{Model}UseCase` | `CreateHabitUseCase` |
| **ViewModels** | `{Screen}ViewModel` | `CreateHabitViewModel` |
| **UI States** | `{Screen}UiState` | `CreateHabitUiState` |
| **Screens** | `{Screen}Screen` | `CreateHabitScreen` |

### File Organization

#### Domain Models (Immutable Data Classes)
```kotlin
// feature/habit/domain/model/Habit.kt
data class Habit(
    val id: String = "",
    val title: String,
    val description: String = "",
    val isGroupHabit: Boolean = false,
    val groupId: String? = null,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    // ... more fields
)
```

#### Repository Interface
```kotlin
// feature/habit/domain/repository/HabitRepository.kt
interface HabitRepository {
    fun getAllHabits(userId: String): Flow<List<Habit>>
    suspend fun insertHabit(habit: Habit): Result<Habit>
    suspend fun deleteHabit(habitId: String): Result<Unit>
}
```

#### Repository Implementation
```kotlin
// feature/habit/data/repository/HabitFirestoreRepository.kt
class HabitFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : HabitRepository {

    override fun getAllHabits(userId: String): Flow<List<Habit>> = callbackFlow {
        val listener = firestore.collection("habits")
            .whereEqualTo("createdBy", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val habits = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(HabitFirestoreDto::class.java)?.toDomain()
                } ?: emptyList()

                trySend(habits)
            }

        awaitClose { listener.remove() }
    }
}
```

#### Use Case
```kotlin
// feature/habit/domain/usecase/CreateHabitUseCase.kt
class CreateHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habit: Habit): Result<Habit> {
        return try {
            // Business logic here
            if (habit.title.isBlank()) {
                return Result.failure(IllegalArgumentException("Title cannot be empty"))
            }

            repository.insertHabit(habit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### ViewModel
```kotlin
// feature/habit/presentation/create/CreateHabitViewModel.kt
@HiltViewModel
class CreateHabitViewModel @Inject constructor(
    private val createHabitUseCase: CreateHabitUseCase,
    private val authManager: FirebaseAuthManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateHabitUiState())
    val uiState: StateFlow<CreateHabitUiState> = _uiState.asStateFlow()

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onCreateClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val habit = Habit(
                title = uiState.value.title,
                createdBy = authManager.currentUserId ?: return@launch
            )

            createHabitUseCase(habit).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, success = true) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
            )
        }
    }
}
```

#### UI State
```kotlin
// feature/habit/presentation/create/CreateHabitUiState.kt
data class CreateHabitUiState(
    val title: String = "",
    val description: String = "",
    val isGroupHabit: Boolean = false,
    val selectedGroupId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)
```

#### Composable Screen
```kotlin
// feature/habit/presentation/create/CreateHabitScreen.kt
@Composable
fun CreateHabitScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateHabitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navigate back on success
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("새 습관 만들기") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("습관 이름") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = viewModel::onCreateClick,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("만들기")
                }
            }

            // Error display
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
```

### DTO Mapping Pattern

```kotlin
// Firestore DTO
data class HabitFirestoreDto(
    val id: String = "",
    val title: String = "",
    // ... other fields
) {
    // DTO to Domain
    fun toDomain(): Habit {
        return Habit(
            id = id,
            title = title,
            // ... map other fields
        )
    }

    companion object {
        // Domain to DTO
        fun fromDomain(habit: Habit): HabitFirestoreDto {
            return HabitFirestoreDto(
                id = habit.id,
                title = habit.title,
                // ... map other fields
            )
        }
    }
}
```

---

## Development Workflows

### Adding a New Feature

**Example: Adding a new "Reward" feature**

1. **Create feature package structure**
```
feature/reward/
├── data/
│   ├── firebase/
│   │   └── RewardFirestoreDto.kt
│   └── repository/
│       └── RewardFirestoreRepository.kt
├── domain/
│   ├── model/
│   │   └── Reward.kt
│   ├── repository/
│   │   └── RewardRepository.kt
│   └── usecase/
│       ├── CreateRewardUseCase.kt
│       └── GetRewardsUseCase.kt
└── presentation/
    ├── list/
    │   ├── RewardListScreen.kt
    │   ├── RewardListViewModel.kt
    │   └── RewardListUiState.kt
    └── create/
        ├── CreateRewardScreen.kt
        ├── CreateRewardViewModel.kt
        └── CreateRewardUiState.kt
```

2. **Define domain model** (`domain/model/Reward.kt`)
```kotlin
data class Reward(
    val id: String = "",
    val title: String,
    val coinCost: Int,
    val createdAt: Long = System.currentTimeMillis()
)
```

3. **Define repository interface** (`domain/repository/RewardRepository.kt`)
```kotlin
interface RewardRepository {
    fun getRewards(userId: String): Flow<List<Reward>>
    suspend fun createReward(reward: Reward): Result<Reward>
}
```

4. **Implement repository** (`data/repository/RewardFirestoreRepository.kt`)
```kotlin
class RewardFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : RewardRepository {
    override fun getRewards(userId: String): Flow<List<Reward>> = callbackFlow {
        // Implementation with Firestore listener
    }
}
```

5. **Create Hilt binding** (in `di/RepositoryModule.kt`)
```kotlin
@Binds
abstract fun bindRewardRepository(
    impl: RewardFirestoreRepository
): RewardRepository
```

6. **Create use cases**
```kotlin
class CreateRewardUseCase @Inject constructor(
    private val repository: RewardRepository
) {
    suspend operator fun invoke(reward: Reward): Result<Reward> {
        return repository.createReward(reward)
    }
}
```

7. **Implement ViewModel and UI** (following patterns above)

8. **Add to navigation** (`core/ui/navigation/Screen.kt`)
```kotlin
sealed class Screen(val route: String) {
    // ... existing screens
    object RewardList : Screen("reward_list")
    object RewardCreate : Screen("reward_create")
}
```

### Adding a New Use Case

1. Create file in `feature/{feature}/domain/usecase/`
2. Inject required repositories
3. Implement business logic in `invoke()` method
4. Return `Result<T>` for error handling
5. Inject into ViewModel

### Modifying Existing Features

**IMPORTANT**: Always maintain Clean Architecture separation!

- **UI changes only** → Modify `presentation/` layer only
- **Business logic changes** → Modify `domain/usecase/` layer
- **Data source changes** → Modify `data/repository/` layer
- **Model changes** → Update domain model, DTO, and entity (if Room is used)

### Working with Navigation

Navigation is centralized in `core/ui/navigation/NavGraph.kt`:

```kotlin
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToGroupList = { navController.navigate(Screen.GroupList.route) }
            )
        }

        composable(Screen.GroupDetail.route) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            GroupDetailScreen(
                groupId = groupId ?: "",
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
```

### Dependency Injection Guidelines

**Hilt annotations to use:**

```kotlin
// Application class
@HiltAndroidApp
class CheckCheckApplication : Application()

// MainActivity
@AndroidEntryPoint
class MainActivity : ComponentActivity()

// ViewModels
@HiltViewModel
class MyViewModel @Inject constructor(
    private val useCase: MyUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel()

// Repositories, UseCases, etc.
class MyRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : MyRepositoryInterface
```

**Module definitions:**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindHabitRepository(
        impl: HabitFirestoreRepository
    ): HabitRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }
}
```

---

## Key Patterns to Follow

### 1. State Management Pattern

**Always use StateFlow for UI state:**

```kotlin
private val _uiState = MutableStateFlow(MyUiState())
val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()

// Update state immutably
fun onEvent() {
    _uiState.update { currentState ->
        currentState.copy(isLoading = true)
    }
}
```

### 2. Error Handling Pattern

**Use Result type for operations that can fail:**

```kotlin
suspend fun invoke(): Result<Data> {
    return try {
        val data = repository.getData()
        Result.success(data)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// In ViewModel
useCase().fold(
    onSuccess = { data ->
        _uiState.update { it.copy(data = data, isLoading = false) }
    },
    onFailure = { error ->
        _uiState.update { it.copy(error = error.message, isLoading = false) }
    }
)
```

### 3. Real-time Data Pattern (Firestore)

**Use callbackFlow for Firestore listeners:**

```kotlin
override fun getItems(): Flow<List<Item>> = callbackFlow {
    // Immediately emit empty list to prevent infinite loading
    trySend(emptyList())

    val listener = firestore.collection("items")
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val items = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(ItemDto::class.java)?.toDomain()
            } ?: emptyList()

            trySend(items)
        }

    awaitClose { listener.remove() }
}
```

### 4. Loading States Pattern

**Always handle loading, success, and error states:**

```kotlin
data class MyUiState(
    val data: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

// In Composable
when {
    uiState.isLoading -> LoadingIndicator()
    uiState.error != null -> ErrorMessage(uiState.error)
    uiState.data.isEmpty() -> EmptyState()
    else -> DataList(uiState.data)
}
```

### 5. Navigation with Side Effects

**Use LaunchedEffect for navigation:**

```kotlin
@Composable
fun MyScreen(
    onNavigateBack: () -> Unit,
    viewModel: MyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navigate on success
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onNavigateBack()
        }
    }

    // Rest of UI
}
```

### 6. Theme and Styling

**Use Orange theme consistently:**

```kotlin
// Access theme colors
MaterialTheme.colorScheme.primary  // OrangePrimary
MaterialTheme.colorScheme.secondary  // OrangeSecondary
MaterialTheme.colorScheme.background  // OrangeBackground

// Custom colors from Color.kt
OrangePrimary
OrangeSecondary
OrangeBackground
OrangeSurface
```

---

## Testing Strategy

### Current State
- Minimal test coverage exists
- Test infrastructure is set up but not actively used

### Test Structure (When Implementing)

```
app/src/
├── test/                 # Unit tests
│   └── java/com/buyoungsil/checkcheck/
│       ├── domain/
│       │   └── usecase/  # Use case tests
│       └── data/
│           └── repository/  # Repository tests (with mocks)
└── androidTest/          # Instrumented tests
    └── java/com/buyoungsil/checkcheck/
        └── presentation/  # UI tests (Compose tests)
```

### Recommended Testing Approach

**1. Use Case Tests (Unit Tests)**
```kotlin
class CreateHabitUseCaseTest {

    private lateinit var useCase: CreateHabitUseCase
    private lateinit var repository: HabitRepository

    @Before
    fun setup() {
        repository = mockk()
        useCase = CreateHabitUseCase(repository)
    }

    @Test
    fun `createHabit with valid data returns success`() = runTest {
        // Given
        val habit = Habit(title = "Exercise")
        coEvery { repository.insertHabit(habit) } returns Result.success(habit)

        // When
        val result = useCase(habit)

        // Then
        assertTrue(result.isSuccess)
    }
}
```

**2. ViewModel Tests**
```kotlin
class CreateHabitViewModelTest {

    @Test
    fun `onCreateClick with valid input updates state to success`() = runTest {
        // Test implementation
    }
}
```

**3. Compose UI Tests**
```kotlin
class CreateHabitScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysTitleInputField() {
        composeTestRule.setContent {
            CreateHabitScreen(onNavigateBack = {})
        }

        composeTestRule
            .onNodeWithText("습관 이름")
            .assertIsDisplayed()
    }
}
```

---

## Common Tasks & Examples

### Task 1: Add a New Screen to Existing Feature

**Example: Add HabitDetailScreen**

1. Create files in `feature/habit/presentation/detail/`:
   - `HabitDetailScreen.kt`
   - `HabitDetailViewModel.kt`
   - `HabitDetailUiState.kt`

2. Add screen to navigation:
```kotlin
// In Screen.kt
object HabitDetail : Screen("habit_detail/{habitId}") {
    fun createRoute(habitId: String) = "habit_detail/$habitId"
}

// In NavGraph.kt
composable(
    route = Screen.HabitDetail.route,
    arguments = listOf(navArgument("habitId") { type = NavType.StringType })
) { backStackEntry ->
    val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
    HabitDetailScreen(
        habitId = habitId,
        onNavigateBack = { navController.popBackStack() }
    )
}
```

### Task 2: Add a New Field to Existing Model

**Example: Add `color` field to Habit**

1. Update domain model:
```kotlin
data class Habit(
    // ... existing fields
    val color: String = "#FF6B35"  // Default orange
)
```

2. Update Firestore DTO:
```kotlin
data class HabitFirestoreDto(
    // ... existing fields
    val color: String = "#FF6B35"
) {
    fun toDomain(): Habit {
        return Habit(
            // ... existing mappings
            color = color
        )
    }
}
```

3. Update Room Entity (if used):
```kotlin
@Entity(tableName = "habits")
data class HabitEntity(
    // ... existing fields
    val color: String
)
```

4. Update UI to display/edit the new field

5. **IMPORTANT**: For Firestore, no migration needed. For Room, create migration:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE habits ADD COLUMN color TEXT NOT NULL DEFAULT '#FF6B35'")
    }
}
```

### Task 3: Implement Real-time Updates

**Pattern for any real-time collection:**

```kotlin
override fun observeItems(groupId: String): Flow<List<Item>> = callbackFlow {
    trySend(emptyList())  // Prevent infinite loading

    val listener = firestore.collection("items")
        .whereEqualTo("groupId", groupId)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val items = snapshot?.documents?.mapNotNull {
                it.toObject(ItemDto::class.java)?.toDomain()
            } ?: emptyList()

            trySend(items)
        }

    awaitClose { listener.remove() }
}
```

### Task 4: Add Background Work with WorkManager

**Example: Schedule periodic sync**

1. Create Worker:
```kotlin
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncUseCase: SyncDataUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            syncUseCase()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
```

2. Schedule work:
```kotlin
class SyncScheduler @Inject constructor(
    private val workManager: WorkManager
) {
    fun scheduleDailySync() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "daily_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
```

### Task 5: Add Notification Type

1. Update `NotificationHelper.kt`:
```kotlin
fun showNewNotificationType(
    title: String,
    message: String
) {
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(message)
        .setSmallIcon(R.drawable.ic_notification)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

    notificationManager.notify(NOTIFICATION_ID, notification)
}
```

2. Add handling in `CheckCheckMessagingService.kt`:
```kotlin
override fun onMessageReceived(message: RemoteMessage) {
    when (message.data["type"]) {
        "new_type" -> notificationHelper.showNewNotificationType(
            title = message.data["title"] ?: "",
            message = message.data["message"] ?: ""
        )
    }
}
```

---

## Important Gotchas

### 1. Authentication Flow
- **CRITICAL**: UI waits for anonymous auth to complete before rendering
- Check `authManager.currentUserId` is not null before operations
- Handle auth state in `CheckCheckApplication.onCreate()`

### 2. Empty State Handling
```kotlin
// ALWAYS emit empty list immediately in Firestore flows
override fun getItems(): Flow<List<Item>> = callbackFlow {
    trySend(emptyList())  // ← IMPORTANT: Prevents infinite loading

    val listener = // ... Firestore listener
}
```

### 3. Navigation State
- Bottom navigation bar visibility is controlled by route matching
- Use `navController.currentBackStackEntryAsState()` to track current route

### 4. WorkManager Initialization
- **Custom initialization** via Hilt (auto-init is disabled)
- Configured in `CheckCheckApplication`:
```kotlin
override val workManagerConfiguration: Configuration
    get() = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()
```

### 5. FCM Token Management
- Token saved to Firestore on user initialization
- Update token in `UpdateFcmTokenUseCase`

### 6. Database Migration Strategy
- **Current**: `fallbackToDestructiveMigration()` (development mode)
- **Production**: Implement proper migrations before release

### 7. Time Handling
- Uses `java.time` API (requires API 26+)
- No desugaring needed due to `minSdk = 26`

### 8. BuildConfig Access
- Kakao SDK key loaded from `local.properties`
- Access via `BuildConfig.KAKAO_NATIVE_APP_KEY`

### 9. Proguard/R8
- Currently `isMinifyEnabled = false`
- Add rules before enabling in production

### 10. Real-time Listener Cleanup
- **ALWAYS** use `awaitClose { listener.remove() }` in callbackFlow
- Prevents memory leaks from unclosed Firestore listeners

---

## Quick Reference

### File Locations

| Component | Path |
|-----------|------|
| Application Class | `CheckCheckApplication.kt` |
| Main Activity | `MainActivity.kt` |
| Navigation Graph | `core/ui/navigation/NavGraph.kt` |
| Screen Routes | `core/ui/navigation/Screen.kt` |
| Theme | `ui/theme/Theme.kt` |
| Colors | `ui/theme/Color.kt` |
| Hilt Modules | `di/` |
| Database | `core/data/local/database/AppDatabase.kt` |
| Auth Manager | `core/data/firebase/FirebaseAuthManager.kt` |
| Notification Helper | `core/notification/NotificationHelper.kt` |

### Gradle Files

| File | Purpose |
|------|---------|
| `build.gradle.kts` (root) | Plugin declarations |
| `app/build.gradle.kts` | App configuration, dependencies |
| `gradle/libs.versions.toml` | Version catalog |
| `gradle.properties` | Gradle properties |
| `local.properties` | Local secrets (Kakao key) |

### Firestore Collections

| Collection | Document ID | Contents |
|------------|-------------|----------|
| `users` | User ID | User profile data |
| `groups` | Group ID | Group metadata |
| `groupMembers` | Member ID | Group membership data |
| `habits` | Habit ID | Habit definitions |
| `habitChecks` | Check ID | Habit completion records |
| `tasks` | Task ID | Task data |
| `coinWallets` | Wallet ID | Coin balances |
| `coinTransactions` | Transaction ID | Coin transaction history |

### Common Commands

```bash
# Build project
./gradlew build

# Run on device/emulator
./gradlew installDebug

# Clean build
./gradlew clean

# Generate APK
./gradlew assembleDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest

# Check dependencies
./gradlew dependencies
```

### Key Dependencies Version Reference

```toml
[versions]
kotlin = "2.0.21"
agp = "8.12.3"
hilt = "2.48"
room = "2.6.0"
compose-bom = "2024.09.00"
firebase-bom = "32.7.0"
work = "2.9.0"
```

---

## Best Practices for AI Assistants

### When Modifying Code

1. **Always maintain Clean Architecture separation**
   - Don't access repositories directly from ViewModels
   - Don't put business logic in ViewModels or Composables
   - Keep domain models independent of frameworks

2. **Follow existing naming conventions**
   - Use established suffixes (UseCase, Repository, Dto, etc.)
   - Match package structure for new components

3. **Use Hilt for dependency injection**
   - Never instantiate repositories or use cases manually
   - Always use constructor injection with `@Inject`

4. **Handle errors gracefully**
   - Use `Result<T>` for operations that can fail
   - Display user-friendly error messages
   - Log errors for debugging

5. **Maintain immutability**
   - Use `data class` for models
   - Use `.copy()` for state updates
   - Avoid mutable collections in public APIs

6. **Write descriptive code**
   - Use meaningful variable and function names
   - Add comments for complex business logic
   - Document public APIs

### When Adding Features

1. **Start with domain layer**
   - Define models first
   - Create repository interfaces
   - Implement use cases

2. **Then implement data layer**
   - Create DTOs/Entities
   - Implement repositories
   - Add Hilt bindings

3. **Finally build presentation layer**
   - Create UI state
   - Implement ViewModel
   - Build Composable UI

4. **Update navigation**
   - Add screen routes
   - Update NavGraph
   - Test navigation flows

### When Debugging

1. **Check Logcat** for exceptions and warnings
2. **Verify Hilt** is providing dependencies correctly
3. **Check Firebase Console** for Firestore data
4. **Inspect StateFlow** values in debugger
5. **Review navigation backstack** state

---

## Additional Resources

### Project Documentation (Korean)
- `core/docs/전체 기획.md` - Full project specifications
- `core/docs/폴더구조.md` - Folder structure guide
- `core/docs/그룹 기능 개선.md` - Group feature improvements
- `core/docs/진행상황.md` - Progress tracking

### External Documentation
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Hilt](https://dagger.dev/hilt/)
- [Firebase Firestore](https://firebase.google.com/docs/firestore)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Material 3](https://m3.material.io/)

---

## Changelog

### 2025-11-14
- Initial CLAUDE.md creation
- Documented full architecture and patterns
- Added comprehensive examples and best practices

---

**For AI Assistants**: This document should be your primary reference when working with the CheckCheck codebase. Always follow the established patterns and maintain the Clean Architecture principles. When in doubt, examine existing implementations in similar features.
