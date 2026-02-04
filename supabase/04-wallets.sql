-- ========================================
-- WALLET & TRANSACTIONS TABLE
-- ========================================
-- 
-- This script creates wallet_balance and transactions tables
-- 
-- Based on business-model.md (Section 3: Platform Commission, Section 4: Wallet):
-- - Platform deducts 6% commission from business wallet
-- - Workers receive net wage (wage - commission)
-- - Transactions are recorded for audit and transparency
-- 
-- Tables:
-- 1. wallet_balance (Current balance for business and worker)
-- 2. transactions (Transaction history: payments, withdrawals, bonuses, penalties)

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ========================================
-- 1. WALLET_BALANCE TABLE
-- ========================================

CREATE TABLE IF NOT EXISTS public.wallet_balance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- User ID (Foreign Key to profiles)
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    
    -- Balance Information
    balance DECIMAL(15, 2) DEFAULT 0.0 CHECK (balance >= 0), -- Current balance
    available_balance DECIMAL(15, 2) DEFAULT 0.0 CHECK (available_balance >= 0), -- Balance - pending withdrawals
    pending_withdrawal DECIMAL(15, 2) DEFAULT 0.0 CHECK (pending_withdrawal >= 0), -- Pending withdrawal amount
    
    -- Transaction Stats
    total_deposits DECIMAL(15, 2) DEFAULT 0.0,
    total_withdrawals DECIMAL(15, 2) DEFAULT 0.0,
    total_spending DECIMAL(15, 2) DEFAULT 0.0,
    total_earnings DECIMAL(15, 2) DEFAULT 0.0,
    
    -- Metadata
    currency VARCHAR(3) DEFAULT 'IDR',
    
    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ========================================
-- 2. TRANSACTIONS TABLE
-- ========================================

CREATE TABLE IF NOT EXISTS public.transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Job Reference
    job_application_id UUID REFERENCES public.job_applications(id) ON DELETE CASCADE,
    
    -- User IDs
    worker_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    business_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    
    -- Amounts (in IDR)
    gross_amount DECIMAL(15, 2) NOT NULL CHECK (gross_amount >= 0), -- Original job wage
    platform_commission DECIMAL(15, 2) NOT NULL CHECK (platform_commission >= 0), -- Platform fee (6% of gross)
    business_commission DECIMAL(15, 2) DEFAULT 0.0 CHECK (business_commission >= 0), -- Business commission (0% now)
    worker_commission DECIMAL(15, 2) DEFAULT 0.0 CHECK (worker_commission >= 0), -- Worker commission (0% now)
    net_worker_amount DECIMAL(15, 2) NOT NULL CHECK (net_worker_amount >= 0), -- Worker net pay
    
    -- Transaction Type & Status
    transaction_type VARCHAR(50) NOT NULL CHECK (transaction_type IN ('job_payment', 'withdrawal', 'deposit', 'bonus', 'penalty')),
    status VARCHAR(50) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'processing', 'completed', 'failed', 'refunded')),
    
    -- Payment Details (For withdrawals/deposits)
    payment_method VARCHAR(50), -- bank_transfer, ewallet, card
    payment_reference TEXT, -- Transaction reference ID
    bank_account_number VARCHAR(50), -- For withdrawals
    
    -- Processing Details
    processed_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    
    -- Metadata
    notes TEXT,
    metadata JSONB, -- Additional transaction data
    
    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ========================================
-- INDEXES FOR PERFORMANCE
-- ========================================

-- Wallet balance indexes
CREATE INDEX IF NOT EXISTS wallet_balance_user_id_idx ON public.wallet_balance(user_id);
CREATE INDEX IF NOT EXISTS wallet_balance_balance_idx ON public.wallet_balance(balance DESC);
CREATE INDEX IF NOT EXISTS wallet_balance_available_balance_idx ON public.wallet_balance(available_balance DESC);

-- Transactions indexes
CREATE INDEX IF NOT EXISTS transactions_job_application_id_idx ON public.transactions(job_application_id);
CREATE INDEX IF NOT EXISTS transactions_worker_id_idx ON public.transactions(worker_id);
CREATE INDEX IF NOT EXISTS transactions_business_id_idx ON public.transactions(business_id);
CREATE INDEX IF NOT EXISTS transactions_transaction_type_idx ON public.transactions(transaction_type);
CREATE INDEX IF NOT EXISTS transactions_status_idx ON public.transactions(status);
CREATE INDEX IF NOT EXISTS transactions_created_at_idx ON public.transactions(created_at DESC);
CREATE INDEX IF NOT EXISTS transactions_completed_at_idx ON public.transactions(completed_at DESC);

-- Composite indexes for common queries
CREATE INDEX IF NOT EXISTS transactions_worker_business_type_idx ON public.transactions(worker_id, business_id, transaction_type);

-- ========================================
-- TRIGGERS FOR UPDATED_AT
-- ========================================

-- Wallet balance table trigger
CREATE OR REPLACE FUNCTION public.update_wallet_balance_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;

CREATE TRIGGER trigger_update_wallet_balance_updated_at
    BEFORE UPDATE ON public.wallet_balance
    FOR EACH ROW
    EXECUTE FUNCTION public.update_wallet_balance_updated_at();

-- Transactions table trigger
CREATE OR REPLACE FUNCTION public.update_transactions_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;

CREATE TRIGGER trigger_update_transactions_updated_at
    BEFORE UPDATE ON public.transactions
    FOR EACH ROW
    EXECUTE FUNCTION public.update_transactions_updated_at();

-- ========================================
-- COMMENTS FOR FUTURE DEVELOPERS
-- ========================================

COMMENT ON TABLE public.wallet_balance IS 'User wallet balance. Stores current balance, available balance, pending withdrawals, and transaction stats.';
COMMENT ON TABLE public.transactions IS 'Transaction history. Records all wallet transactions (payments, withdrawals, bonuses, penalties) with amounts and statuses.';

COMMENT ON COLUMN public.wallet_balance.user_id IS 'Foreign key to profiles table (can be worker or business)';
COMMENT ON COLUMN public.wallet_balance.balance IS 'Current total balance (IDR). CHECK constraint ensures balance >= 0';
COMMENT ON COLUMN public.wallet_balance.available_balance IS 'Available balance (balance - pending withdrawals). Used to prevent negative balance during concurrent withdrawals';
COMMENT ON COLUMN public.wallet_balance.pending_withdrawal IS 'Pending withdrawal amount awaiting processing. Used to calculate available_balance';
COMMENT ON COLUMN public.wallet_balance.total_deposits IS 'Total deposits made (for statistics)';
COMMENT ON COLUMN public.wallet_balance.total_withdrawals IS 'Total withdrawals made (for statistics)';
COMMENT ON COLUMN public.wallet_balance.total_spending IS 'Total spending (business jobs, for statistics)';
COMMENT ON COLUMN public.wallet_balance.total_earnings IS 'Total earnings (worker jobs, for statistics)';

COMMENT ON COLUMN public.transactions.job_application_id IS 'Foreign key to job_applications table. NULL for deposits/withdrawals (bonus, penalty)';
COMMENT ON COLUMN public.transactions.worker_id IS 'Foreign key to profiles table (worker)';
COMMENT ON COLUMN public.transactions.business_id IS 'Foreign key to profiles table (business)';
COMMENT ON COLUMN public.transactions.gross_amount IS 'Original job wage (before commissions)';
COMMENT ON COLUMN public.transactions.platform_commission IS 'Platform fee (6% of gross). Calculated as gross_amount * 0.06';
COMMENT ON COLUMN public.transactions.business_commission IS 'Business commission (0% now, for future features)';
COMMENT ON COLUMN public.transactions.worker_commission IS 'Worker commission (0% now, for future features)';
COMMENT ON COLUMN public.transactions.net_worker_amount IS 'Worker net pay (gross - platform_commission). Amount actually credited to worker wallet';
COMMENT ON COLUMN public.transactions.transaction_type IS 'Transaction type: job_payment, withdrawal, deposit, bonus, penalty';
COMMENT ON COLUMN public.transactions.status IS 'Transaction status: pending, processing, completed, failed, refunded';
COMMENT ON COLUMN public.transactions.payment_method IS 'Payment method: bank_transfer, ewallet, card. Only for withdrawals/deposits';
COMMENT ON COLUMN public.transactions.payment_reference IS 'Transaction reference ID from payment gateway';
COMMENT ON COLUMN public.transactions.bank_account_number IS 'Bank account number. Only for withdrawals';
COMMENT ON COLUMN public.transactions.processed_at IS 'Timestamp when transaction was processed (for withdrawals/deposits)';
COMMENT ON COLUMN public.transactions.completed_at IS 'Timestamp when transaction was completed successfully';
COMMENT ON COLUMN public.transactions.notes IS 'Optional notes (e.g., "Failed due to insufficient balance")';
COMMENT ON COLUMN public.transactions.metadata IS 'Additional transaction data stored as JSONB';

COMMENT ON INDEX wallet_balance_user_id_idx IS 'Index for filtering wallet balances by user';
COMMENT ON INDEX wallet_balance_balance_idx IS 'Index for sorting users by balance (highest first)';
COMMENT ON INDEX wallet_balance_available_balance_idx IS 'Index for sorting users by available balance (highest first)';
COMMENT ON INDEX transactions_job_application_id_idx IS 'Index for filtering transactions by job application';
COMMENT ON INDEX transactions_worker_id_idx IS 'Index for filtering transactions by worker';
COMMENT ON INDEX transactions_business_id_idx IS 'Index for filtering transactions by business';
COMMENT ON INDEX transactions_transaction_type_idx IS 'Index for filtering transactions by type';
COMMENT ON INDEX transactions_status_idx IS 'Index for filtering transactions by status';
COMMENT ON INDEX transactions_created_at_idx IS 'Index for sorting transactions by creation date (newest first)';
COMMENT ON INDEX transactions_worker_business_type_idx IS 'Composite index for filtering transactions by worker, business, and type (common for job payments)';

COMMENT ON TRIGGER trigger_update_wallet_balance_updated_at IS 'Automatically updates wallet_balance.updated_at when row is modified';
COMMENT ON TRIGGER trigger_update_transactions_updated_at IS 'Automatically updates transactions.updated_at when row is modified';
