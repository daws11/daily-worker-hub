#!/usr/bin/env node

/**
 * Import Supabase Schema Script
 * This script imports the database schema to Supabase using REST API
 */

const https = require('https');
const http = require('http');
const fs = require('fs');
const path = require('path');

// Supabase credentials
const SUPABASE_URL = 'https://airhufmbwqxmojnkknan.supabase.co';
const SERVICE_ROLE_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFpcmh1Zm1id3F4bW9qbmtrbmFuIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc2NTcxMTg4OSwiZXhwIjoyMDgxMjg3ODg5fQ.9O2swifcIIxqBMPFl3U0amByKy2bHbRipbnzr75HgOk';

const REST_API_URL = `${SUPABASE_URL}/rest/v1/rpc/execute_sql`;

console.log('🔌 Connecting to Supabase:', SUPABASE_URL);
console.log('📄 Reading schema file...');

// Read the SQL schema file
const schemaPath = path.join(__dirname, '../supabase/schema.sql');
const schemaSQL = fs.readFileSync(schemaPath, 'utf8');

console.log('✅ Schema loaded:', schemaSQL.length, 'characters');

// Split SQL by semicolons for execution
const statements = schemaSQL
  .split(';')
  .map(s => s.trim())
  .filter(s => s.length > 0 && !s.startsWith('--'));

console.log('📝 Found', statements.length, 'SQL statements');

// Execute each statement
async function executeStatement(sql, index) {
  return new Promise((resolve, reject) => {
    const postData = JSON.stringify({ sql });

    const options = {
      hostname: new URL(SUPABASE_URL).hostname,
      port: 443,
      path: '/rest/v1/rpc/execute_sql',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'apikey': SERVICE_ROLE_KEY,
        'Authorization': `Bearer ${SERVICE_ROLE_KEY}`,
        'Content-Length': Buffer.byteLength(postData)
      }
    };

    const req = https.request(options, (res) => {
      let data = '';

      res.on('data', (chunk) => {
        data += chunk;
      });

      res.on('end', () => {
        if (res.statusCode === 200 || res.statusCode === 201) {
          console.log(`✅ Statement ${index + 1}/${statements.length}: Success`);
          resolve();
        } else {
          console.error(`❌ Statement ${index + 1}/${statements.length}: Failed (${res.statusCode})`);
          console.error('Response:', data);
          resolve(); // Continue even if fails
        }
      });
    });

    req.on('error', (error) => {
      console.error(`❌ Statement ${index + 1}/${statements.length}: Error`, error.message);
      resolve(); // Continue even if fails
    });

    req.write(postData);
    req.end();
  });
}

// Execute all statements with delay
async function importSchema() {
  console.log('🚀 Starting schema import...\n');

  for (let i = 0; i < statements.length; i++) {
    await executeStatement(statements[i], i);
    // Add small delay to avoid rate limiting
    await new Promise(resolve => setTimeout(resolve, 100));
  }

  console.log('\n✨ Schema import completed!');
}

importSchema().catch(console.error);
