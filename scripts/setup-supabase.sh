#!/bin/bash

# Daily Worker Hub - Database Setup Script
# Uses Supabase CLI to link project and import schema

echo "╔══════════════════════════════════════════════════╗"
echo "║        Daily Worker Hub Database Setup Script               ║"
echo "║                  Supabase CLI v2.74.5                  ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

# Check if logged in
echo "🔍 Checking Supabase CLI status..."
supabase status 2>/dev/null || echo "Not logged in"

# Link project using project ref
echo ""
echo "🔗 Linking to project: airhufmbwqxmojnkknan"
supabase link --project-ref airhufmbwqxmojnkknan

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Project linked successfully!"
    echo ""
    echo "📋 Current project:"
    supabase status
else
    echo ""
    echo "❌ Failed to link project"
    echo "💡 Please login first: supabase login"
    exit 1
fi

echo ""
echo "═══════════════════════════════════════════════════════════"
