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
-- ROW LEVEL SECURITY (RLS) POLICIES FOR BUSINESSES
-- ============================================

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

-- ============================================
-- TRIGGERS FOR BUSINESSES
-- ============================================

CREATE TRIGGER IF NOT EXISTS update_businesses_updated_at
  BEFORE UPDATE ON businesses
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- END OF BUSINESSES TABLE
-- ============================================
