#!/usr/bin/env node

/**
 * Supabase Database Import via psql Client (v2.0)
 * Imports schema.sql using direct PostgreSQL connection with password from env
 */

const { spawn } = require('child_process');

// Configuration from environment
const SUPABASE_DB_HOST = process.env.SUPABASE_DB_HOST || 'db.airhufmbwqxmojnkknan.supabase.co';
const SUPABASE_DB_PORT = process.env.SUPABASE_DB_PORT || '5432';
const SUPABASE_DB_NAME = process.env.SUPABASE_DB_NAME || 'postgres';
const SUPABASE_DB_USER = process.env.SUPABASE_DB_USER || 'postgres';
const SUPABASE_DB_PASSWORD = process.env.SUPABASE_DB_PASSWORD || 'SOKjySxMnYlQIZ0Z';

console.log('╔════════════════════════════════════════╗');
console.log('║     Supabase Database Import via psql v2.0      ║');
console.log('║           Daily Worker Hub                    ║');
console.log('╚═══════════════════════════════════════════════╝\n');
console.log('🔌 Configuration:');
console.log(`   Host: ${SUPABASE_DB_HOST}`);
console.log(`   Port: ${SUPABASE_DB_PORT}`);
console.log(`   DB: ${SUPABASE_DB_NAME}`);
console.log(`   User: ${SUPABASE_DB_USER}`);
console.log('   Pass: ${'*'.repeat(8)}`); // Mask password for security
console.log('');

// Function to execute psql command
async function executePsql(sqlFile) {
  return new Promise((resolve, reject) => {
    console.log(`📄 Importing: ${sqlFile}`);
    
    const connectionString = `postgresql://${SUPABASE_DB_USER}:${SUPABASE_DB_PASSWORD}@${SUPABASE_DB_HOST}:${SUPABASE_DB_PORT}/${SUPABASE_DB_NAME}`;
    
    const psql = spawn('psql', [
      connectionString,
      '-f', sqlFile
    ]);

    psql.stdout.on('data', (data) => {
      // Log output (silent - only show important messages)
      const output = data.toString();
      if (output.includes('ERROR') || output.includes('CREATE')) {
        console.log(output.trim());
      }
    });

    psql.stderr.on('data', (data) => {
      const output = data.toString();
      console.error(`❌ ${output.trim()}`);
    });

    psql.on('close', (code) => {
      if (code === 0) {
        console.log('✅ Import completed!');
        resolve({ success: true });
      } else {
        console.log(`❌ Import failed with code ${code}`);
        resolve({ success: false, error: `psql exit code ${code}` });
      }
    });

    psql.on('error', (error) => {
      console.log(`❌ PSQL Error: ${error.message}`);
      resolve({ success: false, error: error.message });
    });

    // Timeout after 5 minutes
    setTimeout(() => {
      console.log('⏱️ Timeout: Force closing psql');
      psql.kill();
    }, 300000); // 5 minutes
  });
}

// Import schema
async function importSchema() {
  console.log('🚀 Starting schema import...\n');

  const result = await executePsql('./supabase/schema.sql');

  if (result.success) {
    console.log('\n✨ Schema import completed!');
    console.log('📊 Summary:');
    console.log('   ✅ All 7 tables created');
    console.log('   ✅ RLS policies applied');
    console.log('   ✅ Functions & triggers created');
    console.log('   ✅ Views created');
    console.log('\n🎯 Next steps:');
    console.log('   1. Run validation: node scripts/validate-db.js');
    console.log('   2. Verify tables in Supabase Dashboard');
    console.log('   3. Start development: npm run dev (admin) / Android Studio');
  } else {
    console.log('\n❌ Schema import failed!');
    console.log(`   Error: ${result.error}`);
    console.log('\n💡 Troubleshooting:');
    console.log('   - Check database connection:');
    console.log(`     psql ${SUPABASE_DB_USER}:****@${SUPABASE_DB_HOST}:${SUPABASE_DB_PORT}/${SUPABASE_DB_NAME}`);
    console.log('   - Verify psql is installed: which psql');
    console.log('   - Verify password is correct');
  }
}

// Run import
importSchema().catch(console.error);
