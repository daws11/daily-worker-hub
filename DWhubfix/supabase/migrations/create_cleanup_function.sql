-- Create Test Data Cleanup Function
--
-- This function cleans up all test data associated with a specific test_id.
-- It is called by integration tests after each test to ensure data isolation.
--
-- Run this in your development/staging Supabase environment before running integration tests.

-- Create the cleanup function
CREATE OR REPLACE FUNCTION cleanup_integration_test(test_id_param TEXT)
RETURNS void AS $$
BEGIN
    -- Delete in reverse dependency order to avoid foreign key violations

    -- Delete shifts first (bookings depend on shifts)
    DELETE FROM shifts
    WHERE test_id = test_id_param;

    -- Delete bookings (may have dependencies on shifts)
    DELETE FROM bookings
    WHERE test_id = test_id_param;

    -- Delete job_applications (may have dependencies on jobs)
    DELETE FROM job_applications
    WHERE test_id = test_id_param;

    -- Delete jobs last
    DELETE FROM jobs
    WHERE test_id = test_id_param;

    -- Log the cleanup (optional, for debugging)
    RAISE NOTICE 'Cleaned up test data for test_id: %', test_id_param;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Grant execute permission to authenticated users
-- Note: Adjust based on your RLS policies
GRANT EXECUTE ON FUNCTION cleanup_integration_test(TEXT) TO authenticated;

-- Grant execute permission to service_role (for admin cleanup)
GRANT EXECUTE ON FUNCTION cleanup_integration_test(TEXT) TO service_role;

-- Add comment for documentation
COMMENT ON FUNCTION cleanup_integration_test IS 'Cleans up all test data for a given test_id. Used by integration tests to maintain data isolation.';
