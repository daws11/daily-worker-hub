-- ========================================
-- JOBS TABLE (Business Job Postings)
-- ========================================
-- 
-- This script creates the jobs table and related tables
-- Based on business-model.md (Section 2: Rate Bali, Section 3: Commission)
-- 
-- Tables:
-- 1. jobs (Main job table)
-- 
-- Notes:
-- - Jobs can be "open", "accepted", "ongoing", "completed", "cancelled"
-- - Jobs have wage_type: "per_shift", "per_hour", "per_day"
-- - Jobs have is_urgent flag for prioritization
-- - Jobs have is_compliant flag (21 Days Rule - PP 35/2021)

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ========================================
-- 1. JOBS TABLE
-- ========================================

CREATE TABLE IF NOT EXISTS public.jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Job Information
    business_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    
    -- Wage Information
    wage NUMERIC(10, 2) NOT NULL,
    wage_type VARCHAR(20) CHECK (wage_type IN ('per_shift', 'per_hour', 'per_day')) DEFAULT 'per_shift',
    
    -- Location
    location VARCHAR(255),
    category VARCHAR(100),
    
    -- Shift Information
    shift_date DATE NOT NULL,
    start_time VARCHAR(10) NOT NULL,
    end_time VARCHAR(10) NOT NULL,
    worker_count INTEGER DEFAULT 1,
    
    -- Job Status
    status VARCHAR(50) DEFAULT 'open' CHECK (status IN ('open', 'pending', 'accepted', 'ongoing', 'completed', 'cancelled')),
    
    -- Priority & Compliance Flags
    is_urgent BOOLEAN DEFAULT false,
    is_compliant BOOLEAN DEFAULT true,
    
    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ========================================
-- INDEXES FOR PERFORMANCE
-- ========================================

-- Jobs table indexes
CREATE INDEX IF NOT EXISTS jobs_business_id_idx ON public.jobs(business_id);
CREATE INDEX IF NOT EXISTS jobs_status_idx ON public.jobs(status);
CREATE INDEX IF NOT EXISTS jobs_category_idx ON public.jobs(category);
CREATE INDEX IF NOT EXISTS jobs_shift_date_idx ON public.jobs(shift_date);
CREATE INDEX IF NOT EXISTS jobs_is_urgent_idx ON public.jobs(is_urgent);
CREATE INDEX IF NOT EXISTS jobs_is_compliant_idx ON public.jobs(is_compliant);
CREATE INDEX IF NOT EXISTS jobs_created_at_idx ON public.jobs(created_at DESC);

-- ========================================
-- TRIGGERS FOR UPDATED_AT
-- ========================================

-- Jobs table trigger
CREATE OR REPLACE FUNCTION public.update_jobs_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;

CREATE TRIGGER trigger_update_jobs_updated_at
    BEFORE UPDATE ON public.jobs
    FOR EACH ROW
    EXECUTE FUNCTION public.update_jobs_updated_at();

-- ========================================
-- COMMENTS FOR FUTURE DEVELOPERS
-- ========================================

COMMENT ON TABLE public.jobs IS 'Job postings created by businesses. Workers apply to these jobs.';
COMMENT ON COLUMN public.jobs.business_id IS 'Foreign key to profiles table (business user)';
COMMENT ON COLUMN public.jobs.wage IS 'Base wage (before commission)';
COMMENT ON COLUMN public.jobs.wage_type IS 'Wage type: per_shift, per_hour, per_day';
COMMENT ON COLUMN public.jobs.shift_date IS 'Date of the job shift';
COMMENT ON COLUMN public.jobs.start_time IS 'Start time in HH:MM format (24-hour)';
COMMENT ON COLUMN public.jobs.end_time IS 'End time in HH:MM format (24-hour)';
COMMENT ON COLUMN public.jobs.worker_count IS 'Number of workers required for this job';
COMMENT ON COLUMN public.jobs.status IS 'Job status: open, pending, accepted, ongoing, completed, cancelled';
COMMENT ON COLUMN public.jobs.is_urgent IS 'Urgent flag for prioritization (higher score in matching)';
COMMENT ON COLUMN public.jobs.is_compliant IS 'Compliance flag (21 Days Rule - PP 35/2021) - calculated by backend';

COMMENT ON INDEX jobs_business_id_idx IS 'Index for filtering jobs by business';
COMMENT ON INDEX jobs_status_idx IS 'Index for filtering jobs by status (open, etc.)';
COMMENT ON INDEX jobs_category_idx IS 'Index for filtering jobs by category';
COMMENT ON INDEX jobs_shift_date_idx IS 'Index for filtering jobs by date';
COMMENT ON INDEX jobs_is_urgent_idx IS 'Index for filtering urgent jobs';
COMMENT ON INDEX jobs_is_compliant_idx IS 'Index for filtering compliant jobs';
COMMENT ON INDEX jobs_created_at_idx IS 'Index for sorting jobs by creation date (newest first)';
