-- ========================================
-- PROFILES TABLE (Users, Workers, Businesses)
-- ========================================
-- 
-- This script creates the main profiles table
-- and related tables (worker_profiles, business_profiles)
-- 
-- Based on business-model.md and matching-algorithm.md
-- 
-- Tables:
-- 1. profiles (Main user table)
-- 2. worker_profiles (Worker-specific data)
-- 3. business_profiles (Business-specific data)
-- 4. worker_skills (Worker skills)
-- 5. business_facilities (Business facilities)
-- 6. notification_preferences (Notification settings)

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ========================================
-- 1. PROFILES TABLE (Main User Table)
-- ========================================

CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- User Information
    full_name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20),
    avatar_url TEXT,
    
    -- Role & Verification
    role VARCHAR(20) CHECK (role IN ('worker', 'business', 'admin')) NOT NULL,
    onboarding_status VARCHAR(50) DEFAULT 'pending',
    verification_status VARCHAR(50) DEFAULT 'unverified',
    
    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ========================================
-- 2. WORKER_PROFILES TABLE
-- ========================================

CREATE TABLE IF NOT EXISTS public.worker_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Profile ID (Foreign Key to profiles)
    profile_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    
    -- Job Details
    job_category VARCHAR(100),
    job_role VARCHAR(100),
    years_experience VARCHAR(50),
    work_history TEXT,
    
    -- Location
    address TEXT,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(10, 8),
    
    -- Documents
    address_photo_url TEXT,
    experience_document_url TEXT,
    domicile_document_url TEXT,
    
    -- Skills & Rating
    rating DECIMAL(3, 2) DEFAULT 0.0, -- 0.0 to 5.0
    no_show_rate DECIMAL(3, 2) DEFAULT 0.1, -- 0.0 to 1.0 (10% default)
    total_shifts_completed INTEGER DEFAULT 0,
    max_shifts_per_month INTEGER DEFAULT 21,
    is_available BOOLEAN DEFAULT true,
    last_active_date DATE,
    
    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ========================================
-- 3. BUSINESS_PROFILES TABLE
-- ========================================

CREATE TABLE IF NOT EXISTS public.business_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Profile ID (Foreign Key to profiles)
    profile_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    
    -- Business Information
    business_name VARCHAR(255),
    business_description TEXT,
    business_category VARCHAR(100),
    business_type VARCHAR(50), -- Restaurant, Hotel, Cafe, etc.
    
    -- Location
    address TEXT,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(10, 8),
    
    -- Operating Hours
    operating_hours_open VARCHAR(10), -- e.g., "08:00"
    operating_hours_close VARCHAR(10), -- e.g., "22:00"
    
    -- Documents
    nib_document_url TEXT,
    location_photo_front_url TEXT,
    location_photo_inside_url TEXT,
    
    -- Worker Preferences
    worker_preferences JSONB, -- Preferred skills, experience, etc.
    
    -- Commission & Wallet
    commission_rate DECIMAL(3, 2) DEFAULT 0.06, -- 6%
    wallet_balance DECIMAL(15, 2) DEFAULT 0.0,
    
    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ========================================
-- 4. WORKER_SKILLS TABLE
-- ========================================

CREATE TABLE IF NOT EXISTS public.worker_skills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Profile ID (Foreign Key to worker_profiles)
    worker_profile_id UUID REFERENCES public.worker_profiles(id) ON DELETE CASCADE,
    
    -- Skill Information
    skill_name VARCHAR(100) NOT NULL,
    experience_level VARCHAR(50) DEFAULT 'Beginner', -- Beginner, Intermediate, Advanced
    certifications TEXT, -- JSON array of certifications
    
    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ========================================
-- 5. BUSINESS_FACILITIES TABLE
-- ========================================

CREATE TABLE IF NOT EXISTS public.business_facilities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Profile ID (Foreign Key to business_profiles)
    business_profile_id UUID REFERENCES public.business_profiles(id) ON DELETE CASCADE,
    
    -- Facility Information
    facility_name VARCHAR(100) NOT NULL, -- e.g., "WiFi", "Parking", "AC"
    description TEXT,
    
    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ========================================
-- 6. NOTIFICATION_PREFERENCES TABLE
-- ========================================

CREATE TABLE IF NOT EXISTS public.notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Profile ID (Foreign Key to profiles)
    profile_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE UNIQUE,
    
    -- Push Notifications
    push_enabled BOOLEAN DEFAULT true,
    job_alerts_enabled BOOLEAN DEFAULT true,
    application_updates_enabled BOOLEAN DEFAULT true,
    promotional_enabled BOOLEAN DEFAULT false,
    
    -- Job Alerts
    alert_distance VARCHAR(20) DEFAULT '10 km', -- Max distance for job alerts
    alert_categories JSONB, -- Array of categories to alert on
    
    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ========================================
-- INDEXES FOR PERFORMANCE
-- ========================================

-- Profiles table indexes
CREATE INDEX IF NOT EXISTS profiles_role_idx ON public.profiles(role);
CREATE INDEX IF NOT EXISTS profiles_onboarding_status_idx ON public.profiles(onboarding_status);
CREATE INDEX IF NOT EXISTS profiles_verification_status_idx ON public.profiles(verification_status);

-- Worker profiles indexes
CREATE INDEX IF NOT EXISTS worker_profiles_profile_id_idx ON public.worker_profiles(profile_id);
CREATE INDEX IF NOT EXISTS worker_profiles_job_category_idx ON public.worker_profiles(job_category);
CREATE INDEX IF NOT EXISTS worker_profiles_rating_idx ON public.worker_profiles(rating DESC);
CREATE INDEX IF NOT EXISTS worker_profiles_is_available_idx ON public.worker_profiles(is_available);

-- Business profiles indexes
CREATE INDEX IF NOT EXISTS business_profiles_profile_id_idx ON public.business_profiles(profile_id);
CREATE INDEX IF NOT EXISTS business_profiles_business_category_idx ON public.business_profiles(business_category);

-- Worker skills indexes
CREATE INDEX IF NOT EXISTS worker_skills_worker_profile_id_idx ON public.worker_skills(worker_profile_id);
CREATE INDEX IF NOT EXISTS worker_skills_skill_name_idx ON public.worker_skills(skill_name);

-- Business facilities indexes
CREATE INDEX IF NOT EXISTS business_facilities_business_profile_id_idx ON public.business_facilities(business_profile_id);

-- Notification preferences indexes
CREATE INDEX IF NOT EXISTS notification_preferences_profile_id_idx ON public.notification_preferences(profile_id);

-- ========================================
-- ENABLE ROW LEVEL SECURITY (RLS) FOR MULTI-TENANCY
-- ========================================

-- This will allow us to have separate data for different roles
-- without seeing each other's data
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- Policy: Users can only see their own profile
-- Workers can see all profiles (for job matching)
-- Businesses can only see worker profiles (not business profiles)
-- Admins can see all profiles

CREATE POLICY "Users can see own profile" ON public.profiles
    FOR SELECT
    USING (auth.uid() = id);

CREATE POLICY "Users can update own profile" ON public.profiles
    FOR UPDATE
    USING (auth.uid() = id)
    WITH CHECK (role = 'worker' OR role = 'business');

CREATE POLICY "Admins can see all profiles" ON public.profiles
    FOR SELECT
    TO admin_role
    USING (true);

-- Apply policies
ALTER TABLE public.profiles DROP POLICY IF EXISTS "Users can see own profile";
ALTER TABLE public.profiles DROP POLICY IF EXISTS "Users can update own profile";
ALTER TABLE public.profiles DROP POLICY IF EXISTS "Admins can see all profiles";

-- ========================================
-- TRIGGERS FOR UPDATED_AT
-- ========================================

-- Profiles table trigger
CREATE OR REPLACE FUNCTION public.update_profiles_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;

CREATE TRIGGER trigger_update_profiles_updated_at
    BEFORE UPDATE ON public.profiles
    FOR EACH ROW
    EXECUTE FUNCTION public.update_profiles_updated_at();

-- Worker profiles table trigger
CREATE OR REPLACE FUNCTION public.update_worker_profiles_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;

CREATE TRIGGER trigger_update_worker_profiles_updated_at
    BEFORE UPDATE ON public.worker_profiles
    FOR EACH ROW
    EXECUTE FUNCTION public.update_worker_profiles_updated_at();

-- Business profiles table trigger
CREATE OR REPLACE FUNCTION public.update_business_profiles_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;

CREATE TRIGGER trigger_update_business_profiles_updated_at
    BEFORE UPDATE ON public.business_profiles
    FOR EACH ROW
    EXECUTE FUNCTION public.update_business_profiles_updated_at();

-- Worker skills table trigger
CREATE OR REPLACE FUNCTION public.update_worker_skills_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;

CREATE TRIGGER trigger_update_worker_skills_updated_at
    BEFORE UPDATE ON public.worker_skills
    FOR EACH ROW
    EXECUTE FUNCTION public.update_worker_skills_updated_at();

-- Business facilities table trigger
CREATE OR REPLACE FUNCTION public.update_business_facilities_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;

CREATE TRIGGER trigger_update_business_facilities_updated_at
    BEFORE UPDATE ON public.business_facilities
    FOR EACH ROW
    EXECUTE FUNCTION public.update_business_facilities_updated_at();

-- Notification preferences table trigger
CREATE OR REPLACE FUNCTION public.update_notification_preferences_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;

CREATE TRIGGER trigger_update_notification_preferences_updated_at
    BEFORE UPDATE ON public.notification_preferences
    FOR EACH ROW
    EXECUTE FUNCTION public.update_notification_preferences_updated_at();

-- ========================================
-- COMMENTS FOR FUTURE DEVELOPERS
-- ========================================

COMMENT ON TABLE public.profiles IS 'Main user table storing both workers and businesses';
COMMENT ON TABLE public.worker_profiles IS 'Worker-specific data including skills, rating, location';
COMMENT ON TABLE public.business_profiles IS 'Business-specific data including operating hours, worker preferences, commission';
COMMENT ON TABLE public.worker_skills IS 'Worker skills with experience level (Beginner, Intermediate, Advanced)';
COMMENT ON TABLE public.business_facilities IS 'Business facilities (WiFi, Parking, AC, etc.)';
COMMENT ON TABLE public.notification_preferences IS 'User notification preferences (push, job alerts, distance, categories)';

COMMENT ON COLUMN public.worker_profiles.rating IS 'Worker rating from 0.0 to 5.0 (default 0.0)';
COMMENT ON COLUMN public.worker_profiles.no_show_rate IS 'Worker no-show rate from 0.0 to 1.0 (default 0.1 = 10%)';
COMMENT ON COLUMN public.worker_profiles.is_available IS 'Worker availability status (default true = available)';
COMMENT ON COLUMN public.worker_profiles.max_shifts_per_month IS 'Maximum shifts per month (default 21 = 21 Days Rule compliance)';
COMMENT ON COLUMN public.business_profiles.worker_preferences IS 'Worker preferences stored as JSONB (skills, experience, etc.)';
COMMENT ON COLUMN public.business_profiles.commission_rate IS 'Platform commission rate (default 0.06 = 6%)';
COMMENT ON COLUMN public.business_profiles.wallet_balance IS 'Business wallet balance (default 0.0)';

COMMENT ON COLUMN public.notification_preferences.alert_distance IS 'Maximum distance for job alerts (default 10 km)';
COMMENT ON COLUMN public.notification_preferences.alert_categories IS 'Categories to alert on (stored as JSONB array)';
