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
-- ROW LEVEL SECURITY (RLS) POLICIES FOR WALLET TRANSACTIONS
-- ============================================

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
-- END OF WALLET TRANSACTIONS TABLE
-- ============================================
