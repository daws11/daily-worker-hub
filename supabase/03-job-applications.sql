-- ========================================
-- JOB APPLICATIONS TABLE (Worker Apply for Job)
-- ========================================
-- 
-- This script creates the job_applications table
-- 
-- Based on matching-algorithm.md (Section 3.2: Application Process)
-- Based on business-model.md (Section 3: Platform Commission, Section 4: Wallet)
-- 
-- Table: job_applications
-- 
-- Notes:
-- - Workers apply to jobs (status: pending)
-- - Businesses review applications (status: accepted/rejected)
-- - Workers complete jobs (status: completed)
-- - Platform deducts commission from business wallet
-- - Workers receive net wage (after commission)

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ========================================
-- 1. JOB_APPLICATIONS TABLE
-- ========================================

CREATE TABLE IF NOT EXISTS public.job_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Job & Business
    job_id UUID NOT NULL REFERENCES public.jobs(id) ON DELETE CASCADE,
    business_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    
    -- Worker
    worker_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    
    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'accepted', 'rejected', 'ongoing', 'completed', 'cancelled')),
    
    -- Application Details
    cover_letter TEXT,
    application_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Job Execution
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    hours_worked NUMERIC(5, 2) DEFAULT 0.0,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ========================================
-- 2. JOINS FOR APPLICATION TABLE
-- ========================================

-- Enable joins with jobs table
ALTER TABLE public.job_applications ADD COLUMN IF NOT EXISTS jobs_jobs_id UUID REFERENCES public.jobs(id) ON DELETE SET NULL;

-- ========================================
-- INDEXES FOR PERFORMANCE
-- ========================================

-- Job applications indexes
CREATE INDEX IF NOT EXISTS job_applications_job_id_idx ON public.job_applications(job_id);
CREATE INDEX IF NOT EXISTS job_applications_business_id_idx ON public.job_applications(business_id);
CREATE INDEX IF NOT EXISTS job_applications_worker_id_idx ON public.job_applications(worker_id);
CREATE INDEX IF NOT EXISTS job_applications_status_idx ON public.job_applications(status);
CREATE INDEX IF NOT EXISTS job_applications_application_date_idx ON public.job_applications(application_date DESC);
CREATE INDEX IF NOT EXISTS job_applications_started_at_idx ON public.job_applications(started_at DESC);
CREATE INDEX IF NOT EXISTS job_applications_worker_business_idx ON public.job_applications(worker_id, business_id);

-- ========================================
-- TRIGGERS FOR UPDATED_AT
-- ========================================

CREATE OR REPLACE FUNCTION public.update_job_applications_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;

CREATE TRIGGER trigger_update_job_applications_updated_at
    BEFORE UPDATE ON public.job_applications
    FOR EACH ROW
    EXECUTE FUNCTION public.update_job_applications_updated_at();

-- ========================================
-- COMMENTS FOR FUTURE DEVELOPERS
-- ========================================

COMMENT ON TABLE public.job_applications IS 'Worker applications for jobs. Status flow: pending -> accepted/rejected -> ongoing -> completed/cancelled. Platform processes payments after completed.';
COMMENT ON COLUMN public.job_applications.status IS 'Application status: pending (worker applied), accepted (business hired), rejected (business declined), ongoing (worker working), completed (job done), cancelled (cancelled)';
COMMENT ON COLUMN public.job_applications.started_at IS 'Job start timestamp (when worker starts working)';
COMMENT ON COLUMN public.job_applications.completed_at IS 'Job end timestamp (when worker finishes)';
COMMENT ON COLUMN public.job_applications.hours_worked IS 'Actual hours worked (calculated by start/end time)';

COMMENT ON INDEX job_applications_job_id_idx IS 'Index for filtering applications by job';
COMMENT ON INDEX job_applications_business_id_idx IS 'Index for filtering applications by business';
COMMENT ON INDEX job_applications_worker_id_idx IS 'Index for filtering applications by worker';
COMMENT ON INDEX job_applications_status_idx IS 'Index for filtering applications by status';
COMMENT ON INDEX job_applications_application_date_idx IS 'Index for sorting applications by application date (newest first)';
COMMENT ON INDEX job_applications_started_at_idx IS 'Index for sorting applications by start date (newest first)';
COMMENT ON INDEX job_applications_worker_business_idx IS 'Composite index for filtering applications by worker and business';
