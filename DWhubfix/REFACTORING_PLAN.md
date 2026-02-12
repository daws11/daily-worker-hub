# Refactoring Plan - DailyWorkerHub Android App

**Created:** 2025-02-04
**Status:** Draft
**Priority:** High

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture Refactoring](#architecture-refactoring)
3. [Phase 1: Critical Issues (Week 1-2)](#phase-1-critical-issues-week-1-2)
4. [Phase 2: Medium Priority (Week 3-4)](#phase-2-medium-priority-week-3-4)
5. [Phase 3: Low Priority (Week 5-6)](#phase-3-low-priority-week-5-6)
6. [Testing Strategy](#testing-strategy)
7. [Migration Guide](#migration-guide)

---

## Overview

This refactoring plan addresses **19+ issues** found in the codebase review, focusing on:
- Clean Architecture implementation
- Dependency Injection with Hilt
- Type safety and null safety
- State management with ViewModel
- Testing infrastructure

### Current Issues Summary

| Severity | Count | Files Affected |
|----------|-------|----------------|
| Critical | 4 | All repositories, BookingRepository |
| Medium | 10 | UI screens, Utils, Models |
| Low | 5+ | All files |

---

## Architecture Refactoring

### Target Architecture

```
app/
├── data/
│   ├── local/
│   │   ├── dao/             # Room DAOs
│   │   ├── entity/          # Room entities
│   │   └── PreferencesManager.kt
│   ├── remote/
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── api/             # Supabase API interfaces
│   │   └── SupabaseClient.kt
│   ├── repository/
│   │   └── *Repository.kt   # Repository implementations
│   └── mapper/              # Entity <-> Domain mapper
├── domain/
│   ├── model/               # Domain models (pure Kotlin)
│   ├── repository/          # Repository interfaces
│   ├── usecase/             # Business logic use cases
│   └── util/                # Domain utilities
├── presentation/
│   ├── worker/
│   │   ├── home/
│   │   │   ├── WorkerHomeViewModel.kt
│   │   │   ├── WorkerHomeUiState.kt
│   │   │   └── WorkerHomeScreen.kt
│   │   ├── profile/
│   │   ├── jobs/
│   │   └── wallet/
│   ├── business/
│   │   └── ...
│   ├── common/
│   │   ├── component/       # Reusable UI components
│   │   ├── state/           # Common state holders
│   │   └── theme/
│   └── navigation/
│       └── AppNavigation.kt
├── di/
│   ├── module/
│   │   ├── AppModule.kt
│   │   ├── RepositoryModule.kt
│   │   └── UseCaseModule.kt
│   └── qualifier/
└── core/
    ├── network/
    ├── location/
    ├── permission/
    └── util/
```

---

## Phase 1: Critical Issues (Week 1-2)

### 1.1 Implement Singleton SupabaseClient

**Issue:** Multiple repositories create their own Supabase client instances
**Files:** `SupabaseRepository.kt`, `BookingRepository.kt`, `BusinessMatchingRepository.kt`, `MatchingRepository.kt`

#### Steps:

1. **Create core/network/SupabaseClient.kt**

```kotlin
package com.example.dwhubfix.core.network

import com.example.dwhubfix.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseClient @Inject constructor() {

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}
```

2. **Create di/module/NetworkModule.kt**

```kotlin
package com.example.dwhubfix.di.module

import com.example.dwhubfix.core.network.SupabaseClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return SupabaseClient()
    }
}
```

3. **Update repositories to inject SupabaseClient**

```kotlin
// Before:
object SupabaseRepository {
    val client = createSupabaseClient(...)
}

// After:
class SupabaseRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    private val client get() = supabaseClient.client
    // ...
}
```

**Estimated Time:** 2-3 hours

---

### 1.2 Implement Dependency Injection with Hilt

**Issue:** No DI, manual dependency management, tight coupling

#### Steps:

1. **Add Hilt dependencies to build.gradle.kts**

```kotlin
// app/build.gradle.kts
plugins {
    id("com.google.dagger.hilt.android") version "2.48.1"
    kotlin("kapt")
}

dependencies {
    implementation("com.google.dagger:hilt-android:2.48.1")
    kapt("com.google.dagger:hilt-compiler:2.48.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
}
```

2. **Create Application class**

```kotlin
@HiltAndroidApp
class DWHubApplication : Application() {
    // Add to AndroidManifest.xml
}
```

3. **Annotate MainActivity**

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // ...
}
```

**Estimated Time:** 3-4 hours

---

### 1.3 Fix Repository Pattern - Remove Object Declarations

**Issue:** Context leakage in object repositories, no proper lifecycle

#### Steps:

1. **Convert object to class**

```kotlin
// Before:
object SupabaseRepository {
    suspend fun getProfile(context: Context): Result<Map<String, Any?>>
}

// After:
class SupabaseRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val sessionManager: SessionManager
) {
    suspend fun getProfile(): Result<UserProfile>
}
```

2. **Update all repository files:**

| File | Action |
|------|--------|
| `SupabaseRepository.kt` | Convert to class, inject dependencies |
| `BookingRepository.kt` | Convert to class, inject dependencies |
| `MatchingRepository.kt` | Convert to class, inject dependencies |
| `BusinessMatchingRepository.kt` | Convert to class, inject dependencies |

**Estimated Time:** 4-5 hours

---

### 1.4 Remove Mock Data from Production Code

**Issue:** Hardcoded mock location in BookingRepository

#### Steps:

1. **Create core/location/LocationManager.kt**

```kotlin
package com.example.dwhubfix.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resumeWithException

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    suspend fun getCurrentLocation(): Result<Location> = suspendCancellableCoroutine { cont ->
        try {
            if (!hasLocationPermission()) {
                cont.resume(Result.failure(SecurityException("Location permission not granted")))
                return@suspendCancellableCoroutine
            }

            val cancellationTokenSource = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(Result.success(location))
                } else {
                    cont.resume(Result.failure(Exception("Unable to get location")))
                }
            }.addOnFailureListener { e ->
                cont.resume(Result.failure(e))
            }

            cont.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        } catch (e: Exception) {
            cont.resumeWithException(e)
        }
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
```

2. **Update BookingRepository**

```kotlin
class BookingRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val locationManager: LocationManager,
    private val sessionManager: SessionManager
) {
    suspend fun clockOut(
        bookingId: String
    ): ApiResult<ClockOutResult, ClockOutError> {
        // Get current location
        val locationResult = locationManager.getCurrentLocation()

        return when (val location = locationResult.getOrNull()) {
            null -> ApiResult.Failure(ClockOutError.LocationUnavailable)
            else -> proceedWithClockOut(bookingId, location)
        }
    }
}
```

**Estimated Time:** 2-3 hours

---

### 1.5 Fix Type Safety - Remove @Suppress("UNCHECKED_CAST")

**Issue:** Unsafe type casting in ApiResult pattern

#### Steps:

1. **Create domain/model/Result.kt**

```kotlin
package com.example.dwhubfix.domain.model

/**
 * Generic wrapper for API responses
 * Replaces the unsafe ApiResult pattern
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(
        val exception: Throwable,
        val message: String? = null
    ) : ApiResult<Nothing>()

    val isSuccess: get() = this is Success
    val isError: get() = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
    }

    fun <R> map(transform: (T) -> R): ApiResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    companion object {
        fun <T> success(data: T): ApiResult<T> = Success(data)
        fun <T> error(exception: Throwable, message: String? = null): ApiResult<T> =
            Error(exception, message)
    }
}

// Extension functions for kotlinx.coroutines.Result
fun <T> Result<T>.toApiResult(): ApiResult<T> = when {
    isSuccess -> ApiResult.Success(getOrNull()!!)
    isFailure -> ApiResult.Error(exceptionOrNull()!!)
}

fun <T> ApiResult<T>.toResult(): Result<T> = when (this) {
    is ApiResult.Success -> Result.success(data)
    is ApiResult.Error -> Result.failure(exception)
}
```

2. **Update BookingRepository usage**

```kotlin
// Before:
@Suppress("UNCHECKED_CAST")
return@withContext ApiResult.Failure(ClockInError.NoSession) as ApiResult<ClockInResult, ClockInError>

// After:
return@withContext ApiResult.error(
    exception = SessionNotFoundException(),
    message = "No active session found"
)
```

**Estimated Time:** 3-4 hours

---

## Phase 2: Medium Priority (Week 3-4)

### 2.1 Implement ViewModel Pattern

**Issue:** Business logic in Composables, too many state variables

#### Steps:

1. **Create presentation/worker/home/WorkerHomeUiState.kt**

```kotlin
package com.example.dwhubfix.presentation.worker.home

import com.example.dwhubfix.domain.model.Job
import com.example.dwhubfix.domain.model.JobMatchScore

data class WorkerHomeUiState(
    val isLoading: Boolean = true,
    val jobs: List<JobWithScore> = emptyList(),
    val displayedJobs: List<JobWithScore> = emptyList(),
    val searchQuery: String = "",
    val showFilterSheet: Boolean = false,
    val showMap: Boolean = false,
    val selectedJob: Job? = null,
    val isAccepting: Boolean = false,
    val error: String? = null,
    val snackbarMessage: String? = null
) {
    data class JobWithScore(
        val job: Job,
        val score: JobMatchScore,
        val isCompliant: Boolean
    )

    val hasJobs: Boolean get() = jobs.isNotEmpty()
    val isEmpty: Boolean get() = jobs.isEmpty() && !isLoading
}

sealed class WorkerHomeUiEvent {
    data class RefreshJobs(val location: GeoPoint?) : WorkerHomeUiEvent()
    data class SearchQueryChanged(val query: String) : WorkerHomeUiEvent()
    data class ToggleFilterSheet(val show: Boolean) : WorkerHomeUiEvent()
    data class ToggleMapView(val show: Boolean) : WorkerHomeUiEvent()
    data class AcceptJob(val jobId: String) : WorkerHomeUiEvent()
    data class JobSelected(val job: Job) : WorkerHomeUiEvent()
    object ClearError : WorkerHomeUiEvent()
    object ClearSnackbar : WorkerHomeUiEvent()
}
```

2. **Create presentation/worker/home/WorkerHomeViewModel.kt**

```kotlin
@HiltViewModel
class WorkerHomeViewModel @Inject constructor(
    private val getJobsForWorkerUseCase: GetJobsForWorkerUseCase,
    private val acceptJobUseCase: AcceptJobUseCase,
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerHomeUiState())
    val uiState: StateFlow<WorkerHomeUiState> = _uiState.asStateFlow()

    fun onEvent(event: WorkerHomeUiEvent) {
        when (event) {
            is WorkerHomeUiEvent.RefreshJobs -> refreshJobs(event.location)
            is WorkerHomeUiEvent.SearchQueryChanged -> handleSearchQuery(event.query)
            is WorkerHomeUiEvent.ToggleFilterSheet -> toggleFilterSheet(event.show)
            is WorkerHomeUiEvent.ToggleMapView -> toggleMapView(event.show)
            is WorkerHomeUiEvent.AcceptJob -> acceptJob(event.jobId)
            is WorkerHomeUiEvent.JobSelected -> selectJob(event.job)
            WorkerHomeUiEvent.ClearError -> clearError()
            WorkerHomeUiEvent.ClearSnackbar -> clearSnackbar()
        }
    }

    private fun refreshJobs(location: GeoPoint?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            locationTracker.getCurrentLocationOrNull()
                .onSuccess { currentLocation ->
                    getJobsForWorkerUseCase(currentLocation)
                        .onSuccess { jobs ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    jobs = jobs,
                                    displayedJobs = jobs,
                                    error = null
                                )
                            }
                        }
                        .onError { exception ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = exception.message
                                )
                            }
                        }
                }
                .onError { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Location required: ${exception.message}"
                        )
                    }
                }
        }
    }

    private fun acceptJob(jobId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAccepting = true) }

            acceptJobUseCase(jobId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isAccepting = false,
                            snackbarMessage = "Job accepted successfully!"
                        )
                    }
                    refreshJobs(null) // Refresh list
                }
                .onError { exception ->
                    _uiState.update {
                        it.copy(
                            isAccepting = false,
                            error = "Failed to accept job: ${exception.message}"
                        )
                    }
                }
        }
    }

    private fun handleSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterJobs()
    }

    private fun filterJobs() {
        _uiState.update { state ->
            val filtered = if (state.searchQuery.isBlank()) {
                state.jobs
            } else {
                state.jobs.filter { it.job.title.contains(state.searchQuery, ignoreCase = true) }
            }
            state.copy(displayedJobs = filtered)
        }
    }

    private fun toggleFilterSheet(show: Boolean) {
        _uiState.update { it.copy(showFilterSheet = show) }
    }

    private fun toggleMapView(show: Boolean) {
        _uiState.update { it.copy(showMap = show) }
    }

    private fun selectJob(job: Job) {
        _uiState.update { it.copy(selectedJob = job) }
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
```

**Estimated Time:** 6-8 hours

---

### 2.2 Create Domain Layer with Use Cases

**Issue:** Business logic scattered across repositories and UI

#### Steps:

1. **Create domain/usecase/GetJobsForWorkerUseCase.kt**

```kotlin
package com.example.dwhubfix.domain.usecase

import com.example.dwhubfix.domain.repository.JobRepository
import com.example.dwhubfix.domain.model.Job
import com.example.dwhubfix.domain.model.JobMatchScore
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

class GetJobsForWorkerUseCase @Inject constructor(
    private val jobRepository: JobRepository,
    private val matchingEngine: MatchingEngine
) {
    suspend operator fun invoke(
        workerLocation: GeoPoint?
    ): Result<List<JobWithScore>> {
        return try {
            // 1. Get worker profile
            val profile = jobRepository.getWorkerProfile()
                .getOrElse { throw it }

            // 2. Get worker history for compliance check
            val history = jobRepository.getWorkerHistory()
                .getOrElse { emptyList() }

            // 3. Get all available jobs
            val allJobs = jobRepository.getAvailableJobs()
                .getOrElse { throw it }

            // 4. Filter by compliance (21 Days Rule)
            val compliantJobs = allJobs.filter { job ->
                matchingEngine.isJobCompliant(job, history)
            }

            // 5. Calculate and prioritize by score
            val scoredJobs = compliantJobs.map { job ->
                val score = matchingEngine.calculateJobScore(
                    job = job,
                    worker = profile,
                    workerLocation = workerLocation
                )
                JobWithScore(job, score)
            }.sortedByDescending { it.score.totalScore }

            Result.success(scoredJobs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class JobWithScore(
        val job: Job,
        val score: JobMatchScore
    )
}
```

2. **Create domain/usecase/AcceptJobUseCase.kt**

```kotlin
package com.example.dwhubfix.domain.usecase

import com.example.dwhubfix.domain.repository.JobRepository
import javax.inject.Inject

class AcceptJobUseCase @Inject constructor(
    private val jobRepository: JobRepository
) {
    suspend operator fun invoke(jobId: String): Result<Unit> {
        return jobRepository.acceptJob(jobId)
    }
}
```

**Estimated Time:** 4-5 hours

---

### 2.3 Implement Type-Safe Supabase Responses

**Issue:** Raw Map casting, no type safety

#### Steps:

1. **Create data/remote/dto/JobDto.kt**

```kotlin
package com.example.dwhubfix.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JobDto(
    @SerialName("id")
    val id: String,

    @SerialName("business_id")
    val businessId: String,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("wage")
    val wage: Double? = null,

    @SerialName("wage_type")
    val wageType: String? = null,

    @SerialName("location")
    val location: String? = null,

    @SerialName("category")
    val category: String? = null,

    @SerialName("status")
    val status: String = "open",

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    @SerialName("start_time")
    val startTime: String? = null,

    @SerialName("end_time")
    val endTime: String? = null,

    @SerialName("shift_date")
    val shiftDate: String? = null,

    @SerialName("is_urgent")
    val isUrgent: Boolean = false,

    @SerialName("is_compliant")
    val isCompliant: Boolean? = null,

    @SerialName("worker_count")
    val workerCount: Int? = null,

    @SerialName("profiles")
    val businessInfo: UserProfileDto? = null
)

@Serializable
data class UserProfileDto(
    @SerialName("id")
    val id: String,

    @SerialName("full_name")
    val fullName: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("phone_number")
    val phoneNumber: String? = null,

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    @SerialName("role")
    val role: String? = null,

    @SerialName("business_profile")
    val businessProfile: BusinessProfileDto? = null
)

@Serializable
data class BusinessProfileDto(
    @SerialName("business_name")
    val businessName: String? = null,

    @SerialName("latitude")
    val latitude: Double? = null,

    @SerialName("longitude")
    val longitude: Double? = null
)
```

2. **Create data/mapper/JobMapper.kt**

```kotlin
package com.example.dwhubfix.data.mapper

import com.example.dwhubfix.data.remote.dto.JobDto
import com.example.dwhubfix.domain.model.Job

fun JobDto.toDomain(): Job {
    return Job(
        id = id,
        businessId = businessId,
        title = title,
        description = description,
        wage = wage,
        wageType = wageType,
        location = location,
        category = category,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        startTime = startTime,
        endTime = endTime,
        shiftDate = shiftDate,
        isUrgent = isUrgent,
        isCompliant = isCompliant,
        workerCount = workerCount,
        businessName = businessInfo?.businessProfile?.businessName
            ?: businessInfo?.fullName
            ?: "Unknown Business",
        businessLatitude = businessInfo?.businessProfile?.latitude,
        businessLongitude = businessInfo?.businessProfile?.longitude
    )
}

fun List<JobDto>.toDomain(): List<Job> = map { it.toDomain() }
```

3. **Update repository to use DTOs**

```kotlin
class JobRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : JobRepository {

    private val client get() = supabaseClient.client

    override suspend fun getAvailableJobs(): Result<List<Job>> {
        return try {
            val response = client.from("jobs")
                .select {
                    filter {
                        eq("status", "open")
                    }
                }
                .decodeList<JobDto>()  // Type-safe!

            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Estimated Time:** 4-5 hours

---

### 2.4 Define Constants

**Issue:** Magic numbers throughout codebase

#### Steps:

1. **Create domain/model/MatchingConstants.kt**

```kotlin
package com.example.dwhubfix.domain.model

object MatchingConstants {

    // Distance thresholds (km)
    const val DISTANCE_VERY_CLOSE_MAX = 2.0
    const val DISTANCE_CLOSE_MAX = 5.0
    const val DISTANCE_MEDIUM_MAX = 10.0
    const val DISTANCE_FAR_MAX = 20.0
    const val DISTANCE_VERY_FAR_MAX = 30.0

    // Distance scores
    const val SCORE_DISTANCE_VERY_CLOSE = 30.0
    const val SCORE_DISTANCE_CLOSE = 25.0
    const val SCORE_DISTANCE_MEDIUM = 15.0
    const val SCORE_DISTANCE_FAR = 5.0
    const val SCORE_DISTANCE_VERY_FAR = 2.0
    const val SCORE_DISTANCE_OUT_OF_RANGE = 0.0

    // Score weights
    const val WEIGHT_DISTANCE = 30.0
    const val WEIGHT_SKILL = 25.0
    const val WEIGHT_RATING = 20.0
    const val WEIGHT_RELIABILITY = 15.0
    const val WEIGHT_URGENCY = 10.0

    // Maximum total score
    const val MAX_TOTAL_SCORE = 100.0

    // Compliance
    const val MAX_DAYS_PER_CLIENT = 20
    const val COMPLIANCE_WINDOW_DAYS = 30

    // Match quality thresholds
    const val EXCELLENT_MATCH_THRESHOLD = 85.0
    const val GOOD_MATCH_THRESHOLD = 70.0
    const val ACCEPTABLE_MATCH_THRESHOLD = 50.0
}

object MapConstants {
    const val DEFAULT_MAP_ZOOM = 15.0
    const val DEFAULT_MAP_CENTER_LAT = -8.5069
    const val DEFAULT_MAP_CENTER_LNG = 115.2625
    const val MIN_MAP_ZOOM = 10.0
    const val MAX_MAP_ZOOM = 20.0
}

object SessionConstants {
    const val PREF_NAME = "user_session"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_SELECTED_ROLE = "selected_role"
    const val KEY_CURRENT_STEP = "current_step"

    // Token expiration
    const val TOKEN_EXPIRATION_BUFFER_MS = 5 * 60 * 1000 // 5 minutes
}
```

2. **Update usage in MatchingLogic.kt**

```kotlin
// Before:
val distanceScore = when {
    distance < 2.0 -> 30.0
    distance in 2.0..5.0 -> 25.0
    // ...
}

// After:
val distanceScore = when {
    distance < MatchingConstants.DISTANCE_VERY_CLOSE_MAX ->
        MatchingConstants.SCORE_DISTANCE_VERY_CLOSE
    distance < MatchingConstants.DISTANCE_CLOSE_MAX ->
        MatchingConstants.SCORE_DISTANCE_CLOSE
    // ...
}
```

**Estimated Time:** 2-3 hours

---

### 2.5 Extract Reusable UI Components

**Issue:** Duplicate code in UI screens

#### Steps:

1. **Create presentation/common/component/JobCard.kt**

```kotlin
@Composable
fun JobCard(
    job: Job,
    score: JobMatchScore,
    modifier: Modifier = Modifier,
    onAcceptClick: () -> Unit,
    onDetailClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ... card content
        }
    }
}
```

2. **Create presentation/common/component/LoadingState.kt**

```kotlin
@Composable
fun LoadingState(
    message: String = "Memuat..."
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = Primary)
            Spacer(modifier = Modifier.height(12.dp))
            Text(message, style = MaterialTheme.typography.bodySmall)
        }
    }
}
```

**Estimated Time:** 3-4 hours

---

## Phase 3: Low Priority (Week 5-6)

### 3.1 Add KDoc Documentation

**Estimated Time:** 4-5 hours

### 3.2 Add Unit Tests

**Estimated Time:** 8-10 hours

### 3.3 Clean Up Unused Imports

**Estimated Time:** 1 hour

### 3.4 Refactor Long Functions

**Estimated Time:** 3-4 hours

---

## Testing Strategy

### Unit Tests

```
app/src/test/
├── domain/
│   ├── usecase/
│   │   ├── GetJobsForWorkerUseCaseTest.kt
│   │   └── AcceptJobUseCaseTest.kt
│   ├── model/
│   │   └── MatchingEngineTest.kt
│   └── mapper/
│       └── JobMapperTest.kt
└── util/
    └── DistanceUtilsTest.kt
```

### UI Tests

```
app/src/androidTest/
├── presentation/
│   └── worker/
│       └── WorkerHomeScreenTest.kt
```

---

## Migration Guide

### Step-by-Step Migration

1. **Setup Phase**
   - [ ] Add Hilt dependencies
   - [ ] Create Application class
   - [ ] Setup DI modules

2. **Core Layer**
   - [ ] Create SupabaseClient singleton
   - [ ] Create LocationManager
   - [ ] Create constants files

3. **Data Layer**
   - [ ] Create DTOs
   - [ ] Create mappers
   - [ ] Convert repositories to classes

4. **Domain Layer**
   - [ ] Create domain models
   - [ ] Create repository interfaces
   - [ ] Create use cases

5. **Presentation Layer**
   - [ ] Create ViewModels
   - [ ] Create UiState classes
   - [ ] Refactor Composables

---

## Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|------------|
| Breaking changes | High | Incremental refactoring, feature flags |
| Regression bugs | Medium | Comprehensive testing |
| Timeline overrun | Medium | Prioritize critical issues |
| Team knowledge gap | Low | Documentation, pair programming |

---

## Checklist

### Phase 1: Critical Issues
- [ ] 1.1 Implement Singleton SupabaseClient
- [ ] 1.2 Implement Dependency Injection with Hilt
- [ ] 1.3 Fix Repository Pattern
- [ ] 1.4 Remove Mock Data
- [ ] 1.5 Fix Type Safety

### Phase 2: Medium Priority
- [ ] 2.1 Implement ViewModel Pattern
- [ ] 2.2 Create Domain Layer with Use Cases
- [ ] 2.3 Implement Type-Safe Supabase Responses
- [ ] 2.4 Define Constants
- [ ] 2.5 Extract Reusable UI Components

### Phase 3: Low Priority
- [ ] 3.1 Add KDoc Documentation
- [ ] 3.2 Add Unit Tests
- [ ] 3.3 Clean Up Unused Imports
- [ ] 3.4 Refactor Long Functions

---

## Notes

- All refactoring should be done incrementally
- Each phase should be tested before proceeding
- Keep the app functional throughout the refactoring
- Use feature flags for major changes if needed
- Update this document as progress is made

---

**Last Updated:** 2025-02-04
