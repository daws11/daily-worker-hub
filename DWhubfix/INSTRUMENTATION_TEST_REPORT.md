# Instrumentation Test Setup - Daily Worker Hub

**Date:** 2026-02-08
**Status:** ✅ Ready for execution

---

## Setup Summary

### Configuration Changes Made

1. **Created androidTest Integration Test Directory**
   - Location: `/app/src/androidTest/java/com/example/dwhubfix/data/repository/integration/`
   - Copied all integration tests from `test/` directory

2. **Updated build.gradle.kts**
   - Added instrumentation test dependencies:
     - `androidTestImplementation("io.github.jan-tennert.supabase:auth-kt:3.0.0")`
     - `androidTestImplementation("io.github.jan-tennert.supabase:postgrest-kt:3.0.0")`
     - `androidTestImplementation("io.ktor:ktor-client-android:3.0.1")`

3. **Fixed Test Method Names for DEX Compatibility**
   - Converted all backtick-wrapped method names to underscore-separated names
   - Example: `fun \`login with valid credentials\`` → `fun login_with_valid_credentials()`

4. **Updated TestDataManager.kt for Android Context**
   - Added `init(context: Context)` method
   - Added `getContext()` method
   - Uses Android Context for Supabase Auth session management

5. **Updated BaseIntegrationTest.kt**
   - Uses `InstrumentationRegistry.getInstrumentation().targetContext` for Android Context
   - Initializes TestDataManager with context before creating Supabase client

---

## Test Configuration

### Test Users

| Role | Email | User ID |
|------|-------|---------|
| Worker | integration-test-worker@example.com | 863d1475-5abe-4a51-a929-5b419dc0d790 |
| Business | integration-test-business@example.com | 5fa5132b-39c1-4a47-8bcc-ccd8995613db |

### Supabase Configuration

| Setting | Value |
|---------|-------|
| URL | https://airhufmbwqxmojnkknan.supabase.co |
| Anon Key | eyJhbGc... (configured in test-config.properties) |
| Service Role Key | eyJhbGc... (configured in test-config.properties) |

---

## Available Integration Test Suites

| Suite | File | Tests | Coverage |
|-------|------|-------|----------|
| **Auth** | `AuthRepositoryIntegrationTest.kt` | 15 | Login, registration, logout, sessions |
| **Job** | `JobRepositoryIntegrationTest.kt` | ~15 | CRUD, applications, completion |
| **Booking** | `BookingRepositoryIntegrationTest.kt` | ~13 | Clock-in/out, earnings, shifts |
| **Matching** | `MatchingRepositoryIntegrationTest.kt` | ~13 | Job matching, 21 Days Rule, distance |

---

## Running Instrumentation Tests

### Prerequisites

1. **Android Emulator or Device**
   - API level 24+ (Android 7.0+)
   - Connected via ADB

2. **Verify Device Connection**
   ```bash
   adb devices
   ```

### Running Tests

```bash
# Run all integration tests
./gradlew connectedAndroidTest

# Run specific test class
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.dwhubfix.data.repository.integration.AuthRepositoryIntegrationTest

# Run specific test method
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.dwhubfix.data.repository.integration.AuthRepositoryIntegrationTest#login_with_valid_credentials_returns_success
```

### View Test Reports

```bash
# HTML Report
open app/build/reports/androidTests/connected/index.html

# XML Report (for CI/CD)
cat app/build/test-results/androidTests/connected/*
```

---

## Test Method Naming Convention

All test methods use `snake_case` naming (no spaces, no special characters):

**Examples:**
- ✅ `login_with_valid_credentials_returns_success()`
- ✅ `apply_for_job_duplicate_returns_failure()`
- ✅ `distance_scoring_0_to_5_km_returns_30_points()`
- ❌ `fun \`login with valid credentials\`` (invalid for DEX)

---

## Next Steps

1. ✅ Build successful
2. ⏳ Run tests on emulator/device
3. ⏳ Document test results
4. ⏳ Set up CI/CD pipeline

---

## Notes

- **JVM Tests vs Instrumentation Tests**: The integration tests require Android Context for Supabase Auth session management, so they must run as instrumentation tests on a device/emulator rather than JVM unit tests.

- **Test Data Isolation**: Each test generates a unique `testId = "test-{UUID}"` to tag its data, which is cleaned up after the test completes.

- **Cleanup Function**: PostgreSQL function `cleanup_test_data(p_test_id)` is available for batch test data cleanup.
