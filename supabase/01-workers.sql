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
CREATE INDEX IF NOT EXISTS idx_workers_skill_categories ON workers USING GIN(skill_categories);
CREATE INDEX IF NOT EXISTS idx_workers_rating_avg ON workers(rating_avg DESC);
CREATE INDEX IF NOT EXISTS idx_workers_available ON workers(available);

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
CREATE INDEX IF NOT EXISTS idx_businesses_type ON businesses(business_type);
CREATE INDEX IF NOT EXISTS idx_businesses_verified ON businesses(business_verified);

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
CREATE INDEX IF NOT EXISTS idx_wallets_user_id ON wallets(user_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_wallets_user_unique ON wallets(user_id);

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
CREATE INDEX IF NOT EXISTS idx_wallet_transactions_wallet_id ON wallet_transactions(wallet_id);
CREATE INDEX IF NOT EXISTS idx_wallet_transactions_type ON wallet_transactions(type);
CREATE INDEX IF NOT EXISTS idx_wallet_transactions_created_at ON wallet_transactions(created_at DESC);

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
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at DESC);

-- ============================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ============================================

-- Enable RLS on new tables
ALTER TABLE workers ENABLE ROW LEVEL SECURITY;
ALTER TABLE businesses ENABLE ROW LEVEL SECURITY;
ALTER TABLE job_assignments ENABLE ROW LEVEL SECURITY;
ALTER TABLE wallets ENABLE ROW LEVEL SECURITY;
ALTER TABLE wallet_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;

-- Workers Policies
CREATE POLICY IF NOT EXISTS "Anyone can view verified workers"
ON workers FOR SELECT
USING (
  EXISTS (
    SELECT 1 FROM profiles
    WHERE profiles.id = workers.id
    AND profiles.verification_status = 'verified'
  )
);

CREATE POLICY IF NOT EXISTS "Workers can view their own data"
ON workers FOR SELECT
USING (auth.uid() = id);

CREATE POLICY IF NOT EXISTS "Workers can update their own data"
ON workers FOR UPDATE
USING (auth.uid() = id);

-- Businesses Policies
CREATE POLICY IF NOT EXISTS "Anyone can view verified businesses"
ON businesses FOR SELECT
USING (
  EXISTS (
    SELECT 1 FROM profiles
    WHERE profiles.id = businesses.id
    AND profiles.verification_status = 'verified'
  )
);

CREATE POLICY IF NOT EXISTS "Businesses can view their own data"
ON businesses FOR SELECT
USING (auth.uid() = id);

CREATE POLICY IF NOT EXISTS "Businesses can update their own data"
ON businesses FOR UPDATE
USING (auth.uid() = id);

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

-- Wallets Policies
CREATE POLICY IF NOT EXISTS "Users can view their own wallet"
ON wallets FOR SELECT
USING (user_id = auth.uid());

CREATE POLICY IF NOT EXISTS "Users can view wallet balance only"
ON wallets FOR SELECT
USING (user_id = auth.uid());

CREATE POLICY IF NOT EXISTS "System can update wallets"
ON wallets FOR UPDATE
USING (false); -- Only through database functions

-- Wallet Transactions Policies
CREATE POLICY IF NOT EXISTS "Users can view their own transactions"
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

-- Apply trigger to tables
CREATE TRIGGER IF NOT EXISTS update_workers_updated_at
  BEFORE UPDATE ON workers
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER IF NOT EXISTS update_businesses_updated_at
  BEFORE UPDATE ON businesses
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER IF NOT EXISTS update_job_assignments_updated_at
  BEFORE UPDATE ON job_assignments
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER IF NOT EXISTS update_wallets_updated_at
  BEFORE UPDATE ON wallets
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

-- Function to auto-create wallet for existing profiles
CREATE OR REPLACE FUNCTION create_wallet_for_existing_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO wallets (user_id, balance)
  VALUES (NEW.id, 0)
  ON CONFLICT (user_id) DO NOTHING;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to create wallet when profile is created
CREATE TRIGGER IF NOT EXISTS create_worker_wallet
  AFTER INSERT ON profiles
  FOR EACH ROW
  EXECUTE FUNCTION create_wallet_for_existing_user()
  WHEN (NEW.role = 'worker' OR NEW.role = 'business');

-- ============================================
-- END OF WORKERS, BUSINESSES, JOB ASSIGNMENTS, WALLETS
-- ============================================
