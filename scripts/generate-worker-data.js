// ========================================
// DATA GENERATOR - WORKER SAMPLE DATA
// ========================================
// 
// This script generates sample worker data for testing
// 
// Generated Data:
// - 10 Worker profiles (with skills, ratings, locations)
// - 50 Worker skills (across 10 profiles)
// - Worker applications (for compliance check)
// 
// Based on matching-algorithm.md (Worker Side):
// - Workers have skills (Restaurant, Hotel, Cafe, Bar, Spa, Tour Guide, Driver)
// - Workers have ratings (0.0 to 5.0)
// - Workers have locations (across Bali)
// - Workers have no-show rates (0.0 to 0.2)
// 
// Based on business-model.md (Rate Bali):
// - Workers are in different locations (Badung, Denpasar, Gianyar, Tabanan)
// - Workers have different wage rates (based on location)

// ========================================
// 1. CONFIGURATION
// ========================================

const CONFIG = {
    WORKERS_COUNT: 10,
    SKILLS_PER_WORKER: 5,
    JOB_APPLICATIONS_COUNT: 20,
    LOCATIONS: ['Badung', 'Denpasar', 'Gianyar', 'Tabanan'],
    JOB_CATEGORIES: ['Restaurant', 'Hotel', 'Cafe', 'Bar', 'Spa', 'Tour Guide', 'Driver'],
    SKILL_NAMES: [
        'Server', 'Cook', 'Bartender', 'Housekeeping', 'Front Desk',
        'Waiter', 'Kitchen Staff', 'Receptionist', 'Security', 'Tour Guide',
        'Driver', 'Spa Therapist', 'Masseuse', 'Concierge', 'Bellhop',
        'Chef', 'Sous Chef', 'Pastry Chef', 'Food Runner', 'Dishwasher',
        'Pool Attendant', 'Beach Attendant', 'Activity Coordinator', 'Events Staff',
        'Marketing', 'Social Media Manager', 'Valet', 'Room Service', 'Barista',
        'Host', 'Hostess', 'DJ', 'Sound Engineer', 'Lighting Technician'
    ],
    EXPERIENCE_LEVELS: ['Beginner', 'Intermediate', 'Advanced']
    WAGE_RATES: {
        'Badung': 168302,
        'Denpasar': 157053,
        'Gianyar': 148527,
        'Tabanan': 151240
    }
};

// ========================================
// 2. UTILITY FUNCTIONS
// ========================================

const generateUUID = () => {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        const r = Math.random() * 16 | 0;
        const v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
};

const generateEmail = (name) => {
    const domains = ['gmail.com', 'yahoo.com', 'outlook.com', 'icloud.com'];
    const username = name.toLowerCase().replace(/\s/g, '.');
    return `${username}${Math.floor(Math.random() * 1000)}@${domains[Math.floor(Math.random() * domains.length)]}`;
};

const randomItem = (array) => {
    return array[Math.floor(Math.random() * array.length)];
};

const randomFloat = (min, max) => {
    return Math.random() * (max - min) + min;
};

const generateLocation = (location) => {
    // Bali coordinates for different locations
    const locations = {
        'Badung': { lat: -8.7061 + (Math.random() * 0.1 - 0.05), lon: 115.2625 + (Math.random() * 0.1 - 0.05) },
        'Denpasar': { lat: -8.6705 + (Math.random() * 0.1 - 0.05), lon: 115.2126 + (Math.random() * 0.1 - 0.05) },
        'Gianyar': { lat: -8.4545 + (Math.random() * 0.1 - 0.05), lon: 115.3770 + (Math.random() * 0.1 - 0.05) },
        'Tabanan': { lat: -8.6233 + (Math.random() * 0.1 - 0.05), lon: 115.1575 + (Math.random() * 0.1 - 0.05) }
    };
    return locations[location];
};

const generateWorkerName = () => {
    const firstNames = ['I Made', 'Ketut', 'Nyoman', 'Wayan', 'Putu', 'Gede', 'Kadek', 'Komang', 'Made', 'Agus'];
    const lastNames = ['Suardika', 'Wijaya', 'Putra', 'Saputra', 'Arya', 'Dewi', 'Pratama', 'Kusuma', 'Sanjaya', 'Wibawa'];
    return `${firstNames[Math.floor(Math.random() * firstNames.length)]} ${lastNames[Math.floor(Math.random() * lastNames.length)]}`;
};

// ========================================
// 3. DATA GENERATION
// ========================================

const generateWorkers = () => {
    const workers = [];
    
    for (let i = 0; i < CONFIG.WORKERS_COUNT; i++) {
        const workerName = generateWorkerName();
        const location = randomItem(CONFIG.LOCATIONS);
        const coords = generateLocation(location);
        
        workers.push({
            id: generateUUID(),
            full_name: workerName,
            email: generateEmail(workerName),
            phone_number: `08${Math.floor(Math.random() * 900000000 + 100000000).toString()}`,
            role: 'worker',
            onboarding_status: 'approved',
            verification_status: 'verified',
            avatar_url: null,
            worker_profiles: {
                id: generateUUID(),
                profile_id: workers[i].id, // Will be filled after insertion
                job_category: randomItem(CONFIG.JOB_CATEGORIES),
                job_role: randomItem(CONFIG.SKILL_NAMES),
                years_experience: randomItem(['1-2', '3-5', '5-10', '10+']),
                work_history: `${randomItem(['Restaurant', 'Hotel', 'Cafe'])} di ${location}`,
                address: `Jl. ${randomItem(['Jl. Raya Kuta', 'Jl. Raya Ubud', 'Jl. Raya Seminyak'])} No. ${Math.floor(Math.random() * 200) + 1}`,
                latitude: coords.lat,
                longitude: coords.lon,
                rating: parseFloat(randomFloat(3.5, 5.0).toFixed(2)),
                no_show_rate: parseFloat(randomFloat(0.05, 0.15).toFixed(2)), // 5% - 15%
                total_shifts_completed: Math.floor(Math.random() * 50) + 10,
                max_shifts_per_month: 21,
                is_available: Math.random() > 0.2, // 80% available
                last_active_date: new Date(Date.now() - Math.floor(Math.random() * 30) * 24 * 60 * 60 * 1000).toISOString()
            }
        });
    }
    
    return workers;
};

const generateWorkerSkills = (workers) => {
    const skills = [];
    
    workers.forEach(worker => {
        // Generate 3-5 skills per worker
        const numSkills = Math.floor(Math.random() * 3) + 3; // 3-5 skills
        const workerSkills = [];
        const usedSkills = new Set();
        
        for (let i = 0; i < numSkills; i++) {
            let skillName;
            do {
                skillName = randomItem(CONFIG.SKILL_NAMES);
            } while (usedSkills.has(skillName) && usedSkills.size < CONFIG.SKILL_NAMES.length);
            
            workerSkills.push({
                id: generateUUID(),
                worker_profile_id: worker.worker_profiles.id,
                skill_name: skillName,
                experience_level: randomItem(CONFIG.EXPERIENCE_LEVELS)
            });
            
            usedSkills.add(skillName);
        }
        
        skills.push(...workerSkills);
    });
    
    return skills;
};

const generateJobApplications = (workers) => {
    const applications = [];
    
    for (let i = 0; i < CONFIG.JOB_APPLICATIONS_COUNT; i++) {
        const worker = randomItem(workers);
        const isLastMonth = Math.random() > 0.7; // 30% in last month
        const daysAgo = Math.floor(Math.random() * 60) + 1; // 1-60 days ago
        
        applications.push({
            id: generateUUID(),
            job_id: generateUUID(), // Will be replaced with actual job ID
            worker_id: worker.id,
            business_id: generateUUID(), // Will be replaced with actual business ID
            status: randomItem(['completed', 'completed', 'completed', 'completed', 'completed']), // 80% completed
            application_date: new Date(Date.now() - Math.floor(Math.random() * 90) * 24 * 60 * 60 * 1000).toISOString(),
            started_at: new Date(Date.now() - daysAgo * 24 * 60 * 60 * 1000).toISOString(),
            completed_at: new Date(Date.now() - (daysAgo - 8) * 24 * 60 * 60 * 1000).toISOString(), // 8 hours shift
            hours_worked: 8.0
        });
    }
    
    return applications;
};

const generateNotificationPreferences = (workers) => {
    const preferences = [];
    
    workers.forEach(worker => {
        const alertDistance = randomItem(['5 km', '10 km', '20 km']);
        const alertCategories = randomItem([CONFIG.JOB_CATEGORIES.slice(0, 3), CONFIG.JOB_CATEGORIES.slice(3, 6), CONFIG.JOB_CATEGORIES]);
        
        preferences.push({
            id: generateUUID(),
            profile_id: worker.id,
            push_enabled: Math.random() > 0.1, // 90% enabled
            job_alerts_enabled: Math.random() > 0.1,
            application_updates_enabled: true,
            promotional_enabled: Math.random() > 0.5,
            alert_distance: alertDistance,
            alert_categories: alertCategories
        });
    });
    
    return preferences;
};

// ========================================
// 4. MAIN EXECUTION
// ========================================

const main = () => {
    console.log('🚀 Generating Worker Sample Data...\n');
    
    // 1. Generate Workers
    console.log('📊 Generating Workers...');
    const workers = generateWorkers();
    console.log(`   Generated ${workers.length} workers`);
    
    // 2. Generate Worker Skills
    console.log('💼 Generating Worker Skills...');
    const workerSkills = generateWorkerSkills(workers);
    console.log(`   Generated ${workerSkills.length} skills`);
    
    // 3. Generate Job Applications (for Compliance Check)
    console.log('📝 Generating Job Applications...');
    const jobApplications = generateJobApplications(workers);
    console.log(`   Generated ${jobApplications.length} applications`);
    
    // 4. Generate Notification Preferences
    console.log('🔔 Generating Notification Preferences...');
    const notificationPreferences = generateNotificationPreferences(workers);
    console.log(`   Generated ${notificationPreferences.length} preferences`);
    
    // 5. Combine Data
    const data = {
        profiles: workers,
        worker_profiles: workers.map(w => w.worker_profiles),
        worker_skills: workerSkills,
        job_applications: jobApplications,
        notification_preferences: notificationPreferences
    };
    
    console.log('\n✅ Worker Data Generation Complete!\n');
    console.log(`📋 Summary:`);
    console.log(`   - ${data.profiles.length} Workers`);
    console.log(`   - ${data.worker_skills.length} Skills`);
    console.log(`   - ${data.job_applications.length} Job Applications`);
    console.log(`   - ${data.notification_preferences.length} Notification Preferences`);
    console.log(`   - ${data.profiles.reduce((sum, w) => sum + w.worker_profiles.total_shifts_completed, 0)} Total Shifts Completed`);
    console.log(`   - Avg Worker Rating: ${(data.worker_profiles.reduce((sum, w) => sum + w.rating, 0) / data.worker_profiles.length).toFixed(2)}`);
    
    // 6. Save to file
    const fs = require('fs');
    const path = './supabase/.temp/sample-worker-data.json';
    fs.writeFileSync(path, JSON.stringify(data, null, 2));
    console.log(`\n💾 Data saved to: ${path}`);
    
    return data;
};

// ========================================
// 5. EXPORT FOR USE IN OTHER SCRIPTS
// ========================================

if (require.main === module) {
    main();
} else {
    module.exports = {
        generateWorkers,
        generateWorkerSkills,
        generateJobApplications,
        generateNotificationPreferences,
        generateWorkerName,
        randomItem,
        randomFloat,
        CONFIG
    };
}
