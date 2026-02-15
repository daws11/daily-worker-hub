# Testing Options Without Android Emulator

**Date:** February 12, 2026
**Environment:** VPS (No Android Studio/Emulator)

---

## 🎯 Overview

Since Android Studio + Emulator is not feasible on this machine, here are **alternative testing strategies** to ensure the application will work correctly when integration tests are run.

---

## ✅ 1. JVM Unit Tests (Ready Now)

### What It Tests
- Business logic validation
- Use case behavior
- Input/output validation
- Edge cases and error handling
- Repository method signatures

### Status
- ✅ **155 tests already written**
- ✅ **100% use case coverage**
- ✅ Can run without emulator/Android Studio

### How to Run
```bash
cd DWhubfix
./gradlew test
```

### What It Covers
- Login/Authentication logic
- Job creation, matching, application
- Booking management
- Statistics calculations
- Profile management
- Wallet operations

### Limitations
- Does not test real API calls
- Uses fake repositories (mocks)
- Does not test Android SDK integrations
- Does not test Supabase Auth

---

## ✅ 2. API/Backend Integration Testing

### What It Tests
- Supabase API endpoints
- Database queries
- Authentication flow
- RLS (Row Level Security) policies
- CRUD operations

### How to Run

#### Option A: curl Scripts
```bash
# Test auth endpoint
curl -X POST "http://127.0.0.1:54321/auth/v1/token?grant_type=password" \
  -H "apikey: PUBLISHABLE_KEY_REMOVED" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "integration-test-worker@example.com",
    "password": "TestWorker123!"
  }'

# Test database query
curl -X GET "http://127.0.0.1:54321/rest/v1/profiles?email=eq.integration-test-worker@example.com" \
  -H "apikey: PUBLISHABLE_KEY_REMOVED" \
  -H "Authorization: Bearer <token>"
```

#### Option B: Postman Collection
- Create Postman collection for all API endpoints
- Import environment variables (URL, keys)
- Run automated test suite

#### Option C: Automated Test Script
```bash
# Create scripts/test-api.sh
#!/bin/bash
# Run all API tests and validate responses

# Example: Test worker login
RESPONSE=$(curl -s -X POST "http://127.0.0.1:54321/auth/v1/token?grant_type=password" \
  -H "apikey: PUBLISHABLE_KEY_REMOVED" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "integration-test-worker@example.com",
    "password": "TestWorker123!"
  }')

# Validate response contains access_token
if echo "$RESPONSE" | grep -q "access_token"; then
  echo "✅ Login test PASSED"
else
  echo "❌ Login test FAILED"
  echo "$RESPONSE"
fi
```

### What It Covers
- Auth endpoints (login, register, logout)
- REST API (jobs, profiles, bookings)
- Database constraints
- RLS policies
- Error handling

---

## ✅ 3. Database Schema Validation

### What It Tests
- Table structures match app expectations
- Foreign key constraints work
- Triggers and functions execute correctly
- RLS policies enforce security
- Indexes are created properly

### How to Run

#### Option A: Direct SQL Tests
```sql
-- Test foreign key constraint
INSERT INTO workers (id, skill_categories, experience_level, available)
VALUES (
  (SELECT id FROM auth.users LIMIT 1),
  ARRAY['cleaning'],
  'intermediate',
  true
);

-- Test trigger (should create wallet)
SELECT * FROM wallets WHERE user_id = (SELECT id FROM workers LIMIT 1);

-- Test RLS policy
-- Login as worker and try to access another user's data
SET LOCAL request.jwt.claim.sub = '35a48491-cd86-4e1d-96f9-a195de2da93d';
SELECT * FROM profiles WHERE id != '35a48491-cd86-4e1d-96f9-a195de2da93d';
-- Should return empty (RLS working)
```

#### Option B: Automated Schema Tests
```python
# tests/test_schema.py
import psycopg2
import requests

def test_schema_consistency():
    conn = psycopg2.connect(
        host="127.0.0.1",
        port=54322,
        user="postgres",
        password="postgres",
        database="postgres"
    )

    cur = conn.cursor()

    # Check required tables exist
    cur.execute("""
        SELECT table_name
        FROM information_schema.tables
        WHERE table_schema = 'public'
        ORDER BY table_name;
    """)

    tables = [row[0] for row in cur.fetchall()]
    required_tables = [
        'profiles', 'workers', 'businesses', 'jobs',
        'job_applications', 'job_assignments', 'wallets', 'wallet_transactions'
    ]

    for table in required_tables:
        assert table in tables, f"Missing table: {table}"

    print("✅ Schema validation PASSED")
```

### What It Covers
- All tables created correctly
- Columns match app data models
- Foreign keys work
- RLS policies enforce security
- Triggers create related records
- Indexes exist for performance

---

## ✅ 4. Integration Test Simulation

### What It Tests
- End-to-end workflows using API calls
- Data flow between tables
- Business logic validation
- Error scenarios

### How to Run

#### Script Example
```bash
#!/bin/bash
# scripts/test-integration.sh

# 1. Login as worker
echo "Testing worker login..."
LOGIN_RESPONSE=$(curl -s -X POST "http://127.0.0.1:54321/auth/v1/token?grant_type=password" \
  -H "apikey: PUBLISHABLE_KEY_REMOVED" \
  -H "Content-Type: application/json" \
  -d '{"email":"integration-test-worker@example.com","password":"TestWorker123!"}')

TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.access_token')

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
  echo "❌ Login failed"
  exit 1
fi
echo "✅ Worker login successful"

# 2. Create job as business
echo "Testing job creation..."
BUSINESS_LOGIN=$(curl -s -X POST "http://127.0.0.1:54321/auth/v1/token?grant_type=password" \
  -H "apikey: PUBLISHABLE_KEY_REMOVED" \
  -H "Content-Type: application/json" \
  -d '{"email":"integration-test-business@example.com","password":"TestBusiness123!"}')

BUSINESS_TOKEN=$(echo $BUSINESS_LOGIN | jq -r '.access_token')

JOB_RESPONSE=$(curl -s -X POST "http://127.0.0.1:54321/rest/v1/jobs" \
  -H "apikey: PUBLISHABLE_KEY_REMOVED" \
  -H "Authorization: Bearer $BUSINESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "business_id": "347e9d72-5901-42d5-8231-c5341fb6d562",
    "title": "Test Job",
    "category": "cleaning",
    "start_time": "2026-02-13T08:00:00Z",
    "end_time": "2026-02-13T12:00:00Z",
    "wage_amount": 150000,
    "workers_needed": 1,
    "location": "Ubud, Bali"
  }')

if echo "$JOB_RESPONSE" | grep -q "id"; then
  echo "✅ Job creation successful"
else
  echo "❌ Job creation failed"
  echo "$JOB_RESPONSE"
  exit 1
fi

# 3. Apply for job as worker
echo "Testing job application..."
JOB_ID=$(echo $JOB_RESPONSE | jq -r '.id')

APPLICATION_RESPONSE=$(curl -s -X POST "http://127.0.0.1:54321/rest/v1/job_applications" \
  -H "apikey: PUBLISHABLE_KEY_REMOVED" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"job_id\": \"$JOB_ID\",
    \"worker_id\": \"35a48491-cd86-4e1d-96f9-a195de2da93d\",
    \"status\": \"pending\"
  }")

if echo "$APPLICATION_RESPONSE" | grep -q "id"; then
  echo "✅ Job application successful"
else
  echo "❌ Job application failed"
  echo "$APPLICATION_RESPONSE"
  exit 1
fi

echo "🎉 Integration tests PASSED"
```

### What It Covers
- Full user flows
- API contract validation
- Data integrity
- Business logic
- Error handling

---

## ✅ 5. Build APK & Manual Testing

### What It Tests
- App compiles correctly
- Resources are included
- ProGuard/R8 minification works
- APK is installable

### How to Run
```bash
cd DWhubfix

# Build debug APK
./gradlew assembleDebug

# APK location: app/build/outputs/apk/debug/app-debug.apk

# Install on physical device
adb install app/build/outputs/apk/debug/app-debug.apk

# Manual testing checklist:
# - Login with test user
# - Browse jobs
# - Apply for job
# - Create job (as business)
# - View profile
# - Check wallet
```

### What It Covers
- Build system works
- No compile errors
- Resources included
- APK installable
- Basic app flows (manual)

---

## ✅ 6. Contract Testing

### What It Tests
- API response format matches app expectations
- Data types are correct
- Required fields are present
- Error responses follow expected format

### How to Run

#### JSON Schema Validation
```json
// schemas/job.schema.json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "required": ["id", "business_id", "title", "category", "start_time", "end_time", "wage_amount", "workers_needed", "status"],
  "properties": {
    "id": {"type": "string", "format": "uuid"},
    "business_id": {"type": "string", "format": "uuid"},
    "title": {"type": "string"},
    "category": {"type": "string"},
    "start_time": {"type": "string", "format": "date-time"},
    "end_time": {"type": "string", "format": "date-time"},
    "wage_amount": {"type": "number", "minimum": 0},
    "workers_needed": {"type": "integer", "minimum": 1},
    "status": {"type": "string", "enum": ["open", "filled", "in_progress", "completed", "cancelled"]}
  }
}
```

```bash
# Validate API response against schema
curl -s "http://127.0.0.1:54321/rest/v1/jobs?limit=1" \
  -H "apikey: PUBLISHABLE_KEY_REMOVED" | \
  ajv validate -s schemas/job.schema.json
```

### What It Covers
- API contract compliance
- Data type validation
- Required fields present
- Enum values valid
- Date/time formats correct

---

## ✅ 7. Performance Testing

### What It Tests
- Database query performance
- API response times
- Index effectiveness
- Concurrent access

### How to Run

#### Database Query Performance
```sql
-- Check slow queries
SELECT query, mean_exec_time, calls
FROM pg_stat_statements
ORDER BY mean_exec_time DESC
LIMIT 10;

-- Test index usage
EXPLAIN ANALYZE
SELECT * FROM jobs
WHERE status = 'open'
  AND start_time > NOW()
ORDER BY urgent DESC, created_at ASC;
```

#### API Response Time
```bash
# Measure API response time
for i in {1..100}; do
  time curl -s "http://127.0.0.1:54321/rest/v1/jobs" \
    -H "apikey: PUBLISHABLE_KEY_REMOVED" > /dev/null
done

# Calculate average response time
```

---

## 📊 Testing Matrix

| Test Type | Emulator Required | What It Validates | Effort | Confidence |
|-----------|------------------|-------------------|---------|------------|
| **JVM Unit Tests** | ❌ No | Business logic | Low | Medium |
| **API Testing** | ❌ No | Backend integration | Medium | High |
| **Database Tests** | ❌ No | Schema & data | Low | High |
| **Integration Scripts** | ❌ No | End-to-end flows | Medium | High |
| **APK Build** | ❌ No | Compilable | Low | Medium |
| **Manual Testing** | ✅ Device | Real user experience | High | Very High |
| **Android Studio Tests** | ✅ Yes | Full stack | High | Very High |

---

## 🎯 Recommended Testing Strategy

### Phase 1: Validation (Now)
```bash
# 1. Run JVM unit tests
./gradlew test

# 2. Validate database schema
psql postgresql://postgres:postgres@127.0.0.1:54322/postgres -f scripts/validate-schema.sql

# 3. Test API endpoints
./scripts/test-api.sh

# 4. Run integration scripts
./scripts/test-integration.sh
```

### Phase 2: Build & Deploy (When Ready)
```bash
# 1. Build APK
./gradlew assembleDebug

# 2. Install on physical device
adb install app/build/outputs/apk/debug/app-debug.apk

# 3. Manual testing
# - Login test
# - Job browse test
# - Application test
```

### Phase 3: Full Integration (With Device)
- Run instrumentation tests on physical device
- Manual testing of all flows
- Bug fixing based on real device feedback

---

## 🚀 Next Steps

1. **Run JVM unit tests** (verifying now)
2. **Create API test scripts** (curl/bash)
3. **Create database validation scripts**
4. **Build APK and test on physical device**
5. **Document any issues found**

---

**Conclusion:** While Android instrumentation tests require an emulator/device, you can still achieve **high confidence** in the application by combining JVM unit tests, API testing, database validation, and integration scripts. This approach validates the critical paths without requiring Android Studio or an emulator.
