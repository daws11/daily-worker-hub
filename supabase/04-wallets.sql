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
-- ROW LEVEL SECURITY (RLS) POLICIES FOR WALLETS
-- ============================================

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

-- ============================================
-- TRIGGERS FOR WALLETS
-- ============================================

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
-- END OF WALLETS TABLE
-- ============================================
