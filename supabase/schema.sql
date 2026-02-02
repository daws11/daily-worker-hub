-- Daily Worker Hub - Database Schema
-- Import to Supabase SQL Editor

-- ============================================
-- EXTENSIONS
-- ============================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- PROFILES TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS profiles (
  id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  email TEXT NOT NULL,
  full_name TEXT,
  role TEXT NOT NULL CHECK (role IN ('worker', 'business', 'admin')),
  verification_status TEXT DEFAULT 'pending' CHECK (verification_status IN ('pending', 'verified', 'rejected')),
  phone TEXT,
  avatar_url TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create index for faster queries
CREATE INDEX idx_profiles_role ON profiles(role);
CREATE INDEX idx_profiles_verification_status ON profiles(verification_status);

-- ============================================
-- WORKERS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS workers (
  id UUID PRIMARY KEY REFERENCES profiles(id) ON DELETE CASCADE,
  skill_categories TEXT[] NOT NULL,
  experience_level TEXT CHECK (experience_level IN ('entry', 'intermediate', 'expert')),
  portfolio_urls TEXT[],
  address_verified BOOLEAN DEFAULT FALSE,
  face_verified BOOLEAN DEFAULT FALSE,
  bio TEXT,
  available BOOLEAN DEFAULT TRUE,
  rating_avg NUMERIC(3,2) DEFAULT 0 CHECK (rating_avg >= 0 AND rating_avg <= 5),
  rating_count INTEGER DEFAULT 0,
  completed_jobs INTEGER DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes
CREATE INDEX idx_workers_skill_categories ON workers USING GIN(skill_categories);
CREATE INDEX idx_workers_rating_avg ON workers(rating_avg DESC);
CREATE INDEX idx_workers_available ON workers(available);

-- ============================================
-- BUSINESSES TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS businesses (
  id UUID PRIMARY KEY REFERENCES profiles(id) ON DELETE CASCADE,
  company_name TEXT NOT NULL,
  business_type TEXT NOT NULL,
  business_address TEXT,
  business_phone TEXT,
  business_document_url TEXT,
  business_verified BOOLEAN DEFAULT FALSE,
  rating_avg NUMERIC(3,2) DEFAULT 0 CHECK (rating_avg >= 0 AND rating_avg <= 5),
  rating_count INTEGER DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes
CREATE INDEX idx_businesses_type ON businesses(business_type);
CREATE INDEX idx_businesses_verified ON businesses(business_verified);

-- ============================================
-- JOBS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS jobs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
  title TEXT NOT NULL,
  category TEXT NOT NULL,
  description TEXT,
  required_skills TEXT[],
  start_time TIMESTAMP WITH TIME ZONE NOT NULL,
  end_time TIMESTAMP WITH TIME ZONE NOT NULL,
  wage_amount NUMERIC(10,2) NOT NULL,
  workers_needed INTEGER NOT NULL,
  workers_assigned INTEGER DEFAULT 0,
  location TEXT,
  status TEXT DEFAULT 'open' CHECK (status IN ('open', 'filled', 'in_progress', 'completed', 'cancelled')),
  urgent BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes
CREATE INDEX idx_jobs_business_id ON jobs(business_id);
CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_jobs_start_time ON jobs(start_time);
CREATE INDEX idx_jobs_category ON jobs(category);

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
CREATE INDEX idx_job_assignments_job_id ON job_assignments(job_id);
CREATE INDEX idx_job_assignments_worker_id ON job_assignments(worker_id);
CREATE INDEX idx_job_assignments_status ON job_assignments(status);

-- ============================================
-- WALLETS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS wallets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
  balance NUMERIC(12,2) DEFAULT 0 CHECK (balance >= 0),
  frozen_balance NUMERIC(12,2) DEFAULT 0 CHECK (frozen_balance >= 0),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes
CREATE INDEX idx_wallets_user_id ON wallets(user_id);
CREATE UNIQUE INDEX idx_wallets_user_unique ON wallets(user_id);

-- ============================================
-- WALLET TRANSACTIONS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS wallet_transactions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  wallet_id UUID NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
  type TEXT NOT NULL CHECK (type IN ('credit', 'debit')),
  amount NUMERIC(12,2) NOT NULL,
  description TEXT,
  reference_id TEXT,
  reference_type TEXT CHECK (reference_type IN ('job_payment', 'topup', 'withdrawal', 'refund', 'bonus', 'penalty')),
  status TEXT DEFAULT 'completed' CHECK (status IN ('pending', 'completed', 'failed')),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes
CREATE INDEX idx_wallet_transactions_wallet_id ON wallet_transactions(wallet_id);
CREATE INDEX idx_wallet_transactions_type ON wallet_transactions(type);
CREATE INDEX idx_wallet_transactions_created_at ON wallet_transactions(created_at DESC);

-- ============================================
-- AUDIT LOG TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS audit_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES profiles(id),
  action TEXT NOT NULL,
  entity_type TEXT,
  entity_id UUID,
  old_values JSONB,
  new_values JSONB,
  ip_address TEXT,
  user_agent TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);

-- ============================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ============================================

-- Enable RLS on all tables
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE workers ENABLE ROW LEVEL SECURITY;
ALTER TABLE businesses ENABLE ROW LEVEL SECURITY;
ALTER TABLE jobs ENABLE ROW LEVEL SECURITY;
ALTER TABLE job_assignments ENABLE ROW LEVEL SECURITY;
ALTER TABLE wallets ENABLE ROW LEVEL SECURITY;
ALTER TABLE wallet_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;

-- Profiles Policies
CREATE POLICY "Users can view their own profile"
ON profiles FOR SELECT
USING (auth.uid() = id);

CREATE POLICY "Admins can view all profiles"
ON profiles FOR SELECT
USING (
  EXISTS (
    SELECT 1 FROM profiles
    WHERE profiles.id = auth.uid()
    AND profiles.role = 'admin'
  )
);

CREATE POLICY "Users can update their own profile"
ON profiles FOR UPDATE
USING (auth.uid() = id);

CREATE POLICY "Admins can update all profiles"
ON profiles FOR UPDATE
USING (
  EXISTS (
    SELECT 1 FROM profiles
    WHERE profiles.id = auth.uid()
    AND profiles.role = 'admin'
  )
);

-- Workers Policies
CREATE POLICY "Anyone can view verified workers"
ON workers FOR SELECT
USING (
  EXISTS (
    SELECT 1 FROM profiles
    WHERE profiles.id = workers.id
    AND profiles.verification_status = 'verified'
  )
);

CREATE POLICY "Workers can view their own data"
ON workers FOR SELECT
USING (auth.uid() = id);

CREATE POLICY "Workers can update their own data"
ON workers FOR UPDATE
USING (auth.uid() = id);

-- Businesses Policies
CREATE POLICY "Anyone can view verified businesses"
ON businesses FOR SELECT
USING (
  EXISTS (
    SELECT 1 FROM profiles
    WHERE profiles.id = businesses.id
    AND profiles.verification_status = 'verified'
  )
);

CREATE POLICY "Businesses can view their own data"
ON businesses FOR SELECT
USING (auth.uid() = id);

CREATE POLICY "Businesses can update their own data"
ON businesses FOR UPDATE
USING (auth.uid() = id);

-- Jobs Policies
CREATE POLICY "Anyone can view open jobs"
ON jobs FOR SELECT
USING (status = 'open');

CREATE POLICY "Businesses can view their own jobs"
ON jobs FOR SELECT
USING (business_id = auth.uid());

CREATE POLICY "Businesses can create jobs"
ON jobs FOR INSERT
WITH CHECK (business_id = auth.uid());

CREATE POLICY "Businesses can update their own jobs"
ON jobs FOR UPDATE
USING (business_id = auth.uid());

CREATE POLICY "Admins can view all jobs"
ON jobs FOR SELECT
USING (
  EXISTS (
    SELECT 1 FROM profiles
    WHERE profiles.id = auth.uid()
    AND profiles.role = 'admin'
  )
);

-- Job Assignments Policies
CREATE POLICY "Workers can view their own assignments"
ON job_assignments FOR SELECT
USING (worker_id = auth.uid());

CREATE POLICY "Businesses can view assignments for their jobs"
ON job_assignments FOR SELECT
USING (
  EXISTS (
    SELECT 1 FROM jobs
    WHERE jobs.id = job_assignments.job_id
    AND jobs.business_id = auth.uid()
  )
);

CREATE POLICY "Workers can update their own assignments"
ON job_assignments FOR UPDATE
USING (worker_id = auth.uid());

CREATE POLICY "Businesses can update assignments for their jobs"
ON job_assignments FOR UPDATE
USING (
  EXISTS (
    SELECT 1 FROM jobs
    WHERE jobs.id = job_assignments.job_id
    AND jobs.business_id = auth.uid()
  )
);

-- Wallets Policies
CREATE POLICY "Users can view their own wallet"
ON wallets FOR SELECT
USING (user_id = auth.uid());

CREATE POLICY "Users can view wallet balance only"
ON wallets FOR SELECT
USING (user_id = auth.uid());

CREATE POLICY "System can update wallets"
ON wallets FOR UPDATE
USING (false); -- Only through database functions

-- Wallet Transactions Policies
CREATE POLICY "Users can view their own transactions"
ON wallet_transactions FOR SELECT
USING (
  EXISTS (
    SELECT 1 FROM wallets
    WHERE wallets.id = wallet_transactions.wallet_id
    AND wallets.user_id = auth.uid()
  )
);

-- ============================================
-- FUNCTIONS & TRIGGERS
-- ============================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to tables with updated_at
CREATE TRIGGER update_profiles_updated_at
  BEFORE UPDATE ON profiles
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workers_updated_at
  BEFORE UPDATE ON workers
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_businesses_updated_at
  BEFORE UPDATE ON businesses
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_jobs_updated_at
  BEFORE UPDATE ON jobs
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_job_assignments_updated_at
  BEFORE UPDATE ON job_assignments
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_wallets_updated_at
  BEFORE UPDATE ON wallets
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

-- Function to auto-create wallet for new users
CREATE OR REPLACE FUNCTION create_wallet_for_new_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO wallets (user_id, balance)
  VALUES (NEW.id, 0);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to create wallet when profile is created
CREATE TRIGGER create_user_wallet
  AFTER INSERT ON profiles
  FOR EACH ROW
  EXECUTE FUNCTION create_wallet_for_new_user();

-- ============================================
-- INITIAL DATA
-- ============================================

-- Insert admin user (update email and id after first signup)
-- INSERT INTO profiles (id, email, full_name, role, verification_status)
-- VALUES (
--   '00000000-0000-0000-0000-000000000000',
--   'admin@dailyworkerhub.com',
--   'System Admin',
--   'admin',
--   'verified'
-- );

-- ============================================
-- VIEWS
-- ============================================

-- View for available workers with ratings
CREATE OR REPLACE VIEW available_workers_view AS
SELECT
  w.id,
  p.full_name,
  w.skill_categories,
  w.experience_level,
  w.rating_avg,
  w.rating_count,
  w.completed_jobs,
  p.verification_status
FROM workers w
JOIN profiles p ON p.id = w.id
WHERE w.available = TRUE
  AND p.verification_status = 'verified'
  AND w.face_verified = TRUE;

-- View for business job statistics
CREATE OR REPLACE VIEW business_jobs_stats AS
SELECT
  b.id,
  b.company_name,
  COUNT(j.id) as total_jobs,
  COUNT(ja.id) as total_assignments,
  AVG(w.rating_avg) as avg_worker_rating
FROM businesses b
LEFT JOIN jobs j ON j.business_id = b.id
LEFT JOIN job_assignments ja ON ja.job_id = j.id
LEFT JOIN workers w ON w.id = ja.worker_id
GROUP BY b.id, b.company_name;

-- ============================================
-- END OF SCHEMA
-- ============================================
