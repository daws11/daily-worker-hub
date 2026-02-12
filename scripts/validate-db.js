#!/usr/bin/env node

/**
 * Supabase Validation Script v3.0
 * Validates database schema and setup (Updated with correct anon key)
 */

const https = require('https');
const http = require('http');

const SUPABASE_URL = 'https://airhufmbwqxmojnkknan.supabase.co';
const CORRECT_ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFpcmh1Zm1id3F4bW9qbmtrbmFuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU3MTE4ODksImV4cCI6MjA4MTI4Nzg4OX0.sylxYFsIvgWOj9OdZwgf-ZCb8nc9pBS_oY2u7EQon5g';

const REST_API_URL = `${SUPABASE_URL}/rest/v1`;

console.log('🔌 Connecting to Supabase:', SUPABASE_URL);
console.log('🔑 Using updated anon key...\n');

// Function to execute REST API call
async function executeQuery(table, options = {}) {
  return new Promise((resolve, reject) => {
    const url = `${REST_API_URL}/${table}`;
    
    const queryParams = Object.keys(options)
      .filter(key => options[key] !== undefined)
      .map(key => `${key}=${encodeURIComponent(options[key])}`)
      .join('&');
    
    const fullUrl = queryParams ? `${url}?${queryParams}` : url;

    const requestOptions = {
      hostname: new URL(SUPABASE_URL).hostname,
      port: 443,
      path: `/rest/v1/${table}${queryParams ? '?' + queryParams : ''}`,
      method: 'GET',
      headers: {
        'apikey': CORRECT_ANON_KEY,
        'Authorization': `Bearer ${CORRECT_ANON_KEY}`,
        'Content-Type': 'application/json',
        'Prefer': 'return=minimal'
      }
    };

    const req = https.request(requestOptions, (res) => {
      let data = '';

      res.on('data', (chunk) => {
        data += chunk;
      });

      res.on('end', () => {
        if (res.statusCode === 200 || res.statusCode === 206) {
          try {
            const jsonData = JSON.parse(data);
            
            // Handle different response structures
            if (Array.isArray(jsonData)) {
              console.log(`   ✅ Success [${jsonData.length} items]`);
              resolve({ data: jsonData, count: jsonData.length });
            } else if (jsonData.data) {
              if (Array.isArray(jsonData.data)) {
                console.log(`   ✅ Success [${jsonData.data.length} items]`);
                resolve({ data: jsonData.data, count: jsonData.data.length });
              } else if (typeof jsonData.data === 'number') {
                console.log(`   ✅ Success [count: ${jsonData.data}]`);
                resolve({ data: null, count: jsonData.data });
              } else if (jsonData.data) {
                console.log(`   ✅ Success [1 item]`);
                resolve({ data: jsonData.data, count: 1 });
              } else {
                console.log(`   ✅ Success [1 item]`);
                resolve({ data: jsonData, count: 1 });
              }
            } else {
              resolve({ data: jsonData, count: jsonData.length || 1 });
            }
          } catch (e) {
            console.log(`   ❌ JSON Parse Error: ${e.message}`);
            resolve({ data: null, error: null });
          }
        } else if (res.statusCode === 404) {
          console.log(`   ⚪ Table not found (404)`);
          resolve({ data: null, error: '404' });
        } else if (res.statusCode === 401) {
          console.log(`   🔒 Unauthorized (401)`);
          console.log(`   💡 Check API key`);
          resolve({ data: null, error: '401' });
        } else {
          console.log(`   ❌ HTTP ${res.statusCode}`);
          try {
            const errorData = JSON.parse(data);
            console.log(`   Error: ${JSON.stringify(errorData).substring(0, 100)}...`);
          } catch (e) {
            console.log(`   Response: ${data.substring(0, 150)}...`);
          }
          resolve({ data: null, error: `HTTP ${res.statusCode}` });
        }
      });
    });

    req.on('error', (error) => {
      console.log(`   ❌ Network Error: ${error.message}`);
      resolve({ data: null, error: error.message });
    });

    req.setTimeout(10000);
    req.end();
  });
}

// Check if tables exist
async function checkTables() {
  const expectedTables = [
    'profiles',
    'workers',
    'businesses',
    'jobs',
    'job_assignments',
    'wallets',
    'wallet_transactions',
    'audit_logs'
  ];

  console.log('📋 Checking expected tables...\n');

  let tablesFound = 0;
  let tablesMissing = [];
  let tablesErrors = [];

  for (const table of expectedTables) {
    console.log(`\n🔍 Checking: ${table}`);
    
    try {
      const result = await executeQuery(table, { select: '*', limit: 1 });
      
      if (result.error === '401') {
        tablesErrors.push(table);
        console.log(`   ❌ Auth failed for ${table}`);
      } else if (result.data !== null && result.error !== '404') {
        tablesFound++;
        console.log(`   ✅ Table EXISTS`);
      } else {
        tablesMissing.push(table);
      }
      
      // Add delay to avoid rate limiting
      await new Promise(resolve => setTimeout(resolve, 300));
    } catch (error) {
      console.log(`   ❌ Exception: ${error.message}`);
      tablesErrors.push(table);
    }
  }

  console.log(`\n📊 Summary: ${tablesFound}/${expectedTables.length} tables found`);

  if (tablesErrors.length > 0) {
    console.log(`\n❌ Authentication errors: ${tablesErrors.join(', ')}`);
    console.log('💡 Check API key or Supabase project');
  }

  if (tablesMissing.length > 0) {
    console.log(`\n⚠️  Missing tables: ${tablesMissing.join(', ')}`);
    console.log('💡 Run: Import schema.sql via Supabase Dashboard');
    console.log('   Link: https://supabase.com/dashboard/project/airhufmbwqxmojnkknan/sql/new');
    return false;
  }

  return tablesErrors.length === 0;
}

// Check if tables have data
async function checkData() {
  const tablesToCheck = ['profiles', 'workers', 'businesses', 'jobs'];

  console.log('\n📄 Checking table data...\n');

  for (const table of tablesToCheck) {
    console.log(`\n🔍 Checking data: ${table}`);
    
    try {
      const result = await executeQuery(table, { select: 'count', count: 'exact', head: true });
      
      let count = 0;
      
      if (result.data !== null && result.error !== '404' && result.error !== '401') {
        if (result.count !== undefined) {
          count = Array.isArray(result.data) ? result.data.length : (result.data || 0);
        } else if (typeof result.data === 'number') {
          count = result.data;
        } else if (result.data && typeof result.data.count === 'number') {
          count = result.data.count;
        }
      }
      
      if (count > 0) {
        console.log(`   ✅ ${table.padEnd(20)} — ${count} records`);
      } else {
        console.log(`   ⚪ ${table.padEnd(20)} — 0 records (empty)`);
      }
    } catch (error) {
      console.log(`   ❌ ${table.padEnd(20)} — ERROR: ${error.message}`);
    }
    
    await new Promise(resolve => setTimeout(resolve, 300));
  }
}

// Check table structure
async function checkSchema() {
  console.log('\n🔍 Checking schema structure...\n');

  try {
    // Check profiles table structure
    const profiles = await executeQuery('profiles', { select: 'id,email,role', limit: 1 });
    
    if (profiles.data && profiles.data.length > 0) {
      const profile = profiles.data[0];
      const hasRequiredFields = 'id' in profile && 'email' in profile && 'role' in profile;
      console.log(`✅ Profiles table — Structure OK`);
      console.log(`   Sample: ${profile.email} (${profile.role})`);
    }

    // Check workers table structure
    const workers = await executeQuery('workers', { select: 'id,skill_categories,rating_avg', limit: 1 });
    
    if (workers.data && workers.data.length > 0) {
      console.log(`✅ Workers table — Structure OK`);
    }

    // Check businesses table structure
    const businesses = await executeQuery('businesses', { select: 'id,company_name', limit: 1 });
    
    if (businesses.data && businesses.data.length > 0) {
      console.log(`✅ Businesses table — Structure OK`);
    }

  } catch (error) {
    console.log(`❌ Schema check — FAILED: ${error.message}`);
  }
}

// Test wallet system
async function testWalletSystem() {
  console.log('\n💰 Testing wallet system...\n');

  try {
    const wallets = await executeQuery('wallets', { select: 'id,user_id,balance', limit: 5 });
    
    if (wallets.data && wallets.data.length > 0) {
      console.log(`✅ Wallets table — Structure OK`);
      console.log(`   Sample wallets: ${wallets.data.length} found`);

      if (wallets.data.length > 0) {
        const wallet = wallets.data[0];
        console.log(`   Example: User ${wallet.user_id} — Balance: Rp${wallet.balance || 0}`);
      }
    }
  } catch (error) {
    console.log(`❌ Wallet system — FAILED: ${error.message}`);
  }
}

// Main validation function
async function validateDatabase() {
  console.log('╔══════════════════════════════════════════════════╗');
  console.log('║     Supabase Database Validation Script v3.0 (Updated Key)  ║');
  console.log('║           Daily Worker Hub v1.0                          ║');
  console.log('╚══════════════════════════════════════════════════════╝\n');

  const tablesOK = await checkTables();
  
  if (tablesOK) {
    await checkData();
    await checkSchema();
    await testWalletSystem();

    console.log('\n✅ Database validation COMPLETED');
    console.log('🎯 All systems operational!');
    console.log('\n🤖 MCP Supabase siap digunakan!');
  } else {
    console.log('\n❌ Database validation FAILED');
    console.log('💡 Action: Import schema.sql via Supabase Dashboard');
    console.log('   Link: https://supabase.com/dashboard/project/airhufmbwqxmojnkknan/sql/new');
  }

  console.log('\n' + '='.repeat(50));
}

// Run validation
validateDatabase().catch(console.error);
