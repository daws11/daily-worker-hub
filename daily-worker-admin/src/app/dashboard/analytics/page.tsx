import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { TrendingUp, Users, Briefcase, DollarSign } from "lucide-react"
import { AnalyticsCharts } from "@/components/analytics-charts"
import { getUserGrowthData, getJobCompletionData, getRevenueData, getCategoryData, getGeographicData, getKeyMetrics } from "@/lib/analytics-data"

export default async function AnalyticsPage() {
  // Fetch all data in parallel on the server
  const [
    metrics,
    userGrowthData,
    jobCompletionData,
    revenueData,
    categoryData,
    geographicData,
  ] = await Promise.all([
    getKeyMetrics(),
    getUserGrowthData(),
    getJobCompletionData(),
    getRevenueData(),
    getCategoryData(),
    getGeographicData(),
  ])

  return (
    <div className="space-y-8">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-3xl font-bold tracking-tight text-gray-900">Analytics Dashboard</h2>
          <p className="text-gray-500">Overview of platform performance and metrics</p>
        </div>
      </div>

      {/* Key Metrics */}
      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">Total Users</CardTitle>
            <Users className="h-4 w-4 text-blue-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{metrics?.totalUsers || 0}</div>
            <p className="text-xs text-gray-500">Active registered users</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">Jobs Posted</CardTitle>
            <Briefcase className="h-4 w-4 text-purple-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{metrics?.jobsPosted || 0}</div>
            <p className="text-xs text-gray-500">Total jobs created</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">Revenue (30d)</CardTitle>
            <DollarSign className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              Rp {(metrics?.revenue || 0).toLocaleString('id-ID')}
            </div>
            <p className="text-xs text-gray-500">Last 30 days</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">Completion Rate</CardTitle>
            <TrendingUp className="h-4 w-4 text-orange-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{metrics?.completionRate || 0}%</div>
            <p className="text-xs text-gray-500">
              {metrics?.jobsCompleted || 0} of {metrics?.jobsPosted || 0} jobs completed
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Charts - Client Component */}
      <AnalyticsCharts
        userGrowthData={userGrowthData}
        jobCompletionData={jobCompletionData}
        revenueData={revenueData}
        categoryData={categoryData}
        geographicData={geographicData}
      />
    </div>
  )
}
