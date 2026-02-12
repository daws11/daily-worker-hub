# Integration Tests for Daily Worker Hub

This directory contains JVM-based integration tests that test the actual Supabase operations for the Daily Worker Hub Android app.

## Overview

- **Test Framework**: JUnit 4 + Coroutines Test
- **Test Type**: JVM Tests (no Android device/emulator needed)
- **Coverage**: All 4 repositories - Auth, Job, Booking, Matching
- **Total Tests**: ~64 integration tests

## Prerequisites

### 1. Supabase Development/Staging Environment

You need access to a Supabase project for testing. This can be:
- Your existing development/staging environment
- A dedicated test project

### 2. Database Setup

Run the migration scripts in your Supabase SQL editor:

```sql
-- Run: supabase/migrations/add_test_tracking_columns.sql
-- Then: supabase/migrations/create_cleanup_function.sql
```

### 3. Create Test Users

Create two test users in your Supabase project (via Auth tab or API):

**Worker Test User:**
- Email: `integration-test-worker@example.com`
- Password: `TestWorker123!`

**Business Test User:**
- Email: `integration-test-business@example.com`
- Password: `TestBusiness123!`

## Configuration

### 1. Create Test Configuration File

Copy the example configuration:

```bash
cp app/src/test/resources/test-config.properties.example app/src/test/resources/test-config.properties
```

### 2. Edit the Configuration

Fill in your Supabase credentials:

```properties
# Supabase Project URL
supabase.test.url=https://your-project.supabase.co

# Supabase Anon Key
supabase.test.key=your-anon-key-here

# Test User Credentials
test.user.worker.email=integration-test-worker@example.com
test.user.worker.password=TestWorker123!
test.user.business.email=integration-test-business@example.com
test.user.business.password=TestBusiness123!
```

### 3. Configuration File Security

The `test-config.properties` file is in `.gitignore` and will not be committed.

**Alternative: Environment Variables**

You can also use environment variables instead of the config file:

```bash
export SUPABASE_DEV_URL="https://your-project.supabase.co"
export SUPABASE_DEV_KEY="your-anon-key"
export TEST_WORKER_EMAIL="integration-test-worker@example.com"
export TEST_WORKER_PASSWORD="TestWorker123!"
export TEST_BUSINESS_EMAIL="integration-test-business@example.com"
export TEST_BUSINESS_PASSWORD="TestBusiness123!"
```

## Running Tests

### Run All Integration Tests

```bash
./gradlew :app:testDebugUnitTest --tests "*IntegrationTest*"
```

### Run Specific Test Class

```bash
./gradlew :app:testDebugUnitTest --tests "*AuthRepositoryIntegrationTest"
./gradlew :app:testDebugUnitTest --tests "*JobRepositoryIntegrationTest"
./gradlew :app:testDebugUnitTest --tests "*BookingRepositoryIntegrationTest"
./gradlew :app:testDebugUnitTest --tests "*MatchingRepositoryIntegrationTest"
```

### Run with Verbose Logging

```bash
./gradlew :app:testDebugUnitTest --tests "*IntegrationTest*" --info
```

### Run Specific Test Method

```bash
./gradlew :app:testDebugUnitTest --tests "*AuthRepositoryIntegrationTest.login with valid credentials returns success"
```

## Test Structure

### Base Integration Test

`BaseIntegrationTest.kt` provides:
- Supabase client initialization
- Test authentication helpers
- Test data cleanup
- Unique test ID generation for data isolation

### Test Classes

| Test Class | Tests | Coverage |
|------------|-------|----------|
| `AuthRepositoryIntegrationTest` | ~13 | Login, registration, logout, session management |
| `JobRepositoryIntegrationTest` | ~15 | Job CRUD, applications, completion, stats |
| `BookingRepositoryIntegrationTest` | ~13 | Clock-in/out, earnings, shift management |
| `MatchingRepositoryIntegrationTest` | ~13 | Job matching, 21 Days Rule, distance scoring |

### Data Isolation

Each test generates a unique `testId` (UUID) that tags all created data:

```kotlin
testId = TestDataManager.generateTestId() // "test-{UUID}"
```

All test data includes:
- `test_id`: Unique identifier for the test
- `is_test_data`: Boolean flag for test data

After each test, all data with that `test_id` is cleaned up.

## Troubleshooting

### Tests Fail with Configuration Error

**Error**: `Supabase URL must be configured`

**Solution**: Check that `test-config.properties` exists and has valid values, or set environment variables.

### Tests Fail with Authentication Error

**Error**: `Invalid credentials`

**Solution**:
1. Verify test users exist in Supabase Auth
2. Check email/password in configuration
3. Ensure users are confirmed (not pending email verification)

### Tests Fail with Cleanup Error

**Error**: `Failed to clean up test data`

**Solution**:
1. Run the migration scripts to add test tracking columns
2. Ensure the cleanup function exists
3. Check RLS policies allow deletion

### Tests Timeout

**Error**: Tests take too long or timeout

**Solution**:
1. Check network connectivity to Supabase
2. Verify Supabase project is not paused
3. Increase test timeout if needed

## CI/CD Integration

Example GitHub Actions workflow:

```yaml
name: Integration Tests

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  integration-tests:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'

      - name: Run integration tests
        env:
          SUPABASE_DEV_URL: ${{ secrets.SUPABASE_DEV_URL }}
          SUPABASE_DEV_KEY: ${{ secrets.SUPABASE_DEV_KEY }}
          TEST_WORKER_EMAIL: ${{ secrets.TEST_WORKER_EMAIL }}
          TEST_WORKER_PASSWORD: ${{ secrets.TEST_WORKER_PASSWORD }}
          TEST_BUSINESS_EMAIL: ${{ secrets.TEST_BUSINESS_EMAIL }}
          TEST_BUSINESS_PASSWORD: ${{ secrets.TEST_BUSINESS_PASSWORD }}
        run: ./gradlew :app:testDebugUnitTest --tests "*IntegrationTest*"

      - name: Upload test report
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: integration-test-report
          path: app/build/reports/tests/testDebugUnitTest/
```

## Test Reports

After running tests, view the HTML report:

```bash
open app/build/reports/tests/testDebugUnitTest/index.html
```

## Contributing

When adding new integration tests:

1. Extend `BaseIntegrationTest`
2. Use `buildTestData()` to create data with `test_id` tagging
3. Clean up any additional resources in test teardown
4. Update this README with new test information

## Best Practices

1. **Test Isolation**: Each test should be independent and clean up after itself
2. **Use Test Helpers**: Use `authenticateAsWorker()` and `authenticateAsBusiness()` helpers
3. **Data Tagging**: Always use `buildTestData()` to create test data
4. **Assert First**: Verify state before acting (e.g., assert authenticated before operations)
5. **Error Messages**: Use descriptive assertion messages for debugging
