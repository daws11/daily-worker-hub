#!/bin/bash
# Test Supabase Dev Endpoints
# Run this after deploying nginx configuration

set -e

BASE_URL="http://supabase-dev.dailyworkerhub.com"

echo "=========================================="
echo "  Testing Supabase Dev Endpoints"
echo "=========================================="
echo ""

# Function to test endpoint
test_endpoint() {
    local name=$1
    local url=$2
    local expected=${3:-"200"}

    echo -n "Testing $name... "

    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$url" --max-time 5)

    if [ "$HTTP_CODE" = "$expected" ]; then
        echo "✅ OK (HTTP $HTTP_CODE)"
        return 0
    else
        echo "❌ FAIL (HTTP $HTTP_CODE, expected $expected)"
        return 1
    fi
}

# Test all endpoints
failures=0

test_endpoint "Health Check" "$BASE_URL/" "200" || ((failures++))
test_endpoint "Auth API" "$BASE_URL/auth/v1/" "200" || ((failures++))
test_endpoint "REST API" "$BASE_URL/rest/v1/" "200" || ((failures++))
test_endpoint "Storage API" "$BASE_URL/storage/v1/" "401" || ((failures++))  # Storage returns 401 without auth
test_endpoint "Functions API" "$BASE_URL/functions/v1/" "200" || ((failures++))

echo ""
echo "=========================================="
if [ $failures -eq 0 ]; then
    echo "✅ All endpoints accessible!"
else
    echo "❌ $failures endpoint(s) failed"
    echo ""
    echo "Troubleshooting:"
    echo "1. Check if Supabase is running: supabase status"
    echo "2. Check nginx logs: tail -f /var/log/nginx/supabase-dev-error.log"
    echo "3. Check firewall: sudo ufw status"
fi
echo "=========================================="
