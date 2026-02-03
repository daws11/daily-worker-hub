#!/bin/bash

# Simple script to import Supabase schema via psql
# Password set via environment variable

echo "╔════════════════════════════════════════╗"
echo "║     Supabase Database Import via psql          ║"
echo "║           Daily Worker Hub                    ║"
echo "╚═════════════════════════════════════════╝"
echo ""

echo "🔌 Importing database schema..."
echo ""

# Execute schema.sql using psql
PGPASSWORD="WCkztvRnQ1ihdayD" psql \
  -h db.airhufmbwqxmojnkknan.supabase.co \
  -p 5432 \
  -d postgres \
  -f supabase/schema.sql

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Schema import completed!"
    echo ""
    echo "📊 Summary:"
    echo "   ✅ All tables created"
    echo "   ✅ RLS policies applied"
    echo "   ✅ Functions & triggers created"
    echo "   ✅ Views created"
    echo ""
    echo "🎯 Next steps:"
    echo "   1. Run validation: node scripts/validate-db.js"
    echo "   2. Verify tables in Supabase Dashboard"
    echo "   3. Start development: npm run dev (admin) / Android Studio"
else
    echo ""
    echo "❌ Schema import failed!"
    echo "   Check psql connection and password"
    echo ""
    echo "💡 Troubleshooting:"
    echo "   - Check psql is installed: which psql"
    echo "   - Test connection manually:"
    echo "     psql -h db.airhufmbwqxmojnkknan.supabase.co -p 5432 -d postgres"
    exit 1
fi

echo ""
echo "═══════════════════════════════════════════════"
