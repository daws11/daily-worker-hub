import { createClient } from '@/utils/supabase/server'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { 
  Briefcase, 
  MapPin, 
  Clock, 
  Calendar, 
  DollarSign, 
  User, 
  Building2,
  Filter,
  Download,
  Eye,
  Edit,
  X
} from "lucide-react"
import Link from "next/link"
import { format } from "date-fns"

interface Job {
  id: string
  title: string
  description: string
  category: string
  wage: number
  location: string
  start_time: string
  end_time: string
  status: string
  is_urgent: boolean
  is_compliant: boolean
  created_at: string
  business_profiles: {
    business_name: string
  } | null
  job_assignments: Array<{
    id: string
    status: string
    worker_profiles: {
      full_name: string
      avatar_url: string | null
    } | null
  }>
}

export default async function JobsPage({
  searchParams,
}: {
  searchParams: { status?: string; category?: string }
}) {
  const supabase = await createClient()

  // Get filter values
  const statusFilter = searchParams.status || 'all'
  const categoryFilter = searchParams.category || 'all'

  // Fetch jobs with filters
  let query = supabase
    .from('jobs')
    .select(`
      *,
      business_profiles (
        business_name
      ),
      job_assignments (
        id,
        status,
        worker_profiles (
          full_name,
          avatar_url
        )
      )
    `)
    .order('created_at', { ascending: false })

  if (statusFilter !== 'all') {
    query = query.eq('status', statusFilter)
  }

  if (categoryFilter !== 'all') {
    query = query.eq('category', categoryFilter)
  }

  const { data: jobs, error } = await query

  if (error) {
    return (
      <div className="text-red-500 p-4">
        Error loading jobs: {error.message}
      </div>
    )
  }

  // Get statistics
  const { count: totalJobs } = await supabase
    .from('jobs')
    .select('*', { count: 'exact', head: true })

  const { count: openJobs } = await supabase
    .from('jobs')
    .select('*', { count: 'exact', head: true })
    .eq('status', 'open')

  const { count: activeJobs } = await supabase
    .from('jobs')
    .select('*', { count: 'exact', head: true })
    .eq('status', 'ongoing')

  const { count: completedJobs } = await supabase
    .from('jobs')
    .select('*', { count: 'exact', head: true })
    .eq('status', 'completed')

  const categoryColors: Record<string, string> = {
    driver: 'bg-blue-100 text-blue-800',
    cleaner: 'bg-green-100 text-green-800',
    cook: 'bg-yellow-100 text-yellow-800',
    steward: 'bg-red-100 text-red-800',
  }

  const statusColors: Record<string, string> = {
    open: 'bg-blue-100 text-blue-800',
    pending: 'bg-yellow-100 text-yellow-800',
    accepted: 'bg-purple-100 text-purple-800',
    ongoing: 'bg-indigo-100 text-indigo-800',
    completed: 'bg-green-100 text-green-800',
    cancelled: 'bg-gray-100 text-gray-800',
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-gray-900">Job Management</h2>
          <p className="text-gray-500">Manage and monitor all jobs</p>
        </div>
        <Button variant="outline">
          <Download className="w-4 h-4 mr-2" />
          Export CSV
        </Button>
      </div>

      {/* Statistics */}
      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">Total Jobs</CardTitle>
            <Briefcase className="h-4 w-4 text-blue-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalJobs || 0}</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">Open Jobs</CardTitle>
            <Briefcase className="h-4 w-4 text-yellow-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{openJobs || 0}</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">Active Jobs</CardTitle>
            <Briefcase className="h-4 w-4 text-purple-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{activeJobs || 0}</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">Completed</CardTitle>
            <Briefcase className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{completedJobs || 0}</div>
          </CardContent>
        </Card>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center">
            <Filter className="w-5 h-5 mr-2" />
            Filters
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex gap-4">
            <div className="flex-1">
              <label className="text-sm font-medium text-gray-700 mb-2 block">Status</label>
              <Select defaultValue={statusFilter}>
                <SelectTrigger>
                  <SelectValue placeholder="All Status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Status</SelectItem>
                  <SelectItem value="open">Open</SelectItem>
                  <SelectItem value="pending">Pending</SelectItem>
                  <SelectItem value="accepted">Accepted</SelectItem>
                  <SelectItem value="ongoing">Ongoing</SelectItem>
                  <SelectItem value="completed">Completed</SelectItem>
                  <SelectItem value="cancelled">Cancelled</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="flex-1">
              <label className="text-sm font-medium text-gray-700 mb-2 block">Category</label>
              <Select defaultValue={categoryFilter}>
                <SelectTrigger>
                  <SelectValue placeholder="All Categories" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Categories</SelectItem>
                  <SelectItem value="driver">Driver</SelectItem>
                  <SelectItem value="cleaner">Cleaner</SelectItem>
                  <SelectItem value="cook">Cook</SelectItem>
                  <SelectItem value="steward">Steward</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Job List */}
      <Card>
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Job Info
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Business
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Category
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Wage
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Schedule
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Worker
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {jobs?.length === 0 ? (
                  <tr>
                    <td colSpan={8} className="px-6 py-8 text-center text-sm text-gray-500">
                      No jobs found
                    </td>
                  </tr>
                ) : (
                  jobs?.map((job: Job) => {
                    const assignment = job.job_assignments?.[0]
                    const worker = assignment?.worker_profiles

                    return (
                      <tr key={job.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4">
                          <div className="flex items-start">
                            <div className="flex-shrink-0 h-10 w-10 bg-indigo-100 rounded-lg flex items-center justify-center">
                              <Briefcase className="h-5 w-5 text-indigo-600" />
                            </div>
                            <div className="ml-4">
                              <div className="text-sm font-medium text-gray-900">
                                {job.title}
                              </div>
                              <div className="text-sm text-gray-500 flex items-center mt-1">
                                <MapPin className="w-3 h-3 mr-1" />
                                {job.location}
                              </div>
                              {job.is_urgent && (
                                <Badge className="mt-2" variant="destructive">Urgent</Badge>
                              )}
                              {!job.is_compliant && (
                                <Badge className="mt-2 ml-2" variant="secondary">Non-Compliant</Badge>
                              )}
                            </div>
                          </div>
                        </td>
                        <td className="px-6 py-4">
                          <div className="text-sm text-gray-900 flex items-center">
                            <Building2 className="w-3 h-3 mr-1 text-gray-400" />
                            {job.business_profiles?.business_name || 'Unknown'}
                          </div>
                        </td>
                        <td className="px-6 py-4">
                          <Badge className={categoryColors[job.category] || 'bg-gray-100 text-gray-800'}>
                            {job.category}
                          </Badge>
                        </td>
                        <td className="px-6 py-4">
                          <div className="text-sm font-medium text-gray-900">
                            Rp {job.wage.toLocaleString('id-ID')}
                          </div>
                        </td>
                        <td className="px-6 py-4">
                          <div className="text-sm text-gray-900 flex items-center">
                            <Calendar className="w-3 h-3 mr-1 text-gray-400" />
                            {format(new Date(job.start_time), 'MMM dd')}
                          </div>
                          <div className="text-xs text-gray-500 flex items-center mt-1">
                            <Clock className="w-3 h-3 mr-1" />
                            {format(new Date(job.start_time), 'HH:mm')} - {format(new Date(job.end_time), 'HH:mm')}
                          </div>
                        </td>
                        <td className="px-6 py-4">
                          <Badge className={statusColors[job.status] || 'bg-gray-100 text-gray-800'}>
                            {job.status}
                          </Badge>
                        </td>
                        <td className="px-6 py-4">
                          {worker ? (
                            <div className="flex items-center">
                              <div className="h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 text-sm font-bold">
                                {worker.avatar_url ? (
                                  <img src={worker.avatar_url} alt="" className="h-8 w-8 rounded-full object-cover" />
                                ) : (
                                  worker.full_name.charAt(0)
                                )}
                              </div>
                              <div className="ml-3 text-sm text-gray-900">
                                {worker.full_name}
                              </div>
                            </div>
                          ) : (
                            <span className="text-sm text-gray-400">-</span>
                          )}
                        </td>
                        <td className="px-6 py-4 text-right">
                          <div className="flex justify-end space-x-2">
                            <Link href={`/dashboard/jobs/${job.id}`}>
                              <Button size="sm" variant="ghost">
                                <Eye className="w-4 h-4" />
                              </Button>
                            </Link>
                            <Button size="sm" variant="ghost">
                              <Edit className="w-4 h-4" />
                            </Button>
                            <Button size="sm" variant="ghost" className="text-red-600 hover:text-red-900">
                              <X className="w-4 h-4" />
                            </Button>
                          </div>
                        </td>
                      </tr>
                    )
                  })
                )}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
