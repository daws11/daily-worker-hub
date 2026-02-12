-- ============================================
-- JOB ASSIGNMENTS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS job_assignments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
  worker_id UUID NOT NULL REFERENCES workers(id) ON DELETE CASCADE,
  status TEXT DEFAULT 'assigned' CHECK (status IN ('assigned', 'in_progress', 'completed', 'cancelled', 'no_show')),
  check_in_time TIMESTAMP WITH TIME ZONE,
  check_out_time TIMESTAMP WITH TIME ZONE,
  worker_rating INTEGER CHECK (worker_rating >= 1 AND worker_rating <= 5),
  business_rating INTEGER CHECK (business_rating >= 1 AND business_rating <= 5),
  worker_review TEXT,
  business_review TEXT,
  wage_amount NUMERIC(10,2) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_job_assignments_job_id ON job_assignments(job_id);
CREATE INDEX IF NOT EXISTS idx_job_assignments_worker_id ON job_assignments(worker_id);
CREATE INDEX IF NOT EXISTS idx_job_assignments_status ON job_assignments(status);

-- ============================================
-- ROW LEVEL SECURITY (RLS) POLICIES FOR JOB ASSIGNMENTS
-- ============================================

-- Job Assignments Policies
CREATE POLICY IF NOT EXISTS "Workers can view their own assignments"
ON job_assignments FOR SELECT
USING (worker_id = auth.uid());

CREATE POLICY IF NOT EXISTS "Businesses can view assignments for their jobs"
ON job_assignments FOR SELECT
USING (
  EXISTS (
    SELECT 1 FROM jobs
    WHERE jobs.id = job_assignments.job_id
    AND jobs.business_id = auth.uid()
  )
);

CREATE POLICY IF NOT EXISTS "Workers can update their own assignments"
ON job_assignments FOR UPDATE
USING (worker_id = auth.uid());

CREATE POLICY IF NOT EXISTS "Businesses can update assignments for their jobs"
ON job_assignments FOR UPDATE
USING (
  EXISTS (
    SELECT 1 FROM jobs
    WHERE jobs.id = job_assignments.job_id
    AND jobs.business_id = auth.uid()
  )
);

-- ============================================
-- TRIGGERS FOR JOB ASSIGNMENTS
-- ============================================

CREATE TRIGGER IF NOT EXISTS update_job_assignments_updated_at
  BEFORE UPDATE ON job_assignments
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- END OF JOB ASSIGNMENTS TABLE
-- ============================================
