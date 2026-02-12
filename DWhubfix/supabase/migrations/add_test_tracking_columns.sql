-- Add Test Tracking Columns for Integration Tests
--
-- This migration adds test_id and is_test_data columns to all relevant tables.
-- These columns are used by integration tests to tag and clean up test data.
--
-- Run this in your development/staging Supabase environment before running integration tests.

-- Add test tracking columns to jobs table
ALTER TABLE jobs
ADD COLUMN IF NOT EXISTS test_id TEXT,
ADD COLUMN IF NOT EXISTS is_test_data BOOLEAN DEFAULT false;

-- Add test tracking columns to job_applications table
ALTER TABLE job_applications
ADD COLUMN IF NOT EXISTS test_id TEXT,
ADD COLUMN IF NOT EXISTS is_test_data BOOLEAN DEFAULT false;

-- Add test tracking columns to bookings table
ALTER TABLE bookings
ADD COLUMN IF NOT EXISTS test_id TEXT,
ADD COLUMN IF NOT EXISTS is_test_data BOOLEAN DEFAULT false;

-- Add test tracking columns to shifts table
ALTER TABLE shifts
ADD COLUMN IF NOT EXISTS test_id TEXT,
ADD COLUMN IF NOT EXISTS is_test_data BOOLEAN DEFAULT false;

-- Create indexes for faster test cleanup on jobs table
CREATE INDEX IF NOT EXISTS idx_jobs_test_id ON jobs(test_id) WHERE test_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_jobs_is_test_data ON jobs(is_test_data) WHERE is_test_data = true;

-- Create indexes for faster test cleanup on job_applications table
CREATE INDEX IF NOT EXISTS idx_applications_test_id ON job_applications(test_id) WHERE test_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_applications_is_test_data ON job_applications(is_test_data) WHERE is_test_data = true;

-- Create indexes for faster test cleanup on bookings table
CREATE INDEX IF NOT EXISTS idx_bookings_test_id ON bookings(test_id) WHERE test_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_bookings_is_test_data ON bookings(is_test_data) WHERE is_test_data = true;

-- Create indexes for faster test cleanup on shifts table
CREATE INDEX IF NOT EXISTS idx_shifts_test_id ON shifts(test_id) WHERE test_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_shifts_is_test_data ON shifts(is_test_data) WHERE is_test_data = true;

-- Add comments for documentation
COMMENT ON COLUMN jobs.test_id IS 'Test identifier for integration test data isolation';
COMMENT ON COLUMN jobs.is_test_data IS 'Flag indicating if this is test data from integration tests';
COMMENT ON COLUMN job_applications.test_id IS 'Test identifier for integration test data isolation';
COMMENT ON COLUMN job_applications.is_test_data IS 'Flag indicating if this is test data from integration tests';
COMMENT ON COLUMN bookings.test_id IS 'Test identifier for integration test data isolation';
COMMENT ON COLUMN bookings.is_test_data IS 'Flag indicating if this is test data from integration tests';
COMMENT ON COLUMN shifts.test_id IS 'Test identifier for integration test data isolation';
COMMENT ON COLUMN shifts.is_test_data IS 'Flag indicating if this is test data from integration tests';
