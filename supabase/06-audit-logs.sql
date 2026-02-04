-- ========================================
-- AUDIT LOGS TABLE (System Event Logging)
-- ========================================
-- 
-- This script creates the audit_logs table
-- for complete system transparency and debugging
-- 
-- Based on business-model.md (Section 4: Audit & Compliance):
-- - Platform commission (6%) audit trail
-- - Worker compliance (21 Days Rule) audit trail
-- - Transaction processing audit trail
-- - Wallet operations audit trail
-- 
-- Tables:
-- 1. audit_logs (Main audit table)
-- 
-- Notes:
-- - All matching algorithm decisions are logged
-- - All compliance checks are logged
-- - All wallet transactions are logged
-- - All commission calculations are logged

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ========================================
-- 1. AUDIT_LOGS TABLE
-- ========================================

CREATE TABLE IF NOT EXISTS public.audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Event Information
    event_type VARCHAR(100) NOT NULL, -- worker_matched, worker_blocked, job_accepted, wallet_deducted, etc.
    event_category VARCHAR(50) NOT NULL, -- matching, compliance, payment, wallet, system
    action VARCHAR(100) NOT NULL, -- create, update, delete, process
    
    -- User Information
    actor_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL, -- User who triggered the event
    actor_role VARCHAR(20) CHECK (actor_role IN ('worker', 'business', 'system', 'admin')),
    target_user_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL, -- User affected by the event
    target_user_role VARCHAR(20) CHECK (target_user_role IN ('worker', 'business', 'admin')),
    
    -- Resource Information
    resource_type VARCHAR(50) NOT NULL, -- job, worker, wallet, transaction
    resource_id UUID, -- ID of the affected resource
    
    -- Event Details
    details JSONB NOT NULL, -- Additional event data (scores, reasons, etc.)
    reason VARCHAR(500), -- Why event happened (e.g., "Worker blocked: 21 Days Rule violation")
    result VARCHAR(50), -- success, failure, partial
    
    -- Financial Information (For transactions)
    amount DECIMAL(15, 2) DEFAULT 0.0,
    commission DECIMAL(15, 2) DEFAULT 0.0,
    net_amount DECIMAL(15, 2) DEFAULT 0.0,
    currency VARCHAR(3) DEFAULT 'IDR',
    
    -- Metadata
    ip_address VARCHAR(45),
    user_agent TEXT,
    session_id UUID,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ========================================
-- INDEXES FOR PERFORMANCE
-- ========================================

-- Audit logs indexes
CREATE INDEX IF NOT EXISTS audit_logs_event_type_idx ON public.audit_logs(event_type);
CREATE INDEX IF NOT EXISTS audit_logs_event_category_idx ON public.audit_logs(event_category);
CREATE INDEX IF NOT EXISTS audit_logs_action_idx ON public.audit_logs(action);
CREATE INDEX IF NOT EXISTS audit_logs_actor_id_idx ON public.audit_logs(actor_id);
CREATE INDEX IF NOT EXISTS audit_logs_target_user_id_idx ON public.audit_logs(target_user_id);
CREATE INDEX IF NOT EXISTS audit_logs_resource_type_idx ON public.audit_logs(resource_type);
CREATE INDEX IF NOT EXISTS audit_logs_resource_id_idx ON public.audit_logs(resource_id);
CREATE INDEX IF NOT EXISTS audit_logs_created_at_idx ON public.audit_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS audit_logs_result_idx ON public.audit_logs(result);

-- Composite indexes for common queries
CREATE INDEX IF NOT EXISTS audit_logs_actor_event_type_idx ON public.audit_logs(actor_id, event_type);
CREATE INDEX IF NOT EXISTS audit_logs_target_resource_type_idx ON public.audit_logs(target_user_id, resource_type);
CREATE INDEX IF NOT EXISTS audit_logs_category_result_created_at_idx ON public.audit_logs(event_category, result, created_at DESC);

-- ========================================
-- TRIGGERS FOR CREATED_AT
-- ========================================

-- Audit logs table trigger
CREATE OR REPLACE FUNCTION public.update_audit_logs_created_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.created_at = NOW();
    RETURN NEW;
END;

CREATE TRIGGER trigger_update_audit_logs_created_at
    BEFORE INSERT ON public.audit_logs
    FOR EACH ROW
    EXECUTE FUNCTION public.update_audit_logs_created_at();

-- ========================================
-- COMMENTS FOR FUTURE DEVELOPERS
-- ========================================

COMMENT ON TABLE public.audit_logs IS 'Complete audit trail for all system events. Used for transparency, debugging, and compliance monitoring.';
COMMENT ON COLUMN public.audit_logs.event_type IS 'Type of event (worker_matched, worker_blocked, job_accepted, wallet_deducted, etc.)';
COMMENT ON COLUMN public.audit_logs.event_category IS 'Category of event (matching, compliance, payment, wallet, system)';
COMMENT ON COLUMN public.audit_logs.action IS 'Action performed (create, update, delete, process)';
COMMENT ON COLUMN public.audit_logs.actor_id IS 'User who triggered the event (Foreign key to profiles)';
COMMENT ON COLUMN public.audit_logs.actor_role IS 'Role of actor (worker, business, system, admin)';
COMMENT ON COLUMN public.audit_logs.target_user_id IS 'User affected by the event (Foreign key to profiles)';
COMMENT ON COLUMN public.audit_logs.target_user_role IS 'Role of affected user (worker, business, admin)';
COMMENT ON COLUMN public.audit_logs.resource_type IS 'Type of resource affected (job, worker, wallet, transaction)';
COMMENT ON COLUMN public.audit_logs.resource_id IS 'ID of the affected resource';
COMMENT ON COLUMN public.audit_logs.details IS 'Additional event data stored as JSONB (scores, reasons, etc.)';
COMMENT ON COLUMN public.audit_logs.reason IS 'Why event happened (e.g., "Worker blocked: 21 Days Rule violation")';
COMMENT ON COLUMN public.audit_logs.result IS 'Result of event (success, failure, partial)';
COMMENT ON COLUMN public.audit_logs.amount IS 'Financial amount (for transaction events)';
COMMENT ON COLUMN public.audit_logs.commission IS 'Platform commission (6% of gross)';
COMMENT ON COLUMN public.audit_logs.net_amount IS 'Net amount (gross - commission)';
COMMENT ON COLUMN public.audit_logs.ip_address IS 'IP address of user (for security audit)';
COMMENT ON COLUMN public.audit_logs.user_agent IS 'User agent string (browser, app version, etc.)';
COMMENT ON COLUMN public.audit_logs.session_id IS 'Session ID of user (for tracking multi-device sessions)';

COMMENT ON INDEX audit_logs_event_type_idx IS 'Index for filtering logs by event type';
COMMENT ON INDEX audit_logs_event_category_idx IS 'Index for filtering logs by category';
COMMENT ON INDEX audit_logs_actor_id_idx IS 'Index for filtering logs by actor';
COMMENT ON INDEX audit_logs_target_user_id_idx IS 'Index for filtering logs by target user';
COMMENT ON INDEX audit_logs_resource_type_idx IS 'Index for filtering logs by resource type';
COMMENT ON INDEX audit_logs_resource_id_idx IS 'Index for filtering logs by resource';
COMMENT ON INDEX audit_logs_created_at_idx IS 'Index for sorting logs by creation date (newest first)';
COMMENT ON INDEX audit_logs_result_idx IS 'Index for filtering logs by result (success/failure)';
COMMENT ON INDEX audit_logs_actor_event_type_idx IS 'Composite index for filtering logs by actor and event type';
COMMENT ON INDEX audit_logs_target_resource_type_idx IS 'Composite index for filtering logs by target user and resource type';
COMMENT ON INDEX audit_logs_category_result_created_at_idx IS 'Composite index for filtering logs by category, result, and creation date';

-- ========================================
-- SAMPLE EVENT TYPES (For Reference)
-- ========================================
-- Matching Events:
-- - worker_matched (Worker matched with job)
-- - worker_blocked (Worker blocked due to compliance)
-- - worker_compliant (Worker passed compliance check)
-- 
-- Application Events:
-- - job_applied (Worker applied for job)
-- - job_accepted (Business hired worker)
-- - job_rejected (Business declined worker)
-- - job_completed (Worker completed job)
-- 
-- Payment Events:
-- - wallet_deducted (Business wallet debited)
-- - wallet_credited (Worker wallet credited)
-- - commission_deducted (Platform commission taken)
-- - withdrawal_requested (Worker requested withdrawal)
-- 
-- System Events:
-- - user_registered (New user signed up)
-- - user_verified (User email verified)
-- - wallet_created (New wallet created)
