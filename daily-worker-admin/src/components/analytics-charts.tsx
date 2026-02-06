'use client'

import {
  LineChart,
  Line,
  AreaChart,
  Area,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts"

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

export function AnalyticsCharts({
  userGrowthData,
  jobCompletionData,
  revenueData,
  categoryData,
  geographicData,
}: {
  userGrowthData: UserGrowth[]
  jobCompletionData: JobCompletion[]
  revenueData: Revenue[]
  categoryData: CategoryData[]
  geographicData: GeographicData[]
}) {
  return (
    <>
      {/* Charts Row 1 */}
      <div className="grid gap-4 md:grid-cols-2">
        <div className="border rounded-lg p-6 bg-white">
          <h3 className="text-lg font-semibold mb-4">User Growth</h3>
          <p className="text-sm text-gray-500 mb-4">Workers and businesses over time</p>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={userGrowthData || []}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Line type="monotone" dataKey="workers" stroke="#3b82f6" strokeWidth={2} name="Workers" />
              <Line type="monotone" dataKey="businesses" stroke="#8b5cf6" strokeWidth={2} name="Businesses" />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className="border rounded-lg p-6 bg-white">
          <h3 className="text-lg font-semibold mb-4">Job Completion Rate</h3>
          <p className="text-sm text-gray-500 mb-4">Jobs posted vs completed</p>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={jobCompletionData || []}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Bar dataKey="posted" fill="#94a3b8" name="Posted" />
              <Bar dataKey="completed" fill="#10b981" name="Completed" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Charts Row 2 */}
      <div className="grid gap-4 md:grid-cols-3">
        <div className="border rounded-lg p-6 bg-white">
          <h3 className="text-lg font-semibold mb-4">Revenue Trend</h3>
          <p className="text-sm text-gray-500 mb-4">Monthly revenue (IDR)</p>
          <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={revenueData || []}>
              <defs>
                <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#10b981" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="#10b981" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis tickFormatter={(value) => `${(value / 1000000).toFixed(1)}M`} />
              <Tooltip formatter={(value: number | undefined) => value ? `Rp ${(value / 1000000).toFixed(2)}M` : '-'} />
              <Area
                type="monotone"
                dataKey="revenue"
                stroke="#10b981"
                strokeWidth={2}
                fillOpacity={1}
                fill="url(#colorRevenue)"
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        <div className="border rounded-lg p-6 bg-white">
          <h3 className="text-lg font-semibold mb-4">Job Categories</h3>
          <p className="text-sm text-gray-500 mb-4">Distribution by job type</p>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={categoryData || []}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, percent }) => `${name} ${((percent ?? 0) * 100).toFixed(0)}%`}
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
              >
                {(categoryData || []).map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="border rounded-lg p-6 bg-white">
          <h3 className="text-lg font-semibold mb-4">Geographic Distribution</h3>
          <p className="text-sm text-gray-500 mb-4">Jobs by area in Bali</p>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={geographicData || []} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis type="number" />
              <YAxis dataKey="area" type="category" width={70} />
              <Tooltip />
              <Bar dataKey="jobs" fill="#3b82f6" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </>
  )
}
