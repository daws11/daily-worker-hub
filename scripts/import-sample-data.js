#!/usr/bin/env node

/**
 * Supabase Data Import Script v2.0
 * Imports sample data via REST API (INSERT operations)
 */

const https = require('https');
const http = require('http');

const SUPABASE_URL = 'https://airhufmbwqxmojnkknan.supabase.co';
const ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFpcmh1Zm1id3F4bW9qbmtrbmFuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU3MTE4ODksImV4cCI6MjA4MTI4Nzg4OX0.sylxYFsIvgWOj9OdZwgf-ZCb8nc9pBS_oY2u7EQon5g';

const REST_API_URL = `${SUPABASE_URL}/rest/v1`;

console.log('╔══════════════════════════════════════════════════╗');
console.log('║     Supabase Data Import Script (via REST API) v2.0 ║');
console.log('║           Daily Worker Hub v1.0                         ║');
console.log('╚══════════════════════════════════════════════════════════╝\n');

// Function to execute INSERT via REST API
async function insertData(table, data) {
  return new Promise((resolve, reject) => {
    const postData = JSON.stringify(data);

    const options = {
      hostname: new URL(SUPABASE_URL).hostname,
      port: 443,
      path: `/rest/v1/${table}`,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'apikey': ANON_KEY,
        'Authorization': `Bearer ${ANON_KEY}`,
        'Prefer': 'return=minimal'
      }
    };

    const req = https.request(options, (res) => {
      let responseData = '';

      res.on('data', (chunk) => {
        responseData += chunk;
      });

      res.on('end', () => {
        if (res.statusCode === 201 || res.statusCode === 200) {
          try {
            const result = JSON.parse(responseData);
            console.log(`✅ Inserted into ${table}:`, result.id || result.data || 'success');
            resolve({ success: true, data: result });
          } catch (e) {
            console.log(`✅ Inserted (no error field):`, responseData.substring(0, 100));
            resolve({ success: true });
          }
        } else {
          console.log(`❌ HTTP ${res.statusCode}`);
          console.log(`   Response: ${responseData.substring(0, 200)}...`);
          resolve({ success: false });
        }
      });
    });

    req.on('error', (error) => {
      console.log(`❌ Network Error: ${error.message}`);
      resolve({ success: false, error: error.message });
    });

    req.setTimeout(10000);
    req.write(postData);
    req.end();
  });
}

// Import sample data
async function importSampleData() {
  console.log('🚀 Starting sample data import...\n');

  // Sample worker data (will be updated with real data later)
  const sampleWorkers = [
    {
      skill_categories: ['Housekeeping', 'Cleaning'],
      experience_level: 'intermediate',
      available: true,
      rating_avg: 4.5,
      rating_count: 12,
      completed_jobs: 8
    },
    {
      skill_categories: ['Kitchen', 'Cook'],
      experience_level: 'entry',
      available: true,
      rating_avg: 0,
      rating_count: 0,
      completed_jobs: 0
    }
  ];

  // Sample business data
  const sampleBusinesses = [
    {
      company_name: 'Bali Beach Villa',
      business_type: 'Villa',
      business_verified: true,
      rating_avg: 4.8,
      rating_count: 15
    },
    {
      company_name: 'Warung Rasa Nusantara',
      business_type: 'Restaurant',
      business_verified: true,
      rating_avg: 5.0,
      rating_count: 8
    }
  ];

  let successCount = 0;
  let failCount = 0;

  console.log('📋 Importing workers...');
  for (let i = 0; i < sampleWorkers.length; i++) {
    console.log(`   Worker ${i + 1}/${sampleWorkers.length}...`);
    const result = await insertData('workers', sampleWorkers[i]);
    
    if (result.success) {
      successCount++;
    } else {
      failCount++;
      console.log(`   Failed: ${result.error || 'Unknown error'}`);
    }
    
    await new Promise(resolve => setTimeout(resolve, 300));
  }

  console.log('📋 Importing businesses...');
  for (let i = 0; i < sampleBusinesses.length; i++) {
    console.log(`   Business ${i + 1}/${sampleBusinesses.length}...`);
    const result = await insertData('businesses', sampleBusinesses[i]);
    
    if (result.success) {
      successCount++;
    } else {
      failCount++;
      console.log(`   Failed: ${result.error || 'Unknown error'}`);
    }
    
    await new Promise(resolve => setTimeout(resolve, 300));
  }

  const totalItems = sampleWorkers.length + sampleBusinesses.length;
  console.log(`\n======================================`);
  console.log(`║                    Import Summary                          ║`);
  console.log(`======================================`);
  console.log(`✅ Successful: ${successCount}/${totalItems}`);
  console.log(`❌ Failed: ${failCount}/${totalItems}`);

  if (successCount === totalItems) {
    console.log('\n✨ Sample data import completed!');
    console.log('\n📊 Next steps:');
    console.log('   1. Run validation: node scripts/validate-db.js');
    console.log('   2. Check sample data in Supabase Dashboard');
    console.log('   3. Start development with real data');
  } else {
    console.log('\n⚠️  Some imports failed. Check logs above.');
  }

  return successCount === totalItems;
}

// Run import
importSampleData().catch(console.error);
