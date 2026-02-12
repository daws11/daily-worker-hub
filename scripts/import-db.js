#!/usr/bin/env node

/**
 * Supabase Database Import via REST API (Direct Method)
 * Uses direct SQL execution via psql-compatible endpoint
 */

const https = require('https');
const http = require('http');

const SUPABASE_URL = 'https://airhufmbwqxmojnkknan.supabase.co';
const ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFpcmh1Zm1id3F4bW9qbmtrbmFuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU3MTE4ODksImV4cCI6MjA4MTI4Nzg4OX0.sylxYFsIvgWOj9OdZwgf-ZCb8nc9pBS_oY2u7EQon5g';

console.log('╔════════════════════════════════════════════════╗');
console.log('║  Supabase Database Import via REST API           ║');
console.log('║              Direct SQL Execution                  ║');
console.log('╚══════════════════════════════════════════════════╝\n');

// Function to execute single SQL statement
async function executeSQL(sql, description) {
  return new Promise((resolve, reject) => {
    const postData = JSON.stringify({ query: sql });

    const options = {
      hostname: new URL(SUPABASE_URL).hostname,
      port: 443,
      path: '/sql/v1',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'apikey': ANON_KEY,
        'Authorization': `Bearer ${ANON_KEY}`,
        'Accept': 'application/json'
      }
    };

    const req = https.request(options, (res) => {
      let data = '';

      res.on('data', (chunk) => {
        data += chunk;
      });

      res.on('end', () => {
        if (res.statusCode === 200 || res.statusCode === 201) {
          try {
            const result = JSON.parse(data);
            
            if (result.error) {
              console.log(`❌ ${description} — SQL Error: ${result.error.message}`);
              resolve({ success: false, error: result.error.message });
            } else {
              console.log(`✅ ${description} — Success`);
              resolve({ success: true });
            }
          } catch (e) {
            console.log(`✅ ${description} — Success (no error field)`);
            resolve({ success: true });
          }
        } else {
          console.log(`❌ ${description} — HTTP ${res.statusCode}`);
          console.log(`   Response: ${data.substring(0, 200)}...`);
          resolve({ success: false, error: `HTTP ${res.statusCode}` });
        }
      });
    });

    req.on('error', (error) => {
      console.log(`❌ ${description} — Network Error: ${error.message}`);
      resolve({ success: false, error: error.message });
    });

    req.setTimeout(30000);
    req.write(postData);
    req.end();
  });
}

// Read SQL file
const fs = require('fs');
const path = require('path');

const sqlFiles = [
  { file: 'supabase/01-workers.sql', desc: 'Workers table' },
  { file: 'supabase/02-businesses.sql', desc: 'Businesses table' },
  { file: 'supabase/03-job-assignments.sql', desc: 'Job Assignments table' },
  { file: 'supabase/04-wallets.sql', desc: 'Wallets table' },
  { file: 'supabase/05-wallet-transactions.sql', desc: 'Wallet Transactions table' },
  { file: 'supabase/06-audit-logs.sql', desc: 'Audit Logs table' }
];

// Import all tables
async function importAllTables() {
  console.log('🚀 Starting database import...\n');
  
  let successCount = 0;
  let failCount = 0;

  for (const sqlFile of sqlFiles) {
    console.log(`\n${'─'.repeat(50)}`);
    console.log(`📄 Importing: ${sqlFile.desc}`);
    
    try {
      const sqlContent = fs.readFileSync(sqlFile.file, 'utf8');
      const result = await executeSQL(sqlContent, sqlFile.desc);
      
      if (result.success) {
        successCount++;
      } else {
        failCount++;
      }
      
      // Add delay to avoid rate limiting
      await new Promise(resolve => setTimeout(resolve, 500));
    } catch (error) {
      console.log(`❌ Failed to read ${sqlFile.file}: ${error.message}`);
      failCount++;
    }
  }

  console.log(`\n${'─'.repeat(50)}`);
  console.log('╔════════════════════════════════════════════════════╗');
  console.log('║                    Import Summary                        ║');
  console.log('╚═══════════════════════════════════════════════════════╝');
  console.log(`✅ Successful: ${successCount}/${sqlFiles.length}`);
  console.log(`❌ Failed: ${failCount}/${sqlFiles.length}`);
  
  if (failCount > 0) {
    console.log('\n⚠️  Some imports failed. Check errors above.');
    return false;
  }

  return true;
}

// Run import
importAllTables()
  .then(success => {
    if (success) {
      console.log('\n✨ Database import completed!');
      console.log('🎯 Next: Run validation script to verify');
      console.log('\n   node scripts/validate-db.js');
    } else {
      console.log('\n❌ Database import failed!');
    }
  })
  .catch(console.error);
