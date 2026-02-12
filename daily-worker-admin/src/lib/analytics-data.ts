import { createClient } from '@/utils/supabase/server'
import { format } from 'date-fns'

interface UserGrowth {
  month: string
  workers: number
  businesses: number
}

interface JobCompletion {
  month: string
  posted: number
  completed: number
  rate: number
}

interface Revenue {
  month: string
  revenue: number
}

interface CategoryData {
  name: string
  value: number
  color: string
}

interface GeographicData {
  area: string
  jobs: number
}

// Get user growth data for the last 6 months
export async function getUserGrowthData(): Promise<UserGrowth[]> {
  const supabase = await createClient()

  const months = []
  const today = new Date()

  for (let i = 5; i >= 0; i--) {
    const date = new Date(today.getFullYear(), today.getMonth() - i, 1)
    months.push({
      month: format(date, 'MMM'),
      startDate: date,
      endDate: new Date(today.getFullYear(), today.getMonth() - i + 1, 0),
    })
  }

  const data: UserGrowth[] = []

  for (const month of months) {
    // Get workers count created in this month
    const { count: workerCount } = await supabase
      .from('profiles')
      .select('*', { count: 'exact', head: true })
      .eq('role', 'worker')
      .gte('created_at', month.startDate.toISOString())
      .lte('created_at', month.endDate.toISOString())

    // Get businesses count created in this month
    const { count: businessCount } = await supabase
      .from('profiles')
      .select('*', { count: 'exact', head: true })
      .eq('role', 'business')
      .gte('created_at', month.startDate.toISOString())
      .lte('created_at', month.endDate.toISOString())

    data.push({
      month: month.month,
      workers: workerCount || 0,
      businesses: businessCount || 0,
    })
  }

  return data
}

// Get job completion data for the last 6 months
export async function getJobCompletionData(): Promise<JobCompletion[]> {
  const supabase = await createClient()

  const months = []
  const today = new Date()

  for (let i = 5; i >= 0; i--) {
    const date = new Date(today.getFullYear(), today.getMonth() - i, 1)
    months.push({
      month: format(date, 'MMM'),
      startDate: date,
      endDate: new Date(today.getFullYear(), today.getMonth() - i + 1, 0),
    })
  }

  const data: JobCompletion[] = []

  for (const month of months) {
    // Get total jobs posted in this month
    const { count: postedCount } = await supabase
      .from('jobs')
      .select('*', { count: 'exact', head: true })
      .gte('created_at', month.startDate.toISOString())
      .lte('created_at', month.endDate.toISOString())

    // Get completed jobs in this month
    const { count: completedCount } = await supabase
      .from('jobs')
      .select('*', { count: 'exact', head: true })
      .eq('status', 'completed')
      .gte('created_at', month.startDate.toISOString())
      .lte('created_at', month.endDate.toISOString())

    const rate = postedCount && postedCount > 0 ? Math.round((completedCount! / postedCount) * 100) : 0

    data.push({
      month: month.month,
      posted: postedCount || 0,
      completed: completedCount || 0,
      rate,
    })
  }

  return data
}

// Get revenue data for the last 6 months
export async function getRevenueData(): Promise<Revenue[]> {
  const supabase = await createClient()

  const months = []
  const today = new Date()

  for (let i = 5; i >= 0; i--) {
    const date = new Date(today.getFullYear(), today.getMonth() - i, 1)
    months.push({
      month: format(date, 'MMM'),
      startDate: date,
      endDate: new Date(today.getFullYear(), today.getMonth() - i + 1, 0),
    })
  }

  const data: Revenue[] = []

  for (const month of months) {
    // Get completed wallet transactions (credits) in this month
    const { data: transactions } = await supabase
      .from('wallet_transactions')
      .select('amount')
      .eq('type', 'credit')
      .eq('status', 'completed')
      .eq('category', 'job_payment')
      .gte('created_at', month.startDate.toISOString())
      .lte('created_at', month.endDate.toISOString())

    const revenue = transactions?.reduce((sum, t) => sum + (t.amount || 0), 0) || 0

    data.push({
      month: month.month,
      revenue,
    })
  }

  return data
}

// Get job category distribution
export async function getCategoryData(): Promise<CategoryData[]> {
  const supabase = await createClient()

  const categories = ['driver', 'cleaner', 'cook', 'steward']
  const colors = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444']
  const data: CategoryData[] = []

  for (let i = 0; i < categories.length; i++) {
    const { count } = await supabase
      .from('jobs')
      .select('*', { count: 'exact', head: true })
      .eq('category', categories[i])

    data.push({
      name: categories[i].charAt(0).toUpperCase() + categories[i].slice(1),
      value: count || 0,
      color: colors[i],
    })
  }

  return data
}

// Get geographic distribution
export async function getGeographicData(): Promise<GeographicData[]> {
  const supabase = await createClient()

  const areas = ['Badung', 'Denpasar', 'Gianyar', 'Tabanan', 'Buleleng', 'Karangasem', 'Bangli', 'Jembrana']
  const data: GeographicData[] = []

  for (const area of areas) {
    // Get profiles with business role and matching address in their business_profiles
    const { data: profiles } = await supabase
      .from('profiles')
      .select('id, business_profiles!inner (address)')
      .eq('role', 'business')
      .filter('business_profiles.address', 'ilike', `%${area}%`)

    const profileIds = profiles?.map((p) => p.id) || []

    let jobCount = 0
    if (profileIds.length > 0) {
      const { count } = await supabase
        .from('jobs')
        .select('*', { count: 'exact', head: true })
        .in('business_id', profileIds)
      jobCount = count || 0
    }

    if (jobCount > 0) {
      data.push({
        area,
        jobs: jobCount,
      })
    }
  }

  // Sort by job count
  return data.sort((a, b) => b.jobs - a.jobs).slice(0, 6)
}

// Get key metrics
export async function getKeyMetrics() {
  const supabase = await createClient()

  // Total users
  const { count: totalUsers } = await supabase
    .from('profiles')
    .select('*', { count: 'exact', head: true })

  // Jobs posted
  const { count: jobsPosted } = await supabase
    .from('jobs')
    .select('*', { count: 'exact', head: true })

  // Jobs completed
  const { count: jobsCompleted } = await supabase
    .from('jobs')
    .select('*', { count: 'exact', head: true })
    .eq('status', 'completed')

  // Calculate completion rate
  const completionRate = jobsPosted && jobsPosted > 0 ? Math.round((jobsCompleted! / jobsPosted) * 100) : 0

  // Total revenue (last 30 days)
  const thirtyDaysAgo = new Date()
  thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)

  const { data: recentTransactions } = await supabase
    .from('wallet_transactions')
    .select('amount')
    .eq('type', 'credit')
    .eq('status', 'completed')
    .eq('category', 'job_payment')
    .gte('created_at', thirtyDaysAgo.toISOString())

  const revenue = recentTransactions?.reduce((sum, t) => sum + (t.amount || 0), 0) || 0

  return {
    totalUsers: totalUsers || 0,
    jobsPosted: jobsPosted || 0,
    jobsCompleted: jobsCompleted || 0,
    completionRate,
    revenue,
  }
}
